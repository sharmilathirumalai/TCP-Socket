import java.net.*;
import java.util.Scanner;
import java.io.*; 

public class Server 
{ 
	private Socket          socket   = null; 
	private ServerSocket    server   = null; 
	private DataInputStream in       =  null; 
	private PrintWriter	out 		 = null;


	// constructor with port 
	public Server() 
	{ 
		try
		{ 
			server = new ServerSocket(5000); 
			System.out.println("Server started"); 
			
			// waits for client
			System.out.println("Waiting for a client ..."); 
			socket = server.accept(); 
			
			System.out.println("Accepted client"); 

			// input from the client socket 
			in = new DataInputStream(new BufferedInputStream(socket.getInputStream())); 
			out = new PrintWriter(socket.getOutputStream(), true);
			
			// starts listening to client
			pullIncomingMessage();

			System.out.println("Closing connection"); 

			// exit connection
			socket.close(); 
			in.close(); 
		} 
		catch(IOException i) 
		{ 
			System.out.println(i); 
		} 
	} 


	private void pullIncomingMessage() {
		Scanner scan = new Scanner(in);
		scan.useDelimiter("\r\n");

		String line = "";
		String message = "";
		MessageHandler handle = new MessageHandler(out);

		while(line != "EXIT") {
			if(scan.hasNextLine()) {
				line = scan.nextLine();
				if(line.isEmpty()) {
					if(message != "") {
						handle.incomingMessage(message, scan);
						message = "";
					}
				} else if(!line.isEmpty()){
					message = message + "\r\n" + line;
				}
			}
		}

		scan.close();
	}

	public static void main(String args[]) 
	{
		// Starting server instance
		new Server(); 
	} 
} 