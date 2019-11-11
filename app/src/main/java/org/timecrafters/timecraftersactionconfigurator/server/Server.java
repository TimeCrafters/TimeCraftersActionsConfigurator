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

  private String TAG = "TACNET|Server";

  private int packetsSent, packetsReceived, clientLastPacketsSent, clientLastPacketsReceived = 0;
  private long dataSent, dataReceived, clientLastDataSent, clientLastDataReceived = 0;

  private Runnable handleClientRunner;

  private long lastHeartBeatSent = 0;
  private long heartBeatInterval = 3_000;

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
            Log.i(TAG, "Server bound and ready!");
          } catch (IOException e) {
            connectionAttempts++;
            Log.e(TAG, "Server failed to bind: " + e.getMessage());
          }
        }

        while (!server.isClosed()) {
          try {
            runServer();
          } catch (IOException e) {
            Log.e(TAG, "Error running server: " + e.getMessage());
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
        Log.i(TAG, "Too many clients, already have one connected!");
        client.close("Too many clients!");

      } else {
        Writer.writeJSON(Writer.getBackupConfigFilePath(), AppSync.getDataStructs());

        this.activeClient = client;
        AppSync.getMainActivity().clientConnected();

        activeClient.puts(activeClient.uuid());
        activeClient.puts(Reader.rawConfigFile());

        Log.i(TAG, "Client connected!");

        new Thread(new Runnable() {
          @Override
          public void run() {
            while(activeClient != null && !activeClient.isClosed()) {
              if (System.currentTimeMillis() > lastSyncTime + syncInterval) {
                lastSyncTime = System.currentTimeMillis();

                activeClient.sync(handleClientRunner);
                updateNetStats();
              }

              try {
                Thread.sleep(syncInterval);
              } catch (InterruptedException e) {
                // Failed to sleep, i guess.
              }
            }

            updateNetStats();
            activeClient = null;

            clientLastPacketsSent = 0;
            clientLastPacketsReceived = 0;
            clientLastDataSent = 0;
            clientLastDataReceived = 0;

            AppSync.getMainActivity().clientDisconnected();
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
          Log.i(TAG, "Got valid json: " + message);
          Writer.overwriteConfigFile(message);
        }
      }

      if (System.currentTimeMillis() > lastHeartBeatSent + heartBeatInterval) {
        lastHeartBeatSent = System.currentTimeMillis();

        activeClient.puts(Client.PROTOCOL_HEARTBEAT);
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

  public boolean hasActiveClient() {
    return activeClient != null;
  }

  public Client getActiveClient() {
    return activeClient;
  }

  public int getPacketsSent() {
    return packetsSent;
  }

  public int getPacketsReceived() {
    return packetsReceived;
  }

  public long getDataSent() {
    return dataSent;
  }

  public long getDataReceived() {
    return dataReceived;
  }

  private void updateNetStats() {
    if (activeClient != null) {
      // NOTE: In and Out are reversed for Server stats

      packetsSent += activeClient.getPacketsReceived() - clientLastPacketsReceived;
      packetsReceived += activeClient.getPacketsSent() - clientLastPacketsSent;

      dataSent += activeClient.getDataReceived() - clientLastDataReceived;
      dataReceived += activeClient.getDataSent() - clientLastDataSent;

      clientLastPacketsSent = activeClient.getPacketsSent();
      clientLastPacketsReceived = activeClient.getPacketsReceived();
      clientLastDataSent = activeClient.getDataSent();
      clientLastDataReceived = activeClient.getDataReceived();
    }
  }

  public boolean isBound() {
    return this.server.isBound();
  }

  public boolean isClosed() {
    return this.server.isClosed();
  }
}
