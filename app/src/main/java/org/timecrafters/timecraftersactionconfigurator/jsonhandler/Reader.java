package org.timecrafters.timecraftersactionconfigurator.jsonhandler;

import android.content.Context;
import android.os.Environment;
import android.util.JsonReader;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class Reader {
  private final Context context;
  private ArrayList<DataStruct> dataStructs;
  private boolean loadSuccessful = false;

  public Reader(Context context) {
    this.context = context;
    this.dataStructs = new ArrayList<>();

    if (new File(getDirectory()).exists()) {
      loadJSON();
    } else {
      if (createDirectory(getDirectory())) {
        loadJSON();
      } else {
        Toast.makeText(this.context, "Failed to create directory '" + getDirectory() + "'", Toast.LENGTH_LONG).show();
      }
    }
  }

  public ArrayList dataStructs() {
    return dataStructs;
  }

  static public String getDirectory() {
    return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "FIRST_TC_CONFIG";
  }

  private boolean createDirectory(String path) {
    File directory = new File(path);

    if (directory.mkdirs()) {
      // Success
      return true;
    } else {
      // Failed
      return false;
    }
  }

  private boolean loadJSON() {
    boolean loadSuccessful = false;

    File file = new File(getDirectory() + File.separator + "config.json");
    StringBuilder text = new StringBuilder();

    if (file.exists()) {
      try {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;

        while ((line = br.readLine()) != null) {
          text.append(line);
          text.append('\n');
        }
        br.close();

        Gson gson = new Gson();
        DataStruct[] array = gson.fromJson(text.toString(), DataStruct[].class);
        this.dataStructs = new ArrayList<>(Arrays.asList(array));

        loadSuccessful = true;

      } catch (IOException e) {
        System.out.println(e);
        // TODO: handle this
      }
    }

    this.loadSuccessful = loadSuccessful;
    return loadSuccessful;
  }

  static public String rawConfigFile() {
    File file = new File(Reader.getDirectory() + File.separator + "config.json");
    StringBuilder text = new StringBuilder();

    if (file.exists()) {
      try {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;

        while ((line = br.readLine()) != null) {
          text.append(line);
          text.append('\n');
        }
        br.close();

        return text.toString();

      } catch (IOException e) {
        return null;
      }

    } else {
      return null;
    }
  }

  public boolean getLoadSuccess() { return loadSuccessful; }
}
