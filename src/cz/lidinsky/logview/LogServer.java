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
import cz.lidinsky.tools.text.Table;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 *
 * @author jilm
 */
public class LogServer implements Runnable {

  private final Consumer<LogRecord> consumer;

  public LogServer(Consumer<LogRecord> consumer) {
    this.consumer = consumer;
  }


  public static void main(String[] args) throws Exception {
    LogServer instance = new LogServer(new Consumer<LogRecord>() {

      @Override
      public void accept(LogRecord record) {
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
      ServerSocket server = new ServerSocket(12347);
      Socket socket = server.accept();
      InputStream is = socket.getInputStream();
      Reader reader = new InputStreamReader(is);
      StrBufferReader bufferReader = new StrBufferReader(reader);
      while (true) {
        StrBuffer2 buffer = bufferReader.readBuffer();
        System.out.println(buffer.getBuffer());
        Table table = new Table(buffer);
        System.out.println(table.get("name") + " " + table.get("message"));
        LogRecord record = new LogRecord(Level.INFO, table.get("message"));
        consumer.accept(record);
      }
    } catch (IOException ex) {
      Logger.getLogger(LogServer.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

}
