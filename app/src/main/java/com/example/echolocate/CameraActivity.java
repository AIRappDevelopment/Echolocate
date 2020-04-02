package com.example.echolocate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.CollationElementIterator;
import java.util.ArrayList;
import java.util.Locale;

public class CameraActivity extends AppCompatActivity {

    public static final int SPEECH_TO_TEXT_REQUEST_CODE = 1;
    private static final int REQUEST_AUDIO_PERMISSION_CODE = 1;

    TextView speechTTText;//speech to text result

    private SpeechRecognizer sr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        Intent intent = getIntent();
        speechTTText = findViewById(R.id.speechTTText);

        if(CheckPermissions()) {
            Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getApplicationContext().getPackageName());

            sr = SpeechRecognizer.createSpeechRecognizer(this.getApplicationContext());
            BypassRecognitionListener listener = new BypassRecognitionListener(speechTTText, sr, speechRecognizerIntent);
            sr.setRecognitionListener(listener);
            sr.startListening(speechRecognizerIntent);
            //Log.d("RecognitionListener", "error");
        }else{
            RequestPermissions();
        }
    }

    //gets speech to text conversion
    //done with the help of Smartherd youtube
    public void getSpeechInput (View v){
        if(CheckPermissions()) {
            Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getApplicationContext().getPackageName());

            sr = SpeechRecognizer.createSpeechRecognizer(this.getApplicationContext());
            BypassRecognitionListener listener = new BypassRecognitionListener(speechTTText, sr, speechRecognizerIntent);
            sr.setRecognitionListener(listener);
            sr.startListening(speechRecognizerIntent);
            //Log.d("RecognitionListener", "error");
        }else{
            RequestPermissions();
        }
    }

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
        ActivityCompat.requestPermissions(CameraActivity.this, new String[]{Manifest.permission.RECORD_AUDIO    }, REQUEST_AUDIO_PERMISSION_CODE);
    }

    static public class BypassRecognitionListener implements RecognitionListener {
        private static final String TAG = "RecognitionListener";
        private Object TranslatorUtil;
        private TextView speechTTText;
        private SpeechRecognizer speechRecognizer;
        private Intent speechRecognizerIntent;

        BypassRecognitionListener(TextView speechTTText, SpeechRecognizer speechRecognizer, Intent speechRecognizerIntent){
            this.speechTTText = speechTTText;
            this.speechRecognizer = speechRecognizer;
            this.speechRecognizerIntent = speechRecognizerIntent;
        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {
        }

        @Override
        public void onEndOfSpeech() {
        }

        @Override
        public void onError(int error)
        {
            Log.d(TAG, Integer.toString(error));
        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onReadyForSpeech(Bundle params) {
        }

        @Override
        public void onResults(Bundle results) {
            Log.d(TAG, "error");
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            speechTTText.setText(matches.get(0));
        }

        @Override
        public void onRmsChanged(float rmsdB) {
        }
    }

     /* unused code, uses speech-to-text w/ pop-up box
    @Override
    protected void onActivityResult ( int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SPEECH_TO_TEXT_REQUEST_CODE:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    speechTTText.setText(result.get(0));
                }
                break;
        }
    }
    */
}
