package Client;

import CommonResources.Email;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static Client.MailClientModel.openEmailComposer;

public class MailClientController {

    @FXML
    private Label serverStatusLabel;
    @FXML
    private VBox emailDetailsPane;
    @FXML
    private VBox welcomeScreen;
    @FXML
    private Label senderLabel;
    @FXML
    private Label recipientLabel;
    @FXML
    private Label subjectLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private TextArea bodyTextArea;
    @FXML
    private Button replyButton;
    @FXML
    private Button replyAllButton;
    @FXML
    private Button forwardButton;
    @FXML
    ListView<Email> inboxListView; // Collegato all'fx:id="inboxListView" in FXML
    @FXML
    private Button viewDetailsButton; // Pulsante "Visualizza Dettagli"
    @FXML
    private Button deleteButton; // Pulsante "Elimina"
    @FXML
    private Button newEmailButton; // Pulsante "Nuovo Messaggio"
    @FXML
    private VBox composeEmailPane;  // Nuovo VBox per la composizione
    @FXML
    private TextField fromField;
    @FXML
    private TextField toField;
    @FXML
    private TextField subjectField;
    @FXML
    private TextArea bodyField;

    private MailClientModel model; // Modello per la gestione delle funzionalità
    private String currentUser; // Utente corrente
    private ObservableList<Email> emailList = FXCollections.observableArrayList(); // Lista osservabile per la ListView
    private int lastEmailCount = 0; // Tiene traccia del numero di email attualmente visualizzate

    /**
     * Inizializzazione automatica dei controlli (chiamato da JavaFX dopo il caricamento del FXML).
     */
    @FXML
    public void initialize() {
        setEmails(emailList);
    }

