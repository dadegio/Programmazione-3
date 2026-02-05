package Client;

import CommonResources.Email;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


import java.util.Objects;

public class InboxView {

    public InboxView(ListView<Email> inboxListView) {

        inboxListView.setCellFactory(param -> new ListCell<>() {

            @Override
            protected void updateItem(Email email, boolean empty) {
                super.updateItem(email, empty);
                // Se la cella è vuota o l'email è nulla, non fare nulla
                if (empty || email == null) {
                    setText(null);
                    setGraphic(null); // Rimuovi l'icona quando la cella è vuota
                } else {
                    // Contenitore principale per la cella
                    VBox cellContainer = new VBox();
                    cellContainer.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-border-color: #333333; -fx-border-width: 0 0 1px 0;"); // Riga scura come separatore

                    // Icona dell'email
                    ImageView emailIcon = new ImageView();
                    emailIcon.setFitWidth(20);
                    emailIcon.setFitHeight(20);
                    emailIcon.setPreserveRatio(true); // Mantiene il rapporto d'aspetto
                    if (email.getIsRead()) {
                        emailIcon.setImage(new Image(Objects.requireNonNull(getClass().getResource("/Client/images/isNotRead.png")).toExternalForm()));
                    } else {
                        emailIcon.setImage(new Image(Objects.requireNonNull(getClass().getResource("/Client/images/isReadFinale.png")).toExternalForm()));
                    }

                    // Dettagli dell'email
                    Label subjectLabel = new Label("Oggetto: " + email.getArgEmail());
                    subjectLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: black;");
                    Label senderLabel = new Label("Da: " + email.getMittEmail());
                    senderLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");

                    VBox textContainer = new VBox(subjectLabel, senderLabel);
                    textContainer.setStyle("-fx-background-color: transparent");
                    textContainer.setSpacing(5); // Spaziatura tra le righe di testo

                    // Layout combinato: icona e testo centrati
                    HBox rowLayout = new HBox(emailIcon, textContainer);
                    rowLayout.setAlignment(javafx.geometry.Pos.CENTER_LEFT); // Allinea tutto a sinistra, ma centrato verticalmente
                    rowLayout.setSpacing(10); // Spaziatura tra l'icona e il testo

                    // Aggiungi il layout al contenitore principale
                    cellContainer.getChildren().add(rowLayout);

                    setGraphic(cellContainer);

                    // Quando la cella è selezionata
                    if (isSelected()) {
                        // Cambia solo lo sfondo e il colore del testo quando la cella è selezionata
                        setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #000000;"); // Imposta il testo nero
                    } else {
                        setStyle("-fx-background-color: white; -fx-text-fill: #333333;"); // Imposta il testo scuro quando non selezionata
                    }
                }
            }

        });
    }
}