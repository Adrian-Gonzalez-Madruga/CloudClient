package com.example.adrian.cloudclientv3;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.provider.OpenableColumns;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.adrian.cloudclientv3.model.Item;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;

public class MainActivity extends Activity {

    public static final int GET_DATA = 0001;           //server commands
    public static final int VERIFY_CREDENTIALS = 0002;
    public static final int CREATE_NEW_USER = 0003;
    public static final int UPLOAD_FILE = 0004;

    public static final int LOGIN_CREDENTIALS_ACTRES = 1001;
    public static final String LOGIN_ACTIVITY_EX_USERNAME = "USERNAME";
    public static final String LOGIN_ACTIVITY_EX_PASSWORD = "PASSWORD";

    public static final int UPLOAD_FILE_ACTIVITY = 1002;

    public static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2001;

    public static String jsonCodeIn = "";

    public static final String STR_IP_ADDRESS = "10.16.6.188"; //                        ------***If on different PC hard change it to location of Server project***------
    public static final int PORT_NUM = 19090;
    public static final int DATA_CHUNK_SIZE = 102400; // how many bytes are read in each chunk

    private Menu menu;
    public static RecyclerView recItems;
    public static Context CONTEXT;
    private boolean loggedOut = true;
    private String credentials[] = new String[2];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recItems = findViewById(R.id.rec_items);

        recItems.addItemDecoration(new DividerItemDecoration(recItems.getContext(),
                DividerItemDecoration.VERTICAL));

        RecyclerView.LayoutManager manager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);

        recItems.setLayoutManager(manager);

        CONTEXT = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*if(new File(getFilesDir() + "/FileList.json").exists()) {
            ItemAdapter adapter = new ItemAdapter(Item.getItemElements(null, "", new File(getFilesDir().getPath() + "/FileList.json")));
            recItems.setAdapter(adapter);
        }*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (new File(getFilesDir(), "/FileList.json").exists()) {
            new File(getFilesDir(), "/FileList.json").delete();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Granted Permission: Write To External Storage", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivityForResult(intent, LOGIN_CREDENTIALS_ACTRES);
            } else {
                Toast.makeText(MainActivity.this, "Denied Permission: Write To External Storage", Toast.LENGTH_LONG).show();
            }
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //creates the menu and inflates it
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.login_menu, menu);
        menu.getItem(0).setVisible(false);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //sets the menu title to be Login and opens the Login Screen
        if(item.getItemId() == R.id.menu_login){
            if(item.getTitle().toString().equals("Login")){
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivityForResult(intent, LOGIN_CREDENTIALS_ACTRES);
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                }
                loggedOut = false;
            }else if (item.getTitle().toString().equals("Logout")){
                //Logs the user out and reopens the Login Screen
                credentials = new String[2];
                item.setTitle("Login");
                PrintWriter writer = null;
                try {
                    writer = new PrintWriter(getFilesDir().getPath() + "/FileList.json");
                    writer.print("");
                    writer.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                recItems.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this,"Logged Out", Toast.LENGTH_LONG).show();
                menu.getItem(0).setVisible(false);
            }
        } else if(item.getItemId() == R.id.menu_upload){
            startUri();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == LOGIN_CREDENTIALS_ACTRES && resultCode == RESULT_OK) {
            credentials[0] = data.getStringExtra(LOGIN_ACTIVITY_EX_USERNAME);
            credentials[1] = data.getStringExtra(LOGIN_ACTIVITY_EX_PASSWORD);
            MenuItem menuItem = menu.findItem(R.id.menu_login);
            menuItem.setTitle("Logout");
            Item.setCredentials(credentials[0], credentials[1]);
            ItemAdapter adapter = new ItemAdapter(Item.getItemElements(null, "", new File(getFilesDir().getPath() + "/FileList.json")));
            recItems.setAdapter(adapter);
            recItems.setVisibility(View.VISIBLE);
            menu.getItem(0).setVisible(true);
        } else if(requestCode == UPLOAD_FILE_ACTIVITY && resultCode == RESULT_OK){ // if the resultCode and requestCode are proper, start the file - byteArray

            Uri uri = data.getData();
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            cursor.moveToFirst();
            String fileName = cursor.getString(nameIndex);

            byte[] byteArray = null;
            try {
                InputStream is = MainActivity.this.getContentResolver().openInputStream(uri);
                ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                int bufferSize = 1024;
                byte[] buffer = new byte[bufferSize];
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        byteBuffer.write(buffer, 0, len);
                    }
                    byteArray = byteBuffer.toByteArray();
                    byteBuffer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            new UploadTask(byteArray).execute(fileName, credentials[0], credentials[1], jsonCodeIn);
        }
    }

    private void startUri() {

        //startUri opens up any file in the phones documents that the user can select
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE); //This will only show files that are openable for this app
        intent.setType("*/*"); //setType(*/*) will show every file with every extension
        startActivityForResult(intent, UPLOAD_FILE_ACTIVITY); //Launches the onActivityResult once a file selected


    }
}
