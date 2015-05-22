/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda.ChannelList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.jwetherell.quick_response_code.DecoderActivity;
import com.jwetherell.quick_response_code.DecoderActivityHandler;
import com.jwetherell.quick_response_code.camera.CameraManager;
import com.jwetherell.quick_response_code.result.ResultHandler;
import java.util.ArrayList;
import org.redPanda.R;
import org.redPandaLib.Main;

/**
 *
 * @author mflohr
 */
public class QRCaptureActivity extends DecoderActivity {

    private boolean inScanMode = false;
 //   private TextView statusView = null;
  //  private View resultView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
//        new Thread(new Runnable() {
//
//            public void run() {
//               Main.sendBroadCastMsg("QRCA onCreate");
//            }
//        }).run();
        //   Toast.makeText(QRCaptureActivity.this, "start oncreate", Toast.LENGTH_SHORT).show();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.capture);
       // resultView = findViewById(R.id.result_view);
    //    statusView = (TextView) findViewById(R.id.status_view);
        inScanMode = false;
        //     Toast.makeText(QRCaptureActivity.this, "end oncreate", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (inScanMode) {
                finish();
            } else {
                onResume();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void handleDecode(Result rawResult, Bitmap barcode) {

    }

    @Override
    protected void showScanner() {
        inScanMode = true;
       // resultView.setVisibility(View.GONE);
      //  statusView.setText(R.string.msg_default_status);
        //statusView.setVisibility(View.GONE);
        viewfinderView.setVisibility(View.VISIBLE);
    }

    private void handleDecodeInternally(Result rawResult, ResultHandler resultHandler, Bitmap barcode) {
        onPause();
        CharSequence res = resultHandler.getDisplayContents();

        Intent intent;
        intent = new Intent(QRCaptureActivity.this, FlActivity.class);
        //TODO look at flags
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("newChannel", true);
        intent.putExtra("ChannelName", "");
        intent.putExtra("ChannelKey", res);
        startActivity(intent);

    }

    @Override
    public void onBackPressed() {
        Intent intent;
        intent = new Intent(QRCaptureActivity.this, FlActivity.class);
        //TODO look at flags
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}
