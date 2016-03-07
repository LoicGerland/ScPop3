package pop3;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;

public class Serveur {

	public final static int PORT = 110;
	
	private ServerSocket sc;
	private InputStream input;
	private OutputStream output;
	
	public Serveur() throws IOException {
		this.sc = new ServerSocket(PORT);
	}
	
	public void run() {
		
		System.out.println("Lancement du serveur sur le port : "+ sc.getLocalPort());
		
		while(true) {
			try {
				Socket client = this.sc.accept();
				
				output = client.getOutputStream();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        finally
	        {
	            try
	            {
	                this.sc.close();
	            }
	            catch (IOException e)
	            {
	                e.printStackTrace();
	            }
	        }
			
			
		}
	}
}
