package com.example.echolocate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Matrix;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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

    //Runs vision processing on a separate thread to offload the work
    private Executor executor = Executors.newSingleThreadExecutor();

    private TextView speechTTText;//speech to text result
    private SpeechRecognizer speechRecognizer;//speech recognizer
    private TextureView cameraView;//Camera texture
    private GraphicOverlay graphicOverlay;//Graphic Overlay
    private AtomicBoolean isSpeechDetecting;//State of speech detector

    //Allows options to be selected
    private FirebaseVisionFaceDetectorOptions realTimeOpts =
            new FirebaseVisionFaceDetectorOptions.Builder()
                    .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                    .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                    .build();

    //creates detector from the options selected
    private FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
            .getVisionFaceDetector(realTimeOpts);

    /**
     * Initializes the code
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        speechTTText = (TextView) findViewById(R.id.speechTTText);
        cameraView = findViewById(R.id.camera_texture_view);
        graphicOverlay = findViewById(R.id.graphic_overlay);
        isSpeechDetecting= new AtomicBoolean(false);

        //does a permission check before starting the camera
        if(checkPermissions()){
            startCamera();
        }else{
            requestPermissions();
        }
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

    /**
     * Method starts camera preview and adds analyzer to the lifecycle
     * All camerax done with the help of https://developer.android.com/training/camerax
     * and https://heartbeat.fritz.ai/blink-detection-on-android-using-firebase-ml-kits-face-detection-api-6d09823db535
     */
    private void startCamera(){
        CameraX.unbindAll();

        //configures the preview
        PreviewConfig pConfig = new PreviewConfig.Builder()
                //.setTargetResolution(screen)
                //.setLensFacing(CameraX.LensFacing.FRONT)
                .build();

        Preview preview = new Preview(pConfig);

        //to update the surface texture we have to destroy it first then re-add it
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

        //configures the image analyzer
        ImageAnalysisConfig iaConfig = new ImageAnalysisConfig.Builder()
                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                .build();

        //passes in our new thread, and our vision analyzer class to run image recognition with
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
     * setter for isSpeechDetecting atomic boolean
     * @param val
     */
    public void setIsSpeechDetecting(boolean val){
        isSpeechDetecting.set(val);
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
