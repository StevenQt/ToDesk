package com.example.todesk;

import android.annotation.SuppressLint;
import android.view.InputEvent;

import java.lang.reflect.Method;

@SuppressLint("PrivateApi")
public class InputManagerImpl {
    private static Object inputManager = null;
    private static Method method = null;
    private static Class<?> clazz;

    public static Object getInputManager() {
        try {
            clazz = Class.forName("android.hardware.input.InputManager");
            inputManager = clazz.getMethod("getInstance").invoke(null);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        return inputManager;
    }

    public static Method getInjectInputEvent() {
        try {
            method = inputManager.getClass().getMethod("injectInputEvent", InputEvent.class, int.class);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        return method;
    }
}
