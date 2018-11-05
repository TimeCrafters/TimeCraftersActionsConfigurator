package org.timecrafters.timecraftersactionconfigurator.jsonhandler;

import com.google.gson.annotations.JsonAdapter;

public class DataStruct {
  private String name;
  private boolean enabled;

  public DataStruct() {}
  public DataStruct(String name, boolean enabled) {
    this.name = name;
    this.enabled = enabled;
  }


  public String name() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  public boolean enabled() {
    return enabled;
  }
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
