package org.timecrafters.timecraftersactionconfigurator.server;

import android.util.Log;

import org.timecrafters.timecraftersactionconfigurator.MainActivity;
import org.timecrafters.timecraftersactionconfigurator.jsonhandler.Reader;
import org.timecrafters.timecraftersactionconfigurator.jsonhandler.Writer;
import org.timecrafters.timecraftersactionconfigurator.support.AppSync;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class Server {
  private ServerSocket server;
  private int port;
  private Client activeClient;
  private long lastSyncTime = 0;
  private long syncInterval = 250;

  private Runnable handleClientRunner;

  public Server(int port) throws IOException {
    this.server = new ServerSocket();
    this.port = port;
    this.handleClientRunner = new Runnable() {
      @Override
      public void run() {
        handleClient();
      }
    };
  }

  public void start() throws IOException {
    new Thread(new Runnable() {
      @Override
      public void run() {
        int connectionAttempts = 0;

        while(!server.isBound() && connectionAttempts < 10) {
          try {
            server.bind(new InetSocketAddress(port));
            Log.i("TACNET", "Server bound and ready!");
          } catch (IOException e) {
            connectionAttempts++;
            Log.e("TACNET", "Server failed to bind: " + e.getMessage());
          }
        }

        while (!server.isClosed()) {
          try {
            runServer();
          } catch (IOException e) {
            Log.e("TACNET", "Error running server: " + e.getMessage());
          }

        }
      }
    }).start();
  }

  private void runServer() throws IOException {
    while (!isClosed()) {

      final Client client = new Client();
      client.setSyncInterval(syncInterval);
      client.setSocket(this.server.accept());

      if (activeClient != null && !activeClient.isClosed()) {
        Log.i("TACNET", "Too many clients, already have one connected!");
        client.close("Too many clients!");

      } else {
        Writer.writeJSON(Writer.getBackupConfigFilePath(), AppSync.getDataStructs());

        this.activeClient = client;
        activeClient.puts(activeClient.uuid());
        activeClient.puts(Reader.rawConfigFile());

        Log.i("TACNET", "Client connected!");

        new Thread(new Runnable() {
          @Override
          public void run() {
            while(activeClient != null && !activeClient.isClosed()) {
              if (System.currentTimeMillis() > lastSyncTime + syncInterval) {
                lastSyncTime = System.currentTimeMillis();

                activeClient.sync(handleClientRunner);
              }
            }
          }
        }).start();

      }
    }
  }

  private void handleClient() {
    if (activeClient != null && !activeClient.isClosed()) {
      String message = activeClient.gets();

      if (message != null) {
        if (
                message.length() > 4 && message.charAt(0) == "[".toCharArray()[0] &&
                        message.charAt(message.length() - 1) == "]".toCharArray()[0]
        ) {
          // write json to file
          Log.i("TACNET", "Got valid json: " + message);
          Writer.overwriteConfigFile(message);
        }
      }

      activeClient.puts("heartbeat");
    }
  }

  public void stop() throws IOException {
    if (this.activeClient != null) {
      this.activeClient.close();
      this.activeClient = null;
    }

    this.server.close();
  }

  public boolean isBound() {
    return this.server.isBound();
  }

  public boolean isClosed() {
    return this.server.isClosed();
  }
}
