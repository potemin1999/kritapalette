package com.ilya.kritapalette.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ilya.kritapalette.R;
import com.ilya.kritapalette.net.KritaClient;
import com.ilya.kritapalette.utils.Storage;
import com.ilya.kritapalette.widgets.ColorPickerView;
import com.ilya.kritapalette.widgets.SVSlidersView;

public class MainActivity extends Activity {

    FrameLayout rootWrapFrameLayout;
    LinearLayout rootLinearLayout;
    ColorPickerView colorPickerView;
    SVSlidersView slidersView;
    TextView mDebugView;
    ColorPickerView.ColorPickerAgent colorPickerAgent;
    ImageButton settingsImageButton;
    float[] hsv = new float[3];
    KritaClient kritaClient;
    Storage storage;

    public MainActivity() {
        hsv[1] = 1;
        hsv[2] = 1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storage = new Storage(this);
        kritaClient = new KritaClient();
        rootWrapFrameLayout = new FrameLayout(this);
        rootLinearLayout = new LinearLayout(this);
        rootLinearLayout.setOrientation(LinearLayout.VERTICAL);
        colorPickerView = new ColorPickerView(this);
        colorPickerView.setBackgroundColor(Color.BLACK);
        slidersView = new SVSlidersView(this);
        mDebugView = new TextView(this);
        mDebugView.setBackgroundColor(Color.WHITE);
        mDebugView.setTextColor(Color.BLACK);
        rootLinearLayout.addView(colorPickerView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 1f));
        rootLinearLayout.addView(slidersView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        rootLinearLayout.addView(mDebugView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        settingsImageButton = new ImageButton(this);
        settingsImageButton.setBackground(null);
        settingsImageButton.setImageResource(R.drawable.baseline_settings_white_24);
        settingsImageButton.setOnClickListener((view) -> showSetupDialog());
        rootWrapFrameLayout.addView(rootLinearLayout, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER));
        rootWrapFrameLayout.addView(settingsImageButton, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP | Gravity.RIGHT));
        setContentView(rootWrapFrameLayout);
        colorPickerView.setHSVListener(this::onHSVUpdate);
        colorPickerView.setHueListener(this::onHueUpdate);
        slidersView.setSaturationListener(this::onSaturationUpdate);
        slidersView.setValueListener(this::onValueUpdate);
        colorPickerAgent = colorPickerView.getAgent();
        if (storage.hasIp()){
            setupConnection();
        }else{
            showSetupDialog();
        }
    }

    @Override
    protected void onDestroy() {
        kritaClient.disconnect();
        super.onDestroy();
    }

    public void showSetupDialog(){
        EditText ipText = new EditText(this);
        EditText portText = new EditText(this);
        TextView ipHint = new TextView(this);
        ipHint.setText("IP:");
        TextView portHint = new TextView(this);
        portHint.setText("Port:");
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.addView(ipHint,-1,-2);
        dialogLayout.addView(ipText,-1,-2);
        dialogLayout.addView(portHint,-1,-2);
        dialogLayout.addView(portText,-1,-2);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Setup connection");
        builder.setView(dialogLayout);
        builder.setPositiveButton("OK", (dialog,which) -> {
            try {
                String ip = ipText.getText().toString();
                int port = Integer.parseInt(portText.getText().toString());
                storage.set(ip,port);
                dialog.dismiss();
                setupConnection();
            }catch(Throwable t){
                t.printStackTrace();
                Toast.makeText(this,t.toString(),Toast.LENGTH_SHORT).show();
            }
        });
        builder.create().show();
    }

    public void setupConnection(){
        String ip = storage.getIp();
        int port = storage.getPort();
        kritaClient.connect(ip,port);
    }

    public void onHSVUpdate(float hue, float saturation, float value) {
        hsv[0] = hue;
        hsv[1] = saturation;
        hsv[2] = value;
        onUpdateColor();
        //slidersView.setHue(hue);
    }

    public void onHueUpdate(int color, float hue) {
        hsv[0] = hue;
        onUpdateColor();
        slidersView.setHue(hue);
    }

    public void onSaturationUpdate(int color, float saturation) {
        hsv[1] = saturation;
        onUpdateColor();
        colorPickerAgent.setHSV(hsv[0], hsv[1], hsv[2]);
    }

    public void onValueUpdate(int color, float value) {
        hsv[2] = value;
        onUpdateColor();
        colorPickerAgent.setHSV(hsv[0], hsv[1], hsv[2]);
    }

    public void onUpdateColor() {
        onUpdateColor(Color.HSVToColor(hsv));
    }

    public void onUpdateColor(int rgbaColor) {
        StringBuilder sb = new StringBuilder();
        float[] hsv = new float[3];
        Color.colorToHSV(rgbaColor, hsv);
        sb.append("r: ").append(Color.red(rgbaColor)).append("\n")
                .append("g: ").append(Color.green(rgbaColor)).append("\n")
                .append("b: ").append(Color.blue(rgbaColor)).append("\n")
                .append("h: ").append(hsv[0]).append("\n")
                .append("s: ").append(hsv[1]).append("\n")
                .append("v: ").append(hsv[2]);
        mDebugView.setText(sb.toString());
        mDebugView.setBackgroundColor(rgbaColor);
        byte[] data = new byte[4];
        data[0] = (byte) Color.red(rgbaColor);
        data[1] = (byte) Color.green(rgbaColor);
        data[2] = (byte) Color.blue(rgbaColor);
        data[3] = (byte) 255;
        kritaClient.send(data);
    }

}
