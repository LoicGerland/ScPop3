package pop3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import pop3.Commun.Etat;

/**
 * Classe représentant le serveur serveur secondaire
 * qui communique avec le client
 * 
 * @author GERLAND - LETOURNEUR
 */
public class ServeurSecondaire implements Runnable{

	private Serveur serveurPrincipal;
	private final Socket clientSocket;
	private BufferedReader input;
	private BufferedWriter output;
	private Etat etat;
	private Boolean running;
	private ListeMessages listeMessages;
	private String identifiantClient;
	private int messageASupprimer;

	/**
	 * Constructeur
	 * @param serveur
	 * @param clientSocket
	 */
	public ServeurSecondaire(Serveur serveur, Socket clientSocket) {
		this.serveurPrincipal = serveur;
		this.clientSocket = clientSocket;
		
		this.running = true;
		this.setEtat(Etat.INITIALISATION);
		this.listeMessages = new ListeMessages();
		this.identifiantClient = "Inconnu";
		this.messageASupprimer = 0;
		
		try {
			this.output = new BufferedWriter( new OutputStreamWriter(clientSocket.getOutputStream()));
			this.input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			this.serveurPrincipal.getVue().sop(Commun.ERROR_FLUX_INSTANTIATION);
		}
	}

	/**
	 * Lancement du serveur secondaire
	 */
	public void run() {
		this.setEtat(Etat.CONNEXION);
		sendMessage(Commun.OK_SERVER_READY);
		
		this.setEtat(Etat.AUTHORISATION);
		
		while(this.running) {
		String requete;
			try {
				if((requete = this.input.readLine()) != null){
					this.traiterCommande(requete);
				}
			} catch (IOException e) {
				this.running = false;
				this.serveurPrincipal.getVue().sop(Commun.ERROR_FLUX_READING);
			}
		}
		
		try {
			this.clientSocket.close();
		} catch (IOException e) {
			this.serveurPrincipal.getVue().sop(Commun.ERROR_CLOSE_SOCKET);
		}
		serveurPrincipal.removeServeurSecondaire(this);
	}
	
