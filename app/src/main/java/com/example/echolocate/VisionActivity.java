package com.example.echolocate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.media.Image;
import android.os.Bundle;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.echolocate.helpers.VisionAnalyzer;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Currently place holder activity
 * Runs and tests face detection
 * done with the help of firebase.google.com
 */
public class VisionActivity extends AppCompatActivity {
    Executor executor = Executors.newSingleThreadExecutor();
    private static final int REQUEST_CAMERA_PERMISSION_CODE = 2;
    TextureView cameraView;

    //Allows options to be selected
    FirebaseVisionFaceDetectorOptions realTimeOpts =
            new FirebaseVisionFaceDetectorOptions.Builder()
                    .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                    .build();

    //creates detector from the options selected
    FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
            .getVisionFaceDetector(realTimeOpts);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vision);
        cameraView = findViewById(R.id.camera_texture_view);

        //if permissions are met start the Camera
        if(checkPermissions()){
            startCamera();
        }else{
            requestPermissions();
        }
    }

    /**
     * Method starts camera preview and adds analyzer to the lifecycle
     */
    private void startCamera(){
        CameraX.unbindAll();

        Rational aspectRatio = new Rational (cameraView.getWidth(), cameraView.getHeight());
        Size screen = new Size(cameraView.getWidth(), cameraView.getHeight()); //size of the screen

        //configures the preview
        PreviewConfig pConfig = new PreviewConfig.Builder().setTargetResolution(screen).build();
        Preview preview = new Preview(pConfig);
        //to update the surface texture we  have to destroy it first then re-add it
        preview.setOnPreviewOutputUpdateListener(
                output -> {
                    ViewGroup parent = (ViewGroup) cameraView.getParent();
                    parent.removeView(cameraView);
                    parent.addView(cameraView, 0);
                    cameraView.setSurfaceTexture(output.getSurfaceTexture());
                    updateTransform();
                });

        //configures the image analyzer
        ImageAnalysisConfig iaConfig = new ImageAnalysisConfig.Builder().setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE).build();
        ImageAnalysis imageAnalysis = new ImageAnalysis(iaConfig);
        imageAnalysis.setAnalyzer(executor, new VisionAnalyzer(detector));

        //binds the settings to the Camera
        CameraX.bindToLifecycle(this, preview, imageAnalysis);
    }

    //function that accounts for rotation of the phone
    private void updateTransform(){
        Matrix mx = new Matrix();
        float w = cameraView.getMeasuredWidth();
        float h = cameraView.getMeasuredHeight();

        float cX = w / 2f;
        float cY = h / 2f;

        int rotationDgr;
        int rotation = (int)cameraView.getRotation();

        switch(rotation){
            case Surface.ROTATION_0:
                rotationDgr = 0;
                break;
            case Surface.ROTATION_90:
                rotationDgr = 90;
                break;
            case Surface.ROTATION_180:
                rotationDgr = 180;
                break;
            case Surface.ROTATION_270:
                rotationDgr = 270;
                break;
            default:
                return;
        }
        mx.postRotate((float)rotationDgr, cX, cY);
        cameraView.setTransform(mx);
    }

    /**
     * Sends feedback on permission request
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION_CODE:
                if (grantResults.length> 0) {
                    boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (permissionToRecord ) {
                        Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(),"Permission Denied",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    /**
     * Checks the permissions
     * @return
     */
    public boolean checkPermissions() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Requests the permissions
     */
    private void requestPermissions() {
        ActivityCompat.requestPermissions(VisionActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION_CODE);
    }

}
