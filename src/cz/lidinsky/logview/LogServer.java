/*
 * Copyright (C) 2016 jilm
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.lidinsky.logview;

import cz.lidinsky.tools.text.StrBuffer2;
import cz.lidinsky.tools.text.StrBufferReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jilm
 */
public class LogServer implements Runnable {

  private final Consumer<StrBuffer2> consumer;

  public LogServer(Consumer<StrBuffer2> consumer) {
    this.consumer = consumer;
  }


  public static void main(String[] args) throws Exception {
    LogServer instance = new LogServer(new Consumer<StrBuffer2>() {

      @Override
      public void accept(StrBuffer2 record) {
        System.out.println(record.toString());
      }

    });
    Thread serverThread = new Thread(instance);
    serverThread.start();

//    Logger logger = Logger.getLogger("cz.lidinsky");
//    SocketHandler handler = new SocketHandler("localhost", 12347);
//    handler.setFormatter(new LogFormatter());
//    logger.addHandler(handler);

    serverThread.join();

  }

  @Override
  public void run() {
    try {
//      consumer.accept(
//              new LogRecord(
//                      Level.INFO, "Starting LogView server thread"));
      ServerSocket server = new ServerSocket(12347);
      while (true) {
        Socket socket = server.accept();
//        consumer.accept(new LogRecord(Level.INFO, "Log client accepted."));
        InputStream is = socket.getInputStream();
        Reader reader = new InputStreamReader(is);
        StrBufferReader bufferReader = new StrBufferReader(reader);
        Client client = new Client(bufferReader);
        new Thread(client).start();
      }
    } catch (IOException ex) {
      Logger.getLogger(LogServer.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private class Client implements Runnable {

    private final StrBufferReader reader;

    public Client(final StrBufferReader reader) {
      this.reader = reader;
    }

    @Override
    public void run() {
      try {
        while (true) {
          StrBuffer2 buffer = reader.readBuffer();
          consumer.accept(buffer);
        }
      } catch (IOException ex) {
        Logger.getLogger(LogServer.class.getName()).log(Level.SEVERE, null, ex);
      }
    }



  }

}
