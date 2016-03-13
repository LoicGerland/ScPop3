package pop3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.oracle.jrockit.jfr.RequestableEvent;

import pop3.Commun.Etat;

public class Serveur {

	public final static int PORT = 110;

	private ServerSocket sc;
	private BufferedReader input;
	private PrintStream output;
	private Etat etat;
	private Boolean run;
	private List<Message> verrouMessages;

	public Serveur() throws IOException {
		this.sc = new ServerSocket(PORT);
		this.etat = Etat.INITIALISATION;
		this.run = true;
		this.verrouMessages = new ArrayList<Message>();
	}

	public void run() {

		System.out.println("Lancement du serveur sur le port : 	"+ sc.getLocalPort());
		this.afficherEtat();

		try {
			Socket client = this.sc.accept();
			this.etat = Etat.CONNEXION;
			this.afficherEtat();

			System.out.println("Nouveau client ! Adresse : " + client.getInetAddress() + " Port : " + client.getPort());
			this.output = new PrintStream(client.getOutputStream());
			this.input = new BufferedReader(new InputStreamReader(client.getInputStream()));

			output.println("+OK POP3 server ready");
			
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

	private void traiterRequete(String requete) {
		
		switch(this.etat) {
		
		case AUTHORISATION:
			this.connexion(requete);
			break;
			
		case TRANSACTION:
			this.transaction(requete);
			break;
			
		default: this.output.println("-ERR"); 
		}
	}
	
	
	private void connexion(String requete) {
		
		if(requete.startsWith("APOP")) {
			
			this.etat = Etat.AUTHORISATION;
			this.afficherEtat();
			
			String [] id = requete.split(" ");
			if(this.checkIdentifiants(id[1], id[2])) {
				
				this.etat = Etat.TRANSACTION;
				this.afficherEtat();
				this.output.println("+OK Bonjour "+ id[1]);
			}
			else {
				this.output.println("-ERR Identifiants incorrects");
			}
		}
		else if (requete.startsWith("QUIT")) {
			
			this.output.println("+OK POP3 server signing off");
			this.run = false;
		}
	}
	
	private void transaction(String requete) {
		
		if(requete.startsWith("STAT")) {
			this.output.println("+OK " +this.verrouMessages.size() + " " + "Taille en octet");
		}
		else if(requete.startsWith("LIST")) {
			//TODO récupération du paramètre
			this.output.println("+OK " + this.verrouMessages.size() + " messages" + "(" + "Taille en octets" + ")");
		}
		else if(requete.startsWith("RETR")) {
			//TODO récupération du paramètre
		}
		else if (requete.startsWith("DELE")) {
			//TODO récupération du paramètre 
		}
		else if (requete.startsWith("NOOP")) {
			this.output.println("+OK");
		}
		else if (requete.startsWith("RSET")) {
			
		}
		else if (requete.startsWith("QUIT")) {
			this.etat = Etat.MISEAJOUR;
			
		}
		else {
			this.output.println("-ERR Commande inconnue");
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

	private void afficherEtat() {
		System.out.println("Etat du serveur : " + this.etat);
	}
}

