package com.example.adrian.cloudclientv3;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.adrian.cloudclientv3.model.Item;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import static com.example.adrian.cloudclientv3.MainActivity.*;

public class LoginActivity extends Activity {

    EditText edtUserName;
    EditText edtPassword;
    CheckBox chbNewUser;
    Button btnLogin;

    private boolean Authed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtUserName = findViewById(R.id.edt_username);
        edtPassword = findViewById(R.id.edt_password);

        chbNewUser = findViewById(R.id.cbx_new);

        btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String userName = edtUserName.getText().toString();
                String password = edtPassword.getText().toString();
                boolean flag = false;
                if (!userName.matches("^[a-zA-Z0-9]*$") || userName.length() < 5){
                    edtUserName.setError("UserName must be at least 5 characters long. Only letters & numbers");
                    edtUserName.setText(null);
                    flag = true;
                }
                if (!password.matches("^[a-zA-Z0-9]*$") || password.length() < 5){
                    edtPassword.setError("Password must be at least 5 characters long. Only letters & numbers");
                    edtPassword.setText(null);
                    flag = true;
                }
                if (flag) {
                    return;
                }
                btnLogin.setClickable(false);
                new AuthTask().execute(edtUserName.getText().toString(), edtPassword.getText().toString(), chbNewUser.isChecked() ? "true":"false");
            }
        });
    }

    private class AuthTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                Socket socket = new Socket(STR_IP_ADDRESS, PORT_NUM);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                if(strings[2].equals("true")) {
                    out.println(CREATE_NEW_USER + "");
                } else {
                    out.println(VERIFY_CREDENTIALS + "");
                }
                out.println(strings[0]);
                out.println(strings[1]);
                DataInputStream in = new DataInputStream(socket.getInputStream());
                if(in.readBoolean()) {
                    writeToInternal(getData(out, in));

                    socket.close();
                    return true;
                }
                socket.close();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        public ArrayList<byte[]> getData(PrintWriter out, DataInputStream in) {
            try {
                ArrayList<byte[]> bytes = new ArrayList<>();
                int numOfChunks = 0;
                numOfChunks = in.readInt();
                byte[] tempBytes;
                for(int i = 0; i < numOfChunks; i++) {
                    out.println("req"); //request
                    tempBytes = new byte[DATA_CHUNK_SIZE];
                    in.readFully(tempBytes,0,DATA_CHUNK_SIZE);
                    bytes.add(tempBytes);
                }
                out.println("req");
                tempBytes = new byte[in.readInt()];
                out.println("req");
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                in.readFully(tempBytes, 0, tempBytes.length);
                bytes.add(tempBytes);
                return bytes;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void writeToInternal(ArrayList<byte[]> bytes) {
            try { // change stream to internal storage.
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(getFilesDir().getPath(), "/FileList.json")));
                int offset = 0;
                byte[] concatenatedBytes = new byte[(((bytes.size() - 1) * DATA_CHUNK_SIZE) + bytes.get(bytes.size()-1).length)];
                for (int i = 0; i < bytes.size(); i++) {
                    System.arraycopy(bytes.get(i), 0, concatenatedBytes, offset, bytes.get(i).length);
                    offset += bytes.get(i).length;
                }
                bos.write(concatenatedBytes);
                bos.flush();
                bos.close();
            } catch (IOException ioe) { // do not need fileNotFound since we are creating file
                ioe.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(Boolean access) {
            super.onPostExecute(access);
            btnLogin.setClickable(true);
            if(chbNewUser.isChecked()) {
                if(access) {
                    Toast.makeText(LoginActivity.this, "Created and Logged In new User", Toast.LENGTH_LONG).show(); // if checked (NEW USER)
                    getIntent().putExtra(LOGIN_ACTIVITY_EX_USERNAME, edtUserName.getText().toString());
                    getIntent().putExtra(LOGIN_ACTIVITY_EX_PASSWORD, edtPassword.getText().toString());
                    setResult(Activity.RESULT_OK, getIntent());
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "User Already Exists With Current UserName", Toast.LENGTH_LONG).show(); // if checked (NEW USER)
                }
            } else {
                if (access) {
                    Toast.makeText(LoginActivity.this, "Access Granted. Welcome", Toast.LENGTH_LONG).show(); // if verifying true(VERIFY TRUE)
                    getIntent().putExtra(LOGIN_ACTIVITY_EX_USERNAME, edtUserName.getText().toString());
                    getIntent().putExtra(LOGIN_ACTIVITY_EX_PASSWORD, edtPassword.getText().toString());
                    setResult(Activity.RESULT_OK, getIntent());
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Access Denied. Username And/Or Password May Be Incorrect", Toast.LENGTH_LONG).show(); // if verifying false (VERIFY FALSE)
                }
            }
            edtUserName.setText(null);
            edtPassword.setText(null);
        }
    }
}
