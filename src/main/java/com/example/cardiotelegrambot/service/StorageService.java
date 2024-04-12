package com.example.cardiotelegrambot.service;

import java.nio.file.Path;

public interface StorageService {
    void init();
    void store(String content, String filename);
    Path load(String filename);
    void deleteAll();
}
