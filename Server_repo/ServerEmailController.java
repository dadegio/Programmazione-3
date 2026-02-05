import CommonResources.Email;
import javafx.collections.ObservableList;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;


public class ServerEmailController implements ActionListener {
    private final ServerEmailModel serverEmailMod;
    private final ObservableList<String> logList;
    private ServerSocket syncServerSocket;
    private ServerEmailController controller;
    private int dynamicPort = 0;


    public ServerEmailController(ServerEmailModel serverEmailMod, ObservableList<String> list) {
        this.serverEmailMod = serverEmailMod;
        this.logList = list;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        serverEmailMod.clearLog(logList);
    }

    public void createLog(String textLog) {
        try {
            serverEmailMod.addLog(textLog);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleClient(Socket clientSocket, ExecutorService threadPool) {

        try (ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {

            int lette = serverEmailMod.getCntLette();

            // Leggi l'elenco degli utenti registrati
            Set<String> validUsers = loadRegisteredUsers("src/users.txt");

            // Attendi email dell'utente
            String currentUser = in.readUTF();
            serverEmailMod.addLog("Tentativo di connessione da parte di: " + currentUser);

            // Verifica se l'utente è registrato
            if (!validUsers.contains(currentUser.toLowerCase())) {
                serverEmailMod.addLog("Connessione rifiutata: utente non registrato - " + currentUser);
                out.writeUTF("RIFIUTATO");
                out.writeObject(currentUser);
                out.writeInt(lette);
                out.flush();
            }

            serverEmailMod.setCntLette(0);

            // Ottieni email dell'utente dal modello
            ArrayList<Email> userEmails = serverEmailMod.getEmailDataForUser(currentUser);
            if (userEmails == null) {
                serverEmailMod.addLog("Dati non trovati");
                out.writeUTF("Dati non trovati");
                out.flush();
                return;
            }

            int numLette = serverEmailMod.getCntLette();

            out.writeUTF("ACCETTATO");
            out.writeObject(userEmails);
            out.writeInt(numLette);
            out.flush();

            dynamicPort = 50000 + (int)(Math.random() * 10000); // Porta tra 50000 e 59999
            out.writeInt(dynamicPort);
            out.flush();

            threadPool.submit(() -> {
                try {
                    // Specifica l'indirizzo IP e una porta dinamica
                    InetAddress bindAddress = InetAddress.getByName("0.0.0.0"); // Sostituisci con l'IP desiderato
                    syncServerSocket = new ServerSocket(dynamicPort, 50, bindAddress);
                    serverEmailMod.addLog("Server di sincronizzazione attivo su porta: " + dynamicPort);

                    while (true) {
                        Socket syncSocket = syncServerSocket.accept(); // Accetta connessioni sulla porta dinamica
                        controller = new ServerEmailController(serverEmailMod, logList);
                        threadPool.submit(() -> controller.handleSync(syncSocket, currentUser));
                    }
                } catch (IOException e) {
                    serverEmailMod.addLog("Errore nella sincronizzazione: " + e.getMessage());
                }
            });


        } catch (IOException e) {
            e.printStackTrace();
            serverEmailMod.addLog("Errore nella comunicazione con il client");
        }
    }

    public Set<String> loadRegisteredUsers(String filePath) {
        Set<String> users = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                users.add(line.trim().toLowerCase()); // Converti in minuscolo per evitare problemi di case-sensitive
            }
        } catch (IOException e) {
            serverEmailMod.addLog("Errore durante la lettura degli utenti registrati: " + e.getMessage());
        }
        return users;
    }

    public void handleDelete(Socket deleteSocket) {
        try {
            ObjectOutputStream out_delete = new ObjectOutputStream(deleteSocket.getOutputStream());
            ObjectInputStream in_delete = new ObjectInputStream(deleteSocket.getInputStream());

            Email mailToDelete = (Email) in_delete.readObject(); // struttura mail
            serverEmailMod.deleteEmail(mailToDelete);
            out_delete.writeUTF("OK");
            out_delete.flush();

            in_delete.close();
            out_delete.close();
            deleteSocket.close();

        } catch (IOException | RuntimeException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void handleSend(Socket sendSocket){
        try {

            ObjectOutputStream out_send = new ObjectOutputStream(sendSocket.getOutputStream());
            ObjectInputStream in_send = new ObjectInputStream(sendSocket.getInputStream());

            Email email = (Email) in_send.readObject();
            Set<String> validUsers = loadRegisteredUsers("src/users.txt");

            System.out.println((email.getCcString()));

            if (email.getCcString().contains(",")) {

                String[] users = email.getCcString().split(", ");

                for (int i = 0; i < users.length; i++) {

                    if(!validUsers.contains(users[i])) {
                        serverEmailMod.addLog("Connessione rifiutata: utente non registrato - " + users[i]);
                        out_send.writeUTF("ERROR");
                        out_send.flush();
                        break;
                    } else {
                        serverEmailMod.inviaMail(email, users[i]);
                        out_send.writeUTF("OK");
                        out_send.flush();
                    }
                }

            } else {

                if(!validUsers.contains(email.getCcString())){
                    serverEmailMod.addLog("Connessione rifiutata: utente non registrato - " + email.getDest());
                    out_send.writeUTF("ERROR");
                    out_send.flush();

                } else {
                    serverEmailMod.inviaMail(email, null);
                    out_send.writeUTF("OK");
                    out_send.flush();
                }
            }


            in_send.close();
            out_send.close();
            sendSocket.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void handleSync(Socket syncSocket, String currentUserEmail) {

        try {

            ObjectOutputStream out_sync = new ObjectOutputStream(syncSocket.getOutputStream());
            ObjectInputStream in_sync = new ObjectInputStream(syncSocket.getInputStream());

            String segnale = in_sync.readUTF();
            ArrayList<Email> userEmails = serverEmailMod.getEmailDataForUser(currentUserEmail);
            out_sync.writeObject(userEmails);
            out_sync.flush();

            in_sync.close();
            out_sync.close();
            syncSocket.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void handleShow(Socket showSocket){
        try {

            ObjectInputStream in_show = new ObjectInputStream(showSocket.getInputStream());

            Email email = (Email) in_show.readObject();
            serverEmailMod.setReadMail(email);

            in_show.close();
            showSocket.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}