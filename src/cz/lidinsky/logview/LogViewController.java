/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.lidinsky.logview;

import java.net.URL;
import java.time.Instant;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 *
 * @author jilm
 */
public class LogViewController implements Initializable {

  public static final Logger logger = Logger.getLogger("cz.lidinsky");
  public static final Logger logger2 = Logger.getLogger("cz.control4j");

  @FXML private TableView<LogRecord> logTable;
  @FXML private TableColumn<LogRecord, Instant> logTableTimestampCol;
  @FXML private TableColumn<LogRecord, String> logTableNameCol;
  @FXML private TableColumn<LogRecord, Level> logTableLevelCol;
  @FXML private TableColumn<LogRecord, String> logTableMessageCol;

  private ObservableList<LogRecord> logData;

  @Override
  public void initialize(URL url, ResourceBundle rb) {

    // log timestamp
    logTableTimestampCol.setCellValueFactory(
            param -> new SimpleObjectProperty(
                    Instant.ofEpochMilli(param.getValue().getMillis()))
    );

    // log name
    logTableNameCol.setCellValueFactory(
            param -> new SimpleStringProperty(param.getValue().getLoggerName())
    );

    // log message
    logTableMessageCol.setCellValueFactory(
            param -> new SimpleStringProperty(
              param.getValue().getMessage())
    );

    // log level
    logTableLevelCol.setCellValueFactory(
            param -> new SimpleObjectProperty(param.getValue().getLevel())
    );

    logData = FXCollections.observableArrayList();
    logTable.setItems(logData);
    Handler logHandler = new Handler() {
      @Override
      public void publish(LogRecord record) {
        Platform.runLater(() -> logData.add(record));
      }

      @Override
      public void flush() {
      }

      @Override
      public void close() throws SecurityException {
      }
    };

    LogServer server = new LogServer(
      new Consumer<LogRecord>() {

        @Override
        public void accept(LogRecord record) {
          logData.add(record);
        }

      });
    Thread serverThread = new Thread(server);
    serverThread.start();


    // TODO
    //logger.addHandler(logHandler);
    //Logger.getLogger("cz.control4j").addHandler(logHandler);
    //logData.add(new LogRecord(Level.INFO, "Starting management console."));
    //Logger.getLogger("cz.lidinsky").info("Starting the managemen console");



  }

}
