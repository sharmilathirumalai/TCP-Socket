import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class MessageHandler {

	private String oprn = null;
	private String target = null;
	private String protocol = null;
	private Order order = null;
	private PrintWriter out = null;
	private int contentLength = 0;
	private Map<String, String> requestHeaders = null;
	private Client userDO;

	private ArrayList<String> body = new ArrayList<String>();
	private northwind db = new northwind();

	private final ArrayList<String> supportedProtocols =  new ArrayList<String>(Arrays.asList("Order3901/1.0"));
	private final int successStart = 200;
	private final int successEnd = 299;

	public MessageHandler(PrintWriter out) {
		this.out = out;
	}

	public void incomingMessage(String message, Scanner scan)  {
		requestHeaders = new HashMap<String, String>();
		body = new ArrayList<String>();

		String lines [] = message.split("\r\n");
		String[] firstLine = lines[1].split(" ", 3);
		int i = 1;

		try {

			if(firstLine.length < 3) {
				throw new Exception ("Bad Format");
			}
			protocol = firstLine[2];
			oprn = firstLine[0];
			target = firstLine [1];

			if(!supportedProtocols.contains(protocol)) {
				WriteOutgoingMessage(500, "");
				return;
			}

			while(lines.length > i + 1 ) {
				String[] nextLine = lines[i+1].split(" ", 2);
				String headerName = nextLine[0].split(":")[0];
				requestHeaders.put(headerName, nextLine[1]);
				i++;
			}

			System.out.println("Processed Header");

			// Pulls body if content length is present
			if(requestHeaders.get("Content-Length") != null) {
				contentLength  = Integer.valueOf(requestHeaders.get("Content-Length")).intValue();
				if(processBody(scan)) {
					System.out.println("Processed Body");
				} else {
					return;
				}
			}


			if(!oprn.equalsIgnoreCase("AUTH") && userDO == null) {
				WriteOutgoingMessage(401, "");
			} else {
				performOperation();
			}


		} catch (Exception e){
			e.printStackTrace();
			WriteOutgoingMessage(500, "");
		}
	}

	private boolean processBody(Scanner scan) throws Exception {

		int j = 0;
		while (j < (contentLength - 2)) {
			if(scan.hasNextLine()) {
				String line = scan.nextLine();
				body.add(line);
				j = j + line.length();
			}
		}

		if(j != (contentLength - 2)) {
			WriteOutgoingMessage(404, "");
			return false;
		}

		return true;
	}

	private void performOperation() throws Exception  {
		int statusCode = 404;
		String responsebody = "";

		if(oprn.equalsIgnoreCase("AUTH")) {

			if(userDO != null && userDO.isAUthenticatedUser()) {
				throw new Exception ("Already logged-in");
			} else {
				userDO = new Client(target, java.sql.Date.valueOf(requestHeaders.get("Password")));
				if(db.authenticate(userDO)) {
					statusCode = 200;
				} else {
					statusCode = 401;
				}
			}

		} else if(oprn.equalsIgnoreCase("NEW")) {

			if(!requestHeaders.get("Cookie").contentEquals(userDO.getCookie())) {
				statusCode = 401;
			} else {
				if(order != null) {
					statusCode = 402;
				} else {

					String address = requestHeaders.get("Address");
					String city = requestHeaders.get("City");
					String region = requestHeaders.get("Region");
					String postal = requestHeaders.get("PostalCode");
					String country = requestHeaders.get("Country");
					if(requestHeaders.size() == 1 || requestHeaders.size() == 6) {
						statusCode = 200;
						order = new Order(target, address, city, region, postal, country);
					}
				}
			}

		} else if(oprn.equalsIgnoreCase("ADD")) {

			if(!requestHeaders.get("Cookie").contentEquals(userDO.getCookie())) {
				statusCode = 401;
			} else {

				if(order == null) {
					statusCode = 402;
				} else {
					int PID = Integer.valueOf(target);
					int quantity = Integer.valueOf(body.get(0));
					int pdtstatus = db.checkProduct(PID, quantity);
					if(pdtstatus == 1) {
						statusCode = 200;
						order.addProduct(PID, quantity);
					} else if(pdtstatus == 2) {
						statusCode = 406;
					} else if(pdtstatus == 3) {
						statusCode = 407;
					}
				}

			}

		} else if(oprn.equalsIgnoreCase("LIST")) {

			if(!requestHeaders.get("Cookie").equals(userDO.getCookie())) {
				statusCode = 401;
			} else {
				if(body.size() != 0) {
					responsebody = db.listElements(target, body.get(0), order);
				} else {
					responsebody = db.listElements(target, "", order);
				}
				statusCode = 200;
			}

		} else if(oprn.equalsIgnoreCase("ORDER")) {

			if(!requestHeaders.get("Cookie").equals(userDO.getCookie())) {
				statusCode = 401;
			} else {
				int orderID = db.placeOrder(order, userDO);
				statusCode = 200;
				order = null;
				responsebody = String.valueOf(orderID);
			}

		} else if(oprn.equalsIgnoreCase("DROP")) {

			if(!requestHeaders.get("Cookie").equals(userDO.getCookie())) {
				statusCode = 401;
			} else {
				if(order == null) {
					statusCode = 402;
				} else {
					order = null;
					statusCode = 200;
				}
			}
		} else if(oprn.equalsIgnoreCase("LOGOUT")) {

			if(!requestHeaders.get("Cookie").equals(userDO.getCookie())) {
				statusCode = 401;
			} else {
				shutdownConnection();
				statusCode = 200;
			}
		}

		WriteOutgoingMessage(statusCode, responsebody);
	}


	private void WriteOutgoingMessage(int statusCode, String responsebody) {
		String message = protocol + " " + statusCode;
		String statusmsg = "eee";

		if(statusCode >= successStart && statusCode  <= successEnd) {
			statusmsg = "OK";
		} else if(statusCode == 500) {
			statusmsg = "Internal Error";
		} else if(statusCode == 402) {
			statusmsg = "Wrong state";
		} else if(statusCode == 404) {
			statusmsg = "Bad Client";
		} else if(statusCode == 401) {
			statusmsg = "Unauthorized";
		}

		message = message + " " + statusmsg + "\r\n";

		if(oprn.equalsIgnoreCase("AUTH") && (statusCode >= successStart && statusCode  <= successEnd)) {
			message = message + "Set-Cookie:" + " " + userDO.getCookie() + "\r\n";
		}

		if(!responsebody.isEmpty()) {
			message = message + "Content-Length:" + " " + (responsebody.length() + 2) + "\r\n";
		}

		message = message + "\r\n";

		if(!responsebody.isEmpty()) {
			message = message + responsebody + "\r\n" + "\r\n";
		}

		System.out.println(message);
		out.write(message);
		out.flush();
	}

	private void shutdownConnection() {
		userDO = null;
		order = null;
	}
}
