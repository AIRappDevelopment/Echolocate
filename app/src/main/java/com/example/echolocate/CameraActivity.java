package com.example.echolocate;

import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        Intent intent = getIntent();
        speechTTText = findViewById(R.id.speechTTText);
    }

    //gets speech to text conversion
    //done with the help of Smartherd youtube
    public void getSpeechInput (View v){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        BypassRecognitionListener listener = new BypassRecognitionListener();
        listener.speechTTText = this.speechTTText;
        SpeechRecognizer sr = SpeechRecognizer.createSpeechRecognizer(this);
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
        private Intent SpeechRecognizerIntent;

        BypassRecognitionListener(){

        }
        @Override
        public void onBeginningOfSpeech()
        {
            //Log.d(TAG, "onBeginingOfSpeech");
        }

        @Override
        public void onBufferReceived(byte[] buffer)
        {
        }

        @Override
        public void onEndOfSpeech()
        {
            //Log.d(TAG, "onEndOfSpeech");
        }

        @Override
        public void onError(int error)
        {
            speechRecognizer.startListening(SpeechRecognizerIntent);
            //Log.d(TAG, "error = " + error);
        }

        @Override
        public void onEvent(int eventType, Bundle params)
        {

        }

        @Override
        public void onPartialResults(Bundle partialResults)
        {

        }

        @Override
        public void onReadyForSpeech(Bundle params)
        {
            Log.d(TAG, "onReadyForSpeech"); //$NON-NLS-1$
        }

        @Override
        public void onResults(Bundle results)
        {
            //Log.d(TAG, "onResults"); //$NON-NLS-1$
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            // matches are the return values of speech recognition engine
            // Use these values for whatever you wish to do
            speechTTText.setText(matches.get(0));
        }

        @Override
        public void onRmsChanged(float rmsdB)
        {
        }
    }

}
