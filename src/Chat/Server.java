package Chat;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    static Socket clientSocket = null;
    static ServerSocket serverSocket = null;
    static ArrayList<ClientConnection> connectedClients;

    private static void sendMessageToAllClients(String Message) {

        //Prints the Message in the Server Console
        System.out.println(Message);

        //Loop through all Connected Clients and Send Each of them the Message
        for (ClientConnection currentClient : connectedClients) {
            currentClient.sendMessageToClient(Message);
        }
    }

    public static boolean requestNewUser(ClientConnection user) {
        for (ClientConnection currentClient : connectedClients) {
            if (currentClient.getUserName().equals(user.getUserName())) {
                user.sendMessageToClient("Sorry Your Username is already taken");
                user.sendMessageToClient("Disconnected");
                return false;
            }
        }

        System.out.println("User " + user.getUserName() + " connected from IP " + user.getClientSocket().getInetAddress());

        connectedClients.add(user);
        return true;

    }

    public static void removeUser(ClientConnection user) {
        user.closeConnections();
        connectedClients.remove(user);
    }

    public static void disconnectUser(ClientConnection user) {

        user.sendMessageToClient("Disconnected");

        removeUser(user);

        System.out.println(user.getUserName() + " has left.");
        Server.notifyLeaveUser(user.getUserName());

    }

    public static void notifyNewUser(String userName) {
        sendMessageToAllClients("*** " + userName + " joined !!! ***");
    }

    private static void notifyLeaveUser(String userName) {
        sendMessageToAllClients("*** " + userName + " has left !!! ***");
    }

    public static void sendToAll(ClientConnection sender, String Message) {
        sendMessageToAllClients("<" + sender.getUserName() + "> " + Message);
    }

    public static void main(String args[]) {

        // The default port
        int port_number = 2222;


        //Check all arguments if port is given. If so parse the port argument.
        // How to call Example: java -jar server.jar -port 1337
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-port")) {
                port_number = Integer.parseInt(args[i + 1]);
            }
        }



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

    private DataInputStream _fromClientStream = null;
    private PrintStream _toClientStream = null;
    private Socket clientSocket = null;
    private String userName = null;

    public ClientConnection(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    private void sendMessageToServer(String message) {
        Server.sendToAll(this, message);
    }

    public void sendMessageToClient(String message) {
        this._toClientStream.println(message);
    }

    public void closeConnections() {
        try {
            this._fromClientStream.close();
            this._toClientStream.close();
            this.getClientSocket().close();
        } catch (IOException ex) {
            Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        String line;
        try {
            _fromClientStream = new DataInputStream(getClientSocket().getInputStream());
            _toClientStream = new PrintStream(getClientSocket().getOutputStream());

            this.sendMessageToClient("Please Enter your name:");

            this.setUserName(_fromClientStream.readLine());

            if (!Server.requestNewUser(this)) {

                this.closeConnections();
                return;
            }

            Server.notifyNewUser(this.getUserName());

            while (true) {
                try {
                    line = _fromClientStream.readLine();
                    if (line.startsWith("/quit")) {
                        break;
                    }
                    sendMessageToServer(line);
                } catch (SocketException e) {
                    //Exception occured so Disconnect User!
                    Server.removeUser(this);
                    //Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
                }
            }

            Server.disconnectUser(this);
        } catch (IOException e) {
            //Exception occured so Disconnect User!
            Server.removeUser(this);
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName the userName to set
     */
    private void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return the clientSocket
     */
    public Socket getClientSocket() {
        return clientSocket;
    }
}