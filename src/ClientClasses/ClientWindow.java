package ClientClasses;
import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.swing.JButton;
import java.awt.TextField;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.awt.event.ActionEvent;
import java.awt.Button;

import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;

/*
 * André Normann
 * 2019-10-25
 * IRC Chat with client and server
 * Programmering för internet
 */

public class ClientWindow implements Runnable {

	private JFrame frame;
	private static ClientClass client;

	private TextField txtMessage;
	private List listChat;
	private Button btnConnect;
	private JTextField txtAddress;
	private JTextField txtPort;
	private JTextField txtAlias;
	private JLabel lblAlias;
	Thread t = new Thread(this);
	private JTextField txtRoom;
	@SuppressWarnings("rawtypes")
	private JComboBox cmbPrivacy;
	private JFileChooser fc;
	private SecretKey secretKey;
	private static Encryption encrypt;
	private JComboBox cmbAlgorithm;

	private String decryptIn = null;
	

	private boolean checkSecret  = false;
	private final JButton btnGenerateKey = new JButton("Generate a Secret Key");

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientWindow window = new ClientWindow();
					window.frame.setVisible(true);
					 encrypt = new Encryption();
				} catch (Exception e) {
					System.out.println("Eror in main");
				}
			}
		});

	}

	/**
	 * Create the application.
	 */
	public ClientWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 883, 587);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		listChat = new List();
		listChat.setEnabled(false);
		listChat.setBounds(10, 10, 529, 457);
		frame.getContentPane().add(listChat);
		
		JButton btnSend = new JButton("Send"); 
		btnSend.setEnabled(false);
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { //skickar ett meddelande till servern
				if (cmbPrivacy.getSelectedItem().equals(Privacy.Secret))
					try {
						client.sendMessage(encrypt.enCipherString(txtMessage.getText()));
					} catch (IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e1) {
						e1.printStackTrace();
						System.err.println(e1.toString());
						//listChat.add("Unable to send encrypt and send message");
					}
				else
					client.sendMessage(txtMessage.getText());
				
				if (checkSecret)
					cmbPrivacy.setSelectedIndex(2);
				
			}
		});
		btnSend.setBounds(422, 487, 116, 45);
		frame.getContentPane().add(btnSend);
		
		txtMessage = new TextField();
		txtMessage.setEnabled(false);
		txtMessage.setBounds(10, 489, 376, 43);
		frame.getContentPane().add(txtMessage);
		
		btnConnect = new Button("Connect");
		btnConnect.addActionListener(new ActionListener() {
			@SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent arg0) {
				
				/*
				 * Connects to the given address and port
				 */
				//client = new ClientClass();
				try {
					if (!txtAlias.getText().contains(",") | !txtAlias.getText().contains(","))
					{
					listChat.clear();
					client = new ClientClass();
					
					listChat.add(client.Start(Integer.parseInt(txtPort.getText()), txtAddress.getText(), // startar klienten
							txtAlias.getText(), txtRoom.getText(), cmbPrivacy.getSelectedItem().toString()));
					
					if (cmbPrivacy.getSelectedItem().equals(Privacy.Secret)) // om secret är vald så väljer den public för att kunna skriva in lösenord på rummet
					{
						cmbPrivacy.setSelectedIndex(0); 
						checkSecret = true;
					}
					
					btnSend.setEnabled(true);
					txtMessage.setEnabled(true);
					listChat.setEnabled(true);
					btnConnect.setEnabled(false);
					
					}
					else
						listChat.add("Room and Alias cannot contain \",\"");
				} 
				catch (IOException e) 
				{
					listChat.add("Could not connect to server.");
				}

				
				if(!t.isAlive())
				t.start();

			}
		});
		btnConnect.setBounds(671, 191, 70, 22);
		
		frame.getContentPane().add(btnConnect);

		txtAddress = new JTextField();
		txtAddress.setHorizontalAlignment(SwingConstants.LEFT);
		txtAddress.setText("localhost");
		txtAddress.setBounds(662, 22, 108, 20);
		frame.getContentPane().add(txtAddress);
		txtAddress.setColumns(10);
		
		txtPort = new JTextField();
		txtPort.setText("2000");
		txtPort.setBounds(662, 53, 108, 20);
		frame.getContentPane().add(txtPort);
		txtPort.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("Address:");
		lblNewLabel.setBounds(587, 25, 46, 14);
		frame.getContentPane().add(lblNewLabel);
		
		JLabel lblPort = new JLabel("Port:");
		lblPort.setBounds(587, 56, 46, 14);
		frame.getContentPane().add(lblPort);
		
		txtAlias = new JTextField();
		txtAlias.setColumns(10);
		txtAlias.setBounds(662, 86, 108, 20);
		frame.getContentPane().add(txtAlias);
		
		lblAlias = new JLabel("Alias:");
		lblAlias.setBounds(587, 89, 46, 14);
		frame.getContentPane().add(lblAlias);
		
		/*
		 * Disconnect knapp som stänger kopplingen till serven genom att eliminera tråden i clientclass
		 */
		Button btnDisconnect = new Button("Disconnect");
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					btnSend.setEnabled(false);
					txtMessage.setEnabled(false);
					btnConnect.setEnabled(true);
					checkSecret = false;

					client.disconnect();
				} catch (Exception e) {
					listChat.add("Unable to disconnect from server");
				}
			}
		});
		btnDisconnect.setActionCommand("Disconnect");
		btnDisconnect.setBounds(787, 22, 70, 22);
		frame.getContentPane().add(btnDisconnect);
		
		txtRoom = new JTextField();
		txtRoom.setBounds(662, 116, 108, 20);
		frame.getContentPane().add(txtRoom);
		txtRoom.setColumns(10);
		
		JLabel lblRoom = new JLabel("Room:");
		lblRoom.setBounds(587, 119, 46, 14);
		frame.getContentPane().add(lblRoom);
		
		cmbPrivacy = new JComboBox(Privacy.values());
		
		cmbPrivacy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (cmbPrivacy.getSelectedItem().equals(Privacy.Secret)) // om secret är vald i comboboxen så öppnas en filechooser där man måste hämta sin secretKey
				{
					if (secretKey == null)
					{
					fc = new JFileChooser();
			        int returnVal = fc.showOpenDialog(null);

			        if (returnVal == JFileChooser.APPROVE_OPTION) {
			    		try { // öppnar secretNyckel filen och läser av den samt sätter in den i encrypt klassen
			    			
			    	         ObjectInputStream objectIn = new ObjectInputStream(new FileInputStream(fc.getSelectedFile()));
			    	         secretKey = (SecretKey) objectIn.readObject();
	    					 
			    	         objectIn.close();			 
			    	         
			    	         String cipherIn = encrypt.startCipher(secretKey);
			    	         
			    	         if (cipherIn.equals("Failed to add secret key")) // ifall nyckeln man importerar är fel så kan man försöka igen
			    	        	 secretKey = null;
			    	         
				    	        listChat.add(cipherIn);

			    			}
			    		 catch (IOException | ClassNotFoundException e1) {
			    			listChat.add("Unable to read file");
			    		}
			        }
			        

					}
					
				}
			}
		});
		cmbPrivacy.setBounds(662, 147, 108, 20);
		frame.getContentPane().add(cmbPrivacy);
		
		JLabel lblPrivacyLevel = new JLabel("Privacy level: ");
		lblPrivacyLevel.setBounds(587, 150, 74, 14);
		frame.getContentPane().add(lblPrivacyLevel);
		btnGenerateKey.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				fc = new JFileChooser();
		        int returnVal = fc.showSaveDialog(null);


		        if (returnVal == JFileChooser.APPROVE_OPTION) {
				listChat.add(Encryption.genereteSecretKey(fc.getSelectedFile(), cmbAlgorithm.getSelectedItem().toString()));
		        }
			}
		});
		btnGenerateKey.setBounds(590, 487, 230, 45);
		frame.getContentPane().add(btnGenerateKey);
		
		cmbAlgorithm = new JComboBox();
		cmbAlgorithm.setBounds(671, 454, 88, 22);
		frame.getContentPane().add(cmbAlgorithm);
		
		cmbAlgorithm.addItem("AES");
		cmbAlgorithm.addItem("Blowfish");
		JLabel lblAlgorithm = new JLabel("Algorithm:");
		lblAlgorithm.setBounds(607, 458, 88, 14);
		frame.getContentPane().add(lblAlgorithm);
		
	}

	/*
	 * Tråd som försöker läsa av inkommande strömmen och skriva ner den i en list komponent i fönstret.
	 * 
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		
		while (true)
		{
			
			try {
			if (!client.getInput().isEmpty()) // tittar om det finns någonting att läsa
			{
				if (cmbPrivacy.getSelectedItem().equals(Privacy.Secret) && checkSecret) // väljer att man vill dekryptera
				{
					
					try {
					decryptIn = encrypt.deCipherString(client.getInput()); //dekrypterar input datan
			        listChat.add(decryptIn); 
			        
					}
					catch (Exception e) // om den inte lyckas dekryptera datan kan det betyda att datan är inte krypterad, nyckeln är fel eller 
					{					// att den är korrupt på något sätt.
										// Får alltid error här när ett meddelande skickas som inte är krypterat så input skickas in även om man får en error.
				        listChat.add(client.getInput()); 
					}			
				}
				else
				{
				listChat.add(client.getInput()); // läser och lägger in in i listan
				}
				
				client.setInput(null); //Flushar inputten så den inte fortsätter oändligt
			}
			}
			catch (Exception e)
			{
			}
		}
		
	}
}
