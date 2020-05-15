package com.example.echolocate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import java.util.ArrayList;


import com.example.echolocate.helpers.BypassRecognitionListener;
import com.example.echolocate.helpers.GraphicOverlay;
import com.example.echolocate.helpers.VisionAnalyzer;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class CameraActivity extends AppCompatActivity {

    private static final int REQUEST_AUDIO_CAMERA_PERMISSION_CODE = 1;

    private Executor executor = Executors.newSingleThreadExecutor();

    private TextView speechTTText;//speech to text result
    private SpeechRecognizer speechRecognizer;
    private TextureView cameraView;
    private GraphicOverlay graphicOverlay;
    private AtomicBoolean isSpeechDetecting;
    private int speechX;
    private int speechY;

    public void setSpeechX(int speechX) {
        this.speechX = speechX;
    }

    public void setSpeechY(int speechY) {
        this.speechY = speechY;
    }

    public int getSpeechX() {
        return speechX;
    }

    public int getSpeechY() {
        return speechY;
    }

    //Allows options to be selected
    private FirebaseVisionFaceDetectorOptions realTimeOpts =
            new FirebaseVisionFaceDetectorOptions.Builder()
                    .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                    .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                    .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                    .build();

    //creates detector from the options selected
    private FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
            .getVisionFaceDetector(realTimeOpts);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        speechTTText = (TextView) findViewById(R.id.speechTTText);
        cameraView = findViewById(R.id.camera_texture_view);
        graphicOverlay = findViewById(R.id.graphic_overlay);
        isSpeechDetecting= new AtomicBoolean(false);
        speechX = 0;
        speechY = 0;
        if(checkPermissions()){
            startCamera();
        }else{
            requestPermissions();
        }
    }

    /**
     * Sets the parameters actively of a textView
     * @param xCoord
     * @param yCoord
     */
    public void setSpeechTTText(int xCoord, int yCoord){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(displayMetrics.heightPixels,
                displayMetrics.widthPixels);
        speechTTText.setLayoutParams(layoutParams);
        speechTTText.setX(xCoord);
        speechTTText.setY(yCoord);
    }

    /**
     *Gets Speech to Text Converstion
     */
    public void getSpeechInput(){
        if (!isSpeechDetecting.get()) {
            //creates a speech recognizer intent and sets its settings
            Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getApplicationContext().getPackageName());

            //Adds a listener to run passively in the background
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this.getApplicationContext());
            BypassRecognitionListener listener = new BypassRecognitionListener(speechTTText, speechRecognizer, this);
            speechRecognizer.setRecognitionListener(listener);
            speechRecognizer.startListening(speechRecognizerIntent);
            isSpeechDetecting.set(true);
            //assignText(listener.getResults());
        }

    }
    public void setIsSpeechDetecting(boolean val){
        isSpeechDetecting.set(val);
    }
    /**
     * Method starts camera preview and adds analyzer to the lifecycle
     */
    private void startCamera(){
        CameraX.unbindAll();

        AspectRatio aspectRatio = AspectRatio.RATIO_16_9;
        Size screen = new Size(1080, 1920); //size of the screen

        //configures the preview
        PreviewConfig pConfig = new PreviewConfig.Builder()
                .setTargetResolution(screen)
                //.setLensFacing(CameraX.LensFacing.FRONT)
                .build();

        Preview preview = new Preview(pConfig);

        //to update the surface texture we  have to destroy it first then re-add it
        preview.setOnPreviewOutputUpdateListener(
                new Preview.OnPreviewOutputUpdateListener() {
                    @Override
                    public void onUpdated(@NonNull Preview.PreviewOutput output) {
                        ViewGroup parent = (ViewGroup) cameraView.getParent();
                        parent.removeView(cameraView);
                        parent.addView(cameraView, 0);
                        cameraView.setSurfaceTexture(output.getSurfaceTexture());
                        updateTransform();
                    }
                });
        //preview.setTargetRotation(Surface.ROTATION_270);
        //configures the image analyzer
        ImageAnalysisConfig iaConfig = new ImageAnalysisConfig.Builder()
                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis(iaConfig);
        imageAnalysis.setAnalyzer(executor, new VisionAnalyzer(detector, graphicOverlay, this));

        //binds the settings to the Camera
        CameraX.bindToLifecycle(this, preview, imageAnalysis);
    }

    /**
     * Accounts for rotation of the Phone
     */
    private void updateTransform(){
        Matrix mx = new Matrix();
        float w = 1080;
        float h = 1920;

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
     * allows viewing of permission errors
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_AUDIO_CAMERA_PERMISSION_CODE:
                if (grantResults.length> 0) {
                    boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (permissionToRecord ) {
                        Toast.makeText(getApplicationContext(), "Permission Granted for Mic", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(),"Permission Denied for Mic",Toast.LENGTH_LONG).show();
                    }
                }
                break;

        }
    }

    /**
     * Checks for Audio and Camera Permissions
     * @return
     */
    public boolean checkPermissions() {
        int audioRes = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        int cameraRes = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
        return audioRes  == PackageManager.PERMISSION_GRANTED && cameraRes == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Requests Audio and Camera Permissions
     */
    private void requestPermissions() {
        ActivityCompat.requestPermissions(CameraActivity.this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA}, REQUEST_AUDIO_CAMERA_PERMISSION_CODE);
    }
}
