package org.timecrafters.timecraftersactionconfigurator.server;

import android.util.Log;

import org.timecrafters.timecraftersactionconfigurator.MainActivity;
import org.timecrafters.timecraftersactionconfigurator.jsonhandler.Reader;
import org.timecrafters.timecraftersactionconfigurator.jsonhandler.Writer;
import org.timecrafters.timecraftersactionconfigurator.support.AppSync;

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
  private long lastHeartBeatSent = 0;
  private long heartBeatInterval = 3_000;

  private String TAG = "TACNET|Connection";

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
          client.setSocket(new Socket());
          Log.i(TAG, "Connected to: " + hostname + ":" + port);

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

          Log.e(TAG, e.toString());
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
          Log.i(TAG, "Got valid json: " + message);
           Writer.overwriteConfigFile(message);

          AppSync.getMainActivity().runOnUiThread(new Runnable() {
             @Override
             public void run() {
               AppSync.getMainActivity().reloadConfig();
             }
           });
        }
      }

      if (System.currentTimeMillis() > lastHeartBeatSent + heartBeatInterval) {
        lastHeartBeatSent = System.currentTimeMillis();

        client.puts(Client.PROTOCOL_HEARTBEAT);
      }

      try {
        Thread.sleep(syncInterval);
      } catch (InterruptedException e) {
        // Failed to sleep I suppose.
      }

    } else {
      client = null;
    }
  }

  public void puts(String message) {
    this.client.puts(message);
  }

  public String gets() {
    return this.client.gets();
  }

  public Client getClient() {
    return client;
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
