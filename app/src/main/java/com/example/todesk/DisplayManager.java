package com.example.todesk;

import android.annotation.SuppressLint;
import android.util.Size;

@SuppressLint("PrivateApi")
public class DisplayManager {
    private static Object displayManager;
    private static Class<?> clazz;

    public static void init() {
        try {
            clazz = Class.forName("android.hardware.display.DisplayManagerGlobal");
            displayManager = clazz.getMethod("getInstance").invoke(null);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static DisplayInfo getDisplayInfo(int displayId) {
        try {
            Object displayInfo = displayManager.getClass()
                    .getMethod("getDisplayInfo", int.class)
                    .invoke(displayManager, displayId);

            Class<?> cls = displayInfo.getClass();
            int width = cls.getDeclaredField("logicalWidth").getInt(displayInfo);
            int height = cls.getDeclaredField("logicalHeight").getInt(displayInfo);
            int rotation = cls.getDeclaredField("rotation").getInt(displayInfo);
            return new DisplayInfo(displayId, new Size(width, height), rotation);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static int[] getDisplayIds() {
        try {
            return (int[])displayManager.getClass().getMethod("getDisplayIds").invoke(displayManager);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
