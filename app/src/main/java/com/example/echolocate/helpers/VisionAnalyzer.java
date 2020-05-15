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
                        double maxMouthRatio = -1;
                        Rect speakingFace = new Rect();
//                        int setX = cameraActivity.getSpeechX();
//                        int setY = cameraActivity.getSpeechY();
                        for(FirebaseVisionFace face: firebaseVisionFaces){
                            Rect bounds = face.getBoundingBox();
                            Rect mouthBounds = new Rect();

                            int i = face.getTrackingId();

                            FirebaseVisionFaceLandmark bottomOfMouth = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_BOTTOM);
                            FirebaseVisionFaceLandmark leftOfMouth = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_LEFT);
                            FirebaseVisionFaceLandmark rightOfMouth = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_RIGHT);

                            int newLeft = (int)(((double)bounds.left) * 2.25);
                            int newRight = (int)(((double)bounds.right) * 2.25);
                            int newTop = bounds.top * 3;
                            int newBottom = bounds.bottom * 3;
                            int faceHeight = Math.abs(newTop - newBottom);

                            int mouthLeft = (int) (leftOfMouth.getPosition().getX() * 2.25);
                            int mouthRight = (int) (rightOfMouth.getPosition().getX() * 2.25);
                            int mouthTop = (bottomOfMouth.getPosition().getY().intValue() + 10) * 3;
                            int mouthBottom = bottomOfMouth.getPosition().getY().intValue() * 3;

                            int mouthMidY = (int) (leftOfMouth.getPosition().getY() * 3);
                            int mouthBottomY = bottomOfMouth.getPosition().getY().intValue() * 3;
                            int mouthHeight = Math.abs(mouthMidY - mouthBottomY);

                            double mouthToFaceRatio = (double) (((double) mouthHeight)/ ((double)faceHeight));
                            if(mouthToFaceRatio >= maxMouthRatio){
                                maxMouthRatio = mouthToFaceRatio;
//                                setX = Math.abs((mouthLeft - mouthRight)/ 2);
//                                setY = mouthMidY;
                                speakingFace.set(newLeft, newTop, newRight, newBottom);
                            }

//                            mouthBounds.set(mouthLeft, mouthTop, mouthRight, mouthBottom);
//                            bounds.set(newLeft, newTop, newRight, newBottom);
//
//                            RectOverlay faceOverlay = new RectOverlay(graphicOverlay, bounds);
//                            RectOverlay mouthOverlay = new RectOverlay(graphicOverlay, mouthBounds);
//
//                            graphicOverlay.add(mouthOverlay);
//                            graphicOverlay.add(faceOverlay);

                            cameraActivity.getSpeechInput();
                        }
                        isAnalyzing.set(false);
//                        cameraActivity.setSpeechX(setX);
//                        cameraActivity.setSpeechY(setY);
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
