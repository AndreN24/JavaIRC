package ClientClasses;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


/*
 * Andr� Normann
 * 2019-10-25
 * IRC Chat with client and server
 * Programmering f�r internet
 */

public class ClientClass implements Runnable {
	
		private BufferedReader in;
		private PrintWriter out;
		private Socket socket;
		private String input;
		Thread t = new Thread(this);

		/*
		 * Startar en socket p� vald address och port, alias, room och privacy
		 * �ppnar in och out streamen
		 * F�rsta meddelandet till servern �r alltid ditt alias som registreras d�r
		 * och startar sedan en tr�d
		 */
		public String Start(int port, String address, String alias, String room, String privacy) throws IOException 
		{
			String strOut = "";
			try {
			socket = new Socket(address, port);
			
			if (room.isEmpty())  //Default room
				room = "Default";
			if (alias.isEmpty()) //Default alias
				alias = "Anonymous";
			
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			sendMessage(alias + "," + room + "," + privacy);
			
			if(!t.isAlive())
			t.start();

			strOut =  "Connected to " + socket.getInetAddress() + " at port " + socket.getPort();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				strOut = "Could not resolve address or port";
			}
			return strOut;

			
		}
		 
		/*
		 * Skickar ett meddalende genom utstr�mmen
		 */
		public void sendMessage (String write) {
			out.println(write);	
		}
		

		/*
		 * F�rs�ker l�sa str�mmen och placera den i str�ngen input
		 * (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {

				try {
					while (!socket.isClosed()) 
					{
						input = in.readLine();		
					}
				} catch (IOException e) {
					{
						try {
							in.close();
							socket.close();
						} catch (IOException e1) {
							System.err.println("ERror in closing");
						}
						out.close();
					}
					input = "Closed connection to server";
				}
			
				}
		
	
		
		public String getInput() {
			return input;
		}
		
		public void setInput(String value)
		{
			input = value;
		}
		
		/*
		 * st�nger socketen och avslutar tr�den
		 */
		public void disconnect () throws IOException  {
			if (socket.isConnected())
			{
			socket.close();

			}
		}
		
	
}
