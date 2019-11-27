package ClientClasses;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

/*
 * André Normann
 * 2019-10-25
 * IRC Chat with client and server
 * Programmering för internet
 */


/*
 * Klass är gjord för att hantera kryptering
 */
public class Encryption {
	
	private Cipher enCipher;
	private Cipher deCipher;
	private String aes = "AES";
	private String blowfish = "Blowfish/ECB/PKCS5Padding";
	
	/*
	 * Metod används för att starta alla ciphers som enkrytperar och dekrytperar
	 */
	public String startCipher(SecretKey secretKey) {
        try { //initializerar decipher och encipher 
        	System.out.println(secretKey.getAlgorithm());
        	if (secretKey.getAlgorithm().equals("Blowfish"))
        	{
			deCipher = Cipher.getInstance(blowfish); // algoritm som används
			deCipher.init(Cipher.DECRYPT_MODE, secretKey);
			
	        enCipher = Cipher.getInstance(blowfish); // Krypterar med blowfish
			enCipher.init(Cipher.ENCRYPT_MODE, secretKey);
        	}
        	else 
        	{
    			deCipher = Cipher.getInstance(aes); // algoritm som används
    			deCipher.init(Cipher.DECRYPT_MODE, secretKey);
    			
    	        enCipher = Cipher.getInstance(aes); 
    			enCipher.init(Cipher.ENCRYPT_MODE, secretKey);
        	}
			return "Successfully added secret key";
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
			return "Failed to add secret key";
		}  
	}
	
	/*
	 * Decipherar strängen som tas in med hjälp av AES algortitmnyckel
	 */
	public String deCipherString(String message) throws IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
		
		
		 byte[] decoded = Base64.getDecoder().decode(message.getBytes("UTF-8"));
         byte[] decrypted = deCipher.doFinal(decoded);
         return new String(decrypted, "UTF-8");

	}
	
	 /*
	  *  Encipherar strängen som tas in med hjälp av AES algortitmnyckel 
	  */
	
	public String enCipherString(String message) throws IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {


		byte[] encrypted = enCipher.doFinal(message.getBytes("UTF-8"));
				
		byte[] encoded = Base64.getEncoder().encode(encrypted);
			
		return new String(encoded, "UTF-8");
	}
	
	/*
	 * Skapar och sparar en secretKey till en binär fil dit användaren väljer
	 * och returnerar en sträng om den lyckas eller misslyckas
	 */
	public static String genereteSecretKey(File file, String algorithm) {
		SecretKey key = null;
		String strOut = null;
		KeyGenerator keyGen;
		try {
			if (algorithm.equals("AES")) 
			{
			keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(128); //Initierar hur många bytes nyckeln är
			key = keyGen.generateKey(); //generar nyckeln
	         strOut = "Successfully generated a secret key with a size of 128 bits and the algortim AES to file ";

			}
			else // finns bara två val.
			{
				keyGen = KeyGenerator.getInstance("Blowfish");
				keyGen.init(448); //Initierar hur många bytes nyckeln är
				key = keyGen.generateKey(); //generar nyckeln
		         strOut = "Successfully generated a secret key with a size of 448 bits and the algortim Blowfish to file ";

			}
			

			FileOutputStream fileOut = // binär serializerar nyckeln för att sedan användas i andra klasser
			         new FileOutputStream(file);

			 ObjectOutputStream out = new ObjectOutputStream(fileOut);
	         out.writeObject(key);
	         out.close();
	         fileOut.close();
		} catch (NoSuchAlgorithmException | IOException e) {
			strOut = "Failed to generate and save SecretKey to file";
		}
		return strOut;
	}

}
