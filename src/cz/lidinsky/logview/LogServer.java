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
 * Listen on specified port for incoming connections. A new thread is run for
 * each accepted peer.
 */
public class LogServer implements Runnable, AutoCloseable {

  /**
   * A function which is called whenever new log record is received.
   */
  private final Consumer<StrBuffer2> consumer;

  private final int port;

  private boolean stop;

  private int clients;

  /**
   * Configure new log server.
   *
   * @param port
   *            a port number to listen to
   *
   * @param consumer
   *            a function which is called whenever some client receives
   *            a log message
   */
  public LogServer(final int port, final Consumer<StrBuffer2> consumer) {
    this.consumer = consumer;
    this.port = port;
    this.stop = false;
    this.clients = 0;
  }


  public static void main(String[] args) throws Exception {
    LogServer instance = new LogServer(12347, new Consumer<StrBuffer2>() {

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
    try (
      ServerSocket server = new ServerSocket(port);
            ) {
      Logger.getLogger(LogServer.class.getName())
              .info(String.format(
                      "Log server has been successfuly created and listens on port %d.", port));
      while (!stop) {
        Socket socket = server.accept();
        Client client = new Client(socket.getInputStream());
        new Thread(client).start();
      }
    } catch (IOException ex) {
      Logger.getLogger(LogServer.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
      Logger.getLogger(LogServer.class.getName())
              .info(String.format(
                      "Log server is going down!", port));
    }
  }

  @Override
  public void close() throws Exception {
    this.stop = true;
      Logger.getLogger(LogServer.class.getName())
              .info(String.format(
                      "An order to stop the log server has been received!", port));
  }



  private class Client implements Runnable, AutoCloseable {

    private final InputStream is;

    public Client(final InputStream is) {
      this.is = is;
    }

    @Override
    public void run() {
      try (
        Reader reader = new InputStreamReader(is);
        StrBufferReader bufferReader = new StrBufferReader(reader);
              ) {
        Logger.getLogger(LogServer.class.getName())
                .info(String.format("New log peer client has been accepted, number of peers: %d", ++clients));
        while (!stop) {
          StrBuffer2 buffer = bufferReader.readBuffer();
          consumer.accept(buffer);
        }
      } catch (IOException ex) {
        Logger.getLogger(LogServer.class.getName()).log(Level.SEVERE, null, ex);
      } catch (Exception ex) {
        Logger.getLogger(LogServer.class.getName()).log(Level.SEVERE, null, ex);
      } finally {
        Logger.getLogger(LogServer.class.getName())
                .info(String.format("A client is stopped: %d", --clients));
      }
    }

    @Override
    public void close() throws Exception {
      this.is.close();
    }



  }

}
