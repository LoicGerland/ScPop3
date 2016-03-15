package pop3;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Serveur extends Thread {

	private ServerSocket socket;
	private Boolean running;

	public Serveur() {
		
		try {
			this.socket = new ServerSocket(Commun.PORT);
		} catch (IOException e) {
			Commun.sop("Erreur : Instanciation du ServerSocket impossible");
		}
		this.running = false;
	}

	public void run() {

		Commun.sop("Lancement du serveur " + socket.getInetAddress().getHostAddress() + " sur le port : " + socket.getLocalPort());
		
		try {
			while(this.running) {
				Socket client = this.socket.accept();
				new Thread(new ServeurSecondaire(client)).start();
			}
		} catch (IOException e) {			Commun.sop("Arret du serveur");;
		}
		finally
		{
			try { this.socket.close(); }
			catch (IOException e) {
				Commun.sop("Erreur : Probleme de deconnexion de socket");
			}
		}
	}
	
	public ServerSocket getSocket() {
		return socket;
	}
	public void setSocket(ServerSocket socket) {
		this.socket = socket;
	}
	public Boolean isRunning() {
		return running;
	}
	public void setRunning(Boolean running) {
		this.running = running;
	}
}

