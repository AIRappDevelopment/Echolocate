package com.example.echolocate.helpers;

import android.content.res.Resources;
import android.graphics.Rect;
import android.media.Image;
import android.util.Log;

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

    /**
     * Constructor for Vision Analyzer
     * @param detector
     * @param graphicOverlay
     * @param cameraActivity
     */
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
        //if analyzing is in process, then don't run
        if(isAnalyzing.get()){
            return;
        }
        isAnalyzing.set(true);
        if (imageProxy == null || imageProxy.getImage() == null) {
            return;
        }

        //get image from camerax
        Image mediaImage = imageProxy.getImage();

        //get Screen dimensions
        double screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        double screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        double imageHeight = mediaImage.getHeight();
        double imageWidth = mediaImage.getWidth();
        int rotation = degreesToFirebaseRotation(degrees);

        //Converts into Firebase analyzable image
        FirebaseVisionImage image = FirebaseVisionImage.fromMediaImage(mediaImage, rotation);

        //Detects in the image
        detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                        //clears graphic overlay
                        graphicOverlay.clear();

                        double maxMouthRatio = -1;
                        Rect speakingFace = new Rect();

                        //adjusts for rotation issue, ratio represents scale factor from camera image to screen image
                        double heightRatio;
                        double widthRatio;
                        if(imageWidth < imageHeight){
                            heightRatio = screenHeight / imageHeight;
                            widthRatio = screenWidth / imageWidth;
                        }else {
                            heightRatio = screenHeight / imageWidth;
                            widthRatio = screenWidth / imageHeight;
                        }

                        Log.v("screen dimensions", screenHeight + " " +  imageWidth);
                        Log.v("screen dimensions", screenWidth + " " +  imageHeight);

                        //Loops through each face found
                        for(FirebaseVisionFace face: firebaseVisionFaces){
                            Rect bounds = face.getBoundingBox();
                            Rect mouthBounds = new Rect();

                            //landmarks used in the code
                            FirebaseVisionFaceLandmark bottomOfMouth = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_BOTTOM);
                            FirebaseVisionFaceLandmark leftOfMouth = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_LEFT);
                            FirebaseVisionFaceLandmark rightOfMouth = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_RIGHT);

                            //Finds the bounding box around around the face and adjusts them from the camera image and scales them onto the Preview Image
                            int newLeft = (int)(((double)bounds.left) * widthRatio);
                            int newRight = (int)(((double)bounds.right) * widthRatio);
                            int newTop = (int) (bounds.top * heightRatio);
                            int newBottom = (int) (bounds.bottom * heightRatio);
                            int faceHeight = Math.abs(newTop - newBottom);

                            //mouth bounding box (unused atm)
                            int mouthLeft = (int) (leftOfMouth.getPosition().getX() * widthRatio);
                            int mouthRight = (int) (rightOfMouth.getPosition().getX() * widthRatio);
                            int mouthTop = (int) ((bottomOfMouth.getPosition().getY().intValue() + 10) * heightRatio);
                            int mouthBottom = (int) (bottomOfMouth.getPosition().getY().intValue() * heightRatio);

                            //Calculates the Y values used for mouth height
                            int mouthMidY = (int) (leftOfMouth.getPosition().getY() * heightRatio);
                            int mouthBottomY = (int) (bottomOfMouth.getPosition().getY().intValue() * heightRatio);
                            int mouthHeight = Math.abs(mouthMidY - mouthBottomY) * 100;

                            //Calculates the mouth to face ratio of each user, this ensures that being closer and appearing bigger does not affect "mouth size"
                            //updates the largest mouth
                            double mouthToFaceRatio = (double) (((double) mouthHeight)/ ((double)faceHeight));
                            Log.v("length", String.valueOf(mouthToFaceRatio));
                            if(mouthToFaceRatio >= maxMouthRatio){
                                maxMouthRatio = mouthToFaceRatio;
                                speakingFace.set(newLeft, newTop, newRight, newBottom);
                            }

                            //Tells the speech detection to begin, once a face is found
                            cameraActivity.getSpeechInput();
                        }

                        //sets the state, so analyzation can start again.
                        isAnalyzing.set(false);

                        //draws the face overlay on who is talking
                        RectOverlay faceOverlay = new RectOverlay(graphicOverlay, speakingFace);
                        graphicOverlay.add(faceOverlay);
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
