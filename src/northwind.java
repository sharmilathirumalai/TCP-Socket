import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Map;
import java.util.Properties;

public class northwind {

	Connection con;

	private void connect() throws Exception {
		Class.forName("com.mysql.cj.jdbc.Driver");
		String URL = "jdbc:mysql://db.cs.dal.ca:3306?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";

		//Gets and sets username and password from properties file
		Properties id = new Properties();        
		MyIdentity.setIdentity( id );

		// Connection String
		con = DriverManager.getConnection(URL, id.getProperty("user"), id.getProperty("pwd"));
	}


	public Boolean authenticate(Client userDO) throws Exception {
		connect();
		Statement s= con.createStatement();  
		Properties id = new Properties();        
		MyIdentity.setIdentity( id );

		s.executeQuery("USE "+ id.getProperty("database"));

		// Query for checking the user credentials
		String query = "SELECT * FROM employees where LastName = '"+ userDO.uname +"' and BirthDate = '"+ userDO.DOB +"';";
		ResultSet result = s.executeQuery(query);  

		if(result.next()) {
			// If true start session
			userDO.EmployeeID = result.getInt("EmployeeID");
			userDO.startSession();
			con.close();
			return true;
		}

		con.close();
		return false;
	}

	public int placeOrder(Order order, Client userDO) throws Exception {
		System.out.println("Place order....");
		System.out.println(order.cusID);
		System.out.println(userDO.uname);

		connect();
		Statement s= con.createStatement();  
		Properties id = new Properties();        
		MyIdentity.setIdentity( id );

		s.executeQuery("USE "+ id.getProperty("database"));

		// Get the last placed orderID and computes next orderID.
		ResultSet result = s.executeQuery("SELECT max(OrderID) FROM orders;"); 
		int nextOrderID;

		if(result.next()) {
			nextOrderID = result.getInt(1) + 1;
		} else {
			nextOrderID = 1;
		}
		
		// Inserts the order details into order table
		String orderQuery = "INSERT INTO orders (OrderID, CustomerID, EmployeeID, ShipAddress, ShipCity, ShipRegion, ShipPostalCode, ShipCountry, OrderDate, RequiredDate) VALUES(?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement POStmt=con.prepareStatement(orderQuery);  

		POStmt.setInt(1, nextOrderID);
		POStmt.setString(2, order.cusID);
		POStmt.setInt(3, userDO.EmployeeID);

		// Uses the custom address
		if(order.IsAddressPresent()) {
			POStmt.setString(4, order.address);
			POStmt.setString(5, order.city);
			POStmt.setString(6, order.region);
			POStmt.setString(7, order.postal);
			POStmt.setString(8, order.country);

		} else {
			// Gets the  address from table
			result = s.executeQuery("SELECT * FROM customers where CustomerID='"+ order.cusID+ "';");
			if(result.next()) {
				POStmt.setString(4, result.getString("Address"));
				POStmt.setString(5, result.getString("City"));
				POStmt.setString(6, result.getString("Region"));
				POStmt.setString(7, result.getString("PostalCode"));
				POStmt.setString(8, result.getString("Country"));
			} else {
				return 0;
			}
		}

		LocalDate orderDate = LocalDate.now();
		LocalDate requiredDate = LocalDate.now().plusDays(7);

		POStmt.setDate(9, java.sql.Date.valueOf(orderDate));
		POStmt.setDate(10, java.sql.Date.valueOf(requiredDate));

		if(POStmt.executeUpdate() != 1) {
			return 0;
		} 
		
		// Inserts the product details into order table
		String orderDetailsQuery = "INSERT INTO orderdetails (OrderID, ProductID, Quantity, UnitPrice) VALUES(?,?,?,?)";
		PreparedStatement SODetailsStmt = con.prepareStatement(orderDetailsQuery);  
		SODetailsStmt.setInt(1, nextOrderID);

		for (Map.Entry<Integer, Integer> pair : order.products.entrySet()) {
			System.out.println(pair.getKey() + " " + pair.getValue());
			SODetailsStmt.setInt(2, pair.getKey());
			SODetailsStmt.setInt(3, pair.getValue());
			result = s.executeQuery("SELECT UnitPrice FROM products where ProductID="+ pair.getKey()+ ";");
			result.next();
			SODetailsStmt.setFloat(4, result.getFloat("UnitPrice"));

			if(SODetailsStmt.executeUpdate() != 1) {
				return 0;
			} 
		}

		return nextOrderID;
	}
	
	// Gets the list of customers, products, and products count in the order
	public String listElements(String target, String ID, Order order) throws Exception {
		System.out.println("Listing "+ target +"....");
		connect();
		Statement s= con.createStatement(); 
		Properties id = new Properties();        
		MyIdentity.setIdentity( id );

		s.executeQuery("USE "+ id.getProperty("database"));
		ResultSet result = null;
		String IDColumn = "ProductID";
		String NameColumn = "ProductName";
		String row = "";

		if(target.equals("product")) {
			result = s.executeQuery("SELECT \r\n" + 
					"   *\r\n" + 
					"FROM products\r\n");
			row = IDColumn + "	" + NameColumn + "\r\n";
			while(result.next()) {
				row = row + result.getString(IDColumn) + "	" + result.getString(NameColumn) + "\r\n";
			}

		} else if(target.equals("customer")) {
			IDColumn = "CustomerID";
			NameColumn = "CompanyName";
			result = s.executeQuery("SELECT \r\n" + 
					"   *\r\n" + 
					"FROM customers\r\n");
			row = IDColumn + "	" + NameColumn + "\r\n";
			while(result.next()) {
				row = row + result.getString(IDColumn) + "	" + result.getString(NameColumn) + "\r\n";
			}

		} else if(target.equals("order")) {

			row = IDColumn + "	Quantity" + "\r\n";
			for (Map.Entry<Integer, Integer> pair : order.products.entrySet()) {
				row = row + pair.getKey() + "	" + pair.getValue() + "\r\n";
			}
		}


		con.close();
		return row;
	}

	// Checks whether entered product is valid
	public int checkProduct(int PID, int Quantity) throws Exception {
		connect();
		Statement s= con.createStatement();  
		Properties id = new Properties();        
		MyIdentity.setIdentity( id );

		s.executeQuery("USE "+ id.getProperty("database"));
		ResultSet result = s.executeQuery("SELECT \r\n" + 
				"    Discontinued\r\n" + 
				"FROM\r\n" + 
				"   products\r\n" + 
				"WHERE\r\n" + 
				"    ProductID = " + PID);  
		
		// Checks whether product exist in the table
		if(!result.next()) {
			return 2;
		}
		
		// Checks for discontinued products
		if(result.getInt("Discontinued") == 1) {
			return 3;
		}

		con.close();
		return 1;
	}

}
