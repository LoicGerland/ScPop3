package pop3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientTest {

	private static int    _port;
    private static Socket _socket;

    public static void main(String[] args)
    {
        BufferedReader input   = null;
        BufferedWriter output = null;
   
        try
        {
            _port   = 110;
            _socket = new Socket(InetAddress.getByName("localhost"), _port);

            // Open stream
            input = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(_socket.getOutputStream()));

            // Show the server response
            String reponse;
			if((reponse = input.readLine()) != null){
				System.out.println("Serveur message: " + reponse);
			}
            
            output.write("APOP test test\r\n");
            output.flush();
            
            if((reponse = input.readLine()) != null){
				System.out.println("Serveur message: " + reponse);
			}
            
            output.write("STAT\r\n");
            output.flush();
            
            if((reponse = input.readLine()) != null){
				System.out.println("Serveur message: " + reponse);
			}
            
            output.write("LIST\r\n");
            output.flush();
            if((reponse = input.readLine()) != null){
				System.out.println("Serveur message: " + reponse);
				int nbMessages = Integer.parseInt(reponse.split(" ")[1]);
				for(int i=0;i<=nbMessages;i++) {
					reponse = input.readLine();
					System.out.println("Serveur message: " + reponse);
				}
			}
            
            output.write("RETR 3\r\n");
            output.flush();
            if((reponse = input.readLine()) != null){
				System.out.println("Serveur message: " + reponse);
				if(reponse.startsWith("+OK")) {
					for(int i=0;i<2;i++) {
						reponse = input.readLine();
						System.out.println("Serveur message: " + reponse);
					}
				}
			}
            
            output.write("RETR 4\r\n");
            output.flush();
            if((reponse = input.readLine()) != null){
				System.out.println("Serveur message: " + reponse);
				if(reponse.startsWith("+OK")) {
					for(int i=0;i<2;i++) {
						reponse = input.readLine();
						System.out.println("Serveur message: " + reponse);
					}
				}
			}
                        
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                input.close();
                _socket.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
