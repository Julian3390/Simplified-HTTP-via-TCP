import java.net.*;
import java.io.*;

public class TCPClient {
    public static void main(String[] args) throws IOException{
        Socket tcpSocket        = null;
        PrintWriter socketOut   = null;
        BufferedReader socketIn = null;

        // MUST!!: Include your IP Address or DNS name to establish the connection
        String ipAddressOrDNS = "";
        BufferedReader systemInput = new BufferedReader(new InputStreamReader(System.in));

        try {
            // User is given option to enter their IP Add. or DNS
            System.out.print("Enter server IP address or DNS name to continue: ");
            ipAddressOrDNS = systemInput.readLine();
            System.out.println("");

            // RTT: time for small packet to travel from client to server and back
            // Need a timer to count time needed for each RTT cycle
            long start = System.currentTimeMillis();
            long end = System.currentTimeMillis();

            // Assigned port number is 5010
            tcpSocket = new Socket(ipAddressOrDNS, 5010);
            System.out.println("RTT connection established in " + (end-start) + " milliseconds");

            socketOut = new PrintWriter(tcpSocket.getOutputStream(),true);
            socketIn = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
        }

        // What happens if the IP Address or DNS is unknown? What if it cannot be reached?
        catch (UnknownHostException e) {
            System.err.println("IP Address or DNS given is UNKNOWN " + ipAddressOrDNS);
            System.exit(1);
        }
        catch (IOException e) {
            System.err.println("Could not establish a connection to "  + ipAddressOrDNS);
            System.exit(1);
        }

        String fromServer;
        String fromUser;

        boolean wishToContinue = true;

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

        while (wishToContinue){
            System.out.println("Enter HTTP method type (GET, HEAD, POST, PUT, DELETE): ");
            String httpMethodType = systemInput.readLine();

            System.out.println("Enter htm file name (ex. CS3700.htm): ");
            String htmFileName = systemInput.readLine();

            System.out.println("Enter the http version: ");
            String httpVersion = systemInput.readLine();

            System.out.println("Input the User Agent (ex. Safari or Chrome): ");
            String userAgent = systemInput.readLine();

            // Send outbound message with only 1 \r\n at the end of the outgoing message
            String outBoundMessage = httpMethodType + " /" + htmFileName + " " + "HTTP/" + httpVersion + "\r\n" +
                    "Host: " + ipAddressOrDNS + "\r\n" + "User-Agent: " + userAgent + "\r\n" + "" + "\r\n";

            System.out.println("\nMessage to server: \n" + outBoundMessage);
            try {
                String serverData = "";
                long start = System.currentTimeMillis();
                socketOut.println(outBoundMessage);


                while ((fromServer = socketIn.readLine()) != null) {
                    serverData = serverData + fromServer + "\r\n";
                    if (serverData.contains("\r\n\r\n\r\n\r\n")) {
                        break;
                    }
                }
                long end = System.currentTimeMillis();

                System.out.println("Response from Server: \n" + serverData);
                System.out.println("RTT of HTTP query was " + (end-start) + " milliseconds");
                String[] responseFromServer = serverData.split("\r\n");

                boolean hithtmlData  = false;
                boolean statusTwoHundred = false;
                String htmlMessageSavedOntoFile = "";
                String dataBeforeHtml = "";


                for(String line: responseFromServer) {
                    if(line.contains("200 OK")) {
                        statusTwoHundred = true;
                    }

                    if (!hithtmlData) {
                        dataBeforeHtml = dataBeforeHtml + line + "\r\n";
                    }

                    if (line.equals("") && statusTwoHundred) {
                        hithtmlData = true;
                    }
                }

                if (!statusTwoHundred) {
                    System.out.println("NO HTML DATA WILL BE SAVED AS FILE");
                }


                System.out.println("The header and status lines sent from server: \n" + dataBeforeHtml);

                if (statusTwoHundred) {
                    htmlMessageSavedOntoFile = serverData.replace(dataBeforeHtml,"");
                    String[] splitDataFromFourBlankLines = htmlMessageSavedOntoFile.split("\r\n\r\n\r\n\r\n", 2);
                    String htmlData = splitDataFromFourBlankLines[0] + "\r\n";
                    System.out.println("\nHTML DATA THAT WILL BE SAVED AS FILE\n" + htmlData);

                    PrintWriter printHtmlFile = new PrintWriter(htmFileName);

                    String[] htmlDataArray = htmlData.split("\r\n");
                    for(String lines: htmlDataArray) {
                        printHtmlFile.println(lines);
                    }

                    printHtmlFile.close();
                }
            }
            catch (Exception e) {
                System.out.println("Server does NOT reply anything.");
                wishToContinue = false;
            }

            System.out.println("If you wish to continue enter 'yes'. If you do not wish to continue, press the ENTER key: ");
            fromUser = systemInput.readLine();

            if (!fromUser.equals("yes")) {
                wishToContinue = false;
            }

            // Server is timed-out
            socketOut.println("DONE WITH ONE REQUEST");
        }
        //socketOut.print("Bye"); //added line

        socketOut.close();
        socketIn.close();
        systemInput.close();
        tcpSocket.close();
    }
}
