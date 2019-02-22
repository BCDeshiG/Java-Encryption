/* **********************************************
	AUTHOR: Abhid Islam
		A port of my Python GCSE coursework.
		Optimised by removing repeated code.
		Also supports changing the key length.
		Fixes broken chunking too.
		The specification is still crap though.
   ********************************************** */

import java.util.Scanner; // User Input
import java.io.*; // File Read/Write
import java.util.Random; // Random Number Generator

class encryptor
{
	public static void main(String[] args){
		menu();
		return;
	} // END main

	public static String inputString(String rq){ // Gets user input
		Scanner sc = new Scanner(System.in);
		System.out.println(rq);
		String ans = sc.nextLine();
		return ans;
	} // END inputString

	public static String readFile(){ // Reads contents of file
		Boolean fileValid = false;
		String file = "";
		while(!fileValid){ // Forces user to input valid file
			try{ // Attempt to read file
				String fileName = inputString("\nPlease enter the name of your file.");
				BufferedReader inStream = new BufferedReader(new FileReader(fileName));
				file = inStream.readLine(); // FIXME: Only reads 1st Line
				inStream.close(); // Close file to free it
				fileValid = true; // File read successfully
			} catch (IOException e){ // Runs if file doesn't exist
				System.out.println("\nThis file does not exist.");
				fileValid = false; // File not read
			}
		}
		return file; // Returns contents of file
	} // END readFile

	public static void outputFile(String text){ // Writes contents to specified file
		try{
			String fileName = inputString("\nPlease enter the name of your file.");
			PrintWriter outputStream = new PrintWriter(new FileWriter(fileName));
			outputStream.println(text);
			System.out.println("File written.");
			outputStream.close(); // Close file to free it
		} catch(IOException e){
			System.out.println("WTF?"); // If this happens you are truly special
		}
		return;
	} // END outputFile

	public static int genInt(int bound1, int bound2){ // Generates random integer
		Random r = new Random();
		int gen = r.nextInt(bound2 - bound1) + bound1;
		return gen;
	} // END genInt

	public static int sum(int[] arr){ // Calculates sum of integers in array
		int sum = 0;
		for(int i=0; i<arr.length; i++){
			sum += arr[i];
		}
		return sum;
	} // END sum

	public static void menu(){ // User Interface
		final int keyLength = 8; // Length of the encryption key
		int choice = 0; // Menu choice

		while(choice != 4){ // Forces user to choose an option
			// Display menu options
			System.out.println("\nAbhid's Encryption Software\n");
			System.out.println("Option 1: Encrypt Message");
			System.out.println("Option 2: Decrypt Message");
			System.out.println("Option 3: Advanced Encryption");
			System.out.println("Option 4: Exit\n");

			// Handle user's menu choice
			try{
				choice = Integer.parseInt(inputString("Please type in 1, 2, 3 or 4 to choose an option."));
			} catch(NumberFormatException e){
				System.out.println("INVALID INPUT");
				menu();
			}
			if(choice==1){
				encryptMessage(keyLength, "Normal"); // Standard Encryption
			}
			else if(choice==2){
				decryptMessage(keyLength); // Decryption
			}
			else if(choice==3){
				encryptMessage(keyLength, "Advanced"); // Chunks text before encryption
			}
		}

		System.out.println("\nThank you for using this program. Goodbye.");
		System.exit(0); // Closes program when option 4 is chosen
		return;
	} // END menu

	public static int offset(int[] nKeyChars, final int keyLength){ // Calculates offset factor
		int charSum = sum(nKeyChars); // Gets sum of ASCII values
		int offsetFactor = Math.round(((float)charSum)/keyLength) - 32;
		return offsetFactor;
	} // END offset

