package com.example.echolocate.helpers;

import android.widget.TextView;

public class Person {
    private int trackingId;
    private TextView speech;
    private String currentWords = "";
    private int posX = 0, posY = 0;

    Person(int trackingId, TextView speech){
        this.trackingId = trackingId;
        this.speech = speech;
    }

    public int getTrackingId() {
        return trackingId;
    }

    public TextView getSpeech() {
        return speech;
    }

    public void setCurrentWords(String currentWords) {
        this.currentWords = currentWords;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }
}
