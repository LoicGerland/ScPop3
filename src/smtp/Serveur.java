package smtp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import Commun.Commun;

/**
 * Classe représentant le serveur principal qui accepte les connexions client
 * et qui redirige vers des serveurs secondaires au moyen de thread
 * 
 * @author GERLAND - LETOURNEUR
 */
public class Serveur extends Thread {

	private Vue view;
	private Boolean running;
	private ServerSocket socket;

	/**
	 * Constructeur
	 * @param vue
	 */
	public Serveur(Vue vue) {
		
		this.view = vue;
		this.running = false;
		try {
			this.socket = new ServerSocket(Commun.PORT_SMTP);
			
		} catch (IOException e) {
			this.view.sop(Commun.ERROR_SOCKET_INSTANTIATION);
		}
	}

	/**
	 * Lancement du serveur
	 */
	public void run() {

		this.running = true;
		this.view.sop("Lancement du serveur");
		
		try {
			while(this.running) {
				Socket client = this.socket.accept();
				addSecondaryServer(client);
			}
		} catch (IOException e) {
			//Arrêt du serveur par envoi d'exception car accept() est bloquant			this.view.sop("Arrêt du serveur");
		}
		finally
		{
			try { this.socket.close(); }
			catch (IOException e) {
				this.view.sop(Commun.ERROR_CLOSE_SOCKET);
			}
		}
	}
	
	/**
	 * Arrêt du serveur
	 * @return Succès de l'arrêt du serveur
	 */
	public boolean stopServeur() {
		try {
			this.running = false;
			this.socket.close();
			return true;
		} catch (IOException e1) {
			view.sop(Commun.ERROR_STOP_SERVER);
			return false;
		}
	}
	
	/**
	 * Ajout d'un serveur secondaire de la liste 
	 * lors de l'apparition d'un nouveau client
	 * @param serveurSecondaire
	 */
	public void addSecondaryServer(Socket client) {
		this.view.sop("Nouveau client ! Adresse : " + client.getInetAddress());
		ServeurSecondaire thread = new ServeurSecondaire(this, client);
		new Thread(thread).start();
	}
	
	/********
	 * 
	 * GETTER
	 * 
	 **************/
	
	public ServerSocket getSocket() {
		return socket;
	}
	public Vue getView() {
		return view;
	}
	public Boolean isRunning() {
		return running;
	}
}

