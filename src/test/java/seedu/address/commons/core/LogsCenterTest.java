package seedu.address.commons.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class LogsCenterTest {

    @TempDir
    public Path temporaryFolder;

    @Test
    public void addFileHandler_writableFolder_addsFileHandler() {
        Logger logger = Logger.getLogger("LogsCenterTest.writable");
        logger.setUseParentHandlers(false);

        LogsCenter.addFileHandler(logger, temporaryFolder.resolve("test.log").toString());

        Handler[] handlers = logger.getHandlers();
        assertEquals(1, handlers.length);
        assertTrue(handlers[0] instanceof FileHandler);
        handlers[0].close();
        logger.removeHandler(handlers[0]);
    }

    @Test
    public void addFileHandler_unopenableFile_logsWarningWithoutThrowing() {
        Logger logger = Logger.getLogger("LogsCenterTest.unopenable");
        logger.setUseParentHandlers(false);
        List<LogRecord> records = new ArrayList<>();
        Handler recorder = new Handler() {
            @Override
            public void publish(LogRecord logRecord) {
                records.add(logRecord);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        logger.addHandler(recorder);

        // the parent folder does not exist, so the log file cannot be opened
        LogsCenter.addFileHandler(logger, temporaryFolder.resolve("missing").resolve("test.log").toString());

        assertEquals(1, logger.getHandlers().length);
        assertEquals(1, records.size());
        assertEquals("Error adding file handler for logger.", records.get(0).getMessage());
        logger.removeHandler(recorder);
    }
}
