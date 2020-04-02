package com.example.echolocate;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;

class BypassRecognitionListener implements RecognitionListener {
    private static final String TAG = "RecognitionListener";
    private TextView speechTTText;
    private SpeechRecognizer speechRecognizer;
    private ArrayList<String> resultingStrings;

    BypassRecognitionListener(TextView speechTTText, SpeechRecognizer speechRecognizer){
        this.speechTTText = speechTTText;
        this.speechRecognizer = speechRecognizer;
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
        resultingStrings = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        speechTTText.setText(resultingStrings.get(0));//leave for testing
    }

    @Override
    public void onRmsChanged(float rmsdB) {
    }

    public ArrayList<String> getResults(){
        return resultingStrings;
    }
}