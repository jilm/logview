/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.lidinsky.logview;

import cz.lidinsky.tools.text.StrBuffer2;
import cz.lidinsky.tools.text.Table;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.logging.Logger;
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
  @FXML private TableColumn<LogRecord, String> logTableTimestampCol;
  @FXML private TableColumn<LogRecord, String> logTableNameCol;
  @FXML private TableColumn<LogRecord, String> logTableLevelCol;
  @FXML private TableColumn<LogRecord, String> logTableMessageCol;

  private ObservableList<LogRecord> logData;

  @Override
  public void initialize(URL url, ResourceBundle rb) {

    // log timestamp
    logTableTimestampCol.setCellValueFactory(
            param -> new SimpleStringProperty(param.getValue().timestamp)
    );

    // log name
    logTableNameCol.setCellValueFactory(
            param -> new SimpleStringProperty(param.getValue().name)
    );

    // log message
    logTableMessageCol.setCellValueFactory(
            param -> new SimpleStringProperty(param.getValue().message)
    );

    // log level
    logTableLevelCol.setCellValueFactory(
            param -> new SimpleObjectProperty(param.getValue().level)
    );

    logData = FXCollections.observableArrayList();
    logTable.setItems(logData);
//    Handler logHandler = new Handler() {
//      @Override
//      public void publish(LogRecord record) {
//        Platform.runLater(() -> logData.add(record));
//      }
//
//      @Override
//      public void flush() {
//      }
//
//      @Override
//      public void close() throws SecurityException {
//      }
//    };

    LogServer server = new LogServer(12347,
      new Consumer<StrBuffer2>() {

        @Override
        public void accept(StrBuffer2 record) {
          LogRecord logRecord = new LogRecord();
          Table table = new Table(record);
          logRecord.name = table.get("name");
          logRecord.level = table.get("level");
          logRecord.message = table.get("message");
          logRecord.timestamp = table.get("timestamp");
          logData.add(logRecord);
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

  private static class LogRecord {
    String name;
    String level;
    String timestamp;
    String message;
  }

}
