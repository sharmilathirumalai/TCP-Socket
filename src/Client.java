import java.util.Date;

public class Client {
	String uname;
	Date DOB;
	Session session = new Session();
	int EmployeeID;
	static Boolean isAuthenticated;
	private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";


	Client(String uname, Date DOB) {
		this.uname = uname;
		this.DOB = DOB;
		startSession();
	}

	public Client() {
	}

	public String getName() {
		return uname;
	}
	public Date getDOB() {
		return DOB;
	}

	public  boolean isAUthenticatedUser() {
		return isAuthenticated;
	}

	public void startSession() {
		isAuthenticated = true;
		session.generateCookie(uname.length());
	}


	public void endSession() {
		session.clearCookie();
	}


	public String getCookie() {
		return session.cookie;
	}

	// A client can have only one session opened
	public  class  Session {
		String cookie = null;

		// Generates cookie whose length is bounded by 7
		public  void generateCookie(int count) {
			StringBuilder builder = new StringBuilder();
			while (count-- != 0) {
			int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
			builder.append(ALPHA_NUMERIC_STRING.charAt(character));
			}
			cookie = builder.toString();
		}

		public  void clearCookie() {
			cookie = null;
		}
	}
}
