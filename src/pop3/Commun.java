package pop3;

public class Commun {

	public final static int PORT = 110;
	
	public enum Etat {
		
		INITIALISATION("Initialisation"),
		CONNEXION("Connexion"),
		AUTHORISATION("Authorisation"),
		AUTHENTIFICATION("Authentification"),
		TRANSACTION("Transaction"),
		MISEAJOUR("Mise à jour");

		private String nom = "";
		Etat(String nom) { this.nom = nom; }
		public String toString() { return nom; }
	}
	
	public static void sop(String string) {
		System.out.println(string);
	}
}
