import java.util.HashMap;
import java.util.Map;

public class Order {
	String address;
	String city;
	String region;
	String postal;
	String country;
	String cusID;

	Map<Integer, Integer> products = new HashMap<Integer, Integer>();

	Order(String cusID, String address,String city, String region, String postal, String country) {
		this.address = address;
		this.city = city;
		this.region = region;
		this.postal = postal;
		this.country = country;
		this.cusID = cusID;
	}
	
	boolean IsAddressPresent() {
		return address !=null || city != null || region != null || postal != null || country != null;
	}
	
	void addProduct(int pdtID, int quantity) {
		products.put(pdtID, quantity);
	}
}
