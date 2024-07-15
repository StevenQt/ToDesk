package com.example.todesk;

import android.util.Size;

public class DisplayInfo {
    private int displayId;
    private Size size;
    private int rotation;

    public DisplayInfo(int displayId, Size size, int rotation) {
        this.displayId = displayId;
        this.size = size;
        this.rotation = rotation;
    }

    public int getDisplayId() {
        return displayId;
    }

    public Size getSize() {
        return size;
    }

    public int getRotation() {
        return rotation;
    }

    @Override
    public String toString() {
        return "DisplayInfo{id=" + displayId + " size=" + size + " rotation=" + rotation + "}";
    }
}