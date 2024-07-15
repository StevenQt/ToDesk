package com.example.todesk;

import android.graphics.Rect;
import android.os.IBinder;
import android.util.Log;
import android.view.Surface;

public class SurfaceControl {
    private static String TAG = "SurfaceControl";
    private static Class<?> clazz;

    public static void inti() {
        try {
            clazz = Class.forName("android.view.SurfaceControl");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void openTransaction() {
        try {
            clazz.getMethod("openTransaction").invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void closeTransaction() {
        try {
            clazz.getMethod("closeTransaction").invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setDisplaySurface(IBinder display, Surface consumer) {
        try {
            clazz.getMethod("setDisplaySurface", IBinder.class, Surface.class).invoke(null, display, consumer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setDisplayLayerStack(IBinder displayToken, int layerStack) {
        try {
            clazz.getMethod("setDisplayLayerStack", IBinder.class, int.class).invoke(null, displayToken, layerStack);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setDisplayProjection(IBinder displayToken, int rotation, Rect layerStackRect, Rect displayRect) {
        try {
            clazz.getMethod("setDisplayProjection", IBinder.class, int.class, Rect.class, Rect.class)
                    .invoke(null, displayToken, rotation, layerStackRect, displayRect);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static IBinder createDisplay(String name, boolean secure) {
        try {
            return (IBinder)clazz.getMethod("createDisplay", String.class, boolean.class).invoke(null, name, secure);
        } catch (Exception e) {
            Log.e(TAG, "create display err:" + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
