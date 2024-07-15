package com.example.todesk;

import android.graphics.Bitmap;

public class ImageUtil {
    static public Bitmap rgb2Bitmap(byte[] data, int stride, int pixel, int width, int height) {
        int[] colors = convertByteToColor(data, pixel);
        if (colors == null) {
            return null;
        }
        width = stride / pixel;
        Bitmap bmp = Bitmap.createBitmap(colors, width, height, Bitmap.Config.ARGB_8888);
        return bmp;
    }

    public static int convertByteToInt(byte data) {
        return data & 0xFF;
    }

    public static int[] convertByteToColor(byte[] data, int pixel) {
        int size = data.length;
        if (size == 0 || pixel != 4) {
            return null;
        }

        int[] color = new int[size / 4];
        int red, green, blue, alpha;
        int colorLen = color.length;
        for (int i = 0; i < colorLen; ++i) {
            red = convertByteToInt(data[i * 4]);
            green = convertByteToInt(data[i * 4 + 1]);
            blue = convertByteToInt(data[i * 4 + 2]);
            alpha = convertByteToInt(data[i * 4 + 3]);
            color[i] = (alpha << 24) | (red << 16) | (green << 8) | blue;
        }

        return color;
    }
}