	public static void encryptMessage(final int keyLength, String mode){ // Main method for encryption
		String file = readFile();
		String key = "\'";
		int[] nKeyChars = new int[keyLength];

		for(int i=0; i<keyLength; i++){
			nKeyChars[i] = genInt(33,127); // Generates random number within ASCII range
			key += (char) nKeyChars[i]; // Casts to char then adds to end of 'key' String
		}
		key += "\'";
		System.out.println("\nKeep this key safe for decryption: " + key);

		final int offsetFactor = offset(nKeyChars, keyLength);
		System.out.println("Original text: " + file);
		if(mode.equals("Advanced")){
			file = chunk(file); // Split message into chunks
			//System.out.println("Chunked text: " + file);
		}
		String cipherText = encrypt(file, offsetFactor); // Regular Algorithm
		System.out.println("Encryption Complete: " + cipherText);
		outputFile(cipherText);
		return;
	} // END encryptMessage

	public static String encrypt(final String message, final int offsetFactor){ // Encryption Algorithm
		char[] cipher = new char[message.length()]; // Make cipher same length as message
		final char space = ' ';
		for(int i=0; i<cipher.length; i++){
			if(message.charAt(i) != space){ // Checks for space
				int ch = (int) message.charAt(i) + offsetFactor; // Offset the ASCII number
				if(ch >= 127){
					ch -= 94; // Puts number within ASCII range
				}
				cipher[i] = (char) ch; // Adds offset ASCII number to array
			}
			else{
				cipher[i] = 32; // Adds ASCII number for space
			}
		}
		// Build string from array of offset characters
		String cipherText = "";
		for(int i=0; i<cipher.length; i++){
			cipherText += cipher[i];
		}
		return cipherText;
	} // END encrypt

	/* Encryption Algorithm that removes spaces and chunks text
	NOTE: count is needed to ensure spaces aren't counted. Without count,
	the message won't chunk properly (which works arguably better though) */
	public static String chunk(final String message){
		String alteredMessage = ""; // Stores chunked message
		final char space = ' ';
		int count = 0;
		for(int i=0; i<message.length(); i++){
			if(message.charAt(i) != space){ // Checks for space
				alteredMessage += message.charAt(i); // Add character to alteredMessage
				count++;
				if(count%5==0){ // If a multiple of 5
					alteredMessage += " "; // Add a space
				}
			}
		}
		return alteredMessage;
	} // END chunk

	public static void decryptMessage(final int keyLength){ // Main method for decryption
		final String file = readFile();
		String key = "";
		Boolean keyValid = false;
		while(!keyValid){ // Forces user to input valid encryption key
			key = inputString("Please input your " + keyLength + " character key.");
			if(key.length() == keyLength){ // Checks if input is correct length
				keyValid = true;
			}
			else{
				System.out.println("Invalid key detected.");
				keyValid = false; // Key was invalid
			}
		}

		int[] nKeyChars = new int[keyLength];
		for(int i=0; i<keyLength; i++){
			nKeyChars[i] = (int) key.charAt(i); // Converts to ASCII number and stores in array
		}
		final int offsetFactor = offset(nKeyChars, keyLength);
		String plainText = decrypt(file, offsetFactor);
		System.out.println("Decryption Complete: " + plainText);
		return;
	} // END decryptMessage

	public static String decrypt(final String cipher, final int offsetFactor){
		System.out.println("Encrypted text: " + cipher);
		char[] plain = new char[cipher.length()]; // Make plain length same as cipher
		final char space = ' ';
		for(int i=0; i<plain.length; i++){
			if(cipher.charAt(i) != space){ // Checks for space
				int ch = (int) cipher.charAt(i) - offsetFactor; // Offset the ASCII number
				if(ch <= 32){
					ch += 94; // Puts number within ASCII range
				}
				plain[i] = (char) ch; // Adds offset ASCII number to array
			}
			else{
				plain[i] = 32; // Adds ASCII number for space
			}
		}
		// Build string from array of offset characters
		String plainText = "";
		for(int i=0; i<plain.length; i++){
			plainText += plain[i];
		}
		return plainText;
	}// END decrypt
} // END class encryptor