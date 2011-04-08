package cat;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.logging.Logger;

public class Test {
	static Logger log = Logger.getLogger("Test");

	public static void main(String[] args) throws Exception {
		testDB();
	}

	static void testDB() throws Exception {
		Class.forName("org.sqlite.JDBC");
		Connection conn = DriverManager.getConnection("jdbc:sqlite:testaa.db");
		PreparedStatement ps = conn
				.prepareStatement("CREATE TABLE Source(ID INTEGER PRIMARY KEY AUTOINCREMENT,Type VARCHAR(20) NOT NULL,Item VARCHAR(20) NOT NULL)");
		ps.executeUpdate();
		conn.close();
	}

	static void testStringFormat() {
		log.info(String.format("%-" + (12 - 3) + "s%3.2f", "大长今", 12.0));
		log.info(String.format("%-" + (12 - 2) + "s%3.2f", "大长", 12.1));
	}
}
