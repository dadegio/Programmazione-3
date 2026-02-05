package Client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.*;
import java.net.Socket;

public class MailClientApp extends Application {

    private Socket socket_accesso;
    private ObjectOutputStream out_accesso;
    private ObjectInputStream in_accesso;


    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client/fxml/LoginView.fxml"));
            Parent root = loader.load();

            try {
                socket_accesso = new Socket("127.0.0.1", 12345);
                out_accesso = new ObjectOutputStream(socket_accesso.getOutputStream());
                in_accesso = new ObjectInputStream(socket_accesso.getInputStream());
            } catch (IOException e) {
                showAlert("Errore", "Server momentaneamente non funzionanti, riprova più tardi");
                closeSocket(socket_accesso, in_accesso, out_accesso);
                System.exit(0);
            }

            LoginController controller = loader.getController();
            controller.setStream(socket_accesso, in_accesso, out_accesso); // Passa l'istanza principale dell'app al controller

            Scene loginScene = new Scene(root);
            primaryStage.setScene(loginScene);
            primaryStage.setTitle("Accesso - Mail Client");
            primaryStage.show();

            primaryStage.setOnCloseRequest(event -> {
                try {
                    closeSocket(socket_accesso, in_accesso, out_accesso);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Termina il processo
                Platform.exit();
                System.exit(0);
            });


            new Thread().start();

        } catch (IOException e) {
            showAlert("Errore", "Errore durante il caricamento della finestra.");
            closeSocket(socket_accesso, in_accesso, out_accesso);
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

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

}
