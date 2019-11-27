package ServerClasses;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/*
 * André Normann
 * 2019-10-25
 * IRC Chat with client and server
 * Programmering för internet
 */
class ServerClass
{
	/*
	 * Startar servern med vald port eller default port 2000
	 * samt startar ett fönster som bara agerar som en chatt man enkelt kan läsa.
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
	 * Öppnar ServerSocketen på vald port
	 * och sen startar en loop som försöker acceptera ny klienter hela tiden
	 * sen om den lyckas så lägger den in klienten i en lista 
	 * Om servern redan finns öppen på vald port så stängs programmet
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
			server = new ServerSocket(port); // öppnar server på port
			window.addToList("Server started at port " + server.getLocalPort());
			while (true)
			{
				try {			
			for (ClientHandler item : clientList) { //tittar om någon har blivit sparkad eller banlyst ur 
													//servern och uppdaterar serverlistan efter det
				if(item.getListBoolean())
				{
					clientList = item.getClientList();
					item.setListBoolean(false);
				}
				
			}
			
			
			Socket client = server.accept(); 	//försöker acceptera klient
			window.addToList(("New Client connected " + InetAddress.getLocalHost().getHostName()));
			
			id++;
			ClientHandler clientSock = new ClientHandler(client, window, id, roomList); // skapar ny klient och anger specifikt id för personen
			clientList.add(clientSock);	 //lägger till klient i lista
			
			if (clientSock.getNewRoom() != null)
			roomList.add(clientSock.getNewRoom());
			else
				roomList.add(clientSock.getRoom());
			

			for (ClientHandler item : clientList) { // Uppdaterar alla klienter med klientlistan så meddelande kan skickas till alla
				item.setList(clientList);
			}
			
			for (ClientHandler item : clientList) { // Uppdaterar alla klienter med klientlistan så meddelande kan skickas till alla
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
			catch (IOException e) // Stänger servern om den finns på port
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

 


