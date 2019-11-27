package ClientClasses;
/*
 * André Normann
 * 2019-10-25
 * IRC Chat with client and server
 * Programmering för internet
 */

public enum Privacy {
	 Public, //alla kan ansluta utan lösenord
	 Private, //  alla kan ansluta och man behöver lösenord för att ansluta
	 Secret // alla kan ansluta och man behöver lösenord för att ansluta och allting i chatten är krypterat
 }