package org.timecrafters.timecraftersactionconfigurator.server;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

public class Client {
  private Socket socket;
  private BufferedReader bufferedReader;
  private BufferedWriter bufferedWriter;
  private String uuid;

  private ArrayList<String> readQueue;
  private ArrayList<String> writeQueue;

  final private Object readQueueLock = new Object();
  final private Object writeQueueLock = new Object();

  public Client() {
    this.uuid = (UUID.randomUUID()).toString();

    this.readQueue = new ArrayList<>();
    this.writeQueue = new ArrayList<>();
  }

  public void setSocket(Socket socket) throws IOException {
    this.socket = socket;
    this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

    startReader();
    startWriter();
  }

  public ArrayList<String> readQueue() {
    return readQueue;
  }

  public ArrayList<String> writeQueue() {
    return writeQueue;
  }

  private void startReader() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        while(!socket.isClosed()) {
          // READER
          try {
            String message = read();
            if (!message.equals("")) {
              Log.i("TACNET", "Client read: " + message);

              synchronized (readQueueLock) {
                readQueue.add(message);
              }
            }

          } catch (IOException e) {
            Log.e("TACNET", "Client read error: " + e.getMessage());
          }
        }
      }
    }).start();
  }

  private void startWriter() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        while(!socket.isClosed()) {
          // WRITER
          String message;

          synchronized (writeQueueLock) {
            for (Iterator itr = writeQueue.iterator(); itr.hasNext(); ) {
              try {
                message = (String) itr.next();

                write(message);
                Log.i("TACNET", "Client write: " + message);
                itr.remove();

              } catch (IOException e) {
                Log.e("TACNET", "Client write error: " + e.getMessage());
                try {
                  socket.close();
                } catch (IOException k) {
                }
              }
            }
          }
        }
      }
    }).start();
  }

  public void sync(Runnable runner) {
    runner.run();
  }

  public void handleReadQueue() {
    String message = this.gets();

    while (message != null) {
      Log.i("TACNET", "Writing to Queue: " + message);
      this.puts(message);

      message = this.gets();

    }
  }

  public String uuid() {
    return this.uuid;
  }

  public boolean isConnected() {
    return !isClosed();
  }

  public boolean isBound() {
    return this.socket.isBound();
  }

  public boolean isClosed() {
    return this.socket.isClosed();
  }

  public void write(String message) throws IOException {
    bufferedWriter.write(message + "\r\n\n");
    bufferedWriter.flush();
  }

  public String read() throws IOException {
    String message = "";
    String readLine;

    while((readLine = bufferedReader.readLine()) != null) {
      message+=readLine;
      if (readLine.isEmpty()) { break; }
    }

    return message;
  }

  public void puts(String message) {
    synchronized (writeQueueLock) {
      writeQueue.add(message);
    }
  }

  public String gets() {
    String message = null;

    synchronized (readQueueLock) {
      if (readQueue.size() > 0) {
        message = readQueue.get(0);

        readQueue.remove(0);
      }
    }

    return message;
  }

  public String encode(String message) {
    return message;
  }

  public String decode(String blob) {
    return  blob;
  }

  public void flush() throws IOException {
    this.bufferedWriter.flush();
  }

  public void close(String reason) throws IOException {
    write(reason);
    this.socket.close();
  }

  public void close() throws IOException {
    this.socket.close();
  }
}