	/**
	 * Envoi d'un message dans le flux de sortie
	 * @param message
	 */
	private void sendMessage(String message) {
		
		try {
			this.output.write(message+"\r\n");
			this.output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Traitement d'une commande lors de la réception
	 * @param message
	 */
	private void traiterCommande(String requete) {
		
		this.serveurPrincipal.getVue().sop("Thread N°"+serveurPrincipal.getListeThread().indexOf(this)+" répond à : "+ requete);
		switch(this.etat) {
		
			case AUTHORISATION:
				this.authorisation(requete);
				break;
				
			case TRANSACTION:
				this.transaction(requete);
				break;
				
			default: 
				this.sendMessage("-ERR");
		}
	}
	
	/**
	 * Traitement de la commande APOP
	 * @param params
	 * @return String message
	 */
	private String commandeAPOP(String [] params) {
		
		if(params.length < 3)
			return Commun.ERR_MISSING_ARGS;
		
		if(GestionFichiers.LireAuthentification(params[1], params[2])) {
			this.identifiantClient = params[1];
			this.listeMessages = GestionFichiers.LireMessages(identifiantClient);
			this.setEtat(Etat.TRANSACTION);
			this.serveurPrincipal.getVue().update();
			return Commun.OK_HELLO + identifiantClient;
		}
		else {
			return Commun.ERR_WRONG_LOGIN;
		}
	}
	
	/**
	 * Traitement de la commande USER
	 * @param params
	 * @return String message
	 */
	private String commandeUSER(String [] params) {
		
		if(params.length < 2)
			return Commun.ERR_MISSING_ARGS;
		
		if(GestionFichiers.LireAuthentification(params[1], null)) {
			this.identifiantClient = params[1];
			return Commun.OK_BOX_NAME;

		}
		else {
			return Commun.ERR_WRONG_BOX_NAME;
		}
	}
	
	/**
	 * Traitement de la commande PASS
	 * 
	 * @param params
	 * @return String message
	 */
	private String commandePASS(String requete) {
		
		String [] params = requete.split(" ", 2 );
		
		if(params.length < 2)
			return Commun.ERR_MISSING_ARGS;
		
		if(this.identifiantClient == null){
			return Commun.ERR_USER_NEEDED;
		}

		if(GestionFichiers.LireAuthentification(this.identifiantClient, params[1])) {
			this.setEtat(Etat.TRANSACTION);
			this.listeMessages = GestionFichiers.LireMessages(identifiantClient);
			this.serveurPrincipal.getVue().update();
			return Commun.OK_HELLO + identifiantClient;
		}
		else {
			return Commun.ERR_WRONG_LOGIN;
		}
	}
	
	/**
	 * Traitement de la commande LIST
	 * @param params
	 * @return String message
	 */
	private String commandeLIST(String [] params) {
		
		if(params.length < 2) {
			return "+OK " + this.listeMessages.size() + " messages" 
					+ "(" + this.listeMessages.getTotalOctets() + " octets" + ")\n" 
					+ this.listeMessages.getTousLesMessages();
		}
		
		try {  
		    Integer.parseInt(params[1]);  
	    } catch(NumberFormatException nfe) {  
    		return Commun.ERR_INTEGER_ARGS; 
    	}
		
		if(this.listeMessages.size() < Integer.parseInt(params[1])) {
			return Commun.ERR_MESSAGE_NOT_EXISTS.replaceFirst("_NUMMSG_", this.listeMessages.size()+"");
		} else {
			Message message = this.listeMessages.get(Integer.parseInt(params[1])-1);
			return "+OK " + message.getNumero() + " " + message.getTailleOctets();
		}
	}
	
	/**
	 * Traitement de la commande RETR
	 * @param params
	 * @return String message
	 */
	private String commandeRETR(String [] params) {
		
		if(params.length < 2) {
			return Commun.ERR_MISSING_ARGS;
		}
		
		try {  
		    Integer.parseInt(params[1]);  
	    } catch(NumberFormatException nfe) {  
    		return Commun.ERR_INTEGER_ARGS; 
    	}
		
		if(this.listeMessages.size() < Integer.parseInt(params[1]) && Integer.parseInt(params[1]) > 0) {
			return Commun.ERR_MESSAGE_NOT_EXISTS.replaceFirst("_NUMMSG_", this.listeMessages.size()+"");
		} else {
			Message message = this.listeMessages.get(Integer.parseInt(params[1])-1);
			if (message.getMarque()) {
				return "-ERR Message " + params[1] + " déjà supprimé";
			}
			return "+OK " + message.getTailleOctets() + " octets\n" + message.getCorps();
		}
	}
	
	/**
	 * Traitement de la commande DELE
	 * @param params
	 * @return String message
	 */
	private String commandeDELE(String [] params) {
		
		if(params.length < 2) {
			return Commun.ERR_MISSING_ARGS;
		}
		
		try {  
		    Integer.parseInt(params[1]);  
	    } catch(NumberFormatException nfe) {  
    		return Commun.ERR_INTEGER_ARGS; 
    	}

		if(this.listeMessages.size() < Integer.parseInt(params[1]) && Integer.parseInt(params[1]) > 0) {
			return Commun.ERR_MESSAGE_NOT_EXISTS.replaceFirst("_NUMMSG_", this.listeMessages.size()+"");
		} else {
			Message message = this.listeMessages.get(Integer.parseInt(params[1])-1);
			if (message.getMarque()) {
				return "-ERR Message " + params[1] + " déjà supprimé";
			} else {
				message.setMarque(true);
				this.messageASupprimer++;
				return "+OK Message " + params[1] + " supprimé";
			}
		}
	}
	
	/**
	 * Traitement de la commande RSET
	 * @return String message
	 */
	private String commandeRSET() {
		this.messageASupprimer = 0;
		
		for (Message m : this.listeMessages) {
			m.setMarque(false);
		}
		
		return "+OK";
	}
	
	/**
	 * Traitement de la commande QUIT
	 * @param delete Permet de savoir si l'état MISE-A-JOUR doit être parcouru
	 * @return String message
	 */
	private String commandeQUIT(boolean delete) {
		
		this.running = false;
		
		if(delete) {
			this.setEtat(Etat.MISEAJOUR);
			int beforeDelete = this.listeMessages.size();
			if(this.messageASupprimer > 0)
				GestionFichiers.SupprimerMessages(this.identifiantClient, this.listeMessages);
			this.listeMessages = GestionFichiers.LireMessages(identifiantClient);
			int afterDelete = this.listeMessages.size();
			if(afterDelete != beforeDelete - this.messageASupprimer)
				return Commun.ERR_MARKED_MESSAGE;
		}
		
		return Commun.OK_SERVER_LOGOUT;
	}

	/**
	 * Traitement de la commande lorsque le serveur est dans l'état AUTHORISATION
	 * @param requete
	 */
	private void authorisation(String requete) {
		
		String sortie = "";
		String[] params = requete.split(" ");
		
		switch(params[0]) {
			
			case "APOP" :
				sortie = commandeAPOP(params);
				break;
			
			case "USER" :
				sortie = commandeUSER(params);
				break;
				
			case "PASS" :
				sortie = commandePASS(requete);
				break;
				
			case "QUIT" :
				sortie = commandeQUIT(false);
				break;
				
			case "STAT" :
			case "LIST" :
			case "DELE" :
			case "NOOP" :
			case "RSET" :
				sortie = Commun.ERR_IMPOSSIBLE_COMMAND;
				break;
				
			default :
				sortie = Commun.ERR_UNKNOWN_COMMAND;
		}
		
		this.sendMessage(sortie);
	}
	
	/**
	 * Traitement de la commande lorsque le serveur est dans l'état TRANSACTION
	 * @param requete
	 */
	private void transaction(String requete) {
		
		String [] params = requete.split(" ");
		String sortie = "";
		
		switch(params[0]) {
		
			case "STAT" :
				sortie = "+OK " + this.listeMessages.size() + " " + this.listeMessages.getTotalOctets();
				break;
				
			case "LIST" :
				sortie = commandeLIST(params);
				break;
				
			case "RETR" :
				sortie = commandeRETR(params);
				break;
				
			case "DELE" :
				sortie = commandeDELE(params);
				break;
				
			case "NOOP" :
				sortie = "+OK";
				break;
				
			case "RSET" :
				sortie = commandeRSET();
				break;
				
			case "QUIT" :
				sortie = commandeQUIT(true);
				break;
				
			case "APOP" :
			case "USER" :
			case "PASS" :
				sortie = Commun.ERR_IMPOSSIBLE_COMMAND;
				break;
			
			default :
				sortie = Commun.ERR_UNKNOWN_COMMAND;
		}
		
		this.serveurPrincipal.getVue().sop(sortie.substring(0,4));
		this.sendMessage(sortie);
	}
	
	/********
	 * 
	 * GETTER
	 * 
	 **************/
	
	public Etat getEtat() {
		return etat;
	}

	public String getIdentifiantClient() {
		return identifiantClient;
	}

	public Socket getClientSocket() {
		return clientSocket;
	}
	
	/********
	 * 
	 * SETTER
	 * 
	 **************/
	
	public void setEtat(Etat etat) {
		this.etat = etat;
		System.out.println("Etat du serveur : " + this.etat);
	}
}

