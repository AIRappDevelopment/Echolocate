package com.example.echolocate;
//made with the help of developer.android.com

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "1";
    private SurfaceHolder holder;
    private Camera mCamera;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        //installing a SurfaceHolder.Callback so the user is notified when the surface is created and destroyed
        holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder1){
        //tell the camera where to draw the preview
        try {
            mCamera.setPreviewDisplay(holder1);
            mCamera.startPreview();
        } 
        catch (IOException e) {
          Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder1) {
        
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder1, int format, int w, int h) {
        if (holder.getSurface() == null) {
            return;
        }
        try {
            //stop preview
            mCamera.stopPreview();
        }
        catch (Exception e) {
        }
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        }
        catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
}
