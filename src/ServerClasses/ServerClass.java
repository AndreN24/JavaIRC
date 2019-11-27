package ServerClasses;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/*
 * Andr� Normann
 * 2019-10-25
 * IRC Chat with client and server
 * Programmering f�r internet
 */
class ServerClass
{
	/*
	 * Startar servern med vald port eller default port 2000
	 * samt startar ett f�nster som bara agerar som en chatt man enkelt kan l�sa.
	 */
	public static void main(String[] args) {
		ServerWindow window = new ServerWindow();
		Server server = new Server();
		
		try 
		{
			if (args.length > 0)
				server.Start(window, args[0]);
			else
				server.Start(window, "2000");
		} 
		catch (IOException | InterruptedException e) 
		{
			System.err.println("Error in server main");
		}
	}
	}

class Server
{
	private ServerSocket server;
	private ServerWindow window;
	private int port;
	private ArrayList<ClientHandler> clientList = new ArrayList<ClientHandler>();
	private ArrayList<Rooms> roomList = new ArrayList<Rooms>();
	private int id;


	
	/*
	 * �ppnar ServerSocketen p� vald port
	 * och sen startar en loop som f�rs�ker acceptera ny klienter hela tiden
	 * sen om den lyckas s� l�gger den in klienten i en lista 
	 * Om servern redan finns �ppen p� vald port s� st�ngs programmet
	 */
	public void Start (ServerWindow _window, String args) throws IOException, InterruptedException 
	{
		window = _window;
		
		if (args != null)
		{
			port = Integer.parseInt(args);
		}

		
		while (true)
		{
			try {
			server = new ServerSocket(port); // �ppnar server p� port
			window.addToList("Server started at port " + server.getLocalPort());
			while (true)
			{
				try {			
			for (ClientHandler item : clientList) { //tittar om n�gon har blivit sparkad eller banlyst ur 
													//servern och uppdaterar serverlistan efter det
				if(item.getListBoolean())
				{
					clientList = item.getClientList();
					item.setListBoolean(false);
				}
				
			}
			
			
			Socket client = server.accept(); 	//f�rs�ker acceptera klient
			window.addToList(("New Client connected " + InetAddress.getLocalHost().getHostName()));
			
			id++;
			ClientHandler clientSock = new ClientHandler(client, window, id, roomList); // skapar ny klient och anger specifikt id f�r personen
			clientList.add(clientSock);	 //l�gger till klient i lista
			
			if (clientSock.getNewRoom() != null)
			roomList.add(clientSock.getNewRoom());
			else
				roomList.add(clientSock.getRoom());
			

			for (ClientHandler item : clientList) { // Uppdaterar alla klienter med klientlistan s� meddelande kan skickas till alla
				item.setList(clientList);
			}
			
			for (ClientHandler item : clientList) { // Uppdaterar alla klienter med klientlistan s� meddelande kan skickas till alla
				item.setRoomList(roomList);
			}
			

			new Thread(clientSock).start(); //startar klienten
			
				}
					catch (IOException e)
					{
						System.err.println("Error in accepting new client");
					}
			
				}
			}
			catch (IOException e) // St�nger servern om den finns p� port
			{
				window.addToList("Server is already open at given port: " + port+ ". Program will now terminate");
				Thread.sleep(2500);
				System.exit(0);
				break;
			}
			finally {
	            if (server != null) {
	                try {
	                    server.close();
	                } 
	                catch (IOException e) 
	                {
	                    System.err.println("Error in closing server");
	                }
	            }
			}
		}
	}	
}

 


