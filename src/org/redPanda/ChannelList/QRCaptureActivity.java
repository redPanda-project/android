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
import com.jwetherell.quick_response_code.result.ResultHandlerFactory;
import java.util.ArrayList;
import org.redPanda.R;
import org.redPandaLib.Main;

/**
 *
 * @author mflohr
 */
public class QRCaptureActivity extends DecoderActivity {

    private boolean inScanMode = false;
    private TextView statusView = null;
    private View resultView = null;

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
        resultView = findViewById(R.id.result_view);
        statusView = (TextView) findViewById(R.id.status_view);
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
        drawResultPoints(barcode, rawResult);

        ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(this, rawResult);
        handleDecodeInternally(rawResult, resultHandler, barcode);
    }

    @Override
    protected void showScanner() {
        inScanMode = true;
        resultView.setVisibility(View.GONE);
        statusView.setText(R.string.msg_default_status);
        statusView.setVisibility(View.GONE);
        viewfinderView.setVisibility(View.VISIBLE);
    }

    private void handleDecodeInternally(Result rawResult, ResultHandler resultHandler, Bitmap barcode) {
        onPause();
        CharSequence res = resultHandler.getDisplayContents();

        AlertDialog.Builder builder = new AlertDialog.Builder(QRCaptureActivity.this);
        builder.setTitle("Import Channel");

        final LinearLayout ll = (LinearLayout) getLayoutInflater().inflate(R.layout.ippchandiag, null);

        final EditText name = (EditText) ll.findViewById(R.id.channame);
        final EditText key = (EditText) ll.findViewById(R.id.chankey);
        name.setHintTextColor(Color.RED);
        key.setHintTextColor(Color.CYAN);
        key.setText(res);

        builder.setView(ll);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent;
                intent = new Intent(QRCaptureActivity.this, FlActivity.class);
                //TODO look at flags
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("newChannel", true);
                intent.putExtra("ChannelName", name.getText());
                intent.putExtra("ChannelKey", key.getText());
                startActivity(intent);

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                onBackPressed();
            }
        });

        builder.show();

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
