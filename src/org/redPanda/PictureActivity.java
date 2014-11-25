/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 *
 * @author mflohr
 */
public class PictureActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Intent in = getIntent();

        String path = "file:///" + in.getExtras().getString("path");
        Toast.makeText(this, path, Toast.LENGTH_LONG).show();
        int pic_width = in.getExtras().getInt("Pic_Height");
        RelativeLayout rl = (RelativeLayout) getLayoutInflater().inflate(R.layout.picture_activity, null);
        rl.setBackgroundColor(0x80000000);

        WebView wv = (WebView) rl.findViewById(R.id.PictureView);
        wv.getSettings().setLoadWithOverviewMode(true);
        wv.getSettings().setUseWideViewPort(true);
        wv.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
//        wv.setPadding(0, 0, 0, 0);
//        wv.setInitialScale(getScale(pic_width));
        wv.loadUrl(path);

        wv.setBackgroundColor(0x00000000);
        wv.getSettings().setBuiltInZoomControls(false);
        wv.getSettings().setDisplayZoomControls(false);
        setContentView(rl);
        super.onCreate(savedInstanceState);

    }

    private int getScale(int Pic_Width) {
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = display.getWidth();
        Double val = new Double(width) / new Double(Pic_Width);
        val = val * 100d;
        return val.intValue();
    }
}
