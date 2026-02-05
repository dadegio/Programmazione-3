import CommonResources.Email;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerEmailApp extends Application {

    private static final int PORT = 12345;
    private ExecutorService threadPool;
    private ServerSocket serverSocket;
    private ServerSocket deleteServerSocket;
    private ServerSocket sendServerSocket;
    private ServerSocket showServerSocket;
    private ServerEmailModel emailModel;
    private ServerEmailController controller;
    private ObservableList<String> logList;

    @Override
    public void start(Stage primaryStage) {
        try {

            emailModel = new ServerEmailModel("src/email.csv");
            logList = emailModel.getLogList();


            ServerEmailView emailView = new ServerEmailView(emailModel);

            // Layout dell'interfaccia utente
            BorderPane root = new BorderPane();
            root.setCenter(emailView);
            Scene scene = new Scene(root, 1000, 400);
            primaryStage.setTitle("Server");
            primaryStage.setScene(scene);
            primaryStage.show();

            // Avvio del server
            startServer();

            // Gestione della chiusura della finestra
            primaryStage.setOnCloseRequest(event -> stopServer());
        } catch (Exception e) {
            System.err.println("Errore nell'avvio del server: " + e.getMessage());
        }
    }

    private void startServer() {
        threadPool = Executors.newFixedThreadPool(20);

        threadPool.submit(() -> {
            try {
                // Specifica l'IP e la porta
                InetAddress bindAddress = InetAddress.getByName("0.0.0.0"); // Sostituisci con l'IP desiderato
                serverSocket = new ServerSocket(PORT, 50, bindAddress);
                emailModel.addLog("Server in ascolto su IP: " + bindAddress + " e porta: " + PORT);

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    controller = new ServerEmailController(emailModel, logList);
                    threadPool.submit(() -> controller.handleClient(clientSocket, threadPool));
                }
            } catch (IOException e) {
                emailModel.addLog("Errore nella comunicazione con il server: " + e.getMessage());
            }
        });

        threadPool.submit(() -> {
            try {
                // Specifica l'IP e la porta
                InetAddress bindAddress = InetAddress.getByName("0.0.0.0"); // Sostituisci con l'IP desiderato
                deleteServerSocket = new ServerSocket(12346, 50, bindAddress);

                while (true) {
                    Socket deleteSocket = deleteServerSocket.accept(); // entri solo se accetta
                    controller = new ServerEmailController(emailModel, logList);
                    threadPool.submit(() -> controller.handleDelete(deleteSocket));
                }
            } catch (IOException e) {
                emailModel.addLog("Errore nell'eliminazione: " + e.getMessage());
            }
        });

        threadPool.submit(() -> {
            try {
                // Specifica l'IP e la porta
                InetAddress bindAddress = InetAddress.getByName("0.0.0.0"); // Sostituisci con l'IP desiderato
                sendServerSocket = new ServerSocket(12347, 50, bindAddress);

                while (true) {
                    Socket sendSocket = sendServerSocket.accept(); // entri solo se accetta
                    controller = new ServerEmailController(emailModel, logList);
                    threadPool.submit(() -> controller.handleSend(sendSocket));
                }
            } catch (IOException e) {
                emailModel.addLog("Errore nell'invio: " + e.getMessage());
            }
        });

        threadPool.submit(() -> {
            try {
                // Specifica l'IP e la porta
                InetAddress bindAddress = InetAddress.getByName("0.0.0.0"); // Sostituisci con l'IP desiderato
                showServerSocket = new ServerSocket(12349, 50, bindAddress);

                while (true) {
                    Socket showSocket = showServerSocket.accept(); // entri solo se accetta
                    controller = new ServerEmailController(emailModel, logList);
                    threadPool.submit(() -> controller.handleShow(showSocket));
                }
            } catch (IOException e) {
                emailModel.addLog("Errore nella visualizzazione: " + e.getMessage());
            }
        });
    }

    private void stopServer() {
        try {
            if (threadPool != null) {
                threadPool.shutdown();
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            emailModel.addLog("Server arrestato correttamente");
        } catch (IOException e) {
            emailModel.addLog("Errore durante l'arresto del server");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}