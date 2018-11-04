package org.timecrafters.timecraftersactionconfigurator.jsonhandler;

import android.content.Context;
import android.os.Environment;
import android.util.JsonReader;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Reader {
  private final Context context;
  private ArrayList<HashMap<String, DataStruct>> items;

  public Reader(Context context) {
    this.context = context;
    this.items = new ArrayList<>();

    if (new File(getDirectory()).exists()) {
      loadJSONFile();
    } else {
      if (createDirectory(getDirectory())) {
        loadJSONFile();
      } else {
        Toast.makeText(this.context, "Failed to create directory '" + getDirectory() + "'", Toast.LENGTH_LONG).show();
      }
    }
  }

  private String getDirectory() {
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

  private void loadJSONFile() {
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

        JSONObject array = new JSONObject(text.toString());
        System.out.println("Array" + array.toString());

      } catch (IOException e) {
      } catch (JSONException e) {}
    }
  }
}
