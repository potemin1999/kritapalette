package com.ilya.kritapalette.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.ilya.kritapalette.utils.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ColorPickerView extends ViewGroup {

    private int[] mColors;
    private Paint mCirclePaint;
    private Paint mTrackerBorderPaint;
    private Paint mTrackerSolidPaint;
    private RectF mCircleRect;
    private int mCurrentCircleColor;
    private float mTrackerAngle;
    private HueUpdateListener mHueListener;
    private HSVUpdateListener mHSVListener;
    private float[] colorHSV;
    private float currentHue;
    private float[] triangleVertices;
    private int[] triangleColors;
    private OverlayCircleView circleView;
    private GLSurfaceView triangleSurfaceView;
    private GLTriangleViewRenderer triangleRenderer;
    private boolean mTrackingCenter;
    private boolean mTrackingCircle;

    public ColorPickerView(Context context) {
        super(context);
        init();
    }

    public ColorPickerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ColorPickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ColorPickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }


    public void init() {
        colorHSV = new float[3];
        triangleVertices = new float[6];
        triangleColors = new int[6];
        mColors = new int[]{
                0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF
        };
        Shader s = new SweepGradient(0, 0, mColors, null);

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setShader(s);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeWidth(dpToPx(28));

        mTrackerBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTrackerBorderPaint.setStyle(Paint.Style.STROKE);
        mTrackerBorderPaint.setStrokeWidth(dpToPx(6));

        mTrackerSolidPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTrackerSolidPaint.setStyle(Paint.Style.FILL);

        mCircleRect = new RectF(0, 0, 0, 0);
        updateTriangle();
        triangleRenderer = new GLTriangleViewRenderer(this);
        triangleSurfaceView = new GLSurfaceView(getContext());
        triangleSurfaceView.setEGLContextClientVersion(2);
        triangleSurfaceView.setRenderer(triangleRenderer);
        addView(triangleSurfaceView);
        circleView = new OverlayCircleView(getContext());
        addView(circleView);
        setBackgroundColor(Color.DKGRAY);
    }

    public void updateTriangle() {
        float sqrt34 = (float) (Math.sqrt(3) / 2.0);
        triangleVertices[0] = -sqrt34;
        triangleVertices[1] = -0.5f;
        triangleVertices[2] = 0;
        triangleVertices[3] = 1;
        triangleVertices[4] = sqrt34;
        triangleVertices[5] = -0.5f;
        triangleColors[0] = Color.WHITE;
        triangleColors[1] = Color.GREEN;
        triangleColors[2] = Color.BLACK;
        triangleColors[3] = Color.WHITE;
        triangleColors[4] = Color.GREEN;
        triangleColors[5] = Color.BLACK;

    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public void setHueListener(HueUpdateListener mHueListener) {
        this.mHueListener = mHueListener;
    }

    public void setHSVListener(HSVUpdateListener mHSVListener) {
        this.mHSVListener = mHSVListener;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int min = Math.min(r - l, b - t);
        CENTER_X = Math.round((r - l) * 0.5f);
        CENTER_Y = Math.round((b - t) * 0.5f);
        INNER_RADIUS = min * 0.5f - mCirclePaint.getStrokeWidth();
        RADIUS = min * 0.5f - mCirclePaint.getStrokeWidth() * 0.5f;
        int glL = (int) (l + CENTER_X - INNER_RADIUS);
        int glT = (int) (t + CENTER_Y - INNER_RADIUS);
        int glR = (int) (l + CENTER_X + INNER_RADIUS);
        int glB = (int) (t + CENTER_Y + INNER_RADIUS);
        //triangleSurfaceView.requestRender();
        triangleSurfaceView.layout(glL, glT, glR, glB);
        circleView.layout(l, t, r, b);
    }

    public class OverlayCircleView extends View {

        public OverlayCircleView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.save();
            float r = RADIUS;
            canvas.translate(CENTER_X, CENTER_Y);
            mCircleRect.set(-r, -r, r, r);
            canvas.drawOval(mCircleRect, mCirclePaint);
        /*updateTriangle();
        canvas.rotate(colorHSV[0]);
        canvas.drawVertices(
                Canvas.VertexMode.TRIANGLES,6,
                triangleVertices,0,
                null,0,
                triangleColors,0,
                null,0,
                0,mTrianglePaint);
        canvas.rotate(-1*colorHSV[0]);*/
            canvas.translate((float) Math.cos(mTrackerAngle) * RADIUS,
                    (float) Math.sin(mTrackerAngle) * RADIUS);
            canvas.drawCircle(0, 0,
                    mCirclePaint.getStrokeWidth() * 0.5f, mTrackerBorderPaint);
            canvas.drawCircle(0, 0,
                    mCirclePaint.getStrokeWidth() * 0.5f, mTrackerSolidPaint);
            canvas.restore();
            canvas.rotate(colorHSV[0]);
            //canvas.drawCircle(0, 0, CENTER_RADIUS, mCenterPaint);
        }
    }

    private int CENTER_X = dpToPx(132);
    private int CENTER_Y = dpToPx(132);
    //rad
    private float INNER_RADIUS = 0;
    private float RADIUS = 0;
    private int CENTER_RADIUS = dpToPx(40);
    private static final float PI = 3.1415926f;

    private int ave(int s, int d, float p) {
        return s + java.lang.Math.round(p * (d - s));
    }

    private int interpColor(int colors[], float unit) {
        if (unit <= 0) {
            return colors[0];
        }
        if (unit >= 1) {
            return colors[colors.length - 1];
        }

        float p = unit * (colors.length - 1);
        int i = (int) p;
        p -= i;

        // now p is just the fractional part [0...1) and i is the index
        int c0 = colors[i];
        int c1 = colors[i + 1];
        int a = ave(Color.alpha(c0), Color.alpha(c1), p);
        int r = ave(Color.red(c0), Color.red(c1), p);
        int g = ave(Color.green(c0), Color.green(c1), p);
        int b = ave(Color.blue(c0), Color.blue(c1), p);

        return Color.argb(a, r, g, b);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX() - CENTER_X;
        float y = event.getY() - CENTER_Y;
        boolean inCenter = java.lang.Math.hypot(x, y) <= INNER_RADIUS;
        boolean isOutside = java.lang.Math.hypot(x, y) >= RADIUS + mCirclePaint.getStrokeWidth() * 0.5f;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTrackingCenter = inCenter;
                if (inCenter) {
                    invalidate();
                    break;
                }
                if (isOutside) {
                    mTrackingCircle = false;
                    break;
                }
                mTrackingCircle = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mTrackingCenter) {
                    updateHSVByCenter(event);
                    if (mHSVListener!=null){
                        mHSVListener.setColor(colorHSV[0],colorHSV[1],colorHSV[2]);
                    }
                } else if (mTrackingCircle) {
                    float angle = (float) java.lang.Math.atan2(y, x);
                    // need to turn angle [-PI ... PI] into unit [0....1]
                    float hue = setHueAngle(angle);
                    triangleRenderer.updateData();
                    circleView.invalidate();
                    colorHSV[0] = hue;
                    currentHue = hue;
                    if (mHueListener != null) {
                        mHueListener.setColor(Color.HSVToColor(colorHSV), colorHSV[0]);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mTrackingCenter) {
                    mTrackingCenter = false;

                }
                mTrackingCircle = false;
                break;
        }
        return true;
    }

    public void updateHSVByCenter(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        float cx = CENTER_X;
        float cy = CENTER_Y;
        float cxx = x - cx;
        float cyy = cy - y;
        float hue = colorHSV[0];
        float rad = (hue-90) * (PI / 180f);
        float rx = (float) (cxx * Math.cos(rad) - cyy * Math.sin(rad));
        float ry = (float) (cxx * Math.sin(rad) + cyy * Math.cos(rad));
        float sat = rad + (120) * (PI / 180f);
        //float satx = (float) (cxx * Math.cos(sat) - cyy * Math.sin(sat));
        float saty = (float) (cxx * Math.sin(sat) + cyy * Math.cos(sat));
        float saturation = (saty-INNER_RADIUS)/(-1.5f*INNER_RADIUS);
        float val = rad + (240) * (PI / 180f);
        //float valx = (float) (cxx * Math.cos(val) - cyy * Math.sin(val));
        float valy = (float) (cxx * Math.sin(val) + cyy * Math.cos(val));
        float value = (valy-INNER_RADIUS)/(-1.5f*INNER_RADIUS);
        colorHSV[1] = saturation;
        colorHSV[2] = value;
    }

    private float setHueAngle(float angle) {
        float unit = angle / (2 * PI);
        if (unit < 0) {
            unit += 1;
        }
        mTrackerAngle = angle;
        mCurrentCircleColor = interpColor(mColors, unit);
        mTrackerSolidPaint.setColor(mCurrentCircleColor);
        float trackerUnit = (angle + PI) / (2 * PI);
        if (trackerUnit < 0) {
            trackerUnit += 1;
        }
        mTrackerBorderPaint.setColor(interpColor(mColors, trackerUnit));
        return (unit * 360 + 180) % 360;
    }

    /**
     * @param hue        [0;360]
     * @param saturation [0;1]
     * @param value      [0;1]
     */
    private void setHSV(float hue, float saturation, float value) {
        colorHSV[0] = hue;
        colorHSV[1] = saturation;
        colorHSV[2] = value;
        setHueAngle(hue * (PI / 180) - PI);
        triangleRenderer.updateData();
        circleView.invalidate();
    }

    /**
     * @param alpha
     */
    private void setAlpha(int alpha) {

    }

    public interface HueUpdateListener {
        void setColor(int color, float hue);
    }

    public interface HSVUpdateListener {
        void setColor(float hue, float saturation, float value);
    }

    public ColorPickerAgent getAgent() {
        return new ColorPickerAgent(this);
    }

    public static class ColorPickerAgent {

        final ColorPickerView colorPickerView;

        private ColorPickerAgent(ColorPickerView view) {
            colorPickerView = view;
        }

        public ColorPickerView getColorPickerView() {
            return colorPickerView;
        }

        public void setHSV(float hue, float saturation, float value) {
            colorPickerView.setHSV(hue, saturation, value);
        }

    }

    private class GLTriangleViewRenderer implements GLSurfaceView.Renderer {

        private final ColorPickerView colorPickerView;
        private int vertexShaderId;
        private int fragmentShaderId;
        private int shaderProgramId;
        private FloatBuffer triangleData;
        private float[] triangleDataArray;
        private float[] rotationMatrix;
        private int aPositionLocation;
        private int aColorLocation;
        private int uRotationAngle;
        private int uRotationMatrix;

        public GLTriangleViewRenderer(ColorPickerView view) {
            colorPickerView = view;
            triangleDataArray = new float[3 * 5];
            rotationMatrix = new float[16];
            rotationMatrix[0] = 1;
            rotationMatrix[5] = 1;
            rotationMatrix[10] = 1;
            rotationMatrix[15] = 1;
        }

        public void updateData() {
            triangleData.position(0);
            //copy positions
            triangleDataArray[0] = triangleVertices[0];
            triangleDataArray[1] = triangleVertices[1];
            triangleDataArray[5] = triangleVertices[2];
            triangleDataArray[6] = triangleVertices[3];
            triangleDataArray[10] = triangleVertices[4];
            triangleDataArray[11] = triangleVertices[5];
            //copy colors
            float[] hsv = colorHSV;
            hsv[0] = currentHue;
            hsv[1] = 1;
            hsv[2] = 1;
            int color = Color.HSVToColor(hsv);
            triangleDataArray[2] = 0;
            triangleDataArray[3] = 0;
            triangleDataArray[4] = 0;
            triangleDataArray[7] = Color.red(color) * (1f / 255f);
            triangleDataArray[8] = Color.green(color) * (1f / 255f);
            triangleDataArray[9] = Color.blue(color) * (1f / 255f);
            triangleDataArray[12] = 1;
            triangleDataArray[13] = 1;
            triangleDataArray[14] = 1;
            //triangleData.put(new float[]{-0.5f, -0.2f, 0.0f, 0.2f, 0.5f, -0.2f});
            triangleData.put(triangleDataArray);
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            vertexShaderId = GLUtils.createShader(
                    GLES20.GL_VERTEX_SHADER, getVertexShader());
            fragmentShaderId = GLUtils.createShader(
                    GLES20.GL_FRAGMENT_SHADER, getFragmentShader());
            shaderProgramId = GLUtils.createProgram(vertexShaderId, fragmentShaderId);
            triangleData = ByteBuffer.allocateDirect(triangleDataArray.length * 4)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            //uRotationAngle = GLES20.glGetUniformLocation(shaderProgramId,"u_RotationAngle");
            uRotationMatrix = GLES20.glGetUniformLocation(shaderProgramId, "u_RotationMatrix");
            aPositionLocation = GLES20.glGetAttribLocation(shaderProgramId, "a_Position");
            triangleData.position(0);
            GLES20.glVertexAttribPointer(aPositionLocation, 2,
                    GLES20.GL_FLOAT, false, 20, triangleData);
            GLES20.glEnableVertexAttribArray(aPositionLocation);

            aColorLocation = GLES20.glGetAttribLocation(shaderProgramId, "a_Color");
            triangleData.position(2);
            GLES20.glVertexAttribPointer(aColorLocation, 3,
                    GLES20.GL_FLOAT, false, 20, triangleData);
            GLES20.glEnableVertexAttribArray(aColorLocation);

            GLES20.glUseProgram(shaderProgramId);
            updateData();
            GLES20.glClearColor(0f, 0f, 0f, 1f);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            int color = Color.HSVToColor(colorHSV);
            Matrix.setIdentityM(rotationMatrix, 0);
            Matrix.rotateM(rotationMatrix, 0,
                    colorPickerView.mTrackerAngle * (-180f / PI) - 90, 0, 0, 1);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glUniformMatrix4fv(uRotationMatrix, 1,
                    false, rotationMatrix, 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
        }

        private String getVertexShader() {
            return "uniform mat4 u_RotationMatrix;\n" +
                    "attribute vec4 a_Position;\n" +
                    "attribute vec4 a_Color;\n" +
                    "varying vec4 v_Color;\n" +
                    " \n" +
                    "void main() {\n" +
                    "  gl_Position = u_RotationMatrix * a_Position;\n" +
                    "  v_Color = a_Color;\n" +
                    "}";
        }

        private String getFragmentShader() {
            return "precision mediump float;\n" +
                    "varying vec4 v_Color;\n" +
                    "\n" +
                    "void main() {\n" +
                    "    gl_FragColor = v_Color;\n" +
                    "}";
        }

    }

}
