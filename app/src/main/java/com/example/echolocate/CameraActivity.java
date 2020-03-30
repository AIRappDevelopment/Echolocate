package com.example.echolocate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
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

    TextView speechTTText;//speech to text result

    private SpeechRecognizer sr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        Intent intent = getIntent();
        speechTTText = findViewById(R.id.speechTTText);


        Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getApplicationContext().getPackageName());

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        sr = SpeechRecognizer.createSpeechRecognizer(this.getApplicationContext());
        BypassRecognitionListener listener = new BypassRecognitionListener(speechTTText, sr, speechRecognizerIntent);
        sr.setRecognitionListener(listener);
        sr.startListening(speechRecognizerIntent);
        //Log.d("RecognitionListener", "error");

    }

    //gets speech to text conversion
    //done with the help of Smartherd youtube
    public void getSpeechInput (View v){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        SpeechRecognizer sr = SpeechRecognizer.createSpeechRecognizer(this);
        BypassRecognitionListener listener = new BypassRecognitionListener(speechTTText, sr, intent);
        sr.setRecognitionListener(listener);
        sr.startListening(intent);

//        if (intent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(intent, SPEECH_TO_TEXT_REQUEST_CODE);
//        } else {
//            Toast.makeText(this, "unsupported", Toast.LENGTH_SHORT).show();
//        }
    }

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

}
