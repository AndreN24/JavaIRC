package ServerClasses;
import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.List;


/*
 * André Normann
 * 2019-10-25
 * IRC Chat with client and server
 * Programmering för internet
 */

/*
 * Klass är endast till för att kunna skicka ut meddelanden i en lista
 */
public class ServerWindow {

	private JFrame frame;
	private List list;

	/**
	 * Launch the application.
	 */
	public void start() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					//ServerWindow window = new ServerWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		

	}

	/**
	 * Create the application.
	 */
	public ServerWindow() {
		initialize();
		start();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 605, 525);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		list = new List();
		list.setBounds(10, 10, 569, 466);
		frame.getContentPane().add(list);
	}
	
	public void addToList(String input) {
		list.add(input);
	}

}
