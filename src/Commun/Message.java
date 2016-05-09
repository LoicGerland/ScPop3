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
	private String date;
	
	/**
	 * Contenu du message
	 */
	private String sender;
	
	/**
	 * Contenu du message
	 */
	private String receiver;
	
	/**
	 * Contenu du message
	 */
	private String corps;
	
	/**
	 * Etiquette pour la suppression
	 */
	private Boolean marque;
	
	public String getMessageForSaving() {
		return this.date+this.sender+this.receiver+this.corps+"\n";
	}
	
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
	
	public String getDate() {
		return date;
	}

	public String getSender() {
		return sender;
	}

	public String getReceiver() {
		return receiver;
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
	
	public void setDate(String date) {
		this.date = date;
	}
	
	public void setSender(String sender) {
		this.sender = sender;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}
}
