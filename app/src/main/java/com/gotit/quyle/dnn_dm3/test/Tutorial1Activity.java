package com.gotit.quyle.dnn_dm3.test;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;


import com.gotit.quyle.dnn_dm3.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class Tutorial1Activity extends Activity implements CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";

    private PortraitCameraView mOpenCvCameraView;
    private boolean mIsJavaCamera = true;


    View boudingBox;


    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("MyLib");
    }

    private void loadCVResume() {
        BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS: {
                        mOpenCvCameraView.enableView();
                    }
                    break;
                    default: {
                        super.onManagerConnected(status);
                        break;
                    }
                }
            }
        };

        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
    }

    public Tutorial1Activity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.tutorial1_surface_view);

        if (mIsJavaCamera)
            mOpenCvCameraView = (PortraitCameraView) findViewById(R.id.tutorial1_activity_java_surface_view);
        else
            mOpenCvCameraView = (PortraitCameraView) findViewById(R.id.tutorial1_activity_java_surface_view);


        boudingBox = findViewById(R.id.bouding_box_id);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

        Log.d("external ", "" + Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    public void drawBoudingBox(Rect r) {
        boudingBox.setVisibility(View.VISIBLE);
//        mBoudingBoxView.startAnimation(mCameraFocusAnim);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) boudingBox.getLayoutParams();
        params.width = r.width;
        params.height = r.height;
        params.setMargins(r.x, r.y, 0, 0);
        boudingBox.setLayoutParams(params);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        boudingBox.setVisibility(View.INVISIBLE);
        loadCVResume();

    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }


    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    public void onCameraViewStopped() {

    }

    int index = 0;

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        return inputFrame.rgba();

    }
}
