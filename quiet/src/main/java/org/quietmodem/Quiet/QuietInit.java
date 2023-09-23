package org.quietmodem.Quiet;

import android.util.Log;

public class QuietInit {
    private static native void nativeLWIPInit();

    private static boolean has_init = false;
    private static boolean has_lwip_init = false;

    public static void init() {
        if (has_init) {
            return;
        }
        has_init = true;
        System.loadLibrary("complex");
        System.loadLibrary("fec");
        System.loadLibrary("jansson");
        System.loadLibrary("liquid");
        System.loadLibrary("quiet");
        System.loadLibrary("quiet_lwip");
        System.loadLibrary("quiet-jni");

        Log.d("lol", "libraries loaded");
    }

    static void lwipInit() {
        if (has_lwip_init) {
            return;
        }
        has_lwip_init = true;
        nativeLWIPInit();
    }
}
