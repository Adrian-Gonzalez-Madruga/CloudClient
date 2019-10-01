package com.example.adrian.cloudclientv3.model;

import android.view.View;

import com.example.adrian.cloudclientv3.DownloadTask;
import com.example.adrian.cloudclientv3.ItemAdapter;
import com.example.adrian.cloudclientv3.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

import static com.example.adrian.cloudclientv3.MainActivity.recItems;

public class Item implements Comparable<Item>{

    private String itemName;
    private String btnName;
    private View.OnClickListener onClick;
    private static String credUserName;
    private static String credPassword;

    private Item(String itemName, String btnName, View.OnClickListener onClick) {
        this.itemName = itemName;
        this.btnName = btnName;
        this.onClick = onClick;
    }

    public String getItemName() {
        return itemName;
    }

    public String getBtnName() {
        return btnName;
    }

    public View.OnClickListener getOnClick() {
        return onClick;
    }

    public static void setCredentials(String credUserNamein, String credPasswordin) {
        credUserName = credUserNamein;
        credPassword = credPasswordin;
    }

    public static ArrayList<Item> getItemElements(JSONObject folder, String jsonCode, File jsonFile) {
        ArrayList<Item> itemList = new ArrayList<>();
        ArrayList<Item> itemFiles = new ArrayList<>();
        ArrayList<Item> itemFolders = new ArrayList<>();

        if (folder == null) {
            folder = getObjectFromCode("", jsonFile);
        }

        try {
            JSONArray jsonFiles = folder.getJSONArray("files");
            for(int i = 0; i < jsonFiles.length(); i++) {
                itemFiles.add(new Item(jsonFiles.get(i).toString(), "Download", new Download(jsonCode + "i" + i, jsonFiles.get(i).toString(), credUserName, credPassword)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            JSONArray jsonFolders = folder.getJSONArray("folders");
            for(int i = 0; i < jsonFolders.length(); i++) {
                itemFolders.add(new Item(jsonFolders.getJSONObject(i).getString("name"), "Open Folder", new OpenFolder(jsonCode + "o" + i, jsonFile)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Collections.sort(itemFiles);
        Collections.sort(itemFolders);
        if(jsonCode.length() >= 2) {
            itemList.add(new Item("..", "return", new OpenFolder(new String(Arrays.copyOfRange(jsonCode.toCharArray(), (jsonCode.length()-4 < 0) ? 0 : jsonCode.length()-4, jsonCode.length()-2)), jsonFile)));
        }
        itemList.addAll(itemFiles);
        itemList.addAll(itemFolders);
        return itemList;
    }

    public static JSONObject getObjectFromCode(String jsonCode, File jsonFile) {
        try {
            Scanner sc = new Scanner(new FileInputStream(jsonFile));
            StringBuilder stb = new StringBuilder();
            while (sc.hasNextLine()) {
                stb.append(sc.nextLine());
            }
            JSONObject currentObject = new JSONObject(stb.toString());
            for(int i = 0; i < jsonCode.length() / 2; i++) {
                currentObject = currentObject.getJSONArray("folders").getJSONObject(Integer.parseInt(jsonCode.charAt((i*2)+1) + ""));
            }
            return currentObject;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }  catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int compareTo(Item o) {
        return itemName.compareTo(o.getItemName());
    }

    private static class Download implements View.OnClickListener {

        private String jsonCode;
        private String fileName;
        private String userName;
        private String password;

        public Download(String jsonCode, String fileName, String userName, String password) {
            this.userName = userName;
            this.password = password;
            this.fileName = fileName;
            this.jsonCode = jsonCode;
        }

        @Override
        public void onClick(View v) {
            new DownloadTask().execute(fileName, userName, password, jsonCode);
        }
    }

    private static class OpenFolder implements View.OnClickListener {

        private String jsonCode;
        private File jsonFile;

        public OpenFolder(String jsonCode, File jsonFile) {
            this.jsonCode = jsonCode;
            this.jsonFile = jsonFile;
        }

        @Override
        public void onClick(View v) {
            MainActivity.jsonCodeIn = jsonCode;
            ItemAdapter adapter = new ItemAdapter(getItemElements(getObjectFromCode(jsonCode, jsonFile), jsonCode, jsonFile));
            recItems.setAdapter(adapter);

        }
    }

}
