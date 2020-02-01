package org.timecrafters.timecraftersactionconfigurator.support;

import android.support.design.widget.Snackbar;
import android.view.View;

import org.timecrafters.timecraftersactionconfigurator.EditActivity;
import org.timecrafters.timecraftersactionconfigurator.MainActivity;
import org.timecrafters.timecraftersactionconfigurator.jsonhandler.DataStruct;
import org.timecrafters.timecraftersactionconfigurator.jsonhandler.Writer;
import org.timecrafters.timecraftersactionconfigurator.server.Connection;
import org.timecrafters.timecraftersactionconfigurator.server.PacketHandler;
import org.timecrafters.timecraftersactionconfigurator.server.Server;

import java.util.ArrayList;
import java.util.Timer;

public class AppSync {
  public static AppSync instance;
  final public static String HOSTNAME = "192.168.49.1";
  final public static int    PORT     = 8962;

  public Server server;
  public Connection connection;
  public ArrayList<DataStruct> dataStructs = new ArrayList<>();
  public String searchFilter = null;

  public boolean serverEnabled = false,
                 allowDestructiveEditing = false;
  public MainActivity mainActivity;
  public EditActivity editActivity;

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

  static public EditActivity getEditActivity() {
    return instance.editActivity;
  }

  static public boolean saveJSON() {
    return saveJSON(getMainActivity().primaryLayout, "");
  }

  static public boolean saveJSON(View view, String message) {
    boolean writeSucceeded = Writer.writeJSON(Writer.getConfigFilePath(), getDataStructs());

    if (writeSucceeded) {
      if (getConnection() != null && !getConnection().isClosed()) {
        getConnection().getClient().puts(PacketHandler.packetDumpConfig(Writer.toJson(getDataStructs())).toString());
      }
    }

    return writeSucceeded;
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
