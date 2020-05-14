package com.example.echolocate.helpers;

import android.content.Intent;
import android.graphics.Rect;
import android.media.Image;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.TextView;

import com.example.echolocate.CameraActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

/**
 * Custom analyzer created to find faces
 * Done with the help of firebase.google.com + https://heartbeat.fritz.ai/blink-detection-on-android-using-firebase-ml-kits-face-detection-api-6d09823db535
 */
public class VisionAnalyzer implements ImageAnalysis.Analyzer{
    GraphicOverlay graphicOverlay;
    FirebaseVisionFaceDetector detector;
    AtomicBoolean isAnalyzing = new AtomicBoolean(false);
    CameraActivity cameraActivity;

    public VisionAnalyzer(FirebaseVisionFaceDetector detector, GraphicOverlay graphicOverlay, CameraActivity cameraActivity){
        super();
        this.detector = detector;
        this.graphicOverlay = graphicOverlay;
        this.cameraActivity = cameraActivity;
    }

    /**
     * accounts for phone rotation and adjusts camera image
     * @param degrees
     * @return
     */
    private int degreesToFirebaseRotation(int degrees) {
        switch (degrees) {
            case 0:
                return FirebaseVisionImageMetadata.ROTATION_0;
            case 90:
                return FirebaseVisionImageMetadata.ROTATION_90;
            case 180:
                return FirebaseVisionImageMetadata.ROTATION_180;
            case 270:
                return FirebaseVisionImageMetadata.ROTATION_270;
            default:
                throw new IllegalArgumentException(
                        "Rotation must be 0, 90, 180, or 270.");
        }
    }

    /**
     * Gets the image from CameraX, detects face, and decides what to do, logic layer
     * @param imageProxy
     * @param degrees
     */
    @Override
    public void analyze(ImageProxy imageProxy, int degrees) {
        if(isAnalyzing.get()){
            return;
        }
        isAnalyzing.set(true);
        if (imageProxy == null || imageProxy.getImage() == null) {
            return;
        }
        Image mediaImage = imageProxy.getImage();
        int rotation = degreesToFirebaseRotation(degrees);
        FirebaseVisionImage image = FirebaseVisionImage.fromMediaImage(mediaImage, rotation);
        detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                        graphicOverlay.clear();
                        int maxMouthRatio = 0;
                        for(FirebaseVisionFace face:firebaseVisionFaces){
                            Rect bounds = face.getBoundingBox();
                            int i = face.getTrackingId();
                            FirebaseVisionFaceLandmark bottomOfMouth = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_BOTTOM);
                            FirebaseVisionFaceLandmark leftOfMouth = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_LEFT);
                            FirebaseVisionFaceLandmark rightOfMouth = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_RIGHT);
                            int newLeft = (int)(((double)bounds.left) * 2.25);
                            int newRight = (int)(((double)bounds.right) * 2.25);
                            int newTop = bounds.top * 3;
                            int newBottom = bounds.bottom * 3;
                            Rect mouthBounds = new Rect();
                            mouthBounds.set( leftOfMouth.getPosition().getX().intValue(), bottomOfMouth.getPosition().getY().intValue() + 50, rightOfMouth.getPosition().getX().intValue(), bottomOfMouth.getPosition().getY().intValue());
                            bounds.set(newLeft, newTop, newRight, newBottom);
//                            Log.v("coordinates", String.valueOf(bounds.bottom));
//                            Log.v("coordinates", String.valueOf(bounds.top));
                            RectOverlay rectOverlay = new RectOverlay(graphicOverlay, bounds);
                            graphicOverlay.add(new RectOverlay(graphicOverlay, mouthBounds));
                            graphicOverlay.add(rectOverlay);
                            cameraActivity.getSpeechInput();
                        }
                        isAnalyzing.set(false);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        isAnalyzing.set(false);
                    }
                });

    }
}
