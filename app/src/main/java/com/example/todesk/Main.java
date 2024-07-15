package com.example.todesk;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.input.InputManager;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.view.InputEvent;
import android.view.MotionEvent;

import androidx.core.view.InputDeviceCompat;

import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

public class Main {
    private static String TAG = "Main";
    private static final int LISTEN_PORT = 56789;
    private static final String KEY_FINGER_DOWN = "finger_down";
    private static final String KEY_FINGER_UP = "finger_up";
    private static final String KEY_FINGER_MOVE = "finger_move";
    private static final String KEY_CHANGE_SIZE = "change_size";
    private static final String KEY_HEARTBEAT = "heartbeat";
    private static final String KEY_EVENT_TYPE = "type";
    private static Object sInputManager = null;
    private static Method sInjectInputEventMethod = null;
    private static int sPictureWidth = 360;
    private static int sPictureHeight = 760;
    private static int sRotate = 0;
    private static int sViewWidth = 1080;
    private static int sViewHeight = 2316;

    private static Thread sSendImageThread = null;
    private static boolean sThreadKeepRunning = false;
    private static boolean sFirstHb = true;

    private static SurfaceProvider sp = null;
    private static DisplayListener lis = null;
    private static Handler handler = null;

    public static void main(String[] args) {
        Looper.prepare();
        System.out.println("ToDesk start listen port:" + LISTEN_PORT + " sdk:" + Build.VERSION.SDK_INT);
        handler = new Handler(Looper.myLooper());
        SurfaceProvider.init();
        Size screenSize = SurfaceProvider.getScreenSize();
        sViewWidth = screenSize.getWidth();
        sViewHeight = screenSize.getHeight();

        try {
            sInputManager = InputManagerImpl.getInputManager();
            sInjectInputEventMethod = InputManagerImpl.getInjectInputEvent();
            AsyncHttpServer httpServer = new AsyncHttpServer();
            httpServer.websocket("/input", new InputHandler());
            httpServer.listen(LISTEN_PORT);
            Looper.loop();
        } catch (Exception e) {
            Log.e(TAG,"error: " + e.getMessage());
        }
    }

    private static void startSendImage(WebSocket webSocket) {
        System.out.println("startSendImage enter");
        if (Build.VERSION.SDK_INT <= 31) {
            if (sSendImageThread == null) {
                sThreadKeepRunning = true;
                sSendImageThread = new Thread(new SurfaceShotThread(webSocket));
                sSendImageThread.start();
            }
        } else {
            handler.post(() -> {
                @SuppressLint("WrongConstant")
                ImageReader imgReader = ImageReader.newInstance(sViewWidth, sViewHeight, 0x1, 2);
                sp = new SurfaceProvider(imgReader, handler);
                lis = new DisplayListener(webSocket);
                sp.startShotSurface(lis);
            });
        }
        System.out.println("startSendImage leave");
    }

    private static void stopSendImage() {
        System.out.println("stopSendImage enter " + sFirstHb);
        if (sThreadKeepRunning && sSendImageThread != null) {
            sThreadKeepRunning = false;
            sSendImageThread = null;
        } else {
            if (sp != null) {
                sp.stopShotSurface();
                sp = null;
                lis = null;
            }
        }
        sFirstHb = true;
        System.out.println("stopSendImage leave");
    }

