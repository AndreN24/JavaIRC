package ServerClasses;

/*
 * André Normann
 * 2019-10-25
 * IRC Chat with client and server
 * Programmering för internet
 */
 public class Rooms 
 {
	 private String room;
	 private Privacy privacy = Privacy.Public;
	 private int id;
	 private String password;
	 /*
	  * Klass som håller koll på alla rum genom att kunna namnet på rummet, lösenord, privacy level och id på ledaren av rummet
	  */
	 public Rooms (String room, Privacy privacy, int id)
	 {
		 this.room = room;
		 this.privacy = privacy;
		 this.id = id;
	 }
	 
	 public Rooms (String room, Privacy privacy, String password, int id) {
		 this.room = room;
		 this.password = password;
		 this.privacy = privacy;
		 this.id = id;
	 }
	 
	 public Rooms (String room, Privacy privacy) {
		 this.room = room;
		 this.privacy = privacy;

	 }
	 
	 public Rooms (String room) {
		 this.room = room;
	 }
	 
	 public String getRoomName() {
		 return room;
	 }
	 
	 public Privacy getPrivacy() {
		 return privacy;
	 }
	 
	 public int getLeaderId () {
		 return id;
	 }
	 
	 public void setLeaderId (int value) {
		 id = value;
	 }
	 
	 public String getPassword() {
		 return password;
	 }
 }
 
 enum Privacy {
	 Public, //alla kan se och ansluta
	 Private, // ingen kan se, men alla kan ansluta
	 Secret //ingen kan se, ingen kan anlsluta utan nyckel/lösenord
 }