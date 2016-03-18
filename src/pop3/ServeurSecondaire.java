package pop3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import pop3.Commun.Etat;

public class ServeurSecondaire implements Runnable{

	private Vue vue;
	private final Socket clientSocket;
	private BufferedReader input;
	private BufferedWriter output;
	private Etat etat;
	private Boolean running;
	private ListeMessages listeMessages;
	private String identifiantClient;

	public ServeurSecondaire(Vue vue, Socket clientSocket) {
		this.vue = vue;
		this.clientSocket = clientSocket;
		
		this.running = true;
		this.setEtat(Etat.INITIALISATION);
		this.listeMessages = new ListeMessages();
		this.identifiantClient = "Inconnu";
	}

	public void run() {
		
		try {
			this.output = new BufferedWriter( new OutputStreamWriter(clientSocket.getOutputStream()));
			this.input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			this.setEtat(Etat.CONNEXION);
			this.vue.sop("Nouveau client ! Adresse : " + clientSocket.getInetAddress() + " Port : " + clientSocket.getPort());
			
			this.output.write("+OK POP3 server ready\r\n");
			this.output.flush();
			this.setEtat(Etat.AUTHORISATION);
		
			while(this.running) {
				String requete;
				if((requete = this.input.readLine()) != null){
					this.traiterRequete(requete);
				}
			}
		} 
		catch (IOException e) { this.vue.sop("Erreur : Probleme de socket"); }
		finally
		{
			try { this.clientSocket.close(); }
			catch (IOException e) {
				this.vue.sop("Erreur : Probleme de deconnexion de socket");
			}
		}
	}
	
	private void traiterRequete(String requete) {
		
		switch(this.etat) {
		
			case AUTHORISATION:
				this.authentification(requete);
				break;
				
			case TRANSACTION:
				this.transaction(requete);
				break;
				
			default: 
				this.error();
			
		}
	}
	
	private void error() {
		
		try {
			this.output.write("-ERR\r\n");
			this.output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void authentification(String requete) {
		
		String sortie = "";
		String[] params = requete.split(" ");
		
		if(params[0].equals("APOP")) {
			
			if(GestionFichiers.LireAuthentification(params[1], params[2])) {
				this.identifiantClient = params[1];
				this.setEtat(Etat.TRANSACTION);
				this.listeMessages = GestionFichiers.LireMessages(identifiantClient);
				sortie = "+OK Bonjour "+ identifiantClient;
			}
			else {
				sortie = "-ERR Identifiants incorrects";
			}
		}
		else if (params[0].equals("USER")) {
			
			if(params.length>1 && GestionFichiers.LireAuthentification(params[1], null)) {
				this.identifiantClient = params[1];
				sortie = "+OK Le nom de boite est valide";

			}
			else {
				sortie = "-ERR Nom de boite invalide";
			}
		}
		else if (params[0].equals("PASS")) {
			
			if(this.identifiantClient == null){
				sortie = "-ERR Commande USER necessaire avant";
			}
			else {
				if(GestionFichiers.LireAuthentification(this.identifiantClient, params[1])) {
					this.setEtat(Etat.TRANSACTION);
					this.listeMessages = GestionFichiers.LireMessages(identifiantClient);
					sortie = "+OK Bonjour "+ identifiantClient;
				}
				else {
					sortie = "-ERR Identifiants incorrects";
				}
			}
		}
		else if (params[0].equals("QUIT")) {
			sortie = "+OK POP3 server signing off";
			this.running = false;
		}
		
		try {
			this.output.write(sortie+"\r\n");
			this.output.flush();
		} catch (IOException e) {
			this.vue.sop("Erreur : Probleme de socket");
		}
	}
	
	private void transaction(String requete) {
		
		String [] params = requete.split(" ");
		String sortie = "+OK ";
		
		if(params[0].equals("STAT")) {
			sortie += this.listeMessages.size() + " " + this.listeMessages.getOctetsTotal();
		}
		else if(params[0].equals("LIST")) {
			if(params.length > 1) {
				if(this.listeMessages.size() < Integer.parseInt(params[1])) {
					sortie = "-ERR Le message n'existe pas, seulement " + this.listeMessages.size() + " messages dans votre boite";
				} else {
					Message message = this.listeMessages.get(Integer.parseInt(params[1])-1);
					sortie += message.getNumero() + " " + message.getTailleOctets();
				}
			} else {
				sortie += this.listeMessages.size() + " messages" 
						+ "(" + this.listeMessages.getOctetsTotal() + " octets" + ")\n" 
						+ this.listeMessages.getTousLesMessages();
			}
		}
		else if(params[0].equals("RETR")) {
			
			if(params.length > 1) {
				if(this.listeMessages.size() < Integer.parseInt(params[1])) {
					sortie = "-ERR Le message n'existe pas, seulement " + this.listeMessages.size() + " messages dans votre boite";
				} else {
					Message message = this.listeMessages.get(Integer.parseInt(params[1])-1);
					sortie += message.getTailleOctets() + " octets\n" + message.getCorps() + "\n.";
				}
			} else {
				sortie = "-ERR Parametre requis";
			}
		}
		else if (params[0].equals("DELE")) {
			
			if(params.length > 1) {
				if(this.listeMessages.size() < Integer.parseInt(params[1])) {
					sortie = "-ERR Le message n'existe pas, seulement " + this.listeMessages.size() + " messages dans votre boite";
				} else {
					Message message = this.listeMessages.get(Integer.parseInt(params[1])-1);
					if (message.getMarque()) {
						sortie = "-ERR message " + params[1] + " deja supprimé";
					} else {
						message.setMarque(true);
						sortie += "message " + params[1] + " supprimé";
					}
				}
			}
			else {
				sortie = "-ERR Parametre requis";
			}
		}
		else if (params[0].equals("NOOP")) {
			sortie += ""; 
		}
		else if (params[0].equals("RSET")) {
			for (Message m : this.listeMessages) {
				if(!m.getMarque()) {
					m.setMarque(false);
				}
			}
		}
		else if (params[0].equals("QUIT")) {
			this.setEtat(Etat.MISEAJOUR);
			this.running = false;
			GestionFichiers.SupprimerMessages(this.identifiantClient, this.listeMessages);
		}
		else {
			sortie = "-ERR Commande inconnue";
		}
		
		try {
			this.output.write(sortie+"\r\n");
			this.output.flush();
		} catch (IOException e) {
			this.vue.sop("Erreur : Probleme de socket");
		}
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

