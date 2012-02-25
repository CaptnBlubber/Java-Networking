package Chat;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client implements Runnable {

    static Socket clientSocket = null;
    static PrintStream _toServer = null;
    static DataInputStream _fromServer = null;
    static BufferedReader inputLine = null;
    static boolean isConnected = false;

    public static void main(String[] args) {

        // The default port and host	

        int port_number = 2222;
        String host = "localhost";

        // Check all arguments if port and server are given. If so parse the port and server argument.
        // How to call Example: java -jar server.jar -port 1337 -server localhost
        for(int i = 0; i < args.length; i++) {
            if(args[i].equals("-port")) {
                port_number = Integer.parseInt(args[i +1]);
            }
            if(args[i].equals("-server")) {
                host = args[i + 1];
            }
        }
        

        System.out.println("Trying to connect to: " + host + ":" + port_number);


        try {
            clientSocket = new Socket(host, port_number);
            inputLine = new BufferedReader(new InputStreamReader(System.in));
            _toServer = new PrintStream(clientSocket.getOutputStream());
            _fromServer = new DataInputStream(clientSocket.getInputStream());
        } catch (UnknownHostException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
        } catch (IOException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
        }

        // If everything has been initialized then we want to write some data
        // to the socket we have opened a connection to on port port_number 

        if (clientSocket != null && _toServer != null && _fromServer != null) {
            
            isConnected = true;
            
            try {

                // Create a thread to read from the server

                new Thread(new Client()).start();

                //Read for new Chat Messages
                while (isConnected) {
                    _toServer.println(inputLine.readLine());
                }

                // Clean up:
                // close the output stream
                // close the input stream
                // close the socket

                _toServer.close();
                _fromServer.close();
                clientSocket.close();
            } catch (IOException e) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    @Override
    public void run() {

        String serverMessage;

        // Keep on reading from the socket till we receive the "Disconnected" from the server,
        // once we received that then we want to break.
        try {

            while ((serverMessage = _fromServer.readLine()) != null) {

                //Print the Message
                System.out.println(serverMessage);

                if (serverMessage.equals("Disconnected")) {
                    System.out.println("Your Connection was reset. Pess Enter to Exit");
                    break;
                }
            }

            isConnected = false;

        } catch (IOException e) {

            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}