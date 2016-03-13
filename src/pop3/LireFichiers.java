package pop3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class LireFichiers {

	public static ArrayList<Message> LireMessages(String identifiant) {
		
		ArrayList<Message> messages = new ArrayList<Message>();
		String filePath = new File("").getAbsolutePath();
		filePath += "/Fichiers/" + identifiant + ".txt";
		
		
		try {
			BufferedReader buff = new BufferedReader(new FileReader(filePath));
		 
			try {
				String line;
				String[] parts;
				int i = 0;
				while ((line = buff.readLine()) != null) {
					if(i != 0) {
						parts = line.split(";");
						Message nouveauMessage = new Message();
						nouveauMessage.setNumero(i);
						nouveauMessage.setTailleOctets(Integer.parseInt(parts[1]));
						nouveauMessage.setMarque(false);
						nouveauMessage.setCorps(parts[2]);
						//System.out.println(nouveauLieu);
						messages.add(nouveauMessage);
					}
					i++;
				}
			} finally {
				buff.close();
			}
		} catch (IOException ioe) { System.out.println("Erreur IO --" + ioe.toString());}
		
		return messages;
	}
}
