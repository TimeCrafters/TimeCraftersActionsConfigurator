package org.timecrafters.timecraftersactionconfigurator.jsonhandler;

import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Writer {
  static public String getDirectory() {
    return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "FIRST_TC_CONFIG";
  }

  static public String getConfigFilePath() {
    return getDirectory() + File.separator + "config.json";
  }

  static public String getBackupConfigFilePath() {
    return getDirectory() + File.separator + "config" + System.currentTimeMillis() + ".json.bak";
  }

  static public boolean writeJSON(String writePath, ArrayList<DataStruct> list) {
    boolean writeSuccessful = false;

    try {
      String data = toJson(list);

      Log.i("TC_CONFIG", "COnfig file saved to: " + writePath);
      BufferedWriter bw = new BufferedWriter(new FileWriter(new File(writePath), false));
      bw.write(data);
      bw.newLine();
      bw.flush();

      writeSuccessful = true;

    } catch (IOException e) {
      // TODO: handle this
    }

    return writeSuccessful;
  }

  static public String toJson(ArrayList list) {
    return new Gson().toJson(list);
  }

  static public boolean overwriteConfigFile(String config) {
    boolean writeSuccessful = false;

    File file = new File(Reader.getDirectory() + File.separator + "config.json");

    try {
      BufferedWriter bw = new BufferedWriter(new FileWriter(file, false));

      bw.write(config);
      bw.newLine();
      bw.flush();

      bw.close();

      writeSuccessful = true;

    } catch (IOException e) {
    }

    return writeSuccessful;
  }
}
