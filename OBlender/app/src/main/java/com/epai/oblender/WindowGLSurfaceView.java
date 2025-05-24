package com.epai.oblender;

import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Region;
import android.util.AttributeSet;
import android.opengl.*;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class WindowGLSurfaceView extends SurfaceView implements SurfaceHolder.Callback{

    public interface WindowGLSurfaceViewListener{
        void updateSurfaceDestroyed(SurfaceHolder holder);
        void updateSurface(SurfaceHolder holder);
        void hideWindow(WindowWindow windowWindow);
    }

    void setListener(WindowGLSurfaceViewListener windowGLSurfaceViewListener){
        mWindowGLSurfaceViewListener=windowGLSurfaceViewListener;
    }

    private WindowGLSurfaceViewListener mWindowGLSurfaceViewListener;

    private static final String TAG = "WindowGLSurfaceView";



    public WindowGLSurfaceView(Context context) {
        super(context);
        init();
    }

    public WindowGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WindowGLSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        getHolder().addCallback(this);
    }

        @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG,"SURFACE CREATED");
        mWindowGLSurfaceViewListener.updateSurface(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        Log.i(TAG,"SURFACE CREATED surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG,"SURFACE CREATED surfaceDestroyed");
        mWindowGLSurfaceViewListener.updateSurfaceDestroyed(holder);
    }
}
