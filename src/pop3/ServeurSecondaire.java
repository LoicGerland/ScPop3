package pop3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import pop3.Commun.Etat;

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
			this.serveurPrincipal.getVue().sop("Erreur - Initialisation des flux");
		}
	}

	public void run() {
		this.setEtat(Etat.CONNEXION);
		sendMessage("+OK POP3 server ready");
		
		this.setEtat(Etat.AUTHORISATION);
		
		while(this.running) {
		String requete;
			try {
				if((requete = this.input.readLine()) != null){
					this.traiterRequete(requete);
				}
			} catch (IOException e) {
				this.serveurPrincipal.getVue().sop("Erreur - Lecture du flux entrant");
			}
		}
		
		try {
			this.clientSocket.close();
		} catch (IOException e) {
			this.serveurPrincipal.getVue().sop("Erreur - Fermeture du socket");
		}
		serveurPrincipal.stopServeurSecondaire(this);
	}
	
	private void sendMessage(String message) {
		
		try {
			this.output.write(message+"\r\n");
			this.output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void traiterRequete(String requete) {
		
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
	
	private String commandeAPOP(String [] params) {
		
		if(params.length < 3)
			return "-ERR Pas assez de parametres";
		
		if(GestionFichiers.LireAuthentification(params[1], params[2])) {
			this.identifiantClient = params[1];
			this.listeMessages = GestionFichiers.LireMessages(identifiantClient);
			this.setEtat(Etat.TRANSACTION);
			return "+OK Bonjour "+ identifiantClient;
		}
		else {
			return "-ERR Identifiants incorrects";
		}
	}
	
	private String commandeUSER(String [] params) {
		
		if(params.length < 2)
			return "-ERR Pas assez de parametres";
		
		if(GestionFichiers.LireAuthentification(params[1], null)) {
			this.identifiantClient = params[1];
			return "+OK Le nom de boite est valide";

		}
		else {
			return "-ERR Nom de boite invalide";
		}
	}
	
	private String commandePASS(String [] params) {
		
		if(params.length < 2)
			return "-ERR Pas assez de parametres";
		
		if(this.identifiantClient == null){
			return "-ERR Commande USER necessaire avant";
		}

		if(GestionFichiers.LireAuthentification(this.identifiantClient, params[1])) {
			this.setEtat(Etat.TRANSACTION);
			this.listeMessages = GestionFichiers.LireMessages(identifiantClient);
			return "+OK Bonjour "+ identifiantClient;
		}
		else {
			return "-ERR Identifiants incorrects";
		}
	}
	
	private String commandeLIST(String [] params) {
		
		if(params.length < 2) {
			return "+OK " + this.listeMessages.size() + " messages" 
					+ "(" + this.listeMessages.getOctetsTotal() + " octets" + ")\n" 
					+ this.listeMessages.getTousLesMessages();
		}
		
		if(this.listeMessages.size() < Integer.parseInt(params[1])) {
			return "-ERR Le message n'existe pas, seulement " + this.listeMessages.size() + " messages dans votre boite";
		} else {
			Message message = this.listeMessages.get(Integer.parseInt(params[1])-1);
			return "+OK " + message.getNumero() + " " + message.getTailleOctets();
		}
	}
	
	private String commandeRETR(String [] params) {
		
		if(params.length < 2) {
			return "-ERR Pas assez de parametres";
		}
		
		if(this.listeMessages.size() < Integer.parseInt(params[1])) {
			return "-ERR Le message n'existe pas, seulement " + this.listeMessages.size() + " messages dans votre boite";
		} else {
			Message message = this.listeMessages.get(Integer.parseInt(params[1])-1);
			this.messageASupprimer = 0;
			return "+OK " + message.getTailleOctets() + " octets\n" + message.getCorps() + "\n.";
		}
	}
	
	private String commandeDELE(String [] params) {
		
		if(params.length < 2) {
			return "-ERR Pas assez de parametres";
		}

		if(this.listeMessages.size() < Integer.parseInt(params[1])) {
			return "-ERR Le message n'existe pas, seulement " + this.listeMessages.size() + " messages dans votre boite";
		} else {
			Message message = this.listeMessages.get(Integer.parseInt(params[1])-1);
			if (message.getMarque()) {
				return "-ERR message " + params[1] + " deja supprimé";
			} else {
				message.setMarque(true);
				this.messageASupprimer++;
				return "+OK message " + params[1] + " supprimé";
			}
		}
	}
	
	private String commandeRSET() {
		for (Message m : this.listeMessages) {
			m.setMarque(false);
		}
		
		return "+OK";
	}
	
	private String commandeQUIT(boolean delete) {
		
		this.running = false;
		
		if(delete) {
			this.setEtat(Etat.MISEAJOUR);
			int beforeDelete = this.listeMessages.size();
			GestionFichiers.SupprimerMessages(this.identifiantClient, this.listeMessages);
			this.listeMessages = GestionFichiers.LireMessages(identifiantClient);
			int afterDelete = this.listeMessages.size();
			if(afterDelete != beforeDelete - this.messageASupprimer)
				return "-ERR certains messages marqués comme effacés non effacés";
		}
		
		return "+OK POP3 server signing off";
	}

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
				sortie = commandePASS(params);
				break;
				
			case "QUIT" :
				sortie = commandeQUIT(false);
				break;
				
			case "STAT" :
			case "LIST" :
			case "DELE" :
			case "NOOP" :
			case "RSET" :
				sortie = "Commande impossible dans cet état";
				
			default :
				sortie = "-ERR Commande inconnue";
		}
		
		this.sendMessage(sortie);
	}
	
	private void transaction(String requete) {
		
		String [] params = requete.split(" ");
		String sortie = "";
		
		switch(params[0]) {
		
			case "STAT" :
				sortie = "+OK " + this.listeMessages.size() + " " + this.listeMessages.getOctetsTotal();
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
				
			case "RSET" :
				sortie = commandeRSET();
				
			case "QUIT" :
				sortie = commandeQUIT(true);
				break;
				
			case "APOP" :
			case "USER" :
			case "PASS" :
				sortie = "Commande impossible dans cet état";
				break;
			
			default :
				sortie = "-ERR Commande inconnue";
		}
		
		this.sendMessage(sortie);
	}
	
	public Etat getEtat() {
		return etat;
	}

	public void setEtat(Etat etat) {
		this.etat = etat;
		System.out.println("Etat du serveur : " + this.etat);
	}
	
	public String getIdentifiantClient() {
		return identifiantClient;
	}

	public Socket getClientSocket() {
		return clientSocket;
	}
}

