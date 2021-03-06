package smtp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import Commun.Commun;
import Commun.GestionFichiers;
import Commun.Message;
import Commun.Commun.EtatSMTP;

/**
 * Classe représentant le serveur serveur secondaire
 * qui communique avec le client
 * 
 * @author GERLAND - LETOURNEUR
 */
public class ServeurSecondaire implements Runnable{

	private Serveur primaryServer;
	private Boolean running;
	private EtatSMTP etat;
	private BufferedReader input;
	private BufferedWriter output;
	
	private final Socket clientSocket;
	private String sender;
	private ArrayList<String> receivers;
	private Message message;
	
	/**
	 * Constructeur
	 * @param serveur
	 * @param clientSocket
	 */
	public ServeurSecondaire(Serveur serveur, Socket clientSocket) {
		this.primaryServer = serveur;
		this.clientSocket = clientSocket;
		
		//Initialisation du serveur secondaire
		this.running = true;
		this.setEtat(EtatSMTP.INITIALISATION);
		this.reset();
		
		try {
			this.output = new BufferedWriter( new OutputStreamWriter(clientSocket.getOutputStream()));
			this.input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			this.primaryServer.getView().sop(Commun.ERROR_FLUX_INSTANTIATION);
		}
	}
	
	/**
	 * Réinitialisation de l'émetteur, des destinataires et du message
	 */
	private void reset() {
		this.sender = "";
		this.receivers = new ArrayList<String>();
		this.message = new Message();
	}
	
	/**
	 * Lancement du serveur secondaire
	 * Envoi du message de bienvenue
	 * Lecture du flux entrant pour traiter les commande du client
	 */
	public void run() {
		
		this.primaryServer.getView().sop("Connexion d'un client : "+clientSocket.getInetAddress().getHostAddress());
		
		sendMessage(Commun.SMTP_SERVER_READY);
		
		this.setEtat(EtatSMTP.CONNEXION);
		
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
		try {
			this.clientSocket.close();
		} catch (IOException e) {
			this.primaryServer.getView().sop(Commun.ERROR_CLOSE_SOCKET);
		}
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
		
		String sortie = "";
		
		this.primaryServer.getView().sop("Client "+clientSocket.getInetAddress().getHostAddress()+" : "+requete);
		
		switch(this.etat) {
		
			case CONNEXION:
				sortie = this.connexion(requete);
				break;
			
			case PRESENTATION:
				sortie = this.presentation(requete);
				break;
				
			case DESTINATION:
				sortie = this.destination(requete);
				break;
				
			case DESTINATIONMULTIPLE:
				sortie = this.destinationMultiple(requete);
				break;
				
			case LECTURE:
				sortie = this.lecture(requete);
				break;
			
			default: 
				sortie = Commun.SMTP_500_UNKNOWN_COMMAND;
		}
		
		this.primaryServer.getView().sop(sortie.substring(0,3));
		this.sendMessage(sortie);
	}
	
	/**
	 * Traitement de la commande EHLO
	 * @param params
	 * @return String message
	 */
	private String commandeEHLO(String [] params) {

		//Si il manque le 2ème paramètre, on envoi un message d'erreur
		if(params.length < 2)
			return Commun.SMTP_504_MISSING_ARGS;

		//Si le serveur est indisponible, on envoi un message d'erreur
		if(this.primaryServer.getLocked())
			return Commun.SMTP_550_UNAVAILABLE;
		
		this.setEtat(EtatSMTP.PRESENTATION);
		
		this.primaryServer.getView().sop("Client "+clientSocket.getInetAddress().getHostAddress()+" depuis domaine "+params[1]);
		
		return Commun.SMTP_250_HELLO+params[1];
	}
	
	
	/**
	 * Traitement de la commande RSET
	 * @param params
	 * @return String message
	 */
	private String commandeRSET() {
		
		this.reset();
		
		this.setEtat(EtatSMTP.PRESENTATION);
		
		return "250 OK";
	}
	
