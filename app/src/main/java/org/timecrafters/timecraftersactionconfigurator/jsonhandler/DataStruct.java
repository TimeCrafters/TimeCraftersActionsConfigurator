package org.timecrafters.timecraftersactionconfigurator.jsonhandler;

import com.google.gson.annotations.JsonAdapter;

import java.util.HashMap;

public class DataStruct {
  private String name;
  private boolean enabled;

  /*
    Hash of Strings mapped to Strings, those strings are various types mapped to stings:
    Boolean -> "Bxfalse"
    Integer -> "Ix100"
    Double  -> "Dx0.1"
    Float   -> "Fx10.1"
    String  -> "SxWords can go here."

    DataStruct.valueOf(Hash.get("distanceToDrive")) -> 100
   */
  private HashMap<String, String> variables;

  public DataStruct() {
    this.variables = new HashMap<>();
  }
  public DataStruct(String name, boolean enabled, HashMap<String, String> variables) {
    this.name = name;
    this.enabled = enabled;
    this.variables = variables;
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

  public HashMap<String, String> variables() {
    return variables;
  }

  public <T> T variable(String key) throws NullPointerException {
    return valueOf(variables.get(key));
  }

  @SuppressWarnings("unchecked")
  public static <T> T valueOf(String value) {
    String[] split = value.split("x");

    switch (split[0]) {
      case "B": {
        return (T) Boolean.valueOf(split[(split.length-1)]);
      }
      case "D": {
        return (T) Double.valueOf(split[(split.length-1)]);
      }
      case "F": {
        return (T) Float.valueOf(split[(split.length-1)]);
      }
      case "I": {
        return (T) Integer.valueOf(split[(split.length-1)]);
      }
      case "S": {
        return (T) String.valueOf(split[(split.length-1)]);
      }
      default: {
        return null;
      }
    }
  }

  public static String typeOf(String value) {
    String[] split = value.split("x");

    switch (split[0]) {
      case "B": {
        return "Boolean";
      }
      case "D": {
        return "Double";
      }
      case "F": {
        return "Float";
      }
      case "I": {
        return "Integer";
      }
      case "S": {
        return "String";
      }
      default: {
        return "=!UNKNOWN!=";
      }
    }
  }

  public static String encodeValue(String type, String value) {
    switch (type) {
      case "Boolean": {
        return "Bx"+value;
      }
      case "Double": {
        return "Dx"+value;
      }
      case "Float": {
        return "Fx"+value;
      }
      case "Integer": {
        return "Ix"+value;
      }
      case "String": {
        return "Sx"+value;
      }
      default: {
        return "=!UNKNOWN!=";
      }
    }
  }
}
