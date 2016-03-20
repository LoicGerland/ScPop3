package pop3;

/**
 * Classe représentant les variables final et/ou static du serveur
 * 
 * @author GERLAND - LETOURNEUR
 */
public class Commun {

	/**
	 * Port du serveur POP3
	 */
	public final static int PORT = 110;
	
	/**
	 * Etat du serveur POP3
	 */
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
	
	/**
	 * Messages d'exceptions
	 */
	public final static String ERROR_NOT_ALLOWED = "Erreur : Instanciation du socket impossible";
	public final static String ERROR_SOCKET_INSTANTIATION = "Erreur : Instanciation du socket impossible";
	public final static String ERROR_CLOSE_SOCKET = "Erreur : Fermeture de socket";
	public final static String ERROR_STOP_SERVER = "Erreur : Impossible d'arrêter le serveur";
	public final static String ERROR_FLUX_INSTANTIATION = "Erreur - Initialisation des flux";
	public final static String ERROR_FLUX_READING = "Erreur - Lecture du flux entrant";
	
	/**
	 * Messages de retour du serveur au client
	 */
	public final static String ERR_MISSING_ARGS = "-ERR Paramêtres manquants";
	public final static String ERR_UNKNOWN_COMMAND = "-ERR Commande inconnue";
	public final static String ERR_IMPOSSIBLE_COMMAND = "-ERR Commande impossible dans cet état";
}
