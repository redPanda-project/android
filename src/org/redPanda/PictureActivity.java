/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import pl.droidsonroids.gif.GifDrawable;
import uk.co.senab.photoview.PhotoViewAttacher;
import pl.droidsonroids.gif.GifImageView;

/**
 *
 * @author mflohr
 */
public class PictureActivity extends Activity {

    GifImageView mGifImageView;
    PhotoViewAttacher mAttacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setBackgroundDrawable(new ColorDrawable(0x80000000));
        Intent in = getIntent();
//
        String path = in.getExtras().getString("path");
//        String[] asd = path.split("/");
//        path = asd[asd.length - 1];
//        String mainpath = "file:///" + Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/redPanda/";
//
//        int pic_height = in.getExtras().getInt("Pic_Height");
//        int pic_width = in.getExtras().getInt("Pic_Width");
////        RelativeLayout rl = (RelativeLayout) getLayoutInflater().inflate(R.layout.picture_activity, null);
////        rl.setBackgroundColor(0x80000000);
//
////        WebView wv = (WebView) rl.findViewById(R.id.PictureView);
//        WebView wv = new WebView(this);
//        wv.getSettings().setLoadWithOverviewMode(true);
//        wv.getSettings().setUseWideViewPort(true);
//        //   wv.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
////        wv.setPadding(0, 0, 0, 0);
////        wv.setInitialScale(getScale(pic_width));
//        wv.setBackgroundColor(0x80000000);
//        // wv.setInitialScale(getScale(pic_width));
//        String data;
//        double scale = getScale(pic_width, pic_height);
//        String temp = scale+"\n"+pic_width+" "+pic_height;
//        Double ph = new Double(pic_width) * scale;
//        pic_width = ph.intValue();
//        ph = new Double(pic_height) * scale;
//        pic_height = ph.intValue();
//        temp +="\n"+pic_width+" "+pic_height;
//        Toast.makeText(this,temp, Toast.LENGTH_LONG).show();
////        data = "<table width=\"100%\" height=\"100%\" align=\"center\" valign=\"center\">\n"
////                + "     <tr><td>\n"
////                + "    \n"
////                + "    <div id=\"wrapper\" style=\"text-align: center\">    \n"
////                + "    <div id=\"yourdiv\" style=\"display: inline-block;\"><img src=\""
////                + path
////                + "\" width=\""
////                + 50
////                + "\" height=\""
////                + 50
////                + "\" alt=\"foo\" /></div>\n"
////                + "</div>\n"
////                + "     \n"
////                + "                      </td></tr>\n"
////                + "</table>";
//
//        data = "<html>\n"
//                + "<body>\n"
//                + "<table width=\"100%\" height=\"100%\">\n"
//                + "<tr><td>\n"
//                + "    \n"
//                + "<div style=\"text-align: center\">    \n"
//                + "    <div style=\"display: inline-block;\"><img src=\""
//                +path
//                + "\" width=\""
//                +pic_width
//                + "\" height=\""
//                +pic_height
//                + "\"> </div>\n"
//                + "</div>\n"
//                + "     \n"
//                + "</td></tr>\n"
//                + "</table>\n"
//                + "</body>\n"
//                + "</html>";
//
//        wv.loadDataWithBaseURL(mainpath, data, "text/html", "utf-8", "");
//        // wv.loadDataWithBaseURL(mainpath, data, "text/html", "utf-8", null);
//
//        wv.getSettings().setBuiltInZoomControls(true);
//        wv.getSettings().setDisplayZoomControls(false);
//        wv.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
//        setContentView(wv);

        super.onCreate(savedInstanceState);
        mGifImageView = new GifImageView(this);
        ImageView mImageView = null;
        // final Bitmap bitmap = FlActivity.getBitmapFromMemCache(path);
        boolean isgif = true;
        if (true){//(path.endsWith(".jpg")) { // HACK because file namesget not transmitted
            try {
                mGifImageView.setImageDrawable(new GifDrawable(path));
                mImageView = mGifImageView;
            } catch (IOException ex) {
                Logger.getLogger(PictureActivity.class.getName()).log(Level.SEVERE, null, ex);
                isgif = false;
            }
        } else {
            isgif = false;
        }
        if (!isgif) {
            mImageView = new ImageView(this);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(path, options);
            mImageView.setImageBitmap(bitmap);
        }
        mAttacher = new PhotoViewAttacher(mImageView);
        setContentView(mImageView);

    }

    private double getScale(int Pic_Width, int Pic_Height) {

        int width = ChatAdapter.imageMaxSize;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        Double val = Math.min(new Double(width) / new Double(Pic_Width), new Double(height) / new Double(Pic_Height));
        return val;
    }
}
