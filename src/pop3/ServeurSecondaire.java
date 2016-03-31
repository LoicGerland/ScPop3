package pop3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

import pop3.Commun.Etat;

/**
 * Classe représentant le serveur serveur secondaire
 * qui communique avec le client
 * 
 * @author GERLAND - LETOURNEUR
 */
public class ServeurSecondaire implements Runnable{

	private Serveur primaryServer;
	private Boolean running;
	private Etat etat;
	
	private final Socket clientSocket;
	private String stamp;
	private String clientLogin;
	private ListeMessages listMessages;
	private int messagesToDelete;
	private BufferedReader input;
	private BufferedWriter output;

	/**
	 * Constructeur
	 * @param serveur
	 * @param clientSocket
	 */
	public ServeurSecondaire(Serveur serveur, Socket clientSocket) {
		this.primaryServer = serveur;
		this.clientSocket = clientSocket;
		
		this.running = true;
		this.setEtat(Etat.INITIALISATION);
		this.listMessages = new ListeMessages();
		this.clientLogin = "Inconnu";
		this.messagesToDelete = 0;
		this.generateStamp();
		
		try {
			this.output = new BufferedWriter( new OutputStreamWriter(clientSocket.getOutputStream()));
			this.input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			this.primaryServer.getView().sop(Commun.ERROR_FLUX_INSTANTIATION);
		}
	}

	/**
	 * Construction du timbre-à-date grâce à un encodage en MD5 (pour éviter les espace)
	 * d'une chaine de caractère constitué de la date actuelle et d'un id unique.
	 * Format de la date : Tue Mar 22 21:28:14 CET 2016
	 * Format de l'id unique : b8889313-ad2b-4aac-932a-c30b41b46a80
	 */
	private void generateStamp() {

		Date date = new Date();
		String uniqueID = UUID.randomUUID().toString();
		this.stamp = date.toString()+uniqueID;
		
		MessageDigest m;
		try {
			m = MessageDigest.getInstance("MD5");
			m.update(this.stamp.getBytes(),0,this.stamp.length());
			this.stamp = new BigInteger(1,m.digest()).toString(16);
		} catch (NoSuchAlgorithmException e) {
			this.primaryServer.getView().sop("Erreur MD5");
		}
	}
	
