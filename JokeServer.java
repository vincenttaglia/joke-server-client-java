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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;



/* Worker thread that is created when an admin connection is opened */
class AdminWorker extends Thread{
	Socket sock;
	Data data;
	
	/* Worker constructor, assigns passed values to sock and data */
	AdminWorker (Socket s, Data d) {sock = s; data = d;}
	
	public void run() {
		PrintStream out = null;
		BufferedReader in = null;
		try {
			/* Setting up the input and output streams */
			in = new BufferedReader(
				new InputStreamReader(sock.getInputStream())
			);
			out = new PrintStream(sock.getOutputStream());
			
			try {
				char command;
				command = in.readLine().charAt(0);
				
				/* Execute command based off of command character passed */
				switch(command) {
				case 't':
					out.println(data.toggleMode());
					break;
				case 'm':
					out.println(data.getMode());
					break;
				}
			} catch(IOException x) {
				System.out.println("Server read error");
				x.printStackTrace();
			}
			sock.close();
		} catch(IOException ioe) { System.out.println(ioe); }
	}
	
}




/* Worker thread that is created when a connection is opened */
class Worker extends Thread{
	Socket sock;
	Data data;
	
	/* Worker constructor, assigns passed values to sock and data */
	Worker (Socket s, Data d) {sock = s; data = d;}
	
	public void run() {
		PrintStream out = null;
		BufferedReader in = null;
		try {
			/* Setting up the input and output streams */
			in = new BufferedReader(
				new InputStreamReader(sock.getInputStream())
			);
			out = new PrintStream(sock.getOutputStream());
			
			try {
				String cookie;
				String name;
				
				/* Reads cookie and name */
				cookie = in.readLine();
				name = in.readLine();
				
				/* Add session if cookie is needed, otherwise read 3rd line for command */
				if(cookie.charAt(0) == 's') {
					out.println(data.addSession());
				}else{
					char command;
					command = in.readLine().charAt(0);
					
					/* Execute command based off of command character passed */
					switch(command) {
					case 'j':
						out.println(data.getJoke(cookie, name));
						break;
					case 'm':
						out.println(data.getMode());
						break;
					}
					
				}
			} catch(IOException x) {
				System.out.println("Server read error");
				x.printStackTrace();
			}
			sock.close(); //Close socket
		} catch(IOException ioe) { System.out.println(ioe); }
	}
	
	static String toText(byte ip[]) {  
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < ip.length; ++ i){
			if (i > 0) result.append (".");
			result.append(0xff & ip[i]);
		}
		return result.toString ();
	}
	
}

/* Data class for holding state info */
class Data{
	
	/* State class for holding states */
	class State{
		private Boolean[] jokeState = new Boolean[4];
		private Boolean[] provState = new Boolean[4];
		
		/* State constructor */
		State() {
			for(int i = 0; i < 4; i++) {
				jokeState[i] = false;
				provState[i] = false;
			}
		}
		
		/* Return state based on jokeMode */
		public Boolean getState(Boolean jokeMode, int i) {
			return (jokeMode ? jokeState : provState)[i];
		}
		
		/* Set state based on jokeMode */
		public void setState(Boolean jokeMode, int i, Boolean newState) {
			(jokeMode ? jokeState : provState)[i] = newState;
		}
		
		/* Get number of undseen items based on jokeMode */
		public int numUnseen(Boolean jokeMode) {
			int unseen = 0;
			for(Boolean state: (jokeMode ? jokeState : provState)) {
				if(state == false)
					unseen++;
			}
			return unseen;
		}
	}
	Boolean jokeMode = true;
	int arrayLen = 4;
	String[] jokes = new String[arrayLen];
	String[] provs = new String[arrayLen];
	HashMap<String, State> sessions = new HashMap<>();
	
	/* Constructor initializes jokes and proverbs */
	public Data() {
		jokes[0] = "JA {name}: Telling my daughter garlic is good for you. Good immune system and keeps pests away.Ticks, mosquitos, vampires... men.";
		jokes[1] = "JB {name}: I've been going through a really rough period at work this week It's my own fault for swapping my tampax for sand paper.";
		jokes[2] = "JC {name}: If I could have dinner with anyone, dead or alive... ...I would choose alive. -B.J. Novak-";
		jokes[3] = "JD {name}: Two guys walk into a bar. The third guy ducks.";
		
		provs[0] = "PA {name}: Two wrongs don't make a right.";
		provs[1] = "PB {name}: The pen is mightier than the sword.";
		provs[2] = "PC {name}: When in Rome, do as the Romans.";
		provs[3] = "PD {name}: The squeaky wheel gets the grease.";
	}
	
