package com.gotit.quyle.dnn_dm3;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NativeActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("MyLib");
    }

    boolean isLoadNet;
    String url = "";

    @Override
    protected void onResume() {

        super.onResume();
        if (isReadStorageAllowed()) {
            if (!OpenCVLoader.initDebug()) {
                Toast.makeText(this, "MainActivity OpenCV not loaded", Toast.LENGTH_SHORT);
            } else {
                if (!isLoadNet) {
                    long tStart = System.currentTimeMillis();
                    String s = NativeClass.readNet();
                        long tEnd = System.currentTimeMillis();
                    long tDelta = tEnd - tStart;
                    double elapsedSeconds = tDelta / 1000.0;
                    TextView clock = (TextView) findViewById(R.id.clock);
                    clock.setText("Classified in " + Double.toString(elapsedSeconds) + " secs");
                    Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
                    isLoadNet = true;

                }
            }
            return;
        }
        requestStoragePermission();
    }

    private boolean isReadStorageAllowed() {
        //Getting the permission status
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        //If permission is granted returning true
        if (result != PackageManager.PERMISSION_GRANTED || result1 != PackageManager.PERMISSION_GRANTED)
            return false;

        //If permission is not granted returning false
        return true;
    }

    int STORAGE_PERMISSION_CODE = 100;

    private void requestStoragePermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            if (!OpenCVLoader.initDebug()) {
                Toast.makeText(this, "MainActivity OpenCV not loaded", Toast.LENGTH_SHORT);
            } else {
                if (!isLoadNet) {
                    String s = NativeClass.readNet();
                    Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
                    isLoadNet = true;

                }

            }
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if (requestCode == STORAGE_PERMISSION_CODE) {

            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (!isLoadNet) {
                    String s = NativeClass.readNet();
                    Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
                    isLoadNet = true;

                }
            } else {
                Toast.makeText(this, "Oops you just denied the permission", Toast.LENGTH_LONG).show();
            }
        }
    }

    int REQUEST_CAMERA = 0, SELECT_FILE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tv = (TextView) findViewById(R.id.testTextView);
        tv.setText("");


        final Button buttonClassify = (Button) findViewById(R.id.button);
        buttonClassify.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (TextUtils.isEmpty(url)) {
                    long tStart = System.currentTimeMillis();
                    TextView tv = (TextView) findViewById(R.id.testTextView);
                    tv.setText(NativeClass.getStringFromNative("/sdcard/android-opencv/image.jpg"));
                    long tEnd = System.currentTimeMillis();
                    long tDelta = tEnd - tStart;
                    double elapsedSeconds = tDelta / 1000.0;
                    TextView clock = (TextView) findViewById(R.id.clock);
                    clock.setText("Classified in " + Double.toString(elapsedSeconds) + " secs");
                } else {
                    long tStart = System.currentTimeMillis();
                    TextView tv = (TextView) findViewById(R.id.testTextView);
                    tv.setText(NativeClass.getStringFromNative(url));
                    long tEnd = System.currentTimeMillis();
                    long tDelta = tEnd - tStart;
                    double elapsedSeconds = tDelta / 1000.0;
                    TextView clock = (TextView) findViewById(R.id.clock);
                    clock.setText("Classified in " + Double.toString(elapsedSeconds) + " secs");
                }

            }
        });
        final Button btnSelect = (Button) findViewById(R.id.button2);
        btnSelect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

    }

    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        isLoadNet = true;
        if (resultCode == Activity.RESULT_OK) {
            ImageView imgview = (ImageView) findViewById(R.id.image);
            Bitmap img;
            if (requestCode == REQUEST_CAMERA) {
                img = (Bitmap) data.getExtras().get("data");
                Mat imgMAT = new Mat();
                Utils.bitmapToMat(img, imgMAT);
                Imgproc.cvtColor(imgMAT, imgMAT, Imgproc.COLOR_BGR2GRAY);
                Utils.matToBitmap(imgMAT, img);
                imgview.setImageBitmap(img);
            } else {
                Uri selectedImageUri = data.getData();
                String[] projection = {MediaStore.MediaColumns.DATA};
                Cursor cursor = managedQuery(selectedImageUri, projection, null, null,
                        null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();

                String selectedImagePath = cursor.getString(column_index);
                BitmapFactory.Options options = new BitmapFactory.Options();
                final int REQUIRED_SIZE = 240;
                int scale = 1;
                while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                        && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                    scale *= 2;
                options.inSampleSize = scale;
                options.inJustDecodeBounds = false;
                img = BitmapFactory.decodeFile(selectedImagePath, options);


                Mat imgMAT = new Mat();
                Utils.bitmapToMat(img, imgMAT);
                Imgproc.cvtColor(imgMAT, imgMAT, Imgproc.COLOR_BGR2GRAY);
                Utils.matToBitmap(imgMAT, img);

                imgview.setImageBitmap(img);

            }

            String destFolder = getCacheDir().getAbsolutePath();

            FileOutputStream out = null;
            try {
                out = new FileOutputStream(destFolder + "/image.jpg");
                img.compress(Bitmap.CompressFormat.JPEG, 100, out);

                TextView tv = (TextView) findViewById(R.id.testTextView);
                url = destFolder + "/image.jpg";
                tv.setText(url);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }

    }


}
