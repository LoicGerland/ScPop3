package pop3;

import java.io.IOException;

public class Test {

	public static void main(String[] args) {
		
		try {
			Serveur s = new Serveur();
			s.run();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
