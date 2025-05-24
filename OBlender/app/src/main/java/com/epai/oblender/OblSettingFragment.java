package com.epai.oblender;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class OblSettingFragment extends View {
    private final String TAG = "设置页面";

    private int mColorBG = Color.valueOf(0.64f, 0.64f, 0.64f).toArgb();
    private int mColorControlBtnTxt = Color.valueOf(1.0f, 1.0f, 1.0f).toArgb();
    private int mColorControlBtnBG = Color.valueOf(0.6f, 0.6f, 0.6f).toArgb();
    private int mColorSelectedPageBG = Color.valueOf(0.4f, 0.0f, 0.0f).toArgb();
    private int mColorBtnTxt = Color.valueOf(1.0f, 1.0f, 1.0f).toArgb();
    private int mColorBtnBG = Color.valueOf(0.45f, 0.45f, 0.45f).toArgb();
    private int mColorSelectedBtnBG = Color.valueOf(0.55f, 0.0f, 0.0f).toArgb();
    private int mColorClickEffect = Color.valueOf(0.0f, 0.55f, 0.0f).toArgb();
    private int mIntOffset = 5;
    private Paint mPaint;
    private int mIntWidth = 0;
    private int mIntHeight = 0;
    private int mIntCurrentPage = 0;//    当前页
    private int mIntTotalRow = 5;
    private int mIntTotalColumn = 6;
    private ArrayList<OBLBtn> mOBLBtns = null;
    OBLControlBtn mOBLControlBtnPage1 = null;
    OBLControlBtn mOBLControlBtnPage2 = null;
    OBLControlBtn mOBLControlBtnPage3 = null;
    OBLControlBtn mOBLControlBtnMove=null;
    OBLControlBtn mOBLControlBtnClose = null;
    private OBLBtn mOBLBtnCtrl = null;
    private OBLBtn mOBLBtnAlt = null;
    private OBLBtn mOBLBtnShift = null;
    private OBLBtn mOBLBtnMMB = null;
    private OBLBtn mOBLBtnRMB = null;

    private enum OBLControlBtnID {
        OBL_CONTROL_BTN_ID_PAGE1,
        OBL_CONTROL_BTN_ID_PAGE2,
        OBL_CONTROL_BTN_ID_PAGE3,
        OBL_CONTROL_BTN_ID_MOVE,
        OBL_CONTROL_BTN_ID_CLOSE
    }

    private class OBLBtnBase {
        private boolean mBooleanEffect = false;

        public boolean isBooleanEffect() {
            return mBooleanEffect;
        }

        public void addClickEffect() {
            mBooleanEffect = true;
        }

        public void clearClickEffect() {
            mBooleanEffect = false;
        }
    }

    private class OBLControlBtn extends OBLBtnBase {
        private String mStringText = "";
        private Rect mRect = null;
        private Rect mRectInner = null;
        private OBLControlBtnID mOBLControlBtnID;
        private boolean mBooleanSelected = false;

        OBLControlBtn(OBLControlBtnID oblControlBtnID, String stringText) {
            mStringText = stringText;
            mOBLControlBtnID = oblControlBtnID;
        }

        public void draw(Canvas canvas, Paint paint) {
            if (mRectInner != null) {
                if (isBooleanEffect()) {
                    mPaint.setColor(mColorClickEffect);
                } else {
                    if (mBooleanSelected) {
                        mPaint.setColor(mColorSelectedPageBG);
                    } else {
                        mPaint.setColor(mColorControlBtnBG);
                    }
                }
                canvas.drawRect(mRectInner, paint);
                mPaint.setColor(mColorControlBtnTxt);
                Paint.FontMetrics fontMetrics = paint.getFontMetrics();
                float distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
                canvas.drawText(mStringText, mRectInner.centerX(), mRectInner.centerY() + distance, paint);
            }
        }

        public void setGeometry(int left, int top, int right, int bottom) {
            if (mRect == null) {
                mRect = new Rect();
                mRect.set(left, top, right, bottom);
                mRectInner = new Rect();
                mRectInner.set(left + mIntOffset, top + mIntOffset, right - mIntOffset, bottom - mIntOffset);
            }
        }

        public boolean hitTest(float x, float y) {
            return mRectInner.contains((int) x, (int) y);
        }

        public void setSelected(boolean booleanSelected) {
            mBooleanSelected = booleanSelected;
        }

        public boolean isBooleanSelected() {
            return mBooleanSelected;
        }
    }

    private class OBLBtn extends OBLBtnBase {
        private boolean mBooleanIsSelected = false;
        private int mIntSelected = 0;
        private OBLButtonID mOBLButtonID;
        private String mStringText = "";
        private int mIntPageIndex = 0;
        private int mIntRowIndex = 0;
        private int mIntColIndex = 0;
        private Rect mRect = null;
        private Rect mRectInner = null;

        public OBLBtn(OBLButtonID oblButtonID, String stringText, int intSelected) {
            mStringText = stringText;
            mOBLButtonID = oblButtonID;
            mIntSelected = intSelected;
        }

        public void setPosition(int intRowIndex, int intColIndex, int intPageIndex) {
            mIntRowIndex = intRowIndex;
            mIntColIndex = intColIndex;
            mIntPageIndex = intPageIndex;
        }

        public int getIntPageIndex() {
            return mIntPageIndex;
        }

        public int getIntSelected() {
            return mIntSelected;
        }

        public void setSelected(boolean booleanIsSelected) {
            mBooleanIsSelected = booleanIsSelected;
        }

        public boolean isSelected() {
            return mBooleanIsSelected;
        }

        public OBLButtonID getOBLButtonID() {
            return mOBLButtonID;
        }

        public void draw(Canvas canvas, Paint paint) {
            if (mRectInner != null) {
                if (isBooleanEffect()) {
                    mPaint.setColor(mColorClickEffect);
                } else {
                    if (mBooleanIsSelected) {
                        mPaint.setColor(mColorSelectedBtnBG);
                    } else {
                        mPaint.setColor(mColorBtnBG);
                    }
                }
                canvas.drawRect(mRectInner, paint);
                mPaint.setColor(mColorBtnTxt);
                Paint.FontMetrics fontMetrics = paint.getFontMetrics();
                float distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
                canvas.drawText(mStringText, mRectInner.centerX(), mRectInner.centerY() + distance, paint);
            }
        }

        public void setGeometry(Rect rect) {
            mRect = rect;
        }

        public void setGeometry(int totalWidth, int totalHeight, int totalRow, int totalColumn) {
            if (mIntPageIndex < 0) {
                return;
            }
            if (mRect == null) {
                mRect = new Rect();
                int x1 = totalWidth / totalColumn * mIntColIndex;
                int x2 = totalWidth / totalColumn * (mIntColIndex + 1);
                int y1 = totalHeight / totalRow * mIntRowIndex;
                int y2 = totalHeight / totalRow * (mIntRowIndex + 1);
                mRect.set(x1, y1, x2, y2);

                mRectInner = new Rect();
                mRectInner.set(x1 + mIntOffset, y1 + mIntOffset, x2 - mIntOffset, y2 - mIntOffset);
            }
        }

        public boolean hitTest(float x, float y) {
            return mRectInner.contains((int) x, (int) y);
        }
    }

    public OblSettingFragment(Context context) {
        super(context);
        initial();
    }

    public OblSettingFragment(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initial();
    }

    public OblSettingFragment(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initial();
    }

    public OblSettingFragment(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initial();
    }

    public void initial() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setStrokeWidth(1);
        mPaint.setTextSize(mPaint.getTextSize() * 1.5f);

        mOBLBtns = new ArrayList<>();

        mOBLBtnShift = new OBLBtn(OBLButtonID.OBLButtonID_Shift, "Shift", 1);
        mOBLBtns.add(mOBLBtnShift);
        mOBLBtnCtrl = new OBLBtn(OBLButtonID.OBLButtonID_Ctrl, "Ctrl", 1);
        mOBLBtns.add(mOBLBtnCtrl);
        mOBLBtnAlt = new OBLBtn(OBLButtonID.OBLButtonID_Alt, "Alt", 1);
        mOBLBtns.add(mOBLBtnAlt);
        mOBLBtnMMB = new OBLBtn(OBLButtonID.OBLButtonID_Scroll, "MMB", 1);
        mOBLBtns.add(mOBLBtnMMB);
        mOBLBtnRMB = new OBLBtn(OBLButtonID.OBLButtonID_RightBtn, "RMB", 1);
        mOBLBtns.add(mOBLBtnRMB);

        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_ScrollUp, "Scr.⬆", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_ScrollDown, "Scr.⬇", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_Esc, "Esc", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_F2, "F2", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_F3, "F3", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_F4, "F4", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_F12, "F12", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_Home, "Home", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_Enter, "Enter", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_Tilde, "`", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_1, "1", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_2, "2", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_3, "3", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_4, "4", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_5, "5", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_Q, "Q", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_W, "W", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_T, "T", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_I, "I", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_O, "O", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_A, "A", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_X, "X", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_Y, "Y", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_Z, "Z", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_C, "C", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_N, "N", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_M, "M", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_COMMA, ",", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_PEROID, ".", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_Space, "Spac.", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_PgUp, "PgUp.", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_PgDn, "PgDn.", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_UpArrow, "↑", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_DownArrow, "↓", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_LeftArrow, "←", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_RightArrow, "→", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_Tab, "Tab", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_Delete, "Del.", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_R, "R", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_S, "S", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_G, "G", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_H, "H", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_LeftSlash, "/", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_D, "D", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_J, "J", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_V, "V", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_E, "E", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_B, "B", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_F, "F", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_Num_0, "N.0", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_Num_1, "N.1", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_Num_2, "N.2", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_Num_3, "N.3", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_Num_4, "N.4", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_Num_5, "N.5", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_Num_Plus, "N.+", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_Num_Minus, "N.-", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_Num_Asterisk, "N.*", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_Num_Slash, "N./", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_Num_Period, "N..", 0));
        mOBLBtns.add(new OBLBtn(OBLButtonID.OBLButtonID_Num_Enter, "N.Ent", 0));

        int pageItemNum = mIntTotalRow * mIntTotalColumn;
        for (int i = 0; i < mOBLBtns.size(); i++) {
            int index = i % pageItemNum;
            int pageIndex=i/pageItemNum;
            mOBLBtns.get(i).setPosition(index / mIntTotalColumn, index % mIntTotalColumn,pageIndex);
        }

        mOBLControlBtnPage1 = new OBLControlBtn(OBLControlBtnID.OBL_CONTROL_BTN_ID_PAGE1, "Page 1");
        mOBLControlBtnPage2 = new OBLControlBtn(OBLControlBtnID.OBL_CONTROL_BTN_ID_PAGE2, "Page 2");
        mOBLControlBtnPage3 = new OBLControlBtn(OBLControlBtnID.OBL_CONTROL_BTN_ID_PAGE3, "Page 3");
        mOBLControlBtnMove=new OBLControlBtn(OBLControlBtnID.OBL_CONTROL_BTN_ID_MOVE,"Move");
        mOBLControlBtnClose = new OBLControlBtn(OBLControlBtnID.OBL_CONTROL_BTN_ID_CLOSE, "Close");

        mOBLControlBtnPage1.setSelected(true);
        mOBLControlBtnPage2.setSelected(false);
        mOBLControlBtnPage3.setSelected(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.i(TAG, "绘制 1");
        super.onDraw(canvas);
        Log.i(TAG, "绘制 2");
        //  绘制键盘按钮
        int getWidthValue = getWidth();
        Log.i(TAG, "绘制 3");
        if ((mIntWidth <= 0) && (getWidthValue > 0)) {
            Log.i(TAG, "绘制 4");
            mIntWidth = getWidthValue;
            Log.i(TAG, "绘制 5");
            mIntHeight = getHeight();
            Log.i(TAG, "绘制 6");
            initialOBLBtnGeometry();
            Log.i(TAG, "绘制 7");
        }
        if (mIntWidth > 0) {
            mPaint.setColor(mColorBG);
            canvas.drawRect(0, 0, mIntWidth, mIntHeight, mPaint);
            Log.i(TAG, "绘制 9");
            renderOBLBtns(canvas, mPaint);
            Log.i(TAG, "绘制 10");
        }
        Log.i(TAG, "绘制 12");
    }

    private void initialOBLBtnGeometry() {
        int topPos = mIntHeight * mIntTotalRow / (mIntTotalRow + 1);
        int widthInternal = mIntWidth / mIntTotalColumn;
        mOBLControlBtnPage1.setGeometry(0 * widthInternal, topPos, 1 * widthInternal, mIntHeight);
        mOBLControlBtnPage2.setGeometry(1 * widthInternal, topPos, 2 * widthInternal, mIntHeight);
        mOBLControlBtnPage3.setGeometry(2 * widthInternal, topPos, 3 * widthInternal, mIntHeight);
        mOBLControlBtnMove.setGeometry((mIntTotalColumn - 2) * widthInternal, topPos, (mIntTotalColumn - 1) * widthInternal, mIntHeight);
        mOBLControlBtnClose.setGeometry((mIntTotalColumn - 1) * widthInternal, topPos, mIntTotalColumn * widthInternal, mIntHeight);
        for (OBLBtn btn : mOBLBtns) {
            btn.setGeometry(mIntWidth, mIntHeight * mIntTotalRow / (mIntTotalRow + 1), mIntTotalRow, mIntTotalColumn);
        }
    }

    private void renderOBLBtns(Canvas canvas, Paint paint) {
        for (OBLBtn btn : mOBLBtns) {
            if (btn.getIntPageIndex() == mIntCurrentPage) {
                btn.draw(canvas, paint);
            }
        }
        mOBLControlBtnPage1.draw(canvas, paint);
        mOBLControlBtnPage2.draw(canvas, paint);
        mOBLControlBtnPage3.draw(canvas, paint);
        mOBLControlBtnMove.draw(canvas, paint);
        mOBLControlBtnClose.draw(canvas, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            float posx = event.getX();
            float posy = event.getY();
            boolean hasHit = false;
            for (OBLBtn oblBtn : mOBLBtns) {
                if (oblBtn.hitTest(posx, posy) && (oblBtn.getIntPageIndex() == mIntCurrentPage)) {
                    clearClickEffect();
                    oblBtn.addClickEffect();
                    clickOBLBtn(oblBtn);
                    hasHit = true;
                    break;
                }
            }
            if (!hasHit) {
                if (mOBLControlBtnClose.hitTest(posx, posy)) {
                    clearClickEffect();
                    mOBLControlBtnClose.addClickEffect();
                    mOBLSettingFragmentListener.closeFragment();
                    mOBLBtnShift.setSelected(false);
                    mOBLBtnCtrl.setSelected(false);
                    mOBLBtnAlt.setSelected(false);
                    mOBLBtnMMB.setSelected(false);
                    mOBLBtnRMB.setSelected(false);
                    mOBLControlBtnMove.setSelected(false);
                }else if(mOBLControlBtnMove.hitTest(posx, posy)){
                    clearClickEffect();
                    mOBLControlBtnMove.addClickEffect();
                    mOBLControlBtnMove.setSelected(!mOBLControlBtnMove.isBooleanSelected());
                    if (!mOBLControlBtnMove.isBooleanSelected()){
                        int []values=new int[1];
                        values[0]=10000;
                        mOBLSettingFragmentListener.enterKey(values);
                    }
                }else if (mOBLControlBtnPage1.hitTest(posx, posy)) {
                    if (mIntCurrentPage != 0) {
                        clearClickEffect();
                        mOBLControlBtnPage1.addClickEffect();
                        mIntCurrentPage = 0;
                        mOBLControlBtnPage1.setSelected(true);
                        mOBLControlBtnPage2.setSelected(false);
                        mOBLControlBtnPage3.setSelected(false);
                        invalidate();
                    }
                } else if (mOBLControlBtnPage2.hitTest(posx, posy)) {
                    if (mIntCurrentPage != 1) {
                        clearClickEffect();
                        mOBLControlBtnPage2.addClickEffect();
                        mIntCurrentPage = 1;
                        mOBLControlBtnPage1.setSelected(false);
                        mOBLControlBtnPage2.setSelected(true);
                        mOBLControlBtnPage3.setSelected(false);
                        invalidate();
                    }
                } else if (mOBLControlBtnPage3.hitTest(posx, posy)) {
                    if (mIntCurrentPage != 2) {
                        clearClickEffect();
                        mOBLControlBtnPage3.addClickEffect();
                        mIntCurrentPage = 2;
                        mOBLControlBtnPage1.setSelected(false);
                        mOBLControlBtnPage2.setSelected(false);
                        mOBLControlBtnPage3.setSelected(true);
                        invalidate();
                    }
                }
            }
        }
        if (action == MotionEvent.ACTION_UP) {
            performClick();
            clearClickEffect();
        }
        return super.onTouchEvent(event);
    }

    private void clearClickEffect() {
        for (OBLBtn oblBtn : mOBLBtns) {
            oblBtn.clearClickEffect();
        }
        mOBLControlBtnPage1.clearClickEffect();
        mOBLControlBtnPage2.clearClickEffect();
        mOBLControlBtnPage3.clearClickEffect();
        mOBLControlBtnClose.clearClickEffect();
        mOBLControlBtnMove.clearClickEffect();
        invalidate();
    }

    void clickOBLBtn(OBLBtn oblBtn) {
        if (oblBtn.getIntSelected() != 0) {
            oblBtn.setSelected(!oblBtn.isSelected());
        }
        //  没有状态按钮被选中，则点击的按钮立即发送
        int[] keys = new int[1];
        keys[0] = oblBtn.getOBLButtonID().ordinal();
        if (oblBtn.getIntSelected()!=0){
            if (oblBtn.isSelected()){
                mOBLSettingFragmentListener.enterKeyOn(keys);
            }else{
                mOBLSettingFragmentListener.enterKeyOff(keys);
            }
        }else{
            mOBLSettingFragmentListener.enterKey(keys);
        }
        invalidate();
    }

    void setOBLSettingFragmentListener(OBLSettingFragmentListener oblSettingFragmentListener) {
        mOBLSettingFragmentListener = oblSettingFragmentListener;
    }

    void SetValue(int type,int value){
        if (type==0){
            mOBLBtnShift.setSelected(value==1);
        }
        if (type==1){
            mOBLBtnCtrl.setSelected(value==1);
        }
        if (type==2){
            mOBLBtnAlt.setSelected(value==1);
        }
        if (type==3){
            mOBLBtnMMB.setSelected(value==1);
        }
        if (type==4){
            mOBLBtnRMB.setSelected(value==1);
        }
        invalidate();
    }

    int GetAsyncKeyState(int type) {
        if (type == 0) {
            //  shift
            return mOBLBtnShift.isSelected() ? 1 : 0;
        } else if (type == 1) {
            //  alt
            return mOBLBtnAlt.isSelected() ? 1 : 0;
        } else if (type == 2) {
            // ctrl
            return mOBLBtnCtrl.isSelected() ? 1 : 0;
        } else if (type == 3) {
            //  MMB
            return mOBLBtnMMB.isSelected() ? 1 : 0;
        } else if (type == 4) {
            //  RMB
            return mOBLBtnRMB.isSelected() ? 1 : 0;
        } else if(type==100) {
            return getVisibility()==VISIBLE?1:0;
        }else if(type==101) {
            return mOBLControlBtnMove.isBooleanSelected()?1:0;
        }else{
            return 0;
        }
    }

    private OBLSettingFragmentListener mOBLSettingFragmentListener;

    public interface OBLSettingFragmentListener {
        public void enterKeyOn(int keys[]);

        public void enterKeyOff(int keys[]);

        public void enterKey(int keys[]);

        public void closeFragment();
    }
}
