package pop3;

import java.util.ArrayList;

/**
 * Classe représentant la boite aux lettres POP3
 * 
 * @author GERLAND - LETOURNEUR
 */
public class ListeMessages extends ArrayList<Message> {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Construire un chaine de caractères avec tous les messages
	 * séparés par un retour charriot
	 * 
	 * @return String liste des messages
	 */
	public String getTousLesMessages() {
		
		String liste = "";
		for( Message message : this) {
			liste += message.getNumero() + " " + message.getTailleOctets() + "\n";
		}
		liste += ".";
		
		return liste;
	}
	
	/**
	 * Total des tailles de messages dans la boite aux lettres
	 * 
	 * @return int octets
	 */
	public int getTotalOctets() {
		
		int octets = 0;
		for( Message message : this) {
			octets += message.getCorps().length();
		}
		
		return octets;
	}

}
