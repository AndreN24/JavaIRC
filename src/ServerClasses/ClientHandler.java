package ServerClasses; 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.NoSuchElementException;

/*
 * Andr� Normann
 * 2019-10-25
 * IRC Chat with client and server
 * Programmering f�r internet
 */
class ClientHandler implements Runnable
 {
	 
    private final Socket clientSocket;
    private ServerWindow window;
    private PrintWriter out = null;
    private BufferedReader in = null;
    private String alias;
    private Rooms room;
    private int id;
	private ArrayList<ClientHandler> clientList = new ArrayList<ClientHandler>();
	private ArrayList<Rooms> roomList = new ArrayList<Rooms>();
	private boolean checker = false;
	private Rooms newRoom;
	private boolean closed = false;

    /*
     * �ppnar alla ut och in str�mmar och passerar socketen samt f�nstret vidare
     * samt initialiserar alla v�rden och echoar tillbaka till klienten s� man vet att man �r ansluten
     */
    public ClientHandler(Socket socket, ServerWindow window, int id, ArrayList<Rooms> roomsList) { 
        this.clientSocket = socket;
        this.window = window;
        this.roomList = roomsList;
        this.id = id;

        //hanterar in
        
        String line;
        try {
			out = new PrintWriter(clientSocket.getOutputStream(), true);
	        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			line = in.readLine();

	        getAliasAndRoom(line);
	        

	        sendMessage(alias, " Connected to the room: " + room.getRoomName());
	        Thread.sleep(10);

	        
	        if (newRoom != null)
	        {

	        	sendMessage(alias, " is the leader of " + newRoom.getRoomName());
	        }
	        else
	        	sendMessage("Welcome to " + room.getRoomName() + " , " + alias + "!" + "The privacy level of the server is " + room.getPrivacy().toString());
		} catch (IOException | InterruptedException e) 
        {
			System.err.println("Error in opening in and out stream");
		}


    }
    
    public int getId ()
    {
    	return id;
    }
    
    public String getAlias()
    {
    	return alias;
    }
    
	
	public ArrayList<Rooms> getRoomsList() {
		return roomList;
	}
	
    /*
     * Returns list of all clients
     */
    public ArrayList<ClientHandler> getClientList (){
    	return clientList;
    }
    
    /*
     * Returnerar en boolean som anv�nds f�r att kommunicera bak�t
     */
    public boolean getListBoolean() {
    	return checker;
    }
    
    
    /*
     * Tv� olika rum variabler som anv�nds, newRoom f�r ledaren av rummet och room f�r medlemmar av rummet
     */

    public Rooms getNewRoom() {
    	return newRoom;
    }
    

    /*
     * h�mtar room variabeln
     */
    public Rooms getRoom() 
    {
    	return room;
    }
    
    public void setListBoolean( boolean value) {
    	 checker = value;
    }

    
    /*
     * S�tter listan
     */
    public void setList(ArrayList<ClientHandler> list) 
    {
    	clientList = list;
    }
    public void setRoomList(ArrayList<Rooms> list)
    {
    	roomList = list;
    }
    
    
    /*
     * S�tter alias och rum f�r klienten
     */
    public void getAliasAndRoom(String line) 
    {

    	String[] finish = null;
    	Privacy privacyLevel = null;
    	
    	
    	finish = line.split(",", 3); //splittar finish d� den alltid ser ut s�h�r "Alias,Room,Privacy"


        //finish[0] == alias
    	//Finish[1] == roomName
        //Finish[2] == privacy
        alias = finish[0];
        
        privacyLevel = Privacy.valueOf(finish[2]);
        String roomName = finish[1];
 
        if (checkIfRoomsExists(roomName)) // f�rst tittar om rummet som skrevs in finns
        {
        	if (privacyLevel == Privacy.Public) // om det inte finns s� g�r rummet enligt instruktioner
        	{
        	newRoom = new Rooms(roomName, privacyLevel, id); // om rummet inte finns. Skapa rummet och ge ledare till skaparen 
        	room = new Rooms(roomName);
        	}
        	else
        	{
        		if(roomName.equals("Default")) // om man f�rs�ker g�ra rum deafult med l�senord s� l�ter den inte klienten g�ra det.
        		{
        			sendMessage("Room name Default cannot have a password. Settings are public and there is no password.");
                	newRoom = new Rooms(roomName, Privacy.Public, id); // om rummet inte finns. Skapa rummet och ge ledare till skaparen 
                	room = new Rooms(roomName);
        		}
        		else // annars s� l�gger den in roomName, privacylevel(vilket endast kan vara private eller secret h�r), l�senord och id p� denna klient
        		{
        			newRoom = new Rooms(roomName, privacyLevel, promptPasswordForRoom(), id);
        			room = new Rooms(roomName, privacyLevel); //samt l�gger till i room f�r att room anv�nds �verallt
        		}
        	}
        		
        }
        else
        {
        	for (Rooms item : roomList) { //tittar om rummet som redan finns har ett l�senord
				if (item.getRoomName().equals(roomName))
					if (item.getPrivacy() != Privacy.Public)
					{
						if(requestPasswordForEntry(item.getRoomName(), item)) // om den har l�senord, prompta anv�ndaren f�r l�senord
							room = item;
						else
						{
							closeConnection();
						}
					}
					else //Annars starta
						room = item;
			}
        }
        
        
    }

    
    /*
     * L�ser in l�senordet fr�n skaparen av rummet om rummet inte �r public     */
    public String promptPasswordForRoom() {
    	
    	String line ;
    	sendMessage("Enter a password:");
    	while (true) { //loopar f�revigt tills anv�ndaren skriver in ett l�senord
    	 try {
         	line = in.readLine(); 
				if (line != null) ;  // l�ser instr�mmen
				{
					sendMessage("Password = " + line); // l�senord �r det f�rsta ledaren skriver
					return line;
					
				}
			} catch (IOException e) {
				System.err.println("Error in password checker");
			}
    	}
    }
    
    

    
    /*
     * Skickar meddelande till alla anslutna klienter som delar rum med klienten
     */
    public void sendToAllInRoom(String alias, String line, Rooms room) throws IOException 
    { 
    	for (ClientHandler client : clientList) {
    		if (client.getRoom().getRoomName().equals(room.getRoomName()))
    		{
    		client.sendMessage(alias, line);
    		}
    	}
    }
    
    /*
     * Overload metod f�r secret kanaler d�r alias inte skickas.
     */
    public void sendToAllInRoom( String line, Rooms room) throws IOException 
    { 
    	for (ClientHandler client : clientList) {
    		if (client.getRoom().getRoomName().equals(room.getRoomName()))
    		{
    		client.sendMessage(line);
    		}
    	}
    }

    
    //tittar om rummet redan finns genom att loopa igenom alla rum och tittar om room �r samma som item.getRoomName()
    public boolean checkIfRoomsExists(String room) {
        for (Rooms item : roomList) {
			if (item.getRoomName().equals(room))
			{
				return false;
			}		
		}
        return true;
    }
	    
	/*
	 * Skickar ett meddelande ut i str�mmen
	 */
    public void sendMessage(String alias, String message)
    {
    	out.println(alias + " :" + message);
    	out.flush();
    }
    
    /*
     * G�r igenom en loop som tittar om l�senord p� rummet �r samma som l�senordet man skrev in.
     * Om det �r samma s� bryter den loopen och returnerar sant
     * 
     */
    private boolean requestPasswordForEntry(String roomName, Rooms password) 
    {
    	boolean isCorrectPassword = false;
    	String line = "";
		sendMessage("Enter password for room " + roomName + ". Enter <cancel> to cancel ");

        try {
			while ((line = in.readLine()) != null)  // l�ser instr�mmen
			{

				if (password.getPassword().equals(line))
				{
					isCorrectPassword = true;
					break;
				}
				else
				{
					sendMessage("Wrong password");
				}
		    
			}
			} catch (IOException e) {
			closeConnection();
			
        }
    	return isCorrectPassword;
    	
    		
    }
    
    
    /*
     * Skickar meddelande och flushar utstr�mmen
     */
    public void sendMessage(String message)
    {
    	out.println(message);
    	out.flush();
    }
    
    

    /*
     * st�nger av anv�ndaren fr�n rummet
     */
    public void closeConnection() {
    	try {
			clientSocket.close();
		} catch (IOException e) {
			System.err.println("Error in closing connection");
		}
    }
    
    /*
     * Tr�d som l�ser in str�mmen och sedan skickar meddelandet ut till alla klienter 
     * samt hanterar alla som l�mnar servern
     * (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
	@Override
    public void run() {

        try {
            String line;
            while ((line = in.readLine()) != null)  // l�ser instr�mmen
            {
                window.addToList(line); 

            	if (line.startsWith("/")) // kommand
            	{
            		sendMessage(checkIfLeader(line));
            		
            	}
            	else
            	{ 	
            	if (room.getPrivacy().equals(Privacy.Secret))         	
            		sendToAllInRoom(line, room); // skickar inte alias eftersom den inte krypteras          		
            	else
            		sendToAllInRoom(alias, line, room); // skickar till alla klienter
            	}
            }
        }
        catch (IOException e) 
        	{
        		onClosingClient(); // n�r en klient st�nger av programmet och socketen st�ngs
    		}
        

		try {
			if (in.read() == -1 && !closed) // om anv�ndaren klickar p� disconnect knappen
				{
				  onClosingClient();
				}
			}
				catch (IOException e)
			{
				System.err.println("Closing connection");
			}

		}
	
	/*
	 * Tittar om personen som f�rs�kte g�ra ett kommand �r ledare �ver rummet
	 */
	public String checkIfLeader(String line) {
		for (Rooms item: roomList) {
			if (item.getLeaderId() == id)
			{
				checkMessageForCommand(line); // tittar vilket kommand som kallades
					return "Command executed";
			}
		}  
		return "Server: You are illegible to use this command";
	}
	
	/*
	 * Skriver till alla att personen l�mnat rummet
	 * tar bort klienten och passerar ledare och personen �r en ledare
	 * sen st�nger den alla sockets och str�mmar
	 */
	public void onClosingClient () {
 	   try {
		   sendToAllInRoom(alias, " Closed the connection to the server", getRoom()); 
		   window.addToList("Connection closed to " + alias + "In room " + room.getRoomName());
		   
		   clientList.remove(this);
		   checker = true;
		   
		   
		   if(room.getLeaderId() == id)
		   	passLeaderIfLeaderLeaves();	     
		   
           if (out != null)  //st�nger utstr�mmen 
               {
                   out.close();
               }
           	clientSocket.close();
           	closed = true;
   	   }
   
   catch (IOException | NoSuchElementException p) // om anv�ndaren st�nger av programmet
   {
	   System.err.println("Closing connection...");
   } 
	}
	
	/*
	 * om anv�ndaren b�rjade sitt meddalande med "/ 
	 * s� anv�nds den h�r metoden d�r den tittar resten av meddelandet och 
	 * skickar tillbaka det kommandot ledaren anv�nde
	 * 
	 */
	public void checkMessageForCommand(String line) 
	{
		char command;
		int userId = 0;
		String userAlias = "";
		char[] messageID = null;
		/*
		 * place in array
		 * 0 = /
		 * 1 = command
		 * 2 = blank space
		 * 3 = start of id
		 * Syntax of Command:
		 * "/b <ID>  // Ban
		 * "/k <ID>  // Kick
		 * "/g <Alias>  <all> gets all ids// get id of alias 
		 * "/p <ID>  // Pass lead to id
		 */
		char[] commandArray = line.toCharArray(); // g�r line till en char[]
		
	    command = commandArray[1]; // Tittar vilket kommand anv�ndaren har skrivit

	    if (commandArray.length > 3)
		messageID = Arrays.copyOfRange(commandArray, 3, commandArray.length); //tar position 3 
	    else														 //till sista positionen av arrayen vilket ska vara id
	    	sendMessage("Server", " Incorrect Syntax. /command <id> or <alias>");
	    
		if (command == 'b' | command == 'k' | command == 'p' ) // omvandlar till int
		{
			userId  = (Integer.valueOf(String.copyValueOf(messageID)));
		}
		else if (command == 'g')
		{ //omvandlar till string
			userAlias  = String.valueOf(messageID);
		}
		else
			sendMessage("Server", " Incorrect Syntax. /command <id> or <alias>");

		

		switch (command) {
		case 'b':
			banId(userId);
			break;
		case 'k':	
			kickId(userId);
			break;
		case 'g':
			getId(userAlias);
			break;
		case 'p':		
			passLeadToId(userId);
			break;
		default:
			sendMessage("Server", " Incorrect Syntax. /command <id> or <alias>");
			break;
		}
	}
	
	
	/*
	 * TODO
	 * kanske inte g�r att g�ra som jag vill.
	 */
	public void banId(int id) 
	{
		
	}
	
	/*
	 * Tar bort personen ur client listan vilket g�r att han aldrig inte kan ta emot meddelanden eller skicka meddelanden till servern
	 * samt st�nger av socketen 
	 */
	public void kickId(int currentId) 
	{
		
		try {
			sendToAllInRoom("Server", " " + alias + " with the id "+ id + " was kicked out of the room by " + alias, room);
			

			checker = true;
			for (ClientHandler item : clientList) {
				if (item.getId() == currentId);
				{
					item.closeConnection();
					   System.err.println("Remove client");
					clientList.remove(item);
				}
			}
		} catch (IOException e) {
			sendMessage("Server", "Unable to perform command");
		}
		

	}

	
	/*
	 * H�mtar id p� alla personer i samma rum eller en specifik person
	 */
	
	public void getId(String inAlias) 
	{
		if (inAlias.equals("all") | inAlias.equals("All")) //h�mtar p� alla
		{
			for (ClientHandler item : clientList) {
				if (room.getRoomName().equals(item.getRoom().getRoomName()))
				{
				sendMessage(String.valueOf(item.getId()), " is the id of " + item.getAlias());
				}
				
				try {
					Thread.sleep(10); //sover eftersom annars overflowar in str�mmen i klienten och den tar bara inte in allt
				} catch (InterruptedException e) {

				}
			}
		}
		else //h�mtar endast p� personer med inAlias i sitt namn. te.x skriver den ut "1 is the id of kalle" "2 is the id of kalle2" osv...
		{
			for (ClientHandler item : clientList) {
				if (item.getAlias().contains(inAlias)) {
						sendMessage(String.valueOf(item.getId()), " is the id of " + inAlias);
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				}
			}
		}
	}
	
	/* S�tter ledare p� rummet och sen ropar ut till alla vem som �r gjord till ledare
	 * 
	 */
	public boolean passLeadToId (int _id) 
	{
		
		for (Rooms item : roomList) {
			if(item.getLeaderId() == id)
			{
				item.setLeaderId(_id);
			}
		}

		for (ClientHandler item : clientList) 
		{
			if(item.getId() == _id)
			{
				try {
					sendToAllInRoom(item.getAlias(), " Is the leader of the room", room);
					return true;
				} catch (IOException e) {
					System.err.println("Error in pass to lead");
				}
			}
		}
		

		sendMessage("Server", " Unable to find id");
		return false;
		
	}
	
	/*
	 * h�mtar alla klienter och tar klienten med l�gst id i listan och g�r den till ledaren av rummet
	 */
	public void passLeaderIfLeaderLeaves() 
	{

		   ArrayList<Integer> getLowest = new ArrayList<Integer>();		   
		   
		   for (ClientHandler item : clientList) //h�mtar h�gsta idn i listan 
		   {
			   getLowest.add(item.getId());
		   }
		   
		 passLeadToId(Collections.min(getLowest)); // om ledaren l�mnar rummet s� ges 
		 
	}
 }