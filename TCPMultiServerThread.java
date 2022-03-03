import java.net.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TCPMultiServerThread extends Thread {
    private Socket clientTCPSocket = null;
    public TCPMultiServerThread(Socket socket) {
        super("TCPMultiServerThread");
        clientTCPSocket = socket;
    }

    /*
    HTTP REQUEST MESSAGE: Method Types
        GET
        POST
        HEAD
            Use GET method and upload input in URL field of request line
     */

    /*
    HTTP REQUEST MESSAGE: General Format
        Method URL Version cr lf        <- Request Line
        HeaderFieldName Value cr lf     <- Header Line
        cr lf (\r\n)
     */

    public void run(){
        try{
            PrintWriter clientSocketOut = new PrintWriter(clientTCPSocket.getOutputStream(), true);
            BufferedReader clientSocketIn = new BufferedReader(new InputStreamReader(clientTCPSocket.getInputStream()));
            String fromClient = "";


            do{
                // String variables of communication data
                String messageFromClient = "";
                String fileData = "";
                String requestStatus = "";
                String httpVersion = "";
                String fileName = "";
                fromClient = "";

                while ((fromClient = clientSocketIn.readLine()) != null) {
                    messageFromClient = messageFromClient + fromClient + "\r\n";

                    if (fromClient.equals("")) {
                        break;
                    }
                }

                System.out.println("Client's message: \n" + messageFromClient);
                String[] messageFromClientSplit = messageFromClient.split("\r\n");

                // Client message
                for(String line:messageFromClientSplit) {
                    String[] clientsLines = line.split(" ");

                    if (line.contains("HTTP")) {
                        httpVersion = clientsLines[2];

                        if (!line.contains("GET")) {
                            requestStatus = "400 Bad Request";
                        }
                        else {
                            fileName = clientsLines[1].substring(1);
                        }
                    }
                }

                // Process client request
                if (requestStatus.equals("")){
                    try {
                        BufferedReader fileReader = new BufferedReader(new FileReader(fileName));
                        String line;
                        while ((line = fileReader.readLine()) != null) {
                            fileData = fileData + line + "\r\n";
                        }
                        fileReader.close();
                    }

                    /*
                    HTTP Response Status Codes (Feb. 9 Lecture Video)
                        200 - OK
                        301 - Moved Permanently
                        400 - Bad Request
                        404 - Not Found
                        505 - HTTP Version Not Supported
                    */

                    catch (Exception e){
                        requestStatus = "404 Not Found";
                    }
                }
                if (requestStatus.equals(""))
                {
                    requestStatus = "200 OK";
                }

                // Want date and time to be formatted as yyyy/MM/dd HH:mm:ss
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = new Date();

                String outgoingMessage = httpVersion + " " + requestStatus + "\r\n" + "Date: " + dateFormat.format(date)
                        + " MST" + "\r\n" + "Server..." + "\r\n" + "" + "\r\n";

                // Use .equals() to compare string
                if (requestStatus.equals("200 OK")) {
                    outgoingMessage = outgoingMessage + fileData;
                }

                outgoingMessage = outgoingMessage + "" + "\r\n" + "" + "\r\n" + "" + "\r\n" + "" + "\r\n";

                System.out.println("\nResponse to Client: ");
                System.out.println(outgoingMessage);

                clientSocketOut.println(outgoingMessage);


                // Wait for client to complete process
                clientSocketIn.readLine();
            } while (fromClient != null);

            clientSocketOut.close();
            clientSocketIn.close();
            clientTCPSocket.close();
        }

        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
