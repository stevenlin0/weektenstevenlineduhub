package service;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BackupService {
    private static final String BACKUP_DIR = "backups"; // This is the directory for storing backups
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Path databasePath; // This is the path to the database file
    private final Path backupDir; // This is the path to the backup directory

    public BackupService(String dbPath) {
        this.databasePath = Paths.get(dbPath);
        this.backupDir = Paths.get(BACKUP_DIR);
        initializeBackupDirectory();
    }

    // This is to create a backup directory if it doesn't exist
    private void initializeBackupDirectory() {
        try {
            if (!Files.exists(backupDir)) {
                Files.createDirectories(backupDir);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // This is to start the automatic backups at the set times
    public void startAutomaticBackup(int intervalHours) {
        scheduler.scheduleAtFixedRate(this::createBackup, 0, intervalHours, TimeUnit.HOURS);
    }

    // This is to stop automated backups
    public void stopAutomaticBackup() {
        scheduler.shutdown();
    }

    // This is to make a backup and gives its file path
    public String createBackup() {
        try {
            String timestamp = LocalDateTime.now().format(formatter);
            String backupFileName = "backup_" + timestamp + ".db";
            Path backupPath = backupDir.resolve(backupFileName);
            
            Files.copy(databasePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
            return backupPath.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // This is to show all backup files
    public List<String> listBackups() {
        List<String> backups = new ArrayList<>();
        try {
            Files.list(backupDir)
                 .filter(path -> path.toString().endsWith(".db"))
                 .sorted()
                 .forEach(path -> backups.add(path.getFileName().toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return backups;
    }

    // This is to replace the database with the backup that is selected
    public boolean restoreBackup(String backupFileName) {
        try {
            Path backupPath = backupDir.resolve(backupFileName);
            if (Files.exists(backupPath)) {
                // Create a backup of current state before restore
                createBackup();
                // Restore the selected backup
                Files.copy(backupPath, databasePath, StandardCopyOption.REPLACE_EXISTING);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    // This is to delete a backup file
    public boolean deleteBackup(String backupFileName) {
        try {
            Path backupPath = backupDir.resolve(backupFileName);
            return Files.deleteIfExists(backupPath);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // This is to clean old backups and keeping only the latest ones
    public void cleanOldBackups(int keepCount) {
        try {
            List<Path> backups = new ArrayList<>();
            Files.list(backupDir)
                 .filter(path -> path.toString().endsWith(".db"))
                 .sorted()
                 .forEach(backups::add);

            while (backups.size() > keepCount) {
                Files.delete(backups.remove(0));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
