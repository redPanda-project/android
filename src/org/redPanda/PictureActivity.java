/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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

    
    PhotoViewAttacher mAttacher;
    Context con;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        con = this;
        getWindow().setBackgroundDrawable(new ColorDrawable(0x80000000));
        Intent in = getIntent();
        String path = in.getExtras().getString("path");       
        GifImageView mGifImageView;
        ImageView mImageView = null;      
        //Check if the Image ends with ".gif"
        boolean isgif = false;
        
        
        if (true) {//(path.endsWith(".gif")) { // HACK because all pictures are saved as .jpg
            
            
            
            //Try to load gif in GifImageView
            isgif = true;
            try {
                mGifImageView = new GifImageView(this);
                mGifImageView.setImageDrawable(new GifDrawable(path));
                mImageView = mGifImageView;
            } catch (IOException ex) {
                Logger.getLogger(PictureActivity.class.getName()).log(Level.SEVERE, null, ex);
                isgif = false;               
            }
        }
        //If the file is not a gif or loading the gif fails display it as bitmap
        if (!isgif) {
            mImageView = new ImageView(this);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(path, options);
            mImageView.setImageBitmap(bitmap);
        }
        mAttacher = new PhotoViewAttacher(mImageView);
        mAttacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {

            public void onViewTap(View view, float x, float y) {
                // Close the Activity when taped on the ImageView
                finish();
            }
        });
        setContentView(mImageView);

    }
}
