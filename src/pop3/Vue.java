package pop3;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
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
	private JLabel statusLabel;
	private JLabel adresseLabel;
	private JTextArea txtClientArea;
    private JTextArea txtInfoArea;
    private JScrollPane scrollClientPane;
    private JScrollPane scrollInfoPane;
	
	private Serveur serveur;

	public Vue() {
		setTitle("Serveur");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(550, 400);
		setLocationRelativeTo(null);
        
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
		setContentPane(contentPane);
		
		adresseLabel = new JLabel("Adresse :");
		adresseLabel.setBounds(10, 15, 200, 20);
		contentPane.add(adresseLabel);
		
        statusLabel = new JLabel("Statut : Arret");
        statusLabel.setForeground(Color.red);
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        statusLabel.setBounds(230, 15, 200, 20);
		contentPane.add(statusLabel);
        
		btnStartStop = new JButton("Lancer");
		btnStartStop.setBounds(445, 10, 100, 30);
		btnStartStop.addActionListener(this);
		contentPane.add(btnStartStop);
		
		txtClientArea = new JTextArea();
        txtClientArea.setEditable(false);
        scrollClientPane = new JScrollPane(txtClientArea);
        scrollClientPane.setBounds(10, 50, 530, 100);
		contentPane.add(scrollClientPane);
		
		txtInfoArea = new JTextArea();
		txtInfoArea.setEditable(false);
        scrollInfoPane = new JScrollPane(txtInfoArea);
        scrollInfoPane.setBounds(10, 150, 530, 220);
		contentPane.add(scrollInfoPane);
	}
	
	private void startServer() {
		serveur = new Serveur(this);
		if(serveur.getSocket() != null) {
			String adresse = serveur.getSocket().getInetAddress().getHostAddress();
			String port = serveur.getSocket().getLocalPort()+"";
			btnStartStop.setText("Arreter");
			adresseLabel.setText("Adresse : "+adresse+":"+port);
			statusLabel.setText("Statut : En marche");
			statusLabel.setForeground(Color.green);
			serveur.start();
		}
	}
	
	private void stopServer() {
		btnStartStop.setText("Lancer");
		adresseLabel.setText("Adresse : ");
		statusLabel.setText("Statut : Arret");
		statusLabel.setForeground(Color.red);
		serveur.stopServeur();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == btnStartStop) {
			if(serveur != null && serveur.isRunning()) {
				stopServer();
			} else {
				startServer();
			}
		}
	}

	public void sop(String string) {
		this.txtInfoArea.insert(string+"\n", 0);
		System.out.println(string);
	}
	
	public void update() {
		this.txtClientArea.setText("");
		for(ServeurSecondaire ss : serveur.getListeThread()) {
			this.txtClientArea.insert(
					ss.getIdentifiantClient() + " : " + ss.getClientSocket().getInetAddress().getHostAddress() + "\n", 0
			);
		}
		
	}
}