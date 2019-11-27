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
 * André Normann
 * 2019-10-25
 * IRC Chat with client and server
 * Programmering för internet
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
     * öppnar alla ut och in strömmar och passerar socketen samt fönstret vidare
     * samt initialiserar alla värden och echoar tillbaka till klienten så man vet att man är ansluten
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
     * Returnerar en boolean som används för att kommunicera bakåt
     */
    public boolean getListBoolean() {
    	return checker;
    }
    
    
    /*
     * Två olika rum variabler som används, newRoom för ledaren av rummet och room för medlemmar av rummet
     */

    public Rooms getNewRoom() {
    	return newRoom;
    }
    

    /*
     * hämtar room variabeln
     */
    public Rooms getRoom() 
    {
    	return room;
    }
    
    public void setListBoolean( boolean value) {
    	 checker = value;
    }

    
    /*
     * Sätter listan
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
     * Sätter alias och rum för klienten
     */
    public void getAliasAndRoom(String line) 
    {

    	String[] finish = null;
    	Privacy privacyLevel = null;
    	
    	
    	finish = line.split(",", 3); //splittar finish då den alltid ser ut såhär "Alias,Room,Privacy"


        //finish[0] == alias
    	//Finish[1] == roomName
        //Finish[2] == privacy
        alias = finish[0];
        
        privacyLevel = Privacy.valueOf(finish[2]);
        String roomName = finish[1];
 
        if (checkIfRoomsExists(roomName)) // först tittar om rummet som skrevs in finns
        {
        	if (privacyLevel == Privacy.Public) // om det inte finns så gör rummet enligt instruktioner
        	{
        	newRoom = new Rooms(roomName, privacyLevel, id); // om rummet inte finns. Skapa rummet och ge ledare till skaparen 
        	room = new Rooms(roomName);
        	}
        	else
        	{
        		if(roomName.equals("Default")) // om man försöker göra rum deafult med lösenord så låter den inte klienten göra det.
        		{
        			sendMessage("Room name Default cannot have a password. Settings are public and there is no password.");
                	newRoom = new Rooms(roomName, Privacy.Public, id); // om rummet inte finns. Skapa rummet och ge ledare till skaparen 
                	room = new Rooms(roomName);
        		}
        		else // annars så lägger den in roomName, privacylevel(vilket endast kan vara private eller secret här), lösenord och id på denna klient
        		{
        			newRoom = new Rooms(roomName, privacyLevel, promptPasswordForRoom(), id);
        			room = new Rooms(roomName, privacyLevel); //samt lägger till i room för att room används överallt
        		}
        	}
        		
        }
        else
        {
        	for (Rooms item : roomList) { //tittar om rummet som redan finns har ett lösenord
				if (item.getRoomName().equals(roomName))
					if (item.getPrivacy() != Privacy.Public)
					{
						if(requestPasswordForEntry(item.getRoomName(), item)) // om den har lösenord, prompta användaren för lösenord
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
     * Läser in lösenordet från skaparen av rummet om rummet inte är public     */
    public String promptPasswordForRoom() {
    	
    	String line ;
    	sendMessage("Enter a password:");
    	while (true) { //loopar förevigt tills användaren skriver in ett lösenord
    	 try {
         	line = in.readLine(); 
				if (line != null) ;  // läser inströmmen
				{
					sendMessage("Password = " + line); // lösenord är det första ledaren skriver
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
     * Overload metod för secret kanaler där alias inte skickas.
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

    
    //tittar om rummet redan finns genom att loopa igenom alla rum och tittar om room är samma som item.getRoomName()
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
	 * Skickar ett meddelande ut i strömmen
	 */
    public void sendMessage(String alias, String message)
    {
    	out.println(alias + " :" + message);
    	out.flush();
    }
    
    /*
     * Går igenom en loop som tittar om lösenord på rummet är samma som lösenordet man skrev in.
     * Om det är samma så bryter den loopen och returnerar sant
     * 
     */
    private boolean requestPasswordForEntry(String roomName, Rooms password) 
    {
    	boolean isCorrectPassword = false;
    	String line = "";
		sendMessage("Enter password for room " + roomName + ". Enter <cancel> to cancel ");

        try {
			while ((line = in.readLine()) != null)  // läser inströmmen
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
     * Skickar meddelande och flushar utströmmen
     */
    public void sendMessage(String message)
    {
    	out.println(message);
    	out.flush();
    }
    
    

    /*
     * stänger av användaren från rummet
     */
    public void closeConnection() {
    	try {
			clientSocket.close();
		} catch (IOException e) {
			System.err.println("Error in closing connection");
		}
    }
    
    /*
     * Tråd som läser in strömmen och sedan skickar meddelandet ut till alla klienter 
     * samt hanterar alla som lämnar servern
     * (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
	@Override
    public void run() {

        try {
            String line;
            while ((line = in.readLine()) != null)  // läser inströmmen
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
        		onClosingClient(); // när en klient stänger av programmet och socketen stängs
    		}
        

		try {
			if (in.read() == -1 && !closed) // om användaren klickar på disconnect knappen
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
	 * Tittar om personen som försökte göra ett kommand är ledare över rummet
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
	 * Skriver till alla att personen lämnat rummet
	 * tar bort klienten och passerar ledare och personen är en ledare
	 * sen stänger den alla sockets och strömmar
	 */
	public void onClosingClient () {
 	   try {
		   sendToAllInRoom(alias, " Closed the connection to the server", getRoom()); 
		   window.addToList("Connection closed to " + alias + "In room " + room.getRoomName());
		   
		   clientList.remove(this);
		   checker = true;
		   
		   
		   if(room.getLeaderId() == id)
		   	passLeaderIfLeaderLeaves();	     
		   
           if (out != null)  //stänger utströmmen 
               {
                   out.close();
               }
           	clientSocket.close();
           	closed = true;
   	   }
   
   catch (IOException | NoSuchElementException p) // om användaren stänger av programmet
   {
	   System.err.println("Closing connection...");
   } 
	}
	
	/*
	 * om användaren började sitt meddalande med "/ 
	 * så används den här metoden där den tittar resten av meddelandet och 
	 * skickar tillbaka det kommandot ledaren använde
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
		char[] commandArray = line.toCharArray(); // gör line till en char[]
		
	    command = commandArray[1]; // Tittar vilket kommand användaren har skrivit

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
	 * kanske inte går att göra som jag vill.
	 */
	public void banId(int id) 
	{
		
	}
	
	/*
	 * Tar bort personen ur client listan vilket gör att han aldrig inte kan ta emot meddelanden eller skicka meddelanden till servern
	 * samt stänger av socketen 
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
	 * Hämtar id på alla personer i samma rum eller en specifik person
	 */
	
	public void getId(String inAlias) 
	{
		if (inAlias.equals("all") | inAlias.equals("All")) //hämtar på alla
		{
			for (ClientHandler item : clientList) {
				if (room.getRoomName().equals(item.getRoom().getRoomName()))
				{
				sendMessage(String.valueOf(item.getId()), " is the id of " + item.getAlias());
				}
				
				try {
					Thread.sleep(10); //sover eftersom annars overflowar in strömmen i klienten och den tar bara inte in allt
				} catch (InterruptedException e) {

				}
			}
		}
		else //hämtar endast på personer med inAlias i sitt namn. te.x skriver den ut "1 is the id of kalle" "2 is the id of kalle2" osv...
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
	
	/* Sätter ledare på rummet och sen ropar ut till alla vem som är gjord till ledare
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
	 * hämtar alla klienter och tar klienten med lägst id i listan och gör den till ledaren av rummet
	 */
	public void passLeaderIfLeaderLeaves() 
	{

		   ArrayList<Integer> getLowest = new ArrayList<Integer>();		   
		   
		   for (ClientHandler item : clientList) //hämtar högsta idn i listan 
		   {
			   getLowest.add(item.getId());
		   }
		   
		 passLeadToId(Collections.min(getLowest)); // om ledaren lämnar rummet så ges 
		 
	}
 }