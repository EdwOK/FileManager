package com.project.filemanager.managers;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public final class FileManagerCache {

    private static FileManagerCache instance;

    public static FileManagerCache getInstance() {
        if (instance == null) {
            instance = new FileManagerCache();
        }
        return instance;
    }

    private final Map<String, WeakReference<MultiFileManager>> cache;

    private FileManagerCache() {
        this.cache = new HashMap<>();
    }

    public void clear() {
        this.cache.clear();
    }

    public MultiFileManager getOrCreate(final String path) {
        final WeakReference<MultiFileManager> reference = cache.get(path);
        MultiFileManager observer;
        if (reference != null && (observer = reference.get()) != null) {
            return observer;
        } else {
            observer = new MultiFileManager(path);
            this.cache
                    .put(path, new WeakReference<>(observer));
        }
        return observer;
    }
}
