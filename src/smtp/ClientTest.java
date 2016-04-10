package smtp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import Commun.Commun;

public class ClientTest {

	private static int _port;
    private static Socket _socket;
    private static boolean data;

    public static void main(String[] args)
    {
        BufferedReader input   = null;
        BufferedWriter output = null;
   
        try
        {
            _port   = Commun.PORT_SMTP;
            _socket = new Socket(InetAddress.getByName("localhost"), _port);
            
            // Open stream
            input = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(_socket.getOutputStream()));

            // Show the server response
            String reponse;
			if((reponse = input.readLine()) != null){
				System.out.println("Serveur message: " + reponse);
			}
			
			Scanner sc = new Scanner(System.in);
			
			while(!reponse.equals(Commun.SMTP_SERVER_CLOSED)) {
				System.out.println("Commande :");
				String str = sc.nextLine();
				
				if(str.equals("."))
					data = false;
				
	            output.write(str+"\r\n");
	            output.flush();
	            
	            if(data)
	            	continue;
	            
	            if((reponse = input.readLine()) != null){
					System.out.println("Serveur message: " + reponse);
				}
	            
	            if(str.equals("DATA"))
					data = true;
			}      
			sc.close();
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
