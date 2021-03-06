package smtp;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import Commun.Commun;

import javax.swing.JButton;
 
/**
 * Classe représentant la vue du serveur principal 
 * 
 * @author GERLAND - LETOURNEUR
 */
public class Vue extends JFrame implements ActionListener {
	private static final long serialVersionUID = -1374851023131011832L;
	
	private JPanel contentPane;
	private JButton btnStartStop;
	private JButton btnLocked;
	private JLabel statusLabel;
	private JLabel adresseLabel;
    private JTextArea txtInfoArea;
    private JScrollPane scrollInfoPane;
	
	private Serveur server;

	public Vue() {
		setTitle("Serveur SMTP");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(570, 320);
		setLocationRelativeTo(null);
        
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
		setContentPane(contentPane);
		
		adresseLabel = new JLabel("Adresse :");
		adresseLabel.setBounds(10, 15, 200, 20);
		contentPane.add(adresseLabel);
		
		btnLocked = new JButton("Bloquer");
		btnLocked.setBounds(200, 10, 100, 30);
		btnLocked.addActionListener(this);
		btnLocked.setEnabled(false);
		contentPane.add(btnLocked);
		
        statusLabel = new JLabel("Statut : Arret");
        statusLabel.setForeground(Color.red);
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        statusLabel.setBounds(230, 15, 200, 20);
		contentPane.add(statusLabel);
        
		btnStartStop = new JButton("Lancer");
		btnStartStop.setBounds(445, 10, 100, 30);
		btnStartStop.addActionListener(this);
		contentPane.add(btnStartStop);
		
		txtInfoArea = new JTextArea();
		txtInfoArea.setEditable(false);
        scrollInfoPane = new JScrollPane(txtInfoArea);
        scrollInfoPane.setBounds(10, 50, 530, 220);
		contentPane.add(scrollInfoPane);
	}
	
	/**
	 * Trouver l'IP de la machine sur laquelle le serveur est démarré
	 */
	private void findMyIp() {
		String IPADDRESS_PATTERN = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
		Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
		Matcher matcher;
		Enumeration<NetworkInterface> e;
		try {
			e = NetworkInterface.getNetworkInterfaces();
			while (e.hasMoreElements()) {
				Enumeration<InetAddress> i = e.nextElement().getInetAddresses();
				while (i.hasMoreElements()) {
					InetAddress a = i.nextElement();
					matcher = pattern.matcher(a.getHostAddress());
					if(matcher.find() && !a.isLoopbackAddress() && !a.isSiteLocalAddress() ) {
						adresseLabel.setText("Adresse : "+a.getHostAddress()+":"+Commun.PORT_SMTP);
					}	
				}
			}
		} catch (SocketException e1) {
			System.out.println("Erreur - Lecture des adresses Ip");
		}
	}
	
	/**
	 * Démarrage du serveur et mise à jour de l'interface
	 */
	private void startServer() {
		server = new Serveur(this);
		if(server.getSocket() != null) {
			btnStartStop.setText("Arreter");
			statusLabel.setText("Statut : En marche");
			statusLabel.setForeground(Color.green);
			this.findMyIp();
			server.start();
			btnLocked.setEnabled(true);
		}
	}
	
	/**
	 * Arrêt du serveur et mise à jour de l'interface
	 */
	private void stopServer() {
		btnStartStop.setText("Lancer");
		adresseLabel.setText("Adresse : ");
		statusLabel.setText("Statut : Arret");
		statusLabel.setForeground(Color.red);
		server.stopServeur();
		btnLocked.setEnabled(false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == btnStartStop) {
			if(server != null && server.isRunning()) {
				stopServer();
			} else {
				startServer();
			}
		} else if (e.getSource() == btnLocked) {
			server.setLocked(!server.getLocked());
			btnLocked.setText("Bloquer");
			if(server.getLocked())
				btnLocked.setText("Débloquer");
		}
	}

	/**
	 * Affichage de données dans l'interface et la console
	 */
	public void sop(String string) {
		this.txtInfoArea.insert(string+"\n", 0);
		System.out.println(string);
	}
}