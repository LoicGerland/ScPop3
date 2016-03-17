package pop3;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Serveur extends Thread {

	private Vue vue;
	private ServerSocket socket;
	private Boolean running;
	private ArrayList<ServeurSecondaire> listeThread;

	public Serveur(Vue vue) {
		
		this.vue = vue;
		try {
			this.socket = new ServerSocket(Commun.PORT);
		} catch (IOException e) {
			this.vue.sop("Erreur : Instanciation du ServerSocket impossible");
		}
		this.running = false;
		this.listeThread = new ArrayList<ServeurSecondaire>();
	}

	public void run() {

		this.running = true;
		this.vue.sop("Lancement du serveur " + socket.getInetAddress().getHostAddress() + " sur le port : " + socket.getLocalPort());
		
		try {
			while(this.running) {
				Socket client = this.socket.accept();
				ServeurSecondaire thread = new ServeurSecondaire(vue, client);
				this.listeThread.add(thread);
				this.vue.update();
				new Thread(thread).start();
			}
		} catch (IOException e) {			this.vue.sop("Arret du serveur");;
		}
		finally
		{
			try { this.socket.close(); }
			catch (IOException e) {
				this.vue.sop("Erreur : Probleme de deconnexion de socket");
			}
		}
	}
	
	public boolean stopServeur() {
		try {
			this.setRunning(false);
			this.socket.close();
			return true;
		} catch (IOException e1) {
			vue.sop("Impossible d'arreter le serveur");
			return false;
		}
	}
	
	public ArrayList<ServeurSecondaire> getListeThread() {
		return listeThread;
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

