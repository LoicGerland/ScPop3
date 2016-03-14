package pop3;

import java.util.ArrayList;

public class ListeMessages extends ArrayList<Message> {
	private static final long serialVersionUID = 1L;

	private int octetsTotal;
	
	public String getTousLesMessages() {
		
		String liste = "";
		for( Message message : this) {
			liste += message.getNumero() + " " + message.getTailleOctets() + "\n";
		}
		liste += ".";
		
		return liste;
	}

	public int getOctetsTotal() {
		return octetsTotal;
	}
	public void setOctetsTotal(int octetsTotal) {
		this.octetsTotal = octetsTotal;
	}
}
