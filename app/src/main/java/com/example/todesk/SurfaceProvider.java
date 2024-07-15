package com.example.todesk;

import android.graphics.Rect;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Size;

public class SurfaceProvider {
    private IBinder display = null;
    private static DisplayInfo displayInfo;
    private final ImageReader imageReader;
    private final Handler handler;

    public static void init() {
        SurfaceControl.inti();
        DisplayManager.init();
        displayInfo = DisplayManager.getDisplayInfo(0);
    }

    public static Size getScreenSize() {
        return displayInfo.getSize();
    }

    public SurfaceProvider(ImageReader imageReader, Handler handler) {
        this.imageReader = imageReader;
        this.handler = handler;
    }

    public void startShotSurface(ImageReader.OnImageAvailableListener l) {
        try {
            boolean secure = Build.VERSION.SDK_INT < Build.VERSION_CODES.R ||
                    (Build.VERSION.SDK_INT == Build.VERSION_CODES.R && !"S".equals(Build.VERSION.CODENAME));
            display = SurfaceControl.createDisplay("minicap", secure);
            if (display != null) {
                SurfaceControl.openTransaction();
                SurfaceControl.setDisplaySurface(display, imageReader.getSurface());
                Rect layerStackRect = new Rect(0, 0, getScreenSize().getWidth(), getScreenSize().getHeight());
                Rect displayRect = new Rect(0, 0, getScreenSize().getWidth(), getScreenSize().getHeight());
                SurfaceControl.setDisplayProjection(display, 0, layerStackRect, displayRect);
                SurfaceControl.setDisplayLayerStack(display, 0);
            }
        } catch (Exception e) {
            System.err.println("Method not found: " + e.getMessage());
        } finally {
            if (display != null) {
                SurfaceControl.closeTransaction();
            }
        }
        imageReader.setOnImageAvailableListener(l, handler);
    }

    public void stopShotSurface() {
        if (display != null) {
            SurfaceControl.closeTransaction();
        }
    }
}
