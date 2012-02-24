package Chat;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client implements Runnable {

    static Socket clientSocket = null;
    static PrintStream os = null;
    static DataInputStream is = null;
    static BufferedReader inputLine = null;
    static boolean closed = false;

    public static void main(String[] args) {

        // The default port	

        int port_number = 2222;
        String host = "localhost";


        System.out.println("Trying to connect to: " + host + ":" + port_number);


        try {
            clientSocket = new Socket(host, port_number);
            inputLine = new BufferedReader(new InputStreamReader(System.in));
            os = new PrintStream(clientSocket.getOutputStream());
            is = new DataInputStream(clientSocket.getInputStream());
        } catch (UnknownHostException e) {
            System.err.println("No Route To Host: " + host);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to the host " + host);
        }

        // If everything has been initialized then we want to write some data
        // to the socket we have opened a connection to on port port_number 

        if (clientSocket != null && os != null && is != null) {
            try {

                // Create a thread to read from the server

                new Thread(new Client()).start();

                //Read for new Chat Messages
                while (!closed) {
                    os.println(inputLine.readLine());
                }

                // Clean up:
                // close the output stream
                // close the input stream
                // close the socket

                os.close();
                is.close();
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("IOException:  " + e);
            }
        }
    }

    @Override
    public void run() {

        String serverMessage;

        // Keep on reading from the socket till we receive the "Disconnected" from the server,
        // once we received that then we want to break.
        try {

            while ((serverMessage = is.readLine()) != null) {

                //Print the Message
                System.out.println(serverMessage);

                if (serverMessage.equals("Disconnected")) {
                    System.out.println("Your Connection was reset. Pess Enter to Exit");
                    break;
                }
            }

            closed = true;

        } catch (IOException e) {
            System.err.println("IOException:  " + e);
        }
    }
}