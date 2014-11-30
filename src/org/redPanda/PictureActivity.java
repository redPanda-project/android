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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.redPanda.ChannelList.FlActivity;
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
        // set background to transparent grey
        getWindow().setBackgroundDrawable(new ColorDrawable(0x80000000));
        Intent in = getIntent();
        // get path to the picture from the intent
        String path = in.getExtras().getString("path");

        //Check if the Image ends with ".gif"
        ImageView mImageView = new ImageView(con);
        mImageView.setImageBitmap(FlActivity.getBitmapFromMemCache(path));
        mAttacher = new PhotoViewAttacher(mImageView);
        mAttacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {

            public void onViewTap(View view, float x, float y) {
                // Close the Activity when taped on the ImageView
                finish();
            }
        });

        class PictureAsyncTask extends AsyncTask<Integer, Void, Drawable> {

            private final WeakReference<ImageView> imageViewReference;
            private String path;
            private PictureActivity pa;
            private PhotoViewAttacher attacher;

            public PictureAsyncTask(ImageView imageView, PhotoViewAttacher attacher, String path) {
                // Use a WeakReference to ensure the ImageView can be garbage collected
                imageViewReference = new WeakReference<ImageView>(imageView);
                this.path = path;
                this.attacher = attacher;
            }

            @Override
            protected Drawable doInBackground(Integer... params) {
                Drawable picture = null;
                boolean isgif = false;
                if (true) {//(path.endsWith(".gif")) { // HACK because all pictures are saved as .jpg

                    //Try to load gif in GifImageView
                    isgif = true;
                    try {
                        picture = new GifDrawable(path);
                    } catch (IOException ex) {
                        Logger.getLogger(PictureActivity.class.getName()).log(Level.SEVERE, null, ex);
                        isgif = false;
                    }
                }
                //If the file is not a gif or loading the gif fails display it as bitmap
                if (!isgif) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(path, options);
                    picture = new BitmapDrawable(getResources(), bitmap);

                }
                return picture;
            }

            @Override
            protected void onPostExecute(Drawable picture) {
                ImageView iv = imageViewReference.get();
                iv.setImageDrawable(picture);
                attacher.update();

            }

        }
        PictureAsyncTask pat = new PictureAsyncTask(mImageView, mAttacher, path);
        pat.execute();
        setContentView(mImageView);

    }
}