package Client;
import CommonResources.Email;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class LoginController {

    @FXML
    private Button loginButton;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Socket socket;
    private MailClientController controller;
    private int numLette = 0;
    private int dynamic_port = 0;


    public void setStream(Socket socket, ObjectInputStream in, ObjectOutputStream out) {
        this.socket = socket;
        this.in = in;
        this.out = out;
    }

    @FXML
    private void initialize() throws IOException {
        loginButton.setOnAction(event -> {
            handleLogin();
        });
    }

    @FXML
    private TextField emailTextField;

    @FXML
    private void handleLogin() {
        String selectedUser = emailTextField.getText().trim().toLowerCase();

        if (selectedUser.isEmpty()) {
            showAlert("Errore", "Per favore, inserisci un'email.");
        } else if (!isValidEmail(selectedUser)) {
            showAlert("Errore", "Per favore, inserisci un'email valida.");
        } else {

            try {
                boolean isServerClosed = socket.isClosed();
                if (isServerClosed) {
                    Platform.runLater(() -> showAlert("Errore", "Impossibile connettersi al server. Server disconnesso."));
                    return;
                }
                login(selectedUser); // Tenta il login
            } catch (ClassNotFoundException e) {
                showAlert("Errore", "Errore durante il login. Riprova.");
            } catch (IOException e) {
                showAlert("Errore", "Errore di comunicazione con il server. Controlla la connessione e riprova.");
            }
        }
    }

    public void login(String selectedUser) throws IOException, ClassNotFoundException {

        if (selectedUser == null || selectedUser.isEmpty()) {
            Platform.runLater(() -> showAlert("Errore", "Inserisci un'email valida."));
        }

        synchronized (out) {
            out.writeUTF(selectedUser); // Invia l'email inserita dall'utente
            out.flush();
        }

        String segnale = in.readUTF();
        Object receivedObject = in.readObject(); // Legge la risposta dal server
        numLette = in.readInt();
        dynamic_port = in.readInt();
        System.out.println(dynamic_port);

        if (receivedObject == null) {
            // Caso: il server non ha inviato nulla
            Platform.runLater(() -> showAlert("Errore", "Dati non validi ricevuti dal server. Riprova."));
        } else if (segnale.equals("RIFIUTATO")) {
            showAlert("Errore", "Mail non registrata nel nostro sistema.");
            closeSocket(socket, in, out);
            System.exit(0);
        } else {
            if (receivedObject instanceof ArrayList) {
                ArrayList<Email> emails = (ArrayList<Email>) receivedObject;

                Platform.runLater(() -> openUserWindow(selectedUser, new Stage(), emails)); // Apri la finestra utente
                startEmailPolling(); // Avvia il polling delle email

            } else {
                // Risposta non valida: messaggio di errore senza chiusura
                showAlert("Errore", "Dati non validi ricevuti dal server. Riprova.");
                closeSocket(socket, in, out);
                System.exit(0);
            }

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.close();

            closeSocket(socket, in, out);
        }
    }

    private void openUserWindow(String currentUserEmail, Stage primaryStage, ArrayList<Email> emails) {
        try {
            Parent root;
            // Carica la finestra principale da FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
            root = loader.load();

            // Ottieni il controller FXML
            controller = loader.getController();
            controller.initModel(currentUserEmail, numLette);
            controller.setEmails(FXCollections.observableArrayList(emails));

            // Crea una nuova scena e mostra la finestra
            Scene scene = new Scene(root);
            Stage newStage = new Stage();
            newStage.setScene(scene);
            newStage.setTitle("Mail Client - " + currentUserEmail);
            newStage.show();

            // Gestione chiusura finestra

            primaryStage.close();

            newStage.setOnCloseRequest(event -> {
                Platform.exit();
                System.exit(0);
            });


        } catch (IOException e) {
            showAlert("Errore", "Errore durante il caricamento della finestra.");
        }
    }


    private void startEmailPolling() {
        new Thread(() -> {
            while (true) {
                try {
                    Socket syncSocket = new Socket("127.0.0.1", dynamic_port);

                    Thread.sleep(5000); // Controlla ogni 5 secondi
                    ArrayList<Email> newEmails = requestUpdatedEmails(syncSocket);
                    Platform.runLater(() -> {

                        // Aggiorna l'elenco delle email solo se il server è connesso
                        if (!syncSocket.isClosed()) {
                            this.controller.updateEmailView(newEmails);
                        }

                    });
                } catch (InterruptedException e) {
                    showAlert("Errore", "Errore nell'aggiornamento server");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private ArrayList<Email> requestUpdatedEmails(Socket syncSocket) {

        try {
            ObjectOutputStream out_sync = new ObjectOutputStream(syncSocket.getOutputStream());
            ObjectInputStream in_sync = new ObjectInputStream(syncSocket.getInputStream());

            synchronized (out_sync) {
                out_sync.writeUTF("SYNC");
                out_sync.flush();
                Object receivedObject = in_sync.readObject();
                if (receivedObject instanceof ArrayList) {
                    return (ArrayList<Email>) receivedObject;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            showAlert("Errore", "Errore nella sincronizzazione");
            System.exit(0);
        }
        return new ArrayList<>();
    }
//

    void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void closeSocket(Socket socket, ObjectInputStream in, ObjectOutputStream out) throws IOException {
        socket.close();
        in.close();
        out.close();
    }

    public static boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

}