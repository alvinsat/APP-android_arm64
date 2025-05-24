package com.epai.oblender;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

public class WindowWindow extends LinearLayout {
    public WindowWindow(Context context, WindowGLSurfaceView.WindowGLSurfaceViewListener windowGLSurfaceViewListener) {
        super(context);
        mWindowGLSurfaceViewListener=windowGLSurfaceViewListener;
        setupLayout(context);
    }

    public WindowWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupLayout(context);
    }

    public WindowWindow(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setupLayout(context);
    }

    private WindowGLSurfaceView.WindowGLSurfaceViewListener mWindowGLSurfaceViewListener;
    void setListener(WindowGLSurfaceView.WindowGLSurfaceViewListener windowGLSurfaceViewListener){
        mWindowGLSurfaceViewListener=windowGLSurfaceViewListener;
    }

    void setupLayout(Context context){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view=inflater.inflate(R.layout.fragment_window,this);

        WindowGLSurfaceView mGLSurfaceViewWindow=view.findViewById(R.id.glsurfaceviewWindow);

        mGLSurfaceViewWindow.setListener(mWindowGLSurfaceViewListener);

        ImageView imageView=view.findViewById(R.id.imageView);
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mWindowGLSurfaceViewListener.hideWindow(WindowWindow.this);
            }
        });
    }

}
