package com.example.adrian.cloudclientv3;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.nio.file.Path;

/**
 * Created by connorclarkson on 2018-12-13.
 */

public class TappedActivity extends Activity {

    public static final int NOTIFICATION_TAPPED = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //When the notification is tapped, it brings up the MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        /*
        Intent intent = new Intent();
        String PathName = intent.getStringExtra("pathName");

        Uri uri = Uri.parse(PathName); //filename is string with value 46_1244625499.gif*/
        //intent.setDataAndType(uri, "*/*");
        //startActivity(Intent.createChooser(intent, "Open folder"));
        //File storage = Environment.getExternalStorageDirectory();
        //File file = new File(PathName);
        //Toast.makeText(this, PathName ,Toast.LENGTH_LONG).show();
        /*intent.setAction(android.content.Intent.ACTION_VIEW);
        File file = new File(PathName);*/
        // intent.setDataAndType(Uri.fromFile(file), "*/*");
    }
}