	/**
	 * Traitement de la commande MAIL
	 * @param params
	 * @return String message
	 */
	private String commandeMAIL(String requete) {
		
		//Si la syntaxe de la commande est fausse, on envoi un message d'erreur
		if(!requete.contains("<") || !requete.contains(">"))
			return Commun.SMTP_553_WRONG_SYNTAX;

		//Si le serveur est indisponible, on envoi un message d'erreur
		if(this.primaryServer.getLocked())
			return Commun.SMTP_550_UNAVAILABLE;
		
		String senderAdress = requete.substring(requete.indexOf("<")+1, requete.indexOf(">"));

		//Si l'adresse mail dépasse la taille autorisée, on envoi un message d'erreur
		if(senderAdress.length() > Commun.MAX_MAIL_SIZE)
			return Commun.SMTP_552_MEMORY_ERROR;
		
		this.reset();
		this.sender = senderAdress;
		
		this.primaryServer.getView().sop("Client "+clientSocket.getInetAddress().getHostAddress()+" depuis adresse "+senderAdress);
		
		this.setEtat(EtatSMTP.DESTINATION);
		
		return "250 OK";
	}
	
	/**
	 * Traitement de la commande RCPT
	 * @param requete
	 * @return String message
	 */
	private String commandeRCPT(String requete) {
		
		//Si la syntaxe de la commande est fausse, on envoi un message d'erreur
		if(!requete.contains("<") || !requete.contains(">") || !requete.contains("@"))
			return Commun.SMTP_553_WRONG_SYNTAX;
		
		//Si le serveur est indisponible, on envoi un message d'erreur
		if(this.primaryServer.getLocked())
			return Commun.SMTP_550_UNAVAILABLE;
		
		String receiver = requete.substring(requete.indexOf("<")+1, requete.indexOf(">"));
		
		//Si le domaine n'est pas le bon, on envoi un message d'erreur
		if(!receiver.contains(Commun.DOMAIN_SMTP))
			return Commun.SMTP_551_NOT_LOCAL;
		
		//Si l'adresse mail dépasse la taille autorisée, on envoi un message d'erreur
		if(receiver.length() > Commun.MAX_MAIL_SIZE)
			return Commun.SMTP_552_MEMORY_ERROR;
		
		receiver = receiver.substring(0, receiver.indexOf("@"));
		
		//Si la boite n'existe pas, on envoi un message d'erreur
		if(!GestionFichiers.LireAuthentification(receiver, null))
			return Commun.SMTP_550_UNAVAILABLE;
		
		receivers.add(receiver);
		this.setEtat(EtatSMTP.DESTINATIONMULTIPLE);
		
		return "250 OK";
	}
	
	/**
	 * Traitement de la commande DATA
	 * @return String message
	 */
	private String commandeDATA() {
		
		this.setEtat(EtatSMTP.LECTURE);
		
		return Commun.SMTP_354_START_MAIL;
	}
	
	/**
	 * Traitement de la commande QUIT
	 * @return String message
	 */
	private String commandeQUIT() {
		
		this.running = false;
		
		return Commun.SMTP_SERVER_CLOSED;
	}

	/**
	 * Traitement de la commande lorsque le serveur est dans l'état CONNEXION
	 * @param requete
	 * @return String sortie
	 */
	private String connexion(String requete) {
		
		String[] params = requete.split(" ");
		
		switch(params[0]) {
			
			case "EHLO" :
				return commandeEHLO(params);
				
			case "RSET" :
				return commandeRSET();
				
			case "QUIT" :
				return commandeQUIT();
				
			//Commande inattendue
			case "RCPT" :
			case "MAIL" :
			case "DATA" :
				return Commun.SMTP_503_SEQUENCE_COMMAND;
			
			//Commande inconnue
			default :
				return Commun.SMTP_500_UNKNOWN_COMMAND;
		}
	}
	
