package pop3;

public class Commun {

	public enum Etat {
		
		INITIALISATION("Initialisation"),
		CONNEXION("Connexion"),
		AUTHORISATION("Authorisation"),
		AUTHENTIFICATION("Authentification"),
		TRANSACTION("Transaction"),
		MISEAJOUR("Mise à jour");

		private String nom = "";

		Etat(String nom)
		{
			this.nom = nom;
		}

		public String toString()
		{
			return nom;
		}
	}

}
