package pop3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.oracle.jrockit.jfr.RequestableEvent;

import pop3.Commun.Etat;

public class Serveur {

	public final static int PORT = 110;

	private ServerSocket sc;
	private BufferedReader input;
	private PrintStream output;
	private Etat etat;

	public Serveur() throws IOException {
		this.sc = new ServerSocket(PORT);
		this.etat = Etat.INITIALISATION;
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

			output.println("+OK Bonjour");

			while(true) {
				String requete;
				if((requete = this.input.readLine()) != null){
					this.traiterRequete(requete);
				}
			}
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

	public void traiterRequete(String requete) {
		if(requete.startsWith("APOP"))
		{
			if(this.etat == Etat.CONNEXION)
			{
				this.etat = Etat.AUTHORISATION;
				this.afficherEtat();
				String [] id = requete.split(" ");
				if(this.checkIdentifiants(id[1], id[2])) {
					this.etat = Etat.TRANSACTION;
					this.afficherEtat();
					this.output.println("+OK Bonjour "+ id[1]);
				}
			}
		}
		//TODO Tout les autres commandes
		else if(requete.startsWith("")){
			
		}
		else {
			if(this.etat == Etat.TRANSACTION){
				this.output.println("-ERR Commande inconnue");
			}
		}
	}

	public boolean checkIdentifiants(String identifiant, String mdp) {
		if(identifiant == null || mdp == null) {
			return false;
		}
		else if(identifiant.equals("test") && mdp.equals("test")) {
			return true;
		}
		
		return false;
	}

	public void afficherEtat() {
		System.out.println("Etat du serveur : " + this.etat);
	}
}