    /**
     * Metodo per inizializzare il controller con dati esterni.
     *
     * @param currentUser Email dell'utente corrente
     */
    public void initModel(String currentUser, int numLette) throws IOException {
        this.currentUser = currentUser;

        Platform.runLater(()->showAlert("Benvenuto", "Hai " + numLette + " email non lette"));

        // Inizializza il modello
        model = new MailClientModel(inboxListView, serverStatusLabel);

        deleteButton.setOnAction(event -> {
            try {
                handleDeleteEmailAction();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        viewDetailsButton.setOnAction(event -> {
            handleViewDetailsAction();
        });

        newEmailButton.setOnAction(event -> {
            handleNewEmailAction();
        });

    }

    /**
     * Aggiorna la lista delle email visualizzata nella ListView.
     *
     * @param newEmails Nuova lista di email da visualizzare.
     */
    public void updateEmailList(ListView<Email> inboxListView, ArrayList<Email> newEmails) {

        lastEmailCount = inboxListView.getItems().size();

        // Verifica se ci sono nuove email rispetto all'ultimo aggiornamento
        if (newEmails.size() > lastEmailCount) {
            int newEmailCount = newEmails.size() - lastEmailCount;

            // Mostra un popup per informare l'utente delle nuove email
            showAlert("Nuove Email", "Hai ricevuto " + newEmailCount + " nuove email.");

            // Aggiorna il contatore per riflettere il nuovo stato
            lastEmailCount = newEmails.size();
        }

        // Aggiorna la lista visualizzata nella ListView
        inboxListView.getItems().clear();
        inboxListView.getItems().addAll(newEmails);

        inboxListView.refresh(); // Refresh to reflect changes
    }

    public void setEmails(ObservableList<Email> emails) {
        inboxListView.setItems(emails);
        InboxView inbox = new InboxView(inboxListView);
    }

    /**
     * INIZIO HANDLER PULSANTI
     */


    @FXML
    private void handleViewDetailsAction() {

        // Nascondi i pannelli di composizione e di benvenuto
        welcomeScreen.setVisible(false);
        welcomeScreen.setManaged(false);
        composeEmailPane.setVisible(false);
        composeEmailPane.setManaged(false);

        // Mostra i dettagli dell'email selezionata
        emailDetailsPane.setVisible(true);
        emailDetailsPane.setManaged(true);

        Email selectedEmail = inboxListView.getSelectionModel().getSelectedItem();

        if (selectedEmail != null) {

            try {

                MailClientModel.showEmailDetails(selectedEmail, currentUser, senderLabel, recipientLabel, subjectLabel, dateLabel, bodyTextArea, replyButton, forwardButton);

                // Mostra il pulsante "Reply All" solo se ci sono più destinatari
                if (selectedEmail.getDest().contains(",")) {
                    replyAllButton.setVisible(true);
                    replyAllButton.setManaged(true);
                } else {
                    replyAllButton.setVisible(false);
                    replyAllButton.setManaged(false);
                }

                inboxListView.refresh();

            } catch (Exception e) {
                showAlert("Errore", "Impossibile visualizzare i dettagli dell'email.");
            }
        } else {
            showAlert("Seleziona un'email", "Nessuna email selezionata per visualizzare i dettagli.");
        }

    }

    @FXML
    private void handleDeleteEmailAction() throws IOException {
        model.deleteSelectedEmail();
        lastEmailCount = inboxListView.getItems().size();
    }

    void updateEmailView(ArrayList<Email> newEmails) {
        lastEmailCount = newEmails.size();
        updateEmailList(inboxListView, newEmails);
        inboxListView.refresh();
    }

    @FXML
    private void handleCloseDetailsAction() {
        // Nascondi i dettagli dell'email
        emailDetailsPane.setVisible(false);
        emailDetailsPane.setManaged(false);

        // Torna alla schermata di benvenuto
        welcomeScreen.setVisible(true);
        welcomeScreen.setManaged(true);
    }

    // Mostra la finestra di composizione
    @FXML
    private void handleNewEmailAction() {
        // Nascondi tutti i pannelli prima di mostrare quello necessario
        welcomeScreen.setVisible(false);
        emailDetailsPane.setVisible(false);

        // Mostra il pannello per la composizione
        composeEmailPane.setVisible(true);
        composeEmailPane.setManaged(true);

        // Pre-imposta il campo "Da" con l'email dell'utente
        fromField.setText(currentUser);
    }

    // Chiudi la finestra di composizione
    @FXML
    private void handleCloseComposeAction() {
        // Nascondi la finestra di composizione
        composeEmailPane.setVisible(false);
        composeEmailPane.setManaged(false);

        // Torna alla schermata di benvenuto
        welcomeScreen.setVisible(true);
        welcomeScreen.setManaged(true);
    }


    // Invia la nuova email
    @FXML
    private void handleSendEmailAction() {
        String dests = toField.getText();
        String subject = subjectField.getText();
        String body = bodyField.getText();

        openEmailComposer(fromField.getText(), dests, subject, "", body);

        fromField.clear();
        toField.clear();
        subjectField.clear();
        bodyField.clear();

        handleCloseComposeAction();
    }

    @FXML
    private void handleReply() {
        // Ottieni l'email selezionata
        Email selectedEmail = inboxListView.getSelectionModel().getSelectedItem();

        if (selectedEmail != null) {
            // Nascondi la schermata dei dettagli dell'email
            emailDetailsPane.setVisible(false);
            emailDetailsPane.setManaged(false);

            // Mostra la finestra di composizione dell'email
            composeEmailPane.setVisible(true);
            composeEmailPane.setManaged(true);

            // Pre-imposta il campo "Da" con l'email dell'utente
            fromField.setText(currentUser);

            // Pre-imposta il campo "A" con il mittente dell'email selezionata
            toField.setText(selectedEmail.getMittEmail());

            // Pre-imposta l'oggetto con "Re: " seguito dall'oggetto dell'email
            subjectField.setText("Re: " + selectedEmail.getArgEmail());
        }
    }

    @FXML
    private void handleForward() {
        // Ottieni l'email selezionata
        Email selectedEmail = inboxListView.getSelectionModel().getSelectedItem();

        if (selectedEmail != null) {
            // Nascondi la schermata dei dettagli dell'email
            emailDetailsPane.setVisible(false);
            emailDetailsPane.setManaged(false);

            // Mostra la finestra di composizione dell'email
            composeEmailPane.setVisible(true);
            composeEmailPane.setManaged(true);

            // Pre-imposta il campo "Da" con l'email dell'utente
            fromField.setText(currentUser);

            // Pre-imposta l'oggetto con "Fwd: " seguito dall'oggetto dell'email
            subjectField.setText("Fwd: " + selectedEmail.getArgEmail());
        }
    }

    @FXML
    private void handleReplyAll() {
        // Ottieni l'email selezionata
        Email selectedEmail = inboxListView.getSelectionModel().getSelectedItem();

        if (selectedEmail != null) {
            // Nascondi la schermata dei dettagli dell'email
            emailDetailsPane.setVisible(false);
            emailDetailsPane.setManaged(false);

            // Mostra la finestra di composizione dell'email
            composeEmailPane.setVisible(true);
            composeEmailPane.setManaged(true);

            // Pre-imposta il campo "Da" con l'email dell'utente
            fromField.setText(currentUser);

            // Pre-imposta il campo "A" con il mittente dell'email selezionata + gli altri destinatari (escludendo l'utente corrente)
            String[] corretta = selectedEmail.getDest().split(", ");
            List<String> recipients = new ArrayList<>(Arrays.asList(corretta));
            recipients.remove(currentUser);
            toField.setText(selectedEmail.getMittEmail() + ", " + String.join(", ", recipients));

            // Pre-imposta l'oggetto con "Re to all: " seguito dall'oggetto dell'email
            subjectField.setText("Re to all: " + selectedEmail.getArgEmail());
        }
    }

    void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

}