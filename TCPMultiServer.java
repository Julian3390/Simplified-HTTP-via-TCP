import java.net.*;
import java.io.*;

public class TCPMultiServer {
    public static void main(String[] args) throws IOException{

        // Instantiate your TCP Socket
        ServerSocket serverTCPSocket = null;
        boolean connectionEstablished = true;

        try{
            // My assigned port number: 5010
            serverTCPSocket = new ServerSocket(5010);

        } catch (IOException e) {
            //Return message stating that given port number is not accessible
            System.err.println("Could not connect with the port " + serverTCPSocket.getLocalPort());
            System.exit(-1);
        }


        while (connectionEstablished){
            new TCPMultiServerThread(serverTCPSocket.accept()).start();
        }
        serverTCPSocket.close();
    }
}
