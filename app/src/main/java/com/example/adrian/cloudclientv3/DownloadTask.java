package com.example.adrian.cloudclientv3;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import static com.example.adrian.cloudclientv3.MainActivity.*;

/*strings
    0 = Filename, 1 = Username, 2 = Password, 3 = jsonCode
 */

public class DownloadTask extends AsyncTask<String, Integer, Integer> {

    private Context mContext = CONTEXT;

    private NotificationManager manager;
    private Notification.Builder builder;
    private static final String MY_CHANNEL_ID = "myChannelId"; //notifications
    private static final String MY_CHANNEL_NAME = "myChannelName";
    private static final int MY_NOTIFICATION = 3001; //activity intent

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        displayNotification();
        // Displays the progress bar and sets the current progress to 0 with a max of 100
        builder.setProgress(100, 0, false);
        manager.notify(MY_NOTIFICATION, builder.build());
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        // Continuously updates the progress bar in the notification
        builder.setProgress(values[1], values[0], false).setContentText("Downloading " + ((int) Math.ceil(values[0].doubleValue() / values[1].doubleValue() * 100)) + "%");
        manager.notify(MY_NOTIFICATION, builder.build());
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        // Removes the progress bar and sets the content text to be Download Complete
        builder.setContentText("Download complete");
        builder.setProgress(0, 0, false).setOngoing(false).setAutoCancel(true); //Allows the notification to be removed once the download is done
        manager.notify(MY_NOTIFICATION, builder.build());
    }

    @Override
    protected Integer doInBackground(String... strings) {
        builder.setContentTitle(strings[0]);//lets the Notification title to the file name
        ArrayList<byte[]> bytes = readFileFromServer(strings[1], strings[2], strings[3]);
        if (!bytes.isEmpty()) {
            writeToFile(bytes, strings[0]);
        }
        return null;
    }

    public ArrayList<byte[]> readFileFromServer(String username, String password, String jsonCode) {
        ArrayList<byte[]> bytes = new ArrayList<>();
        try {
            Socket socket = new Socket(STR_IP_ADDRESS, PORT_NUM);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(GET_DATA); //command getData
            out.println(username); //cred username
            out.println(password); //cred password
            out.println(jsonCode); //jsonCode
            out.flush();
            DataInputStream in = new DataInputStream(socket.getInputStream());
            int numOfChunks = in.readInt();
            byte[] tempBytes;
            Integer[] values = new Integer[2];
            for(int i = 0; i < numOfChunks; i++) {
                out.println("req"); //request
                tempBytes = new byte[DATA_CHUNK_SIZE];
                in.readFully(tempBytes,0,DATA_CHUNK_SIZE);
                bytes.add(tempBytes);
                values[0] = i; //sets the first value to i
                values[1] = numOfChunks; //sets the second value to the size of the file
                publishProgress(values); //publishes the current file download size
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
            socket.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
        return bytes;
    }

    public void writeToFile(ArrayList<byte[]> bytes, String filename) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename)));
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

    private void displayNotification() {


        // create an explicit Intent to launch NotificationActivity
        /*Intent intent = new Intent(CONTEXT.getApplicationContext(), TappedActivity.class);

        // Create a PendingIntent for Second Activity
        PendingIntent contentIntent = PendingIntent.getActivity(
                CONTEXT.getApplicationContext(),
                TappedActivity.NOTIFICATION_TAPPED,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);*/


        builder = new Notification.Builder(CONTEXT);//creates the notification builder


        // Sets the builder to the settings we are using
        builder.setWhen(System.currentTimeMillis()).setSmallIcon(R.drawable.baseline_highlight_off_white_24).setTicker("Notification").setContentText("Downloading")
                //.setContentIntent(contentIntent) // Reopens the activity_main when the user taps the notification
                .setProgress(100, 0, false)// sets the progress for the notification bar
                .setOngoing(true); // Makes the notification permanent

        // Creates the Notification Manager
        manager = (NotificationManager) CONTEXT.getSystemService(NOTIFICATION_SERVICE);
        // Checks to see if the device has a modern operating system
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(MY_CHANNEL_ID, MY_CHANNEL_NAME,
                            NotificationManager.IMPORTANCE_LOW);

            // While the notification is active, it will flash the devices light and vibrate the device
            channel.enableLights(true);
            channel.enableVibration(true);

            // create the notification channel
            manager.createNotificationChannel(channel);

            builder.setChannelId(MY_CHANNEL_ID);
        } else {
            // If the device is Nugget or lower, it will run this code

            builder.setLights(Color.BLUE, 1000, 1000);

            builder.setVibrate(new long[]{500, 1000, 500, 1000});
        }

        Notification notify = builder.build();
        // Call notify off of the manager
        manager.notify(MY_NOTIFICATION, notify);
    }

}
