package com.project.filemanager.previews;

import android.graphics.Bitmap;
import android.util.LruCache;

public final class BitmapCache<T> extends LruCache<T, Bitmap> {

    public BitmapCache() {
        super(512 * 1024);
    }

    @Override
    protected int sizeOf(T key, Bitmap value) {
        return value.getByteCount() / 1024;
    }
}
