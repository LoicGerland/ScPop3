package pop3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
        PrintStream output = null;
   
        try
        {
            _port   = 110;
            _socket = new Socket(InetAddress.getByName("localhost"), _port);

            // Open stream
            input = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
            output = new PrintStream(_socket.getOutputStream());

            // Show the server response
            String reponse;
			if((reponse = input.readLine()) != null){
				System.out.println("Serveur message: " + reponse);
			}
            
            output.println("APOP test test");
            
            if((reponse = input.readLine()) != null){
				System.out.println("Serveur message: " + reponse);
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
