package org.timecrafters.timecraftersactionconfigurator.support;

import android.support.design.widget.Snackbar;
import android.view.View;

import org.timecrafters.timecraftersactionconfigurator.MainActivity;
import org.timecrafters.timecraftersactionconfigurator.jsonhandler.DataStruct;
import org.timecrafters.timecraftersactionconfigurator.jsonhandler.Writer;
import org.timecrafters.timecraftersactionconfigurator.server.Connection;
import org.timecrafters.timecraftersactionconfigurator.server.Server;

import java.util.ArrayList;
import java.util.Timer;

public class AppSync {
  public static AppSync instance;
  final public static String HOSTNAME = "192.168.1.3";//"192.168.49.1";
  final public static int    PORT     = 8962;

  public Server server;
  public Connection connection;
  public ArrayList<DataStruct> dataStructs = new ArrayList<>();

  public boolean serverEnabled = false,
                 allowDestructiveEditing = false;
  public MainActivity mainActivity;

  private Timer uiController;

  public AppSync() {
    this.instance = this;

    this.uiController = new Timer("uicontroller", false);
    this.uiController.schedule(new UIController(), 0, 500);
  }

  static public Server getServer() {
    return instance.server;
  }

  static public Connection getConnection() {
    return instance.connection;
  }

  static public ArrayList<DataStruct> getDataStructs() {
    return instance.dataStructs;
  }

  static public MainActivity getMainActivity() {
    return instance.mainActivity;
  }

  static public void saveJSON() {
    saveJSON(getMainActivity().primaryLayout, "");
  }

  static public void saveJSON(View view, String message) {
    boolean writeSucceeded = Writer.writeJSON(Writer.getConfigFilePath(), getDataStructs());

    if (writeSucceeded) {
      if (getConnection() != null && !getConnection().isClosed()) {
        getConnection().puts(Writer.toJson(getDataStructs()));
      }

      Snackbar.make(view, message+" JSON Saved.", Snackbar.LENGTH_SHORT)
              .setAction("Action", null).show();
    } else {
      Snackbar.make(view, "Failed to write JSON!", Snackbar.LENGTH_LONG)
              .setAction("Action", null).show();
    }
  }

  static public void addNewAction(String name) {
    DataStruct dataStruct = new DataStruct();
    dataStruct.setName(name);
    AppSync.getDataStructs().add(dataStruct);

    getMainActivity().addActionToLayout(dataStruct);
    AppSync.saveJSON();
  }

  static public boolean actionNameIsUnique(String name) {
    boolean unique = true;

    for (DataStruct dataStruct : AppSync.getDataStructs()) {
      if (dataStruct.name().equals(name)) { unique = false; }
    }

    return unique;
  }
}