	/**
	 * Lancement du serveur secondaire
	 * Envoi du message de bienvenue avec le timbre-à-date
	 * Lecture du flux entrant pour traiter les commande du client
	 */
	public void run() {
		
		this.setEtat(Etat.CONNEXION);
		
		sendMessage(Commun.OK_SERVER_READY + " <" + this.stamp + ">");
		
		this.setEtat(Etat.AUTHORISATION);
		
		while(this.running) {
		String requete;
			try {
				if((requete = this.input.readLine()) != null){
					this.traiterCommande(requete);
				}
			} catch (IOException e) {
				this.running = false;
				this.primaryServer.getView().sop(Commun.ERROR_FLUX_READING);
			}
		}
		
		//Lorsque le client envoie un QUIT, la socket est fermée
		//et la vue n'affiche plus le client
		try {
			this.clientSocket.close();
		} catch (IOException e) {
			this.primaryServer.getView().sop(Commun.ERROR_CLOSE_SOCKET);
		}
		primaryServer.removeSecondaryServer(this);
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
			this.primaryServer.getView().sop(Commun.ERROR_SEND_MESSAGE);
		}
	}
	
	/**
	 * Traitement d'une commande en fonction de l'état lors de la réception
	 * @param message
	 */
	private void traiterCommande(String requete) {
		
		this.primaryServer.getView().sop("Thread N°"+primaryServer.getListSecondary().indexOf(this)+" répond à : "+ requete);
		
		String sortie = "";
		
		switch(this.etat) {
		
			case AUTHORISATION:
				sortie = this.authorisation(requete);
				break;
				
			case TRANSACTION:
				sortie = this.transaction(requete);
				break;
				
			default: 
				sortie = "-ERR";
		}
		
		this.primaryServer.getView().sop(sortie.substring(0,4));
		this.sendMessage(sortie);
	}
	
	/**
	 * Traitement de la commande APOP
	 * @param params
	 * @return String message
	 */
	private String commandeAPOP(String [] params) {
		
		if(params.length < 3)
			return Commun.ERR_MISSING_ARGS;
		
		if(primaryServer.checkLock(params[1]))
			return Commun.ERR_USER_ALREADY_CONNECTED;
		
		if(GestionFichiers.LireAuthentificationMD5(params[1], params[2], this.stamp)) {
			this.clientLogin = params[1];
			this.listMessages = GestionFichiers.LireMessages(clientLogin);
			this.setEtat(Etat.TRANSACTION);
			this.primaryServer.getView().update();
			return Commun.OK_HELLO + clientLogin + ", maildrop has "+listMessages.size()+" messages.";
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
		
		if(primaryServer.checkLock(params[1]))
			return Commun.ERR_USER_ALREADY_CONNECTED;
		
		if(GestionFichiers.LireAuthentification(params[1], null)) {
			this.clientLogin = params[1];
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
		
		if(this.clientLogin == null){
			return Commun.ERR_USER_NEEDED;
		}

		if(GestionFichiers.LireAuthentification(this.clientLogin, params[1])) {
			this.setEtat(Etat.TRANSACTION);
			this.listMessages = GestionFichiers.LireMessages(clientLogin);
			this.primaryServer.getView().update();
			return Commun.OK_HELLO + clientLogin + ", maildrop has "+listMessages.size()+" messages.";
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
			return "+OK " + this.listMessages.size() + " messages" 
					+ "(" + this.listMessages.getTotalOctets() + " octets" + ")\n" 
					+ this.listMessages.getTousLesMessages();
		}
		
		try {  
		    Integer.parseInt(params[1]);  
	    } catch(NumberFormatException nfe) {  
    		return Commun.ERR_INTEGER_ARGS; 
    	}
		
		if(this.listMessages.size() < Integer.parseInt(params[1]) || Integer.parseInt(params[1]) < 1) {
			return Commun.ERR_MESSAGE_NOT_EXISTS.replaceFirst("_NUMMSG_", this.listMessages.size()+"");
		} else {
			Message message = this.listMessages.get(Integer.parseInt(params[1])-1);
			if (message.getMarque()) {
				return "-ERR Message " + params[1] + " déjà supprimé";
			}
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
		
		if(this.listMessages.size() < Integer.parseInt(params[1]) || Integer.parseInt(params[1]) < 1) {
			return Commun.ERR_MESSAGE_NOT_EXISTS.replaceFirst("_NUMMSG_", this.listMessages.size()+"");
		} else {
			Message message = this.listMessages.get(Integer.parseInt(params[1])-1);
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

		if(this.listMessages.size() < Integer.parseInt(params[1]) || Integer.parseInt(params[1]) < 1) {
			return Commun.ERR_MESSAGE_NOT_EXISTS.replaceFirst("_NUMMSG_", this.listMessages.size()+"");
		} else {
			Message message = this.listMessages.get(Integer.parseInt(params[1])-1);
			if (message.getMarque()) {
				return "-ERR Message " + params[1] + " déjà supprimé";
			} else {
				message.setMarque(true);
				this.messagesToDelete++;
				return "+OK Message " + params[1] + " supprimé";
			}
		}
	}
	
	/**
	 * Traitement de la commande RSET
	 * @return String message
	 */
	private String commandeRSET() {
		this.messagesToDelete = 0;
		
		for (Message m : this.listMessages) {
			m.setMarque(false);
		}
		
		return "+OK ";
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
			int beforeDelete = this.listMessages.size();
			if(this.messagesToDelete > 0)
				GestionFichiers.SupprimerMessages(this.clientLogin, this.listMessages);
			this.listMessages = GestionFichiers.LireMessages(clientLogin);
			int afterDelete = this.listMessages.size();
			if(afterDelete != beforeDelete - this.messagesToDelete)
				return Commun.ERR_MARKED_MESSAGE;
		}
		
		return Commun.OK_SERVER_LOGOUT;
	}

	/**
	 * Traitement de la commande lorsque le serveur est dans l'état AUTHORISATION
	 * @param requete
	 * @return String sortie
	 */
	private String authorisation(String requete) {
		
		String[] params = requete.split(" ");
		
		switch(params[0]) {
			
			case "APOP" :
				return commandeAPOP(params);
			
			case "USER" :
				return commandeUSER(params);
				
			case "PASS" :
				return commandePASS(requete);
				
			case "QUIT" :
				return commandeQUIT(false);
				
			case "STAT" :
			case "LIST" :
			case "RETR" :
			case "DELE" :
			case "NOOP" :
			case "RSET" :
				return Commun.ERR_IMPOSSIBLE_COMMAND;
				
			default :
				return Commun.ERR_UNKNOWN_COMMAND;
		}
	}
	
	/**
	 * Traitement de la commande lorsque le serveur est dans l'état TRANSACTION
	 * @param requete
	 * @return String sortie
	 */
	private String transaction(String requete) {
		
		String [] params = requete.split(" ");
		
		switch(params[0]) {
		
			case "STAT" :
				return "+OK " + this.listMessages.size() + " " + this.listMessages.getTotalOctets();
				
			case "LIST" :
				return commandeLIST(params);
				
			case "RETR" :
				return commandeRETR(params);
				
			case "DELE" :
				return commandeDELE(params);
				
			case "NOOP" :
				return "+OK ";
				
			case "RSET" :
				return commandeRSET();
				
			case "QUIT" :
				return commandeQUIT(true);
				
			case "APOP" :
			case "USER" :
			case "PASS" :
				return Commun.ERR_IMPOSSIBLE_COMMAND;
			
			default :
				return Commun.ERR_UNKNOWN_COMMAND;
		}
	}
	
	/********
	 * 
	 * GETTER
	 * 
	 **************/
	
	public Etat getEtat() {
		return etat;
	}

	public String getClientLogin() {
		return clientLogin;
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

