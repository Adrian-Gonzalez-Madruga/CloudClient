package com.example.adrian.cloudclientv3;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import static com.example.adrian.cloudclientv3.MainActivity.DATA_CHUNK_SIZE;
import static com.example.adrian.cloudclientv3.MainActivity.PORT_NUM;
import static com.example.adrian.cloudclientv3.MainActivity.STR_IP_ADDRESS;
import static com.example.adrian.cloudclientv3.MainActivity.UPLOAD_FILE;

/*strings
    0 = Filename, 1 = Username, 2 = Password, 3 = jsonCode
 */

public class UploadTask extends AsyncTask <String, Void, Void> {
    private byte[] fileContent;

    public UploadTask(byte[] byteArray) {
        this.fileContent = byteArray;
    }

    @Override
    protected Void doInBackground(String... strings) {
        try {
            Socket socket = new Socket(STR_IP_ADDRESS, PORT_NUM);
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
            pw.println(UPLOAD_FILE); //command getData
            pw.println(strings[1]); //cred username
            pw.println(strings[2]); //cred password
            pw.println(strings[3]); //jsonCode
            pw.println(strings[0]); //fileName

            try {
                ArrayList<byte[]> bytes = new ArrayList<>();
                for (int i = 0; i < fileContent.length / DATA_CHUNK_SIZE; i++) {
                    byte[] temp = fileContent;
                    bytes.add(Arrays.copyOfRange(temp, i * DATA_CHUNK_SIZE, (i * DATA_CHUNK_SIZE) + DATA_CHUNK_SIZE));
                }
                byte[] temp = fileContent;
                bytes.add(Arrays.copyOfRange(temp, (temp.length / DATA_CHUNK_SIZE) * DATA_CHUNK_SIZE, temp.length));
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out.writeInt(bytes.size() - 1);
                for (int i = 0; i < bytes.size() - 1; i++) {
                    if (br.readLine().equals("req")) {
                        out.write(bytes.get(i));
                        System.out.println(i + " / " + (bytes.size() - 1));
                    }
                }
                if (br.readLine().equals("req")) {
                    out.writeInt(bytes.get(bytes.size() - 1).length);
                }
                System.out.println(bytes.get(bytes.size() - 1).length);
                if (br.readLine().equals("req")) {
                    out.write(bytes.get(bytes.size() - 1));
                }
                if(br.readLine().equals("clear")) {

                }
                out.flush();
                socket.close();
            } catch (IOException ioe) {
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                ioe.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //UPLOAD_FILE
        return null;
    }

   /* public void appendToJson(String userDirPath, String fileName, String jsonDataStr) {

        /*
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

        try {
            InputStream is = MainActivity.this.getContentResolver().openInputStream(new File(userDirPath + "\\FileList.json"));
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len;
            while ((len = is.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            byteArray = byteBuffer.toByteArray();
            byteBuffer.close();
            JSONObject head = new JSONObject(new String(Files.readAllBytes(Paths.get(userDirPath + "\\FileList.json"))));
            for (int i = 0; i < jsonDataStr.length() / 2; i++) {
                head = head.getJSONArray("folders").getJSONObject(jsonDataStr.charAt((i * 2) + 1));
            }
            head.getJSONArray("files").put(fileName);
            BufferedWriter bw = new BufferedWriter(new FileWriter(userDirPath + "\\FileList.json"));
            String temp = head.toString();
            bw.write(temp);
            bw.flush();
            bw.close();
        } catch (JSONException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }*/
}
