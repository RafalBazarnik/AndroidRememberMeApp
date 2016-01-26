package com.example.bazarnik.rafal.remembermeapp;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

/**
 * Created by User on 2016-01-26.
 */
public class QRCodeActivity extends AppCompatActivity {

    static final String SCAN = "com.google.zxing.client.android.SCAN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_reader_layout);
    }

    public void scanQRCode(View v) {
        try {
            Intent intent = new Intent(SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        }
        catch (ActivityNotFoundException e) {
            downloadDialog(QRCodeActivity.this, "No Scanner", "Do you want to download the scanner, now???", "Yes", "No").show();
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0 && resultCode == RESULT_OK) {
                String content = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                Toast toast = Toast.makeText(this, "Content:" + content + " Format:" + format, Toast.LENGTH_LONG);
                toast.show();

            }
        else {
            Toast toast = Toast.makeText(this, "Problem occured while reading QR code", Toast.LENGTH_LONG);
            toast.show();
        }
    }


        private static AlertDialog downloadDialog (final Activity activ, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
        AlertDialog.Builder download = new AlertDialog.Builder(activ);
        download.setTitle(title).setMessage(message);
        download.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    activ.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                }
            }
        }).setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                dialog.cancel();
            }
        });
            return download.show();


        }
}
