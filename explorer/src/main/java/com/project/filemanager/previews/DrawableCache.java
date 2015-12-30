package com.project.filemanager.previews;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.LruCache;

public final class DrawableCache<T> extends LruCache<T, Drawable> {

    public DrawableCache() {
        super(512 * 1024);
    }

    @Override
    protected int sizeOf(T key, Drawable value) {
        if (value instanceof BitmapDrawable) {
            return ((BitmapDrawable) value).getBitmap().getByteCount() / 1024;
        } else {
            return super.sizeOf(key, value);
        }
    }
}
