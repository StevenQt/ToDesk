package com.example.todesk;

import android.media.Image;
import android.media.ImageReader;

public class ImageListener implements ImageReader.OnImageAvailableListener {
    private String TAG = this.getClass().toString();
    @Override
    public void onImageAvailable(ImageReader reader) {
        try (Image image = reader.acquireLatestImage()) {
            System.out.println("onImageAvailable " + image.getWidth());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
