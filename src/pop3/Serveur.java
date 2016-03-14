package pop3;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Serveur {

	private ServerSocket socket;
	private Boolean running;

	public Serveur() {
		
		try {
			this.socket = new ServerSocket(Commun.PORT);
			System.out.println("Lancement du serveur " + socket.getInetAddress().getHostAddress() + " sur le port : 	" + socket.getLocalPort());
		} catch (IOException e) {
			System.out.println("Erreur : Instanciation du ServerSocket impossible");
		}
		this.running = true;
	}

	public void run() {

		try {
			while(this.running) {
				Socket client = this.socket.accept();
				new Thread(new ServeurSecondaire(client)).start();
			}
			
			System.out.println("Fin du serveur");
			
		} catch (IOException e) {
			System.out.println("Erreur : Probleme de socket");
		}
		finally
		{
			try { this.socket.close(); }
			catch (IOException e) {
				System.out.println("Erreur : Probleme de deconnexion de socket");
			}
		}
	}
}

