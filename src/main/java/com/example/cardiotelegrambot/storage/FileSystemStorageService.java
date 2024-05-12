package com.example.cardiotelegrambot.storage;

import com.example.cardiotelegrambot.config.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileSystemStorageService implements StorageService {

    private final Path rootLocation;
    private final Logger logger;

    @Autowired
    public FileSystemStorageService(StorageProperties properties, Logger logger) {
        this.rootLocation = Paths.get(properties.getLocation());
        this.logger = logger;
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException exception) {
            logger.logError(exception);
        }
    }

    @Override
    public void store(String content, String filename) {
        try {
            Path destinationFile = this.rootLocation.resolve(filename).normalize().toAbsolutePath();
            Files.write(destinationFile, content.getBytes());
        } catch (IOException exception) {
            logger.logError(exception);
        }
    }

    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }
}
