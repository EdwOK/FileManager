package com.project.filemanager.utils;

public final class ClipBoard {

    private static final Object LOCK = new Object();

    private static String[] clipBoard;

    private static boolean isMove;

    private static volatile boolean isLocked;

    private ClipBoard() {
    }

    public static synchronized void cutCopy(String[] files) {
        synchronized (LOCK) {
            if (!isLocked) {
                isMove = false;
                clipBoard = files;
            }
        }
    }

    public static synchronized void cutMove(String[] files) {
        synchronized (LOCK) {
            if (!isLocked) {
                isMove = true;
                clipBoard = files;
            }
        }
    }

    public static void lock() {
        synchronized (LOCK) {
            isLocked = true;
        }
    }

    public static void unlock() {
        synchronized (LOCK) {
            isLocked = false;
        }
    }

    public static String[] getClipBoardContents() {
        synchronized (LOCK) {
            return clipBoard;
        }
    }

    public static boolean isEmpty() {
        synchronized (LOCK) {
            return clipBoard == null;
        }
    }

    public static boolean isMove() {
        synchronized (LOCK) {
            return isMove;
        }
    }

    public static void clear() {
        synchronized (LOCK) {
            if (!isLocked) {
                clipBoard = null;
                isMove = false;
            }
        }
    }
}
