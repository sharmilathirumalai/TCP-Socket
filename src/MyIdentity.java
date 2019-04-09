import java.util.Properties;

public class MyIdentity {

	public static void setIdentity(Properties dbProps) {
		dbProps.setProperty("database", "class_3901");
		dbProps.setProperty("user", "root");
		dbProps.setProperty("pwd", "root");
	}
}

