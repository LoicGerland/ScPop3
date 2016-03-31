package pop3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class ClientTest {

	private static int _port;
    //private static Socket _socket;
    private static SSLSocket _socket;

    public static void main(String[] args)
    {
        BufferedReader input   = null;
        BufferedWriter output = null;
   
        try
        {
            _port   = Commun.PORT;
            //_socket = new Socket(InetAddress.getByName("localhost"), _port);
            SSLSocketFactory fabrique = (SSLSocketFactory) SSLSocketFactory.getDefault();
            _socket = (SSLSocket) fabrique.createSocket("127.0.0.1", _port);

            String [] ciphers = _socket.getSupportedCipherSuites();
            _socket.setEnabledCipherSuites(ciphers);

            // Open stream
            input = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(_socket.getOutputStream()));

            // Show the server response
            String reponse;
			if((reponse = input.readLine()) != null){
				System.out.println("Serveur message: " + reponse);
			}
            
			
			Scanner sc = new Scanner(System.in);
			
			while(reponse != Commun.OK_SERVER_LOGOUT) {
				System.out.println("Commande :");
				String str = sc.nextLine();
				
	            output.write(str+"\r\n");
	            output.flush();
	            
	            if((str.startsWith("LIST") && !str.startsWith("LIST ")) || str.startsWith("RETR")) {
	            	if((reponse = input.readLine()) != null){
	    				System.out.println("Serveur message: " + reponse);
	    				if(reponse.startsWith("+OK")) {
	    					while(!reponse.equals(".")) {
	    						reponse = input.readLine();
	    						System.out.println("Serveur message: " + reponse);
	    					}
	    				}
	    			}
	            } else {
		            if((reponse = input.readLine()) != null){
						System.out.println("Serveur message: " + reponse);
					}
	            }
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
