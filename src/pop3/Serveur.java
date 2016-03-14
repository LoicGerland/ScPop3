package pop3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import pop3.Commun.Etat;

public class Serveur {

	public final static int PORT = 110;

	private ServerSocket sc;
	private BufferedReader input;
	private BufferedWriter output;
	private Etat etat;
	private Boolean run;
	private ListeMessages verrouMessages;
	private String identifiantClient;

	public Serveur() throws IOException {
		this.sc = new ServerSocket(PORT);
		this.etat = Etat.INITIALISATION;
		this.run = true;
		this.verrouMessages = new ListeMessages();
	}

	public void run() {

		System.out.println("Lancement du serveur " + sc.getInetAddress().getHostAddress() + " sur le port : 	" + sc.getLocalPort());
		this.afficherEtat();

		try {
			Socket client = this.sc.accept();
			this.etat = Etat.CONNEXION;
			this.afficherEtat();

			System.out.println("Nouveau client ! Adresse : " + client.getInetAddress() + " Port : " + client.getPort());
			this.output = new BufferedWriter( new OutputStreamWriter(client.getOutputStream()));
			this.input = new BufferedReader(new InputStreamReader(client.getInputStream()));

			this.output.write("+OK POP3 server ready\r\n");
			this.output.flush();

			this.etat = Etat.AUTHORISATION;
			this.afficherEtat();
			
			while(this.run) {
				String requete;
				if((requete = this.input.readLine()) != null){
					this.traiterRequete(requete);
				}
			}
			
			System.out.println("Fin du serveur");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally
		{
			try
			{
				this.sc.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	private void traiterRequete(String requete) throws IOException {
		
		switch(this.etat) {
		
		case AUTHORISATION:
			this.authentification(requete);
			break;
			
		case TRANSACTION:
			this.transaction(requete);
			break;
			
		default: this.output.write("-ERR\r\n"); 
		}
	}
	
	
	private void authentification(String requete) {
		
		String sortie = "";
		String params[];
		
		if(requete.startsWith("APOP")) {
			
			params = requete.split(" ");
			if(this.checkIdentifiants(params[1], params[2])) {
				this.identifiantClient = params[1];
				this.etat = Etat.TRANSACTION;
				this.afficherEtat();
				this.verrouMessages = LireFichiers.LireMessages(identifiantClient);
				sortie = "+OK Bonjour "+ identifiantClient;
			}
			else {
				sortie = "-ERR Identifiants incorrects";
			}
		}
		else if (requete.startsWith("USER")) {
			
			params = requete.split(" ");
			
			if(this.checkIdentifiants(params[1])) {
				this.identifiantClient = params[1];
				sortie = "+OK Le nom de boite est valide";
			}
			else {
				sortie = "-ERR Nom de boite invalide";
			}
		}
		else if (requete.startsWith("PASS")) {
			
			if(this.identifiantClient == null){
				sortie = "-ERR Commande USER necessaire avant";
			}
			else {
				params = requete.split(" ");
				
				if(this.checkIdentifiants(this.identifiantClient, params[1])) {
					this.etat = Etat.TRANSACTION;
					this.afficherEtat();
					this.verrouMessages = LireFichiers.LireMessages(identifiantClient);
					sortie = "+OK Bonjour "+ identifiantClient;
				}
				else {
					sortie = "-ERR Identifiants incorrects";
				}
			}
		}
		else if (requete.startsWith("QUIT")) {
			sortie = "+OK POP3 server signing off";
			this.run = false;
		}
		
		try {
			this.output.write(sortie+"\r\n");
			this.output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void transaction(String requete) {
		
		try {
			
			String [] params;
			String sortie = "+OK ";
			
			if(requete.startsWith("STAT")) {
				sortie += this.verrouMessages.size() + " " + this.verrouMessages.getOctetsTotal();
			}
			else if(requete.startsWith("LIST")) {
				params = requete.split(" ");
				
				if(params.length > 1) {
					if(this.verrouMessages.size() < Integer.parseInt(params[1])) {
						sortie = "-ERR Le message n'existe pas, seulement " + this.verrouMessages.size() + " messages dans votre boite";
					} else {
						Message message = this.verrouMessages.get(Integer.parseInt(params[1])-1);
						sortie += message.getNumero() + " " + message.getTailleOctets();
					}
				} else {
					sortie += this.verrouMessages.size() + " messages" 
							+ "(" + this.verrouMessages.getOctetsTotal() + " octets" + ")\n" 
							+ this.verrouMessages.getTousLesMessages();
				}
			}
			else if(requete.startsWith("RETR")) {
				params = requete.split(" ");
				if(params.length > 1) {
					if(this.verrouMessages.size() < Integer.parseInt(params[1])) {
						sortie = "-ERR Le message n'existe pas, seulement " + this.verrouMessages.size() + " messages dans votre boite";
					} else {
						Message message = this.verrouMessages.get(Integer.parseInt(params[1])-1);
						sortie += message.getTailleOctets() + " octets\n" + message.getCorps() + "\n.";
					}
				} else {
					sortie = "-ERR Paramètre requis";
				}
			}
			else if (requete.startsWith("DELE")) {
				params = requete.split(" ");
				
				if(params.length > 1) {
					if(this.verrouMessages.size() < Integer.parseInt(params[1])) {
						sortie = "-ERR Le message n'existe pas, seulement " + this.verrouMessages.size() + " messages dans votre boite";
					} else {
						Message message = this.verrouMessages.get(Integer.parseInt(params[1])-1);
						if (message.getMarque()) {
							sortie = "-ERR message " + params[1] + " déjà supprimé";
						} else {
							message.setMarque(true);
							sortie += "message " + params[1] + " supprimé";
						}
					}
				}
				else {
					sortie = "-ERR Paramètre requis";
				}
			}
			else if (requete.startsWith("NOOP")) {
				sortie += ""; 
			}
			else if (requete.startsWith("RSET")) {
				for (Message m : this.verrouMessages) {
					if(!m.getMarque()) {
						m.setMarque(false);
					}
				}
			}
			else if (requete.startsWith("QUIT")) {
				this.etat = Etat.MISEAJOUR;
				this.afficherEtat();
				this.run = false;
				LireFichiers.SupprimerMessages(this.identifiantClient, this.verrouMessages);
			}
			else {
				sortie = "-ERR Commande inconnue";
			}
			
			this.output.write(sortie+"\r\n");
			this.output.flush();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private boolean checkIdentifiants(String identifiant, String mdp) {
		if(identifiant == null || mdp == null) {
			return false;
		}
		else if(identifiant.equals("test") && mdp.equals("test")) {
			return true;
		}
		
		return false;
	}
	
	private boolean checkIdentifiants(String identifiant) {
		if(identifiant == null){
			return false;
		}
		else if(identifiant.equals("test")){
			return true;
		}
		
		return false;
	}

	private void afficherEtat() {
		System.out.println("Etat du serveur : " + this.etat);
	}
}

