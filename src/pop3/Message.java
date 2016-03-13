package pop3;

public class Message {
	
	private int numero;
	private int tailleOctets;
	private String corps;
	private Boolean marque;
	
	public int getNumero() {
		return numero;
	}

	public void setNumero(int numero) {
		this.numero = numero;
	}

	public int getTailleOctets() {
		return tailleOctets;
	}

	public void setTailleOctets(int tailleOctets) {
		this.tailleOctets = tailleOctets;
	}

	public String getCorps() {
		return corps;
	}

	public void setCorps(String corps) {
		this.corps = corps;
	}

	public Boolean getMarque() {
		return marque;
	}

	public void setMarque(Boolean marque) {
		this.marque = marque;
	}	
}
