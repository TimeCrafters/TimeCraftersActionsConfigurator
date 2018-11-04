package org.timecrafters.timecraftersactionconfigurator.jsonhandler;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class Writer {
  private ArrayList<HashMap<String, DataStruct>> list;
  private File file;

  public Writer(ArrayList list) {
    this.list = list;
    file = new File(getDirectory()+File.separator+"config.json");

    writeJSON();
  }

  private String getDirectory() {
    return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "FIRST_TC_CONFIG";
  }

  private void writeJSON() {

  }
}