	/* addSession initializes new jokeState and new provState, and adds state index to the hashmap, using the cookie as a key */
	public String addSession() {
		System.out.println("Generating new session");
		Random rand = new Random();
		/* Generate hex cookie */
		String cookie = Integer.toHexString(rand.nextInt(Integer.MAX_VALUE));
		
		/* Make sure cookie is unique */
		while(sessions.get(cookie) != null)
			cookie = Integer.toHexString(rand.nextInt(Integer.MAX_VALUE));
		
		/* Add the index of the state array to the hashmap, using the cookie as the key */
		sessions.put(cookie, new State());
		
		System.out.println("New session generated: " + cookie);
		
		return cookie;
	}
	
	/* Returns a personalized joke using the client's cookie */
	public String getJoke(String cookie, String name) {
		System.out.println("Getting " + getMode() + " for session " + cookie);
		Random rand = new Random();
		String fin;
		State state;
		
		/* Get state index from valid cookie */
		try {
			state = sessions.get(cookie);
			
		}catch(Exception x) {
			System.out.println("Invalid cookie");
			return "Invalid cookie";
		}
		
		
		/* Keep track of how many jokes or proverbs are left to return */
		int numUnseen = state.numUnseen(jokeMode);
		
		/* Get random joke or proverb that hasn't been returned yet */
		int indx = rand.nextInt(arrayLen);
		while(state.getState(jokeMode, indx) == true) {
			indx = rand.nextInt(arrayLen);
		}
		
		/* Get the joke or proverb */
		if(jokeMode)
			fin = jokes[indx];
		else
			fin = provs[indx];
		
		
		/* Set joke state to seen, unless it is the last one to be seen */
		if(numUnseen > 1) {
			state.setState(jokeMode, indx, true);
		}else {
			for(int i = 0; i < arrayLen; i++) {
				state.setState(jokeMode, i, false);;
			}
		}
		
		/* Return the joke string with the name substituted */
		return fin.replaceAll("\\{name}", name);
	}
	
	/* Toggle server mode */
	public String toggleMode() {
		jokeMode = !jokeMode;
		
		String str;
		if(jokeMode)
			str = "Mode toggled to Joke Mode";
		else
			str = "Mode toggled to Proverb Mode";
		System.out.println(str);
		return str;
	}
	
	/* Get server mode */
	public String getMode() {
		System.out.println("Getting server mode");
		if(jokeMode)
			return "joke";
		else
			return "proverb";
	}
	
	/* Helper function for how many jokes are left to see */
	private int numUnseen(Boolean[] states) {
		int unseen = 0;
		for(Boolean state: states) {
			if(state == false)
				unseen++;
		}
		return unseen;
	}
	
}

class AdminLooper implements Runnable {
	Data data;
	
	/* AdminLooper constructor */
	public AdminLooper(Data data) {
		this.data = data;
	}
	
	public void run(){   
	    int q_len = 6;
	    int port = 5050;
	    Socket sock;
	    /* Try statement for socket creation */
	    try{
			ServerSocket servsock = new ServerSocket(port, q_len);
			/* Wait until someone connects to the socket, launch worker */
			while (true) {
				sock = servsock.accept();
				new AdminWorker(sock, data).start(); 
			}
	    }catch (IOException ioe) {System.out.println(ioe);}
	}
}

public class JokeServer{
	public static void main(String a[]) throws IOException{
		int q_len = 6;
		int port = 4545;
		Socket sock;
		Data data = new Data();
		
		/* Create and launch AdminLooper thread */
		AdminLooper AL = new AdminLooper(data);
	    Thread t = new Thread(AL);
	    t.start();
		
		ServerSocket servsock = new ServerSocket(port, q_len);
		System.out.println("Victor Taglia's JokeServer 1.8 starting up, listening at port " + port + ".\n");
		/* Keep looking for a socket connection */
		while(true) {
			sock = servsock.accept();
			new Worker(sock, data).start();
		}
	}
}





