package cat;

import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import java.util.logging.Logger;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;

public class Test {
	static Logger log = Logger.getLogger("Test");

	public static void main(String[] args) throws Exception {
//		testGetLocalInfo();
		RemoteOne.run();
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

	static void testGetLocalInfo() {
		LocalDevice localdev = null;

		try {
			localdev = LocalDevice.getLocalDevice();
		} catch (BluetoothStateException e) {
			System.out.println("ERROR: cannot access local device");
			System.exit(1);
		}

		System.out.println("a local bluetooth device is found:");
		System.out.println("   Name: " + localdev.getFriendlyName());
		System.out.println("Address: " + localdev.getBluetoothAddress());

		System.out.println("\nIts device classes are:");
		DeviceClass devcla = localdev.getDeviceClass();
		System.out.println(devcla.toString());
		System.out.println("Service Class: " + devcla.getServiceClasses());
		System.out.println("Major Device Class: "
				+ devcla.getMajorDeviceClass());
		System.out.println("Minor Device Class: "
				+ devcla.getMinorDeviceClass());
	}
}

class RemoteOne implements DiscoveryListener {
	public static void run() throws BluetoothStateException {
		LocalDevice localdev = null;
		RemoteOne listener = new RemoteOne();

		try {
			localdev = LocalDevice.getLocalDevice();
		} catch (BluetoothStateException e) {
			System.out.println("ERROR: cannot access local device");
			System.exit(1);
		}

		DiscoveryAgent agent = localdev.getDiscoveryAgent();
		try {
			agent.startInquiry(DiscoveryAgent.GIAC, listener);
		} catch (BluetoothStateException e) {
			System.out.println("Device unable to inquiry");
			System.exit(2);
		}
	}

	// implement required methods.
	private RemoteDevice remote = null;

	public void deviceDiscovered(RemoteDevice rt, DeviceClass cod) {
		remote = rt;
		System.out.println("A Remote Device Found:");
		System.out.println("Address: " + rt.getBluetoothAddress());
		try {
			System.out.println("   Name: " + rt.getFriendlyName(true));
		} catch (IOException e) {
		}
	}

	public void inquiryCompleted(int distype) {
		if (distype == INQUIRY_TERMINATED)
			System.out.println("Application Terminated");
		else if (distype == INQUIRY_COMPLETED)
			System.out.println("Inquiry Completed");
		else
			System.out.println("ERROR: Inquiry aborted");
	}

	public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
	}

	public void serviceSearchCompleted(int transID, int respCode) {
	}
}
