package com.example.echolocate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;


import com.example.echolocate.helpers.BypassRecognitionListener;


import java.util.Locale;

public class CameraActivity extends AppCompatActivity {

    private static final int REQUEST_AUDIO_PERMISSION_CODE = 1;

    private TextView speechTTText;//speech to text result
    private SpeechRecognizer speechRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        speechTTText = (TextView) findViewById(R.id.speechTTText);
    }

    public void setSpeechTTText(int xCoord, int yCoord){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(displayMetrics.heightPixels,
                displayMetrics.widthPixels);
        speechTTText.setLayoutParams(layoutParams);
        speechTTText.setX(xCoord);
        speechTTText.setY(yCoord);
    }
    public void getSpeechInput (View v){
        boolean on = ((ToggleButton) v).isChecked();
        if(on) {
            if (CheckPermissions()) {
                Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getApplicationContext().getPackageName());

                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this.getApplicationContext());
                BypassRecognitionListener listener = new BypassRecognitionListener(speechTTText, speechRecognizer);
                speechRecognizer.setRecognitionListener(listener);
                speechRecognizer.startListening(speechRecognizerIntent);
                //assignText(listener.getResults());
            } else {
                RequestPermissions();
            }
        }
    }

//    public void assignText(ArrayList<String> speechTTResults){
//
//    }

    //allows viewing of permission errors
    //eventually to be deleted
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSION_CODE:
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

    public boolean CheckPermissions() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void RequestPermissions() {
        ActivityCompat.requestPermissions(CameraActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO_PERMISSION_CODE);
    }
}