	/**
	 * Traitement de la commande lorsque le serveur est dans l'état PRESENTATION
	 * @param requete
	 * @return String sortie
	 */
	private String presentation(String requete) {
		
		String[] params = requete.split(" ");
		
		switch(params[0]) {
			
			case "RSET" :
				return commandeRSET();
				
			case "MAIL" :
				return commandeMAIL(requete);
				
			case "QUIT" :
				return commandeQUIT();
			
			//Commande inattendue
			case "RCPT" :
			case "EHLO" :
			case "DATA" :
				return Commun.SMTP_503_SEQUENCE_COMMAND;
			
			//Commande inconnue
			default :
				return Commun.SMTP_500_UNKNOWN_COMMAND;
		}
	}
	
	/**
	 * Traitement de la commande lorsque le serveur est dans l'état DESTINATION
	 * @param requete
	 * @return String sortie
	 */
	private String destination(String requete) {
		
		String [] params = requete.split(" ");
		
		switch(params[0]) {
			
			case "RSET" :
				return commandeRSET();
			
			case "RCPT" :
				return commandeRCPT(requete);
				
			case "QUIT" :
				return commandeQUIT();
			
			//Commande inattendue
			case "DATA" :
			case "EHLO" :
			case "MAIL" :
				return Commun.SMTP_503_SEQUENCE_COMMAND;
			
			//Commande inconnue
			default :
				return Commun.SMTP_500_UNKNOWN_COMMAND;
		}
	}
	
	/**
	 * Traitement de la commande lorsque le serveur est dans l'état DESTINATION MULTIPLE
	 * @param requete
	 * @return String sortie
	 */
	private String destinationMultiple(String requete) {
		
		String [] params = requete.split(" ");
		
		switch(params[0]) {
			
			case "RSET" :
				return commandeRSET();
			
			case "RCPT" :
				return commandeRCPT(requete);
				
			case "DATA" :
				return commandeDATA();
				
			case "QUIT" :
				return commandeQUIT();
			
			//Commande inattendue
			case "EHLO" :
			case "MAIL" :
				return Commun.SMTP_503_SEQUENCE_COMMAND;
			
			//Commande inconnue
			default :
				return Commun.SMTP_500_UNKNOWN_COMMAND;
		}
	}
	
	/**
	 * Traitement de la commande lorsque le serveur est dans l'état LECTURE
	 * @param requete
	 * @return String sortie
	 */
	private String lecture(String requete) {
		
		this.message.setCorps(requete);
		
		//Tant qu'il n'y a pas le ".", on enregistre le message
		while(!requete.equals(".")) {
			try {
				if((requete = this.input.readLine()) != null){
					this.message.setCorps(this.message.getCorps()+"\n"+requete);
					
					//Si le message dépasse la taille autorisée, on envoi un message d'erreur
					if(this.message.getCorps().length() > Commun.MAX_MESSAGE_SIZE)
						return Commun.SMTP_552_MEMORY_ERROR;
				}
			} catch (IOException e) { e.printStackTrace(); }
		}
		
		//Ajout de la date au message
		Date aujourdhui = new Date();
		SimpleDateFormat formater = new SimpleDateFormat("dd/MM/yy");
		this.message.setDate("Orig-date:"+formater.format(aujourdhui)+"\n");
		
		//Ajout de l'emetteur
		this.message.setSender("From:"+this.sender+"\n");
		
		//Envoi du message pour chaque destinataire
		for(String receiver : this.receivers) {
			this.message.setReceiver("To:"+receiver+"@"+Commun.DOMAIN_SMTP+"\n");
			GestionFichiers.AjouterMessage(receiver, message);
		}
		
		this.primaryServer.getView().sop("Client "+clientSocket.getInetAddress().getHostAddress()+" ("+this.sender+") a envoyé un message");
		
		this.reset();
		this.setEtat(EtatSMTP.PRESENTATION);
		
		return "250 OK";
	}
	
	/********
	 * 
	 * GETTER
	 * 
	 **************/
	
	public EtatSMTP getEtat() {
		return etat;
	}

	public String getClientLogin() {
		return sender;
	}

	public Socket getClientSocket() {
		return clientSocket;
	}
	
	/********
	 * 
	 * SETTER
	 * 
	 **************/
	
	public void setEtat(EtatSMTP etat) {
		this.etat = etat;
		this.primaryServer.getView().sop("Etat du serveur : " + this.etat);
	}
}

