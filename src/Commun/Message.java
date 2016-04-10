package Commun;

/**
 * Classe représentant un message POP3
 * 
 * @author GERLAND - LETOURNEUR
 */
public class Message {
	
	/**
	 * Numéro du message dans la boite aux lettres
	 */
	private int numero;
	
	/**
	 * Nombre de caractères dans le message
	 */
	private int tailleOctets;
	
	/**
	 * Contenu du message
	 */
	private String corps;
	
	/**
	 * Etiquette pour la suppression
	 */
	private Boolean marque;
	
	
	/********
	 * 
	 * GETTER
	 * 
	 **************/
	
	public int getNumero() {
		return numero;
	}
	
	public int getTailleOctets() {
		return tailleOctets;
	}

	public String getCorps() {
		return corps;
	}
	
	public Boolean getMarque() {
		return marque;
	}
	
	/********
	 * 
	 * SETTER
	 * 
	 **************/
	
	public void setNumero(int numero) {
		this.numero = numero;
	}
	
	public void setTailleOctets(int tailleOctets) {
		this.tailleOctets = tailleOctets;
	}
	
	public void setCorps(String corps) {
		this.corps = corps;
	}
	
	public void setMarque(Boolean marque) {
		this.marque = marque;
	}	
}
