package com.gotit.quyle.dnn_dm3;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ImageView;

import org.opencv.core.Mat;

import java.io.IOException;
import java.util.List;

/**
 * Created by QUYLE on 4/10/17.
 */

public class CameraPreview implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private Camera mCamera = null;
    private ImageView MyCameraPreview = null;
    private Bitmap bitmap = null;
    private int[] pixels = null;
    private byte[] FrameData = null;
    private int imageFormat;
    private int PreviewSizeWidth;
    private int PreviewSizeHeight;
    private boolean bProcessing = false;

    Handler mHandler = new Handler(Looper.getMainLooper());

    public CameraPreview(
            ImageView CameraPreview) {

        MyCameraPreview = CameraPreview;

    }


    private Runnable DoImageProcessing = new Runnable() {
        public void run() {
            bProcessing = true;
            NativeClass.imageProcessing(PreviewSizeWidth, PreviewSizeHeight, FrameData, pixels);
            bitmap.setPixels(pixels, 0, PreviewSizeWidth, 0, 0, PreviewSizeWidth, PreviewSizeHeight);
            MyCameraPreview.setImageBitmap(bitmap);
            bProcessing = false;
        }
    };

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        if (!bProcessing) {
            FrameData = bytes;
            mHandler.post(DoImageProcessing);
        }
    }

    public void onPause() {
        mCamera.stopPreview();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        mCamera = Camera.open();

        try {
            // If did not set the SurfaceHolder, the preview area will be black.
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.setDisplayOrientation(90);
            mCamera.getParameters().set("orientation", "portrait");
            mCamera.setPreviewCallback(this);
        } catch (IOException e) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int t, int i1, int i2) {
        Camera.Parameters parameters;

        parameters = mCamera.getParameters();
        // Set the camera preview size


        Camera.Size bestSize = null;

        List<Camera.Size> sizeList = mCamera.getParameters().getSupportedPreviewSizes();
        bestSize = sizeList.get(0);

        for (int i = 1; i < sizeList.size(); i++) {
            if ((sizeList.get(i).width * sizeList.get(i).height) >
                    (bestSize.width * bestSize.height)) {
                bestSize = sizeList.get(i);
            }
        }
        PreviewSizeWidth = bestSize.width;
        PreviewSizeHeight = bestSize.height;
        bitmap = Bitmap.createBitmap(PreviewSizeWidth, PreviewSizeHeight, Bitmap.Config.ARGB_8888);
        pixels = new int[PreviewSizeWidth * PreviewSizeHeight];
        parameters.setPreviewSize(bestSize.width, bestSize.height);

        imageFormat = parameters.getPreviewFormat();

        mCamera.setParameters(parameters);

        mCamera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;

    }


}
