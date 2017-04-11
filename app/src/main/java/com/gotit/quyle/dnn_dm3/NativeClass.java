package com.gotit.quyle.dnn_dm3;

/**
 * Created by QUYLE on 4/4/17.
 */

public class NativeClass {
    public native static String getStringFromNative();

    public native static String getStringFromNative(String file);

    public native static  void findFeatures(int weight, int height, byte[] yuv, int[] bgra);

    public native static String readNet();

    public native static boolean imageProcessing(int width, int height,
                                          byte[] NV21FrameData, int [] pixels);


}
