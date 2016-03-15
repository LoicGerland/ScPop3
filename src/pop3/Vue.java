package pop3;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
 
public class Vue extends JFrame implements ActionListener {
	private static final long serialVersionUID = -1374851023131011832L;
	
	private JPanel contentPane;
	private JPanel clientsPane;
	private JButton btnStartStop;
	private JLabel statusLabel;
	private JLabel adresseLabel;
	
	private Serveur serveur;
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Vue frame = new Vue();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public Vue() {
		setTitle("Serveur");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(600, 550);
		setLocationRelativeTo(null);
        
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
		setContentPane(contentPane);
		
		adresseLabel = new JLabel("Adresse : 127.0.0.1:110");
		adresseLabel.setBounds(10, 15, 200, 20);
		contentPane.add(adresseLabel);
		
        statusLabel = new JLabel("Statut : Arret");
        statusLabel.setForeground(Color.red);
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        statusLabel.setBounds(230, 15, 200, 20);
		contentPane.add(statusLabel);
        
		btnStartStop = new JButton("Lancer");
		btnStartStop.setBounds(450, 10, 100, 30);
		btnStartStop.addActionListener(this);
		contentPane.add(btnStartStop);
		
		clientsPane = new JPanel();
		clientsPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		clientsPane.setLayout(null);
		clientsPane.setBounds(20, 60, 530, 400);
		clientsPane.setBackground(Color.white);
		contentPane.add(clientsPane);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == btnStartStop) {
			if(serveur != null && serveur.isRunning()) {
				btnStartStop.setText("Lancer");
				statusLabel.setForeground(Color.red);
				statusLabel.setText("Statut : Arret");
				try {
					serveur.setRunning(false);
					serveur.getSocket().close();
				} catch (IOException e1) {}
			} else {
				btnStartStop.setText("Arreter");
				statusLabel.setForeground(Color.green);
				statusLabel.setText("Statut : En marche");
				serveur = new Serveur();
				serveur.setRunning(true);
				serveur.start();
			}
		}
	}
}