package Client;

import CommonResources.Email;
import javafx.application.Platform;
import javafx.scene.control.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MailClientModel {

    static ListView<Email> inboxListView;
    private static Label serverStatusLabel;


    public MailClientModel(ListView<Email> inboxListView, Label serverStatusLabel) {
        MailClientModel.inboxListView = inboxListView;
        this.serverStatusLabel = serverStatusLabel;
    }

    public void deleteSelectedEmail() {

        try {
            Socket deleteSocket = new Socket("127.0.0.1",12346);
            ObjectOutputStream out_delete = new ObjectOutputStream(deleteSocket.getOutputStream());
            ObjectInputStream in_delete = new ObjectInputStream(deleteSocket.getInputStream());

            updateServerStatus(deleteSocket, serverStatusLabel);


            Email selectedEmail = inboxListView.getSelectionModel().getSelectedItem();
            if (selectedEmail != null) {

                // Alert di conferma
                Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationAlert.setTitle("Conferma eliminazione");
                confirmationAlert.setHeaderText("Sei sicuro di voler eliminare questo messaggio?");
                confirmationAlert.setContentText("Questa azione è irreversibile.");

                confirmationAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {

                        if (!deleteSocket.isClosed()) {
                            try {
                                out_delete.writeObject(selectedEmail); //email da cancellare
                                out_delete.flush();
                                String serverResponse = in_delete.readUTF();
                                System.out.println(serverResponse);

                                if ("OK".equals(serverResponse)) {
                                    showAlert("Messaggio eliminato", "Il messaggio è stato eliminato.");
                                    // Rimuove l'email dall'interfaccia utente
                                    inboxListView.getItems().remove(selectedEmail);

                                } else {
                                    showAlert("Errore", "Impossibile eliminare il messaggio.");
                                }


                            } catch (IOException e) {
                                showAlert("Errore di comunicazione", "C'è stato un problema nel comunicare con il server.");
                            }
                        } else {
                            showAlert("Connessione persa", "La connessione al server è stata interrotta.");
                        }
                    }
                });
            } else {
                showAlert("Nessuna email selezionata", "Seleziona un'email da eliminare.");
            }

            closeSocket(deleteSocket, in_delete, out_delete);
            updateServerStatus(deleteSocket, serverStatusLabel);

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void openEmailComposer(String mitt, String dests, String oggetto, String data, String corpo) {

        try {
            Socket sendSocket = new Socket("127.0.0.1", 12347);
            ObjectOutputStream out_send = new ObjectOutputStream(sendSocket.getOutputStream());
            ObjectInputStream in_send = new ObjectInputStream(sendSocket.getInputStream());

            updateServerStatus(sendSocket, serverStatusLabel);

            Date dataSpedEMail = new Date(); // Crea una nuova data corrente
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            data = dateFormat.format(dataSpedEMail); // Formattare la data per visualizzazione (se necessario)

            Email newEmail = new Email();

            try {
                if (!validateEmails(dests)){
                    showAlert("Errore", "L'indirizzo email del destinatario non esiste");
                } else {
                    newEmail.setMittEmail(mitt);
                    newEmail.setDestsEmail(List.of(dests.split(",")));
                    newEmail.setArgEmail(oggetto);
                    newEmail.setDataSpedEmail(data);
                    newEmail.setTestoEmail(corpo);
                    newEmail.setDest(dests);

                    System.out.println(newEmail);

                    out_send.writeObject(newEmail);
                    out_send.flush();

                    String serverResponse = in_send.readUTF();
                    System.out.println(serverResponse);
                    if (serverResponse.equals("OK")) {
                        showAlert("Invio", "L'email è stata inviata correttamente");
                    }else{
                        showAlert("Invio", "Errore! Destinatario non esiste");

                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            closeSocket(sendSocket, in_send, out_send);
            updateServerStatus(sendSocket, serverStatusLabel);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void showEmailDetails(Email selectedEmail, String currentUser, Label senderLabel, Label recipientLabel, Label subjectLabel, Label dateLabel, TextArea bodyTextArea, Button replyButton, Button forwardButton) throws IOException {

        try {

            Socket showSocket = new Socket("127.0.0.1", 12349);
            ObjectOutputStream out_show = new ObjectOutputStream(showSocket.getOutputStream());

            updateServerStatus(showSocket, serverStatusLabel);

            if (selectedEmail != null) {
                // Aggiorna lo stato dell'email come letta
                if (!selectedEmail.getIsRead()) {
                    out_show.writeObject(selectedEmail);
                    out_show.flush();
                }

                // Aggiorna i dettagli nel pannello
                senderLabel.setText("Mittente: " + selectedEmail.getMittEmail());
                recipientLabel.setText("Destinatari: " + selectedEmail.getDest());
                subjectLabel.setText("Oggetto: " + selectedEmail.getArgEmail());
                dateLabel.setText("Data di invio: " + selectedEmail.getDataSpedEmail());
                bodyTextArea.setText(selectedEmail.getTestoEmail());

            } else {
                // Se non ci sono email selezionate
                throw new IllegalArgumentException("Nessuna email selezionata.");
            }

            showSocket.close();
            out_show.close();
            updateServerStatus(showSocket, serverStatusLabel);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static void updateServerStatus(Socket socket_in_uso, Label serverStatusLabel) {
        Platform.runLater(() -> {

            if (socket_in_uso != null && !socket_in_uso.isClosed() && socket_in_uso.isConnected()) {
                MailClientModel.serverStatusLabel.setText("Connesso");
                MailClientModel.serverStatusLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #4CAF50; -fx-font-weight: bold;");
            } else {
                MailClientModel.serverStatusLabel.setText("Disconnesso");
                MailClientModel.serverStatusLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #FF5252; -fx-font-weight: bold;");
            }

        });
    }

    static boolean validateEmails(String emails) {
        String[] emailArray = emails.split(",");
        for (String email : emailArray) {
            if (!isValidEmail(email.trim())) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void closeSocket(Socket socket, ObjectInputStream in, ObjectOutputStream out) throws IOException {
        socket.close();
        in.close();
        out.close();
    }
}

/*
 * @TODO
 *   da modificare notifiche per errori
 *   modifica rispondi, inoltra, rispondi a tutti -> non devi selezionare una email
 * */