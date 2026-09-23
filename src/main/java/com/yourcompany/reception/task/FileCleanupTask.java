package com.yourcompany.reception.task;
import com.yourcompany.reception.service.*;
import com.yourcompany.reception.entity.FileCabinet;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
import java.io.IOException;
import java.util.logging.*;

@Component
public class FileCleanupTask {
    private static final Logger LOG=Logger.getLogger(FileCleanupTask.class.getName());
    private final FileCabinetService files;
    private final FileStorage storage;
    public FileCleanupTask(FileCabinetService files,FileStorage storage){this.files=files;this.storage=storage;}
    public void purge(FileCabinet meta) throws IOException {
        storage.delete(meta.getFile_name());
        // Keep the tombstone until storage deletion succeeds. Repeated deletion is safe.
        files.finishDeletion(meta.getId());
    }
    @Scheduled(fixedDelay=60000)
    public void cleanup() {
        try {
            for(FileCabinet meta:files.pendingCleanup()) {
                try { purge(meta); } catch(Exception ex){ LOG.log(Level.WARNING,"File cleanup will retry, id="+meta.getId(),ex); }
            }
        } catch(Exception ex) { LOG.log(Level.WARNING,"Unable to scan pending file cleanup",ex); }
    }
}
