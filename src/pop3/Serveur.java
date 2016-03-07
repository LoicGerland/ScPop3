package pop3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.*;

public class Serveur {

	public final static int PORT = 110;

	private ServerSocket sc;
	private BufferedReader input;
	private PrintStream output;

	public Serveur() throws IOException {
		this.sc = new ServerSocket(PORT);
	}

	public void run() {

		System.out.println("Lancement du serveur sur le port : 	"+ sc.getLocalPort());

		try {
			while(true) {
				Socket client = this.sc.accept();

				System.out.println("Nouveau client ! Adresse : " + client.getInetAddress() + " Port : " + client.getPort());
				this.output = new PrintStream(client.getOutputStream());
				
				output.println("+OK Bonjour");
			}
		} catch (IOException e) {
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

