import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Observable;
import java.util.Observer;

public class ServerEmailView extends VBox implements Observer {
    private ServerEmailController serverEmailCtrl;
    private TextArea logTxtArea = new TextArea();

    public ServerEmailView(ServerEmailModel model) {
        // Registra questa vista come osservatore del modello
        model.addObserver(this);

        setPadding(new Insets(10));
        setSpacing(10);

        getChildren().add(logAreaPanel());
    }

    /**
     * Metodo che crea e restituisce pannello contenente il log
     */
    private VBox logAreaPanel() {

        VBox mainPanel = new VBox();
        mainPanel.setSpacing(10);
        mainPanel.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-padding: 10;");


        logTxtArea.setEditable(false);
        logTxtArea.setWrapText(true);
        logTxtArea.setFont(Font.font("Helvetica", 14));
        logTxtArea.setStyle("-fx-background-color: black; -fx-text-fill: green;");

        ScrollPane logScrollPane = new ScrollPane(logTxtArea);
        logScrollPane.setFitToWidth(true);
        logScrollPane.setPrefHeight(300);

        Button cleanButton = new Button("Pulisci log");
        cleanButton.setFont(Font.font("Helvetica", 14));
        cleanButton.setOnAction(event -> {
            logTxtArea.clear();
            serverEmailCtrl.createLog(logTxtArea.getText());
        });

        mainPanel.getChildren().addAll(new Text("LOG"), logScrollPane, cleanButton);
        return mainPanel;
    }

    /**
     * Metodo update, viene richiamato in seguito ad una modifica dell'elemento osservato notificata attraverso il metodo notifyObservers
     */

    @Override
    public void update(Observable o, Object arg) {

        if (arg instanceof ObservableList<?> logList) {
            // Converti la lista in una stringa per l'area di testo
            StringBuilder sb = new StringBuilder();
            for (Object log : logList) {
                sb.append(log.toString()).append("\n");
            }
            logTxtArea.setText(sb.toString());
        }

    }
}