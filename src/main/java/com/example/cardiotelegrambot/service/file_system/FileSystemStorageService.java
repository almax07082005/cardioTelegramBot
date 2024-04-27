package com.example.cardiotelegrambot.service.file_system;

import com.example.cardiotelegrambot.config.LogConfig;
import com.example.cardiotelegrambot.config.StorageProperties;
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

    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException exception) {
            LogConfig.logError(exception);
        }
    }

    @Override
    public void store(String content, String filename) {
        try {
            Path destinationFile = this.rootLocation.resolve(filename).normalize().toAbsolutePath();
            Files.write(destinationFile, content.getBytes());
        } catch (IOException exception) {
            LogConfig.logError(exception);
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
