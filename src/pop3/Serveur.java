package pop3;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Classe représentant le serveur principal qui accepte les connexions client
 * et qui redirige vers des serveurs secondaires au moyen de thread
 * 
 * @author GERLAND - LETOURNEUR
 */
public class Serveur extends Thread {

	private Vue vue;
	private ServerSocket socket;
	private Boolean running;
	private ArrayList<ServeurSecondaire> listeThread;

	/**
	 * Constructeur
	 * @param vue
	 */
	public Serveur(Vue vue) {
		
		this.vue = vue;
		try {
			this.socket = new ServerSocket(Commun.PORT);
		} catch (IOException e) {
			this.vue.sop(Commun.ERROR_SOCKET_INSTANTIATION);
		}
		this.running = false;
		this.listeThread = new ArrayList<ServeurSecondaire>();
	}

	/**
	 * Lancement du serveur
	 */
	public void run() {

		this.running = true;
		this.vue.sop("Lancement du serveur");
		
		try {
			while(this.running) {
				Socket client = this.socket.accept();
				this.vue.sop("Nouveau client ! Adresse : " + client.getInetAddress());
				this.vue.sop("Demarrage du thread N°"+(listeThread.size()+1));
				ServeurSecondaire thread = new ServeurSecondaire(this, client);
				this.listeThread.add(thread);
				this.vue.update();
				new Thread(thread).start();
			}
		} catch (IOException e) {			this.vue.sop("Arret du serveur");
		}
		finally
		{
			try { this.socket.close(); }
			catch (IOException e) {
				this.vue.sop(Commun.ERROR_CLOSE_SOCKET);
			}
		}
	}
	
	/**
	 * Arrêt du serveur
	 * @return Succès de l'arrêt du serveur
	 */
	public boolean stopServeur() {
		try {
			this.listeThread.clear();
			this.setRunning(false);
			this.socket.close();
			return true;
		} catch (IOException e1) {
			vue.sop(Commun.ERROR_STOP_SERVER);
			return false;
		}
	}
	
	/**
	 * Suppression d'un serveru secondaire de la liste 
	 * lors de l'arrêt d'un serveur secondaire
	 * @param serveurSecondaire
	 */
	public void removeServeurSecondaire(ServeurSecondaire serveurSecondaire) {
		vue.sop("Arret du thread N°"+(listeThread.indexOf(serveurSecondaire)+1));
		this.listeThread.remove(serveurSecondaire);
		this.vue.update();
	}
	
	/**
	 * Vérification des verrous utilisateur
	 * @param serveurSecondaire
	 * @return boolean
	 */
	public boolean checkLock(String id) {
		for(ServeurSecondaire ss : listeThread) {
			if(ss.getIdentifiantClient().equals(id))
				return true;
		}
		return false;
	}
	
	/********
	 * 
	 * GETTER
	 * 
	 **************/
	
	public ArrayList<ServeurSecondaire> getListeThread() {
		return listeThread;
	}
	public ServerSocket getSocket() {
		return socket;
	}
	public Vue getVue() {
		return vue;
	}
	public Boolean isRunning() {
		return running;
	}
	
	/********
	 * 
	 * SETTER
	 * 
	 **************/
	
	public void setRunning(Boolean running) {
		this.running = running;
	}
}