    private static class InputHandler implements AsyncHttpServer.WebSocketRequestCallback {
        @Override
        public void onConnected(WebSocket webSocket, AsyncHttpServerRequest request) {
            System.out.println("websockets connected. path:" + request.getPath());
            stopSendImage();
            webSocket.setStringCallback(new WebSocket.StringCallback() {
                @Override
                public void onStringAvailable(String s) {
                    try {
                        JSONObject event = new JSONObject(s);
                        String eventType = event.getString(KEY_EVENT_TYPE);
                        switch (eventType) {
                            case KEY_FINGER_DOWN:
                                float x = event.getInt("x") * (sViewWidth * 1.0f / sPictureWidth);
                                float y = event.getInt("y") * (sViewHeight * 1.0f  / sPictureHeight);
                                injectMotionEvent(InputDeviceCompat.SOURCE_TOUCHSCREEN, 0,
                                        SystemClock.uptimeMillis(), x, y, 1.0f);
                                break;
                            case KEY_FINGER_UP:
                                x = event.getInt("x") * (sViewWidth * 1.0f  / sPictureWidth);
                                y = event.getInt("y") * (sViewHeight * 1.0f  / sPictureHeight);
                                injectMotionEvent(InputDeviceCompat.SOURCE_TOUCHSCREEN, 1,
                                        SystemClock.uptimeMillis(), x, y, 1.0f);
                                break;
                            case KEY_FINGER_MOVE:
                                x = event.getInt("x") * (sViewWidth * 1.0f / sPictureWidth);
                                y = event.getInt("y") * (sViewHeight * 1.0f / sPictureHeight);
                                injectMotionEvent(InputDeviceCompat.SOURCE_TOUCHSCREEN, 2,
                                        SystemClock.uptimeMillis(), x, y, 1.0f);
                                break;
                            case KEY_HEARTBEAT:
                                if (sFirstHb) {
                                    sFirstHb = false;
                                    startSendImage(webSocket);
                                }
                                break;
                            case KEY_CHANGE_SIZE:
                                sPictureWidth = event.getInt("w");
                                sPictureHeight = event.getInt("h");
                                sRotate = event.getInt("r");
                                break;
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            });
        }
    }

    private static class DisplayListener implements ImageReader.OnImageAvailableListener {
        WebSocket mWebSocket;
        DisplayListener(WebSocket webSocket) {
            mWebSocket = webSocket;
        }

        @Override
        public void onImageAvailable(ImageReader reader) {
            try (Image image = reader.acquireLatestImage()) {
                Image.Plane[] planes = image.getPlanes();
                if (planes.length > 0 ) {
                    int row = planes[0].getRowStride();
                    int pixel = planes[0].getPixelStride();
                    ByteBuffer buffer = planes[0].getBuffer();
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);
                    Bitmap bitmapImage = ImageUtil.rgb2Bitmap(bytes, row, pixel, image.getWidth(), image.getHeight());
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    mWebSocket.send(out.toByteArray());
                }
            } catch (Exception e) {
                System.out.println("screenshot error");
                System.out.println(e.getMessage());
            }
        }
    }

    private static class SurfaceShotThread implements Runnable {
        WebSocket mWebSocket;
        String mSurfaceName;
        Method method = null;
        Class cls = null;
        SurfaceShotThread(WebSocket webSocket) {
            mWebSocket = webSocket;
            if (Build.VERSION.SDK_INT <= 17) {
                mSurfaceName = "android.view.Surface";
            } else {
                mSurfaceName = "android.view.SurfaceControl";
            }

            try {
                cls = Class.forName(mSurfaceName);
            } catch (Exception e) {
                System.out.println("for name class error");
                System.out.println(e.getMessage());
            }

            // sdk = 31
            // public static Bitmap screenshot(Rect sourceCrop, int width, int height, int rotation)
            if (cls != null) {
                try {
                    method = cls.getDeclaredMethod("screenshot", new Class[]{Rect.class, Integer.TYPE, Integer.TYPE, Integer.TYPE});
                } catch (Exception e) {
                    System.out.println("get method error");
                    System.out.println(e.getMessage());
                }
            }
        }
        @Override
        public void run() {
            while (sThreadKeepRunning && method != null) {
                try {
                    Rect rc = new Rect(0, 0, sViewWidth, sViewHeight);
                    Bitmap bitmap = (Bitmap)method.invoke(null, new Object[]{rc, Integer.valueOf(sViewWidth), Integer.valueOf(sViewHeight), 0});
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    Matrix matrix = new Matrix();
                    matrix.setRotate(sRotate);
                    Bitmap resultBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix,false);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    resultBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    mWebSocket.send(out.toByteArray());
                } catch (Exception e) {
                    System.out.println("screenshot error");
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    private static void injectMotionEvent(int inputSource, int action, long when, float x, float y, float pressure) {
        //System.out.println("motion event action:" + action + " x:" + x + " y:" + y);
        handler.post(() -> {
            try {
                MotionEvent event = MotionEvent.obtain(when, when, action, x, y, pressure, 1.0f, 0, 1.0f, 1.0f, 0, 0);
                event.setSource(inputSource);
                sInjectInputEventMethod.invoke(sInputManager, event, 0);
                event.recycle();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });
    }
}