/*--------------------------------------------------------

Victor Taglia
10/23/18
Java v1.8.0_181

Compile command:
> javac JokeServer.java
> javac JokeClient.java
> javac JokeClientAdmin.java

Run commands:
> java JokeServer
> java JokeClient
> java JokeClientAdmin

Cross-computer commands:
> java JokeClient 140.192.1.22
> java JokeClientAdmin 140.192.1.22

Required Documents:
a. checklist.html
b. JokeServer.java
c. JokeClient.java
d. JokeClientAdmin.java

Notes:


----------------------------------------------------------*/

import java.io.*;
import java.net.*;


public class JokeClientAdmin {
	public static void main (String args[]) {
		String serverName;
		String mode;
		/* Use localhost if no hostname argument given */
		if(args.length < 1) serverName = "localhost";
		else serverName = args[0];
		
		System.out.println("Victor Taglia's JokeAdminClient, 1.8.\n");
		System.out.println("Using server: " + serverName + ", Port: 5050");
		
		/* Get server mode */
		mode = executeCommand(serverName, 'm');
		
		/* Create input buffer for user input */
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));		
		/* Try statement for input capturing */
		try {
			String input;
			do {
				System.out.print("Hit enter to switch server mode from " + mode + " mode, (quit) to end: ");
				System.out.flush();
				input = in.readLine();
				/* If they don't quit, toggle server mode and update server mode on client side */
				if(input.indexOf("quit") < 0) {
					System.out.println(executeCommand(serverName, 't'));
					mode = executeCommand(serverName, 'm');
				}
			} while(input.indexOf("quit") < 0);
			System.out.println("Cancelled by user request.");
		} catch (IOException x) {x.printStackTrace();}
		
	}
	
	static String executeCommand(String serverName, char command) {
		Socket sock;
		BufferedReader fromServer;
		PrintStream toServer;
		String textFromServer = null;
		
		/* Try statement for socket connection */
		try {
			sock = new Socket(serverName, 5050);
			/* set up upstream and downstream buffers */
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			toServer = new PrintStream(sock.getOutputStream());
			/* Give cookie, name, and command */
			toServer.println(command);
			toServer.flush();
			
			/* Get server response */
			textFromServer = fromServer.readLine();
			
			sock.close();// Close socket
		} catch (IOException x) {
			System.out.println ("Socket error.");
			x.printStackTrace ();
		}
		return textFromServer;
	}
	
}
