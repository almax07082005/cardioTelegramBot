package com.example.cardiotelegrambot.service;

import java.nio.file.Path;

/**
 * This is an interface for a storage service.
 * It provides methods for initializing the storage, storing content, loading content, and deleting all content.
 */
public interface StorageService {

    /**
     * This method initializes the storage.
     * It should be implemented to set up any necessary storage infrastructure.
     */
    void init();

    /**
     * This method stores content.
     * It takes a string of content and a filename as input.
     * The content should be stored in a file with the given filename.
     *
     * @param content The content to be stored.
     * @param filename The name of the file in which to store the content.
     */
    void store(String content, String filename);

    /**
     * This method loads content.
     * It takes a filename as input and returns a Path object representing the location of the file.
     * The file should contain the content that was previously stored with the same filename.
     *
     * @param filename The name of the file to load.
     * @return A Path object representing the location of the file.
     */
    Path load(String filename);

    /**
     * This method deletes all content.
     * It should be implemented to remove all content from the storage.
     */
    void deleteAll();
}
