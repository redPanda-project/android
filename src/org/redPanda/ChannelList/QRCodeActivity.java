/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda.ChannelList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.jwetherell.quick_response_code.data.Contents;
import com.jwetherell.quick_response_code.qrcode.QRCodeEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.redPanda.ChatAdapter;
import org.redPanda.R;

/**
 *
 * @author mflohr
 */
public class QRCodeActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qrview);
        Intent in = getIntent();
        
     
        ImageView iv = (ImageView) findViewById(R.id.QRImage);
        TextView tv = (TextView) findViewById(R.id.QRText);
        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(in.getExtras().getString("Key"),
                null,
                Contents.Type.TEXT,
                BarcodeFormat.QR_CODE.toString(),
                720);
        Bitmap bitmap = null;
        try {
            bitmap = qrCodeEncoder.encodeAsBitmap();
        } catch (WriterException ex) {
            Logger.getLogger(FlActivity.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (bitmap != null) {
            iv.setImageBitmap(bitmap);
            tv.setText(in.getExtras().getString("title"));
        }
    }

}
