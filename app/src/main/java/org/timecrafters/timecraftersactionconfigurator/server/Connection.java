package org.timecrafters.timecraftersactionconfigurator.server;

import android.support.design.widget.Snackbar;
import android.util.Log;

import org.timecrafters.timecraftersactionconfigurator.MainActivity;

import java.io.IOException;
import java.net.Socket;

public class Connection {
  private Client client;
  private String hostname;
  private int port;
  private String lastSocketError;
  private boolean socketError;

  public Connection(String hostname, int port) {
    this.hostname = hostname;
    this.port = port;
  }

  public void connect() {
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
        } catch (IOException e) {
          socketError = true;
          lastSocketError = e.getMessage();

          Log.e("TACNET", e.toString());
        }
      }
    }).start();
  }

  public void write(String message) throws IOException {
    this.client.write(message);
  }

  public String read() throws IOException {
    return this.client.read();
  }

  public boolean isBound() {
    return this.client.isBound();
  }

  public boolean isConnected() {
    return this.client.isConnected();
  }

  public boolean isClosed() {
    return this.client.isClosed();
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
