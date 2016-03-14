package pop3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LireFichiers {

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
		} catch (IOException ioe) { System.out.println("Erreur IO --" + ioe.toString());}
		
		return messages;
	}
/*
	public static void SupprimerMessages(String identifiantClient, ListeMessages verrouMessages) {
		
		String filePath = new File("").getAbsolutePath();
		filePath += "/Fichiers/" + identifiantClient + ".txt";
		
		try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
 
            StringBuffer sb = new StringBuffer(); 
            String line;    
            int nbLinesRead = 0;       
            while ((line = reader.readLine()) != null) {
                if (nbLinesRead != lineNumber) {
                    sb.append(line + "\n");
                }
                nbLinesRead++;
            }
            reader.close();
            BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
            out.write(sb.toString());
            out.close();
 
        } catch (Exception e) {
            return false;
        }
        return true;
	}*/
}
