/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda;

import android.graphics.BitmapFactory;
import java.io.IOException;
import org.redPandaLib.core.ImageInfos;

/**
 *
 * @author mflohr
 */
public class ImageInfosAndroid implements ImageInfos {

    public Infos getInfos(String string) throws IOException {
        Infos in = new Infos();
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(string, o);
        in.heigth = o.outHeight;
        in.width = o.outWidth;
        return in;
    }
}
