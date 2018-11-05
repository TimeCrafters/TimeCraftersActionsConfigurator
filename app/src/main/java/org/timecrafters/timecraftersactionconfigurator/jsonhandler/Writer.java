package org.timecrafters.timecraftersactionconfigurator.jsonhandler;

import android.os.Environment;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Writer {
  private ArrayList<DataStruct> dataStructs;
  private File file;

  public Writer(ArrayList<DataStruct> dataStructs) {
    this.dataStructs = dataStructs;
    file = new File(getDirectory()+File.separator+"config.json");

    writeJSON(this.dataStructs);
  }

  private String getDirectory() {
    return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "FIRST_TC_CONFIG";
  }

  private boolean writeJSON(ArrayList list) {
    boolean writeSuccessful = false;

    try {
      Gson gson = new Gson();
      String data = gson.toJson(dataStructs);

      BufferedWriter bw = new BufferedWriter(new FileWriter(file, false));
      bw.write(data);
      bw.newLine();
      bw.flush();

      writeSuccessful = true;

    } catch (IOException e) {
      // TODO: handle this
    }

    return writeSuccessful;
  }
}
