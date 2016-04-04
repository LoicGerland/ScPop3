package pop3;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

/**
 * Classe représentant le serveur principal qui accepte les connexions client
 * et qui redirige vers des serveurs secondaires au moyen de thread
 * 
 * @author GERLAND - LETOURNEUR
 */
public class Serveur extends Thread {

	private Vue view;
	private Boolean running;
	//private ServerSocket socket;
	private SSLServerSocket socket;
	private ArrayList<ServeurSecondaire> listSecondary;

	/**
	 * Constructeur
	 * @param vue
	 */
	public Serveur(Vue vue) {
		
		this.view = vue;
		this.running = false;
		this.listSecondary = new ArrayList<ServeurSecondaire>();
		try {
			//this.socket = new ServerSocket(Commun.PORT);
			SSLServerSocketFactory fab = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
			this.socket =(SSLServerSocket) fab.createServerSocket(Commun.PORT);
			
			//Récupération de tous les ciphers
			String [] ciphers = this.socket.getSupportedCipherSuites();
			
			//Contruction d'un tableau de tous les ciphers contenant "anon"
			ArrayList<String> pickedCiphersList = new ArrayList<String>();
			for(String cipher : ciphers) {
				if(cipher.contains("anon")) {
					pickedCiphersList.add(cipher);
				}
			}

			String [] pickedCiphers = new String[pickedCiphersList.size()];
			pickedCiphers = pickedCiphersList.toArray(pickedCiphers);
			
			socket.setEnabledCipherSuites(pickedCiphers);
			
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
			this.listSecondary.clear();
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
		this.view.sop("Démarrage du thread N°"+(listSecondary.size()+1));
		ServeurSecondaire thread = new ServeurSecondaire(this, client);
		this.listSecondary.add(thread);
		this.view.update();
		new Thread(thread).start();
	}
	
	/**
	 * Suppression d'un serveur secondaire de la liste 
	 * lors de l'arrêt d'un serveur secondaire
	 * @param serveurSecondaire
	 */
	public void removeSecondaryServer(ServeurSecondaire serveurSecondaire) {
		view.sop("Arrêt du thread N°"+(listSecondary.indexOf(serveurSecondaire)+1));
		this.listSecondary.remove(serveurSecondaire);
		this.view.update();
	}
	
	/**
	 * Vérification des verrous utilisateur
	 * @param serveurSecondaire
	 * @return boolean
	 */
	public boolean checkLock(String id) {
		for(ServeurSecondaire ss : listSecondary) {
			if(ss.getClientLogin().equals(id))
				return true;
		}
		return false;
	}
	
	/********
	 * 
	 * GETTER
	 * 
	 **************/
	
	public ArrayList<ServeurSecondaire> getListSecondary() {
		return listSecondary;
	}
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

