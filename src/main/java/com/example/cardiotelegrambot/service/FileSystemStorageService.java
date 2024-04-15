package com.example.cardiotelegrambot.service;

import com.example.cardiotelegrambot.config.LogConfig;
import com.example.cardiotelegrambot.config.StorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This is a service class that implements the StorageService interface.
 * It provides methods for initializing the storage, storing content, loading content, and deleting all content.
 * It uses the FileSystemUtils utility class from Spring and the LogConfig class for logging.
 */
@Service
public class FileSystemStorageService implements StorageService {
    private final Path rootLocation;

    /**
     * This is a constructor that takes a StorageProperties object as input.
     * It uses the location property of the StorageProperties object to set the root location of the storage.
     *
     * @param properties A StorageProperties object that contains the location of the storage.
     */
    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
    }

    /**
     * This method initializes the storage.
     * It tries to create the directories for the root location of the storage.
     * If an IOException occurs, it logs the stack trace of the exception.
     */
    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            LogConfig.logError(e.getStackTrace());
        }
    }

    /**
     * This method stores content.
     * It takes a string of content and a filename as input.
     * It tries to write the content to a file with the given filename in the root location of the storage.
     * If an IOException occurs, it logs the stack trace of the exception.
     *
     * @param content The content to be stored.
     * @param filename The name of the file in which to store the content.
     */
    @Override
    public void store(String content, String filename) {
        try {
            Path destinationFile = this.rootLocation.resolve(filename).normalize().toAbsolutePath();
            Files.write(destinationFile, content.getBytes());
        } catch (IOException e) {
            LogConfig.logError(e.getStackTrace());
        }
    }

    /**
     * This method loads content.
     * It takes a filename as input and returns a Path object representing the location of the file in the root location of the storage.
     *
     * @param filename The name of the file to load.
     * @return A Path object representing the location of the file.
     */
    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    /**
     * This method deletes all content.
     * It deletes all files in the root location of the storage.
     */
    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }
}
