package org.timecrafters.timecraftersactionconfigurator.server;

import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketException;

public class Server {
  private ServerSocket server;
  private int port;
  private Client activeClient;
  private long lastSyncTime = 0;
  private long syncInterval = 100;

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
      client.setSocket(this.server.accept());

      if (activeClient != null && !activeClient.isClosed()) {
        Log.i("TACNET", "Too many clients, already have one connected!");
        client.close("Too many clients!");

      } else {
        this.activeClient = client;
        activeClient.puts(activeClient.uuid());

        Log.i("TACNET", "Client connected!");

        new Thread(new Runnable() {
          @Override
          public void run() {
            while(activeClient != null && !activeClient.isClosed()) {
              syncActiveClient(handleClientRunner);
            }
          }
        }).start();

      }
    }
  }

  private void syncActiveClient(Runnable runner) {
    if (System.currentTimeMillis() > lastSyncTime + syncInterval) {
      lastSyncTime = System.currentTimeMillis();

      activeClient.sync(runner);
    }
  }

  private void handleClient() {
    if (activeClient != null && !activeClient.isClosed()) {
      String message = activeClient.gets();

      while(message != null) {
        activeClient.puts(message);

        message = activeClient.gets();
      }
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
