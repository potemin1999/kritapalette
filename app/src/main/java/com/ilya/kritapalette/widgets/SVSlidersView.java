package com.ilya.kritapalette.widgets;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.LinearLayout;
import android.widget.SeekBar;

public class SVSlidersView extends LinearLayout {

    private LinearSlider mSaturationSlider;
    private LinearSlider mValueSlider;
    private float[] colorHSV;
    private int colorRGB;
    private SaturationUpdateListener saturationUpdateListener;
    private ValueUpdateListener valueUpdateListener;

    public SVSlidersView(Context context) {
        super(context);
        init();
    }

    public SVSlidersView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SVSlidersView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SVSlidersView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        setOrientation(VERTICAL);
        colorHSV = new float[3];
        mSaturationSlider = new LinearSlider(getContext());
        mSaturationSlider.setMax(1000);
        mValueSlider = new LinearSlider(getContext());
        mValueSlider.setMax(1000);
        mSaturationSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                onSaturationChanged( ((float)(progress))/seekBar.getMax());
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        mValueSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                onValueChanged(((float)(progress))/seekBar.getMax());
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        addView(mSaturationSlider,LayoutParams.MATCH_PARENT,dpToPx(50));
        addView(mValueSlider,LayoutParams.MATCH_PARENT,dpToPx(50));
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public void setColorRGB(int color){
        Color.colorToHSV(color,colorHSV);
    }

    public int getColorRGB() {
        return Color.HSVToColor(colorHSV);
    }

    public void setHue(float hue){
        colorHSV[0] = hue;
    }

    public void setSaturation(float saturation){
        colorHSV[1] = saturation;
    }

    public void setValue(float value){
        colorHSV[2] = value;
    }

    private void onSaturationChanged(float saturation){
        colorHSV[1] = saturation;
        if (saturationUpdateListener!=null){
            saturationUpdateListener.onSaturationUpdate(Color.HSVToColor(colorHSV),colorHSV[1]);
        }
    }

    private void onValueChanged(float value){
        colorHSV[2] = value;
        if (valueUpdateListener!=null){
            valueUpdateListener.onValueUpdate(Color.HSVToColor(colorHSV),colorHSV[2]);
        }
    }

    public void setSaturationListener(SaturationUpdateListener saturationUpdateListener) {
        this.saturationUpdateListener = saturationUpdateListener;
    }

    public void setValueListener(ValueUpdateListener valueUpdateListener) {
        this.valueUpdateListener = valueUpdateListener;
    }

    public interface SaturationUpdateListener{
        void onSaturationUpdate(int color,float saturation);
    }

    public interface ValueUpdateListener{
        void onValueUpdate(int color,float value);
    }


    private class LinearSlider extends SeekBar{

        private int startColor;
        private int endColor;

        public LinearSlider(Context context) {
            super(context);
            init();
        }

        public LinearSlider(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public LinearSlider(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        public void init(){

        }

        public void setStartColor(int color){

        }

        public void setEndColor(int color){

        }

    }
}
