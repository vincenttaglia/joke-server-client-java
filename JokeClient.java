/*--------------------------------------------------------

Victor Taglia
10/23/18
Java v1.8.0_181

Compile commands:
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


public class JokeClient {
	public static void main (String args[]) {
		String serverName;
		String reqName = "";
		String cookie = null;
		/* Use localhost if no hostname argument given */
		if(args.length < 1) serverName = "localhost";
		else serverName = args[0];
		
		System.out.println("Victor Taglia's JokeClient, 1.8.\n");
		System.out.println("Using server: " + serverName + ", Port: 4545");
		
		
		/* Create input buffer for user input */
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		/* Try statement for input capturing */
		try {
			do {
				System.out.print("Please enter your name: ");
				System.out.flush();
				reqName = in.readLine();
			} while(reqName.isEmpty());
		}catch (IOException x) {x.printStackTrace();}
		
		/* Get cookie for new client session */
		cookie = generateSession(serverName, cookie, reqName);
		
		/* Try statement for user input capturing */
		try {
			String name;
			do {
				System.out.print("Hit enter to get a " + executeCommand(serverName, cookie, reqName, 'm') + ", (quit) to end: "); //executeCommand to get server mode
				System.out.flush();
				name = in.readLine();
				/* Get new joke if user didn't ask to quit */
				if(name.indexOf("quit") < 0) System.out.println(executeCommand(serverName, cookie, reqName, 'j'));
			} while(name.indexOf("quit") < 0);
			System.out.println("Cancelled by user request.");
		} catch (IOException x) {x.printStackTrace();}
		
	}
	
	static String generateSession(String serverName, String cookie, String name) {
		Socket sock;
		BufferedReader fromServer;
		PrintStream toServer;
		String textFromServer = null;
		
		/* Try statement for socket connection */
		try {
			System.out.println("Generating new session.");
			sock = new Socket(serverName, 4545);
			/* set up upstream and downstream buffers */
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			toServer = new PrintStream(sock.getOutputStream());
			/* Request new cookie and give name */
			toServer.println('s');
			toServer.println(name);
			toServer.flush();
			
			/* Get server response */
			textFromServer = fromServer.readLine();
			
			sock.close(); // Close socket
			System.out.println("Cookie: " + textFromServer);
		} catch (IOException x) {
			System.out.println ("Socket error.");
			x.printStackTrace ();
		}
		
		return textFromServer;
	}
	
	static String executeCommand(String serverName, String cookie, String name, char command) {
		Socket sock;
		BufferedReader fromServer;
		PrintStream toServer;
		String textFromServer = null;
		
		/* Try statement for socket connection */
		try {
			sock = new Socket(serverName, 4545);
			/* set up upstream and downstream buffers */
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			toServer = new PrintStream(sock.getOutputStream());
			/* Give cookie, name, and command */
			toServer.println(cookie);
			toServer.println(name);
			toServer.println(command);
			toServer.flush();
			
			/* Get server response */
			textFromServer = fromServer.readLine();
			
			sock.close(); // Close socket
		} catch (IOException x) {
			System.out.println ("Socket error.");
			x.printStackTrace ();
		}
		
		return textFromServer;
	}
	
}
