package org.timecrafters.timecraftersactionconfigurator.server;

import android.util.Log;

import org.timecrafters.timecraftersactionconfigurator.MainActivity;
import org.timecrafters.timecraftersactionconfigurator.jsonhandler.Writer;

import java.io.IOException;
import java.net.Socket;

public class Connection {
  private Client client;
  private String hostname;
  private int port;
  private String lastSocketError = null;
  private boolean socketError = false;

  private long lastSyncTime = 0;
  private long syncInterval = 250;

  private Runnable connectionHandlingRunner;

  public Connection(String hostname, int port) {
    this.hostname = hostname;
    this.port = port;

    this.connectionHandlingRunner = new Runnable() {
      @Override
      public void run() {
        handleConnection();
      }
    };
  }

  public void connect(final Runnable callback) {
    if (client != null) {
      return;
    }

    client = new Client();

    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          client.setSocket(new Socket(hostname, port));
          Log.i("TACNET", "Connected to: " + hostname + ":" + port);

          while(client != null && !client.isClosed()) {
            if (System.currentTimeMillis() > lastSyncTime + syncInterval) {
              lastSyncTime = System.currentTimeMillis();

              client.sync(connectionHandlingRunner);
            }
          }
        } catch (IOException e) {
          socketError = true;
          lastSocketError = e.getMessage();

          callback.run();

          Log.e("TACNET", e.toString());
        }
      }
    }).start();
  }

  private void handleConnection() {
    if (client != null && !client.isClosed()) {
      String message = client.gets();

      if (message != null) {
        if (
                message.length() > 4 && message.charAt(0) == "[".toCharArray()[0] &&
                message.charAt(message.length() - 1) == "]".toCharArray()[0]
        ) {
          // write json to file
          Log.i("TACNET", "Got valid json: " + message);
           Writer.overwriteConfigFile(message);

           MainActivity.instance.runOnUiThread(new Runnable() {
             @Override
             public void run() {
               MainActivity.instance.reloadConfig();
             }
           });
        }
      }

      client.puts("heartbeat");
    }
  }

  public void write(String message) throws IOException {
    this.client.write(message);
  }

  public String read() throws IOException {
    return this.client.read();
  }

  public boolean isClosed() {
    return this.client == null || this.client.isClosed();
  }

  public boolean socketError() {
    return socketError;
  }
  public String lastError() {
    return lastSocketError;
  }

  public void close() throws IOException {
    this.client.close();
  }
}
