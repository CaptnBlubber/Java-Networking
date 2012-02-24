package Chat;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    static Socket clientSocket = null;
    static ServerSocket serverSocket = null;
    static ArrayList<ClientConnection> connectedClients;

    private static void sendMessageToAllClients(String Message) {
        for (ClientConnection currentClient : connectedClients) {
            currentClient.sendMessageToClient(Message);
        }
    }

    public static boolean requestNewUser(ClientConnection user) {
        for (ClientConnection currentClient : connectedClients) {
            if (currentClient.name.equals(user.name)) {
                user.sendMessageToClient("Sorry Your Username is already taken");
                user.sendMessageToClient("Disconnected");
                return false;
            }
        }
        
        System.out.println("User " + user.name + " connected from IP " + user.clientSocket.getInetAddress());
        
        connectedClients.add(user);
        return true;

    }

    public static void disconnectUser(ClientConnection user) {
        
        user.sendMessageToClient("Disconnected");
        user.closeConnections();
        connectedClients.remove(user);

        System.out.println(user.name + " has left.");
        
        Server.notifyLeaveUser(user.name);

    }

    public static void notifyNewUser(String userName) {
        sendMessageToAllClients("*** " + userName + " joined !!! ***");
    }

    private static void notifyLeaveUser(String userName) {
        sendMessageToAllClients("*** " + userName + " has left !!! ***");
    }

    public static void sendToAll(ClientConnection sender, String Message) {
        sendMessageToAllClients("<" + sender.name + "> " + Message);
    }

    public static void main(String args[]) {

        // The default port

        int port_number = 2222;

        System.out.println("Starting server.\n" + "Listening to Port: " + port_number);

        try {
            serverSocket = new ServerSocket(port_number);
        } catch (IOException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
        }

        connectedClients = new ArrayList<>();


        while (true) {

            try {
                clientSocket = serverSocket.accept(); //Thread halts untli accept occurs
                
                System.out.println("New Connection from " + clientSocket.getInetAddress());
                
                ClientConnection newClient = new ClientConnection(clientSocket);
                newClient.start();

            } catch (IOException e) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }
}

class ClientConnection extends Thread {

    DataInputStream is = null;
    PrintStream os = null;
    Socket clientSocket = null;
    String name = null;

    public ClientConnection(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    private void sendMessageToServer(String message) {
        Server.sendToAll(this, message);
    }
    
    public void sendMessageToClient(String message) {
        this.os.println(message);
    }

    public void closeConnections() {
        try {
            this.is.close();
            this.os.close();
            this.clientSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        String line;
        try {
            is = new DataInputStream(clientSocket.getInputStream());
            os = new PrintStream(clientSocket.getOutputStream());

            this.sendMessageToClient("Please your name:");

            this.name = is.readLine();

            if (!Server.requestNewUser(this)) {
                
                this.closeConnections();
                return;
            }

            Server.notifyNewUser(this.name);

            while (true) {
                line = is.readLine();
                if (line.startsWith("/quit")) {
                    break;
                }
                sendMessageToServer(line);
            }
            Server.disconnectUser(this);
        } catch (IOException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}