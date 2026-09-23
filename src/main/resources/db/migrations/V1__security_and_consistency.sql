-- Existing MySQL 8 installation. Stop application writes and back up first.
-- This script never chooses which duplicate attendance record to discard.
-- Resolve reported duplicates according to business rules, then retry.
DELIMITER //
CREATE PROCEDURE office_security_v1()
BEGIN
 IF EXISTS (SELECT 1 FROM attendance_record GROUP BY user_id,work_date HAVING COUNT(*) > 1) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Duplicate attendance dates: resolve manually before migration';
 END IF;
 IF EXISTS (SELECT 1 FROM attendance_record WHERE user_id IS NULL OR work_date IS NULL) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Attendance contains NULL user/date: resolve before migration';
 END IF;
 IF EXISTS (SELECT 1 FROM file_cabinet GROUP BY file_name HAVING COUNT(*) > 1) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Duplicate storage keys: resolve before migration';
 END IF;
 CREATE TABLE IF NOT EXISTS admin_account(username VARCHAR(64) PRIMARY KEY,password_hash VARCHAR(255) NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
 ALTER TABLE visitor_record MODIFY password VARCHAR(255) NOT NULL;
 ALTER TABLE attendance_record MODIFY user_id INT NOT NULL, MODIFY work_date DATE NOT NULL;
 IF NOT EXISTS(SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='attendance_record' AND index_name='uk_attendance_user_date') THEN
  ALTER TABLE attendance_record ADD CONSTRAINT uk_attendance_user_date UNIQUE(user_id,work_date);
 END IF;
 IF NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='message_record' AND column_name='client_message_id') THEN
  ALTER TABLE message_record ADD client_message_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NULL;
 END IF;
 IF NOT EXISTS(SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='message_record' AND index_name='uk_message_sender_client') THEN
  ALTER TABLE message_record ADD CONSTRAINT uk_message_sender_client UNIQUE(sender_id,client_message_id);
 END IF;
 IF NOT EXISTS(SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='message_record' AND index_name='ix_message_pair_id') THEN
  CREATE INDEX ix_message_pair_id ON message_record(sender_id,receiver_id,id);
 END IF;
 IF NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='file_cabinet' AND column_name='storage_status') THEN
  ALTER TABLE file_cabinet ADD storage_status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE';
 END IF;
 IF NOT EXISTS(SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='file_cabinet' AND index_name='uk_file_storage_key') THEN
  ALTER TABLE file_cabinet ADD CONSTRAINT uk_file_storage_key UNIQUE(file_name);
 END IF;
 IF NOT EXISTS(SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='file_cabinet' AND index_name='ix_file_cleanup') THEN
  CREATE INDEX ix_file_cleanup ON file_cabinet(storage_status,upload_time);
 END IF;
END//
DELIMITER ;
CALL office_security_v1();
DROP PROCEDURE office_security_v1;
-- If CALL fails, inspect the error and DROP PROCEDURE office_security_v1 before retrying.
-- DDL can commit independently: restore a backup for rollback; do not assume transaction rollback.
