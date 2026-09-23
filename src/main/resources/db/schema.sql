-- Fresh MySQL 8 database only. Never run this over an existing installation.
CREATE TABLE admin_account (
 username VARCHAR(64) PRIMARY KEY,
 password_hash VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE institution_info (
 id INT PRIMARY KEY AUTO_INCREMENT, inst_name VARCHAR(100) NOT NULL, inst_desc VARCHAR(1000),
 create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE department_info (
 id INT PRIMARY KEY AUTO_INCREMENT, dept_name VARCHAR(100) NOT NULL, dept_desc VARCHAR(1000),
 manager_id INT, inst_id INT, create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY (inst_id) REFERENCES institution_info(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE visitor_record (
 id INT PRIMARY KEY AUTO_INCREMENT, visitor_name VARCHAR(100) NOT NULL, phone VARCHAR(32) NOT NULL,
 purpose VARCHAR(500), password VARCHAR(255) NOT NULL, dept_id INT,
 FOREIGN KEY (dept_id) REFERENCES department_info(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE attendance_record (
 id INT PRIMARY KEY AUTO_INCREMENT, user_id INT NOT NULL, user_name VARCHAR(100) NOT NULL,
 work_date DATE NOT NULL, clock_in_time DATETIME, clock_out_time DATETIME, status VARCHAR(32),
 CONSTRAINT uk_attendance_user_date UNIQUE (user_id,work_date),
 FOREIGN KEY(user_id) REFERENCES visitor_record(id) ON DELETE RESTRICT,
 INDEX ix_attendance_date_id(work_date,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE schedule_record (
 id INT PRIMARY KEY AUTO_INCREMENT, user_id INT NOT NULL, title VARCHAR(200) NOT NULL,
 content VARCHAR(2000), schedule_date DATE NOT NULL, status INT NOT NULL DEFAULT 0,
 create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(user_id) REFERENCES visitor_record(id) ON DELETE RESTRICT,
 INDEX ix_schedule_user_date(user_id,schedule_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE message_record (
 id INT PRIMARY KEY AUTO_INCREMENT, sender_id INT NOT NULL, receiver_id INT NOT NULL,
 content TEXT NOT NULL, send_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, is_read INT NOT NULL DEFAULT 0,
 client_message_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin,
 CONSTRAINT uk_message_sender_client UNIQUE(sender_id,client_message_id),
 INDEX ix_message_pair_id(sender_id,receiver_id,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE file_cabinet (
 id INT PRIMARY KEY AUTO_INCREMENT, user_id INT NOT NULL,
 file_name VARCHAR(255) NOT NULL, original_name VARCHAR(255) NOT NULL, file_size BIGINT NOT NULL,
 file_type VARCHAR(100), upload_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 is_deleted INT NOT NULL DEFAULT 0, delete_time DATETIME,
 storage_status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
 CONSTRAINT uk_file_storage_key UNIQUE(file_name),
 FOREIGN KEY(user_id) REFERENCES visitor_record(id) ON DELETE RESTRICT,
 INDEX ix_file_cleanup(storage_status,upload_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
