package Commun;

/**
 * Classe représentant les variables final et/ou static du serveur
 * 
 * @author GERLAND - LETOURNEUR
 */
public class Commun {

	/**
	 * Port du serveur POP3
	 */
	public final static int PORT_POP3S = 1026;
	
	/**
	 * Port du serveur SMTP
	 */
	public final static int PORT_SMTP = 25;//1027;
	
	/**
	 * Etat du serveur POP3
	 */
	public enum EtatPOP3 {
		
		INITIALISATION("Initialisation"),
		CONNEXION("Connexion"),
		AUTHORISATION("Authorisation"),
		AUTHENTIFICATION("Authentification"),
		TRANSACTION("Transaction"),
		MISEAJOUR("Mise à jour");

		private String nom = "";
		EtatPOP3(String nom) { this.nom = nom; }
		public String toString() { return nom; }
	}
	
	/**
	 * Domaine SMTP supporté
	 */
	public final static String DOMAIN_SMTP = "gerletourn.com";
	
	/**
	 * Etat du serveur SMTP
	 */
	public enum EtatSMTP {
		
		INITIALISATION("Initialisation"),
		CONNEXION("Connexion"),
		PRESENTATION("Présentation"),
		DESTINATION("Destination"),
		DESTINATIONMULTIPLE("Destination Multiple"),
		LECTURE("Lecture");

		private String nom = "";
		EtatSMTP(String nom) { this.nom = nom; }
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
	public final static String ERROR_SEND_MESSAGE = "Erreur - Envoi du message";
	
	/**
	 * Messages de retour du serveur au client POP3
	 */
	public final static String OK_SERVER_READY = "+OK POP3 server ready";
	public final static String OK_HELLO = "+OK Bonjour ";
	public final static String OK_BOX_NAME = "+OK Le nom de boite est valide";
	public final static String OK_SERVER_LOGOUT = "+OK Déconnexion du serveur POP3";
	public final static String ERR_MISSING_ARGS = "-ERR Paramètre(s) manquant(s)";
	public final static String ERR_INTEGER_ARGS = "-ERR Paramètres non conformes (entier)";
	public final static String ERR_UNKNOWN_COMMAND = "-ERR Commande inconnue";
	public final static String ERR_IMPOSSIBLE_COMMAND = "-ERR Commande impossible dans cet état";
	public final static String ERR_WRONG_LOGIN = "-ERR Identifiants incorrects";
	public final static String ERR_WRONG_BOX_NAME = "-ERR Nom de boite invalide";
	public final static String ERR_USER_NEEDED = "-ERR Commande USER nécessaire avant";
	public final static String ERR_MARKED_MESSAGE = "-ERR Certains messages marqués comme effacés non effacés";
	public final static String ERR_MESSAGE_NOT_EXISTS = "-ERR Le message n'existe pas, seulement _NUMMSG_ message(s) dans votre boite";
	public final static String ERR_USER_ALREADY_CONNECTED = "-ERR Utilisateur déjà connecté";
	
	/**
	 * Messages de retour du serveur au client SMTP
	 */
	public final static String SMTP_SERVER_READY = "220 Simple Mail Transfer Service Ready";
	public final static String SMTP_SERVER_CLOSED = "221 Fermeture du canal de transmission";
	public final static String SMTP_250_HELLO = "250 Bonjour ";
	public final static String SMTP_354_START_MAIL = "354 Début du message ; fin avec <CRLF>.<CRLF>";
	public final static String SMTP_501_ARGS = "501 Erreur de syntaxe dans les paramètres ou les arguments";
	public final static String SMTP_500_UNKNOWN_COMMAND = "500 Erreur de syntaxe, commande non reconnue";
	public final static String SMTP_503_SEQUENCE_COMMAND = "503 Mauvaise séquence de commandes";
	public final static String SMTP_504_MISSING_ARGS = "504 Paramètre(s) manquant(s)";
	public final static String SMTP_553_UNKNOWN_USER = "553 Action demandée non effectuée : nom de boîte à lettres interdit";
	public final static String SMTP_551_NOT_LOCAL = "551 Usager non local. Prière d’essayer";
}
