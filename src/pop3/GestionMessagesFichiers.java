package pop3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class GestionMessagesFichiers {

	public static ListeMessages LireMessages(String identifiant) {
		
		ListeMessages messages = new ListeMessages();
		String filePath = new File("").getAbsolutePath();
		filePath += "/Fichiers/" + identifiant + ".txt";
		
		try {
			BufferedReader buff = new BufferedReader(new FileReader(filePath));
		 
			try {
				String line;
				int i = 0;
				while ((line = buff.readLine()) != null) {
					Message nouveauMessage = new Message();
					nouveauMessage.setNumero(i+1);
					nouveauMessage.setTailleOctets(line.length());
					nouveauMessage.setMarque(false);
					nouveauMessage.setCorps(line);
					messages.add(nouveauMessage);

					messages.setOctetsTotal(messages.getOctetsTotal()+line.length());
					i++;
				}
			} finally {
				buff.close();
			}
		} catch (IOException ioe) { System.out.println("Erreur IO --" + ioe.toString()); }
		
		return messages;
	}
	
	public static void SupprimerMessages(String identifiantClient, ListeMessages listeMessages) {
		
		String filePath = new File("").getAbsolutePath();
		filePath += "/Fichiers/" + identifiantClient + ".txt";
		String tempFile = new File("").getAbsolutePath();
		tempFile += "/Fichiers/tmp.txt";
		File f = new File(filePath);
        File tmp = new File(tempFile);
        
		try {
	        BufferedReader reader = new BufferedReader(new FileReader(filePath));
	        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
	
	        String line;
	        int i = 0;
	
	        while((line = reader.readLine()) != null) {
	            if(!listeMessages.get(i).getMarque())
	            {
	            	writer.write(line + "\n");
	            }
	            i++;
	        }
	        
	        writer.close(); 
	        reader.close();
	        
	        Files.copy(tmp.toPath(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
        
		} catch (IOException e) { System.out.println("Erreur IO --" + e.toString());}
	}
}
