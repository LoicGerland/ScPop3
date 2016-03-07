package pop3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientTest {

	private static int    _port;
    private static Socket _socket;

    public static void main(String[] args)
    {
        InputStream input   = null;
   
        try
        {
            _port   = 110;
            _socket = new Socket(InetAddress.getByName("localhost"), _port);

            // Open stream
            input = _socket.getInputStream();

            // Show the server response
            String response = new BufferedReader(new InputStreamReader(input)).readLine();
            System.out.println("Server message: " + response);
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
