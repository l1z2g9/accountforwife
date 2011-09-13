package cat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

public class RFClientOne implements DiscoveryListener {
	public static void main(String args[]) throws BluetoothStateException {
		LocalDevice localdev = null;
		RFClientOne listener = new RFClientOne();

		try {
			localdev = LocalDevice.getLocalDevice();
		} catch (BluetoothStateException e) {
			System.out.println("ERROR: cannot access local device");
			System.exit(1);
		}

		DiscoveryAgent agent = localdev.getDiscoveryAgent();
		try {
			// startInquiry() is non-blocking.
			agent.startInquiry(DiscoveryAgent.GIAC, listener);
		} catch (BluetoothStateException e) {
			System.out.println("ERROR: device inquiry failed");
			System.exit(2);
		}

		// pause for a while
		// NOTE: if there is no remote device, this may go into infinite loop
		// this program also shows that to start services from a client it's
		// better to have a GUI interface.
		while (remote == null)
			try {
				Thread.sleep(5000);
			} catch (Exception e) {
			}

		UUID[] uuidSet = new UUID[1];
		uuidSet[0] = new UUID("102030405060708090A0B0C0D0E0F011", false);
		System.out.println("XXXXXXXXX  " + remote);
		int trans;
		try {
			trans = agent.searchServices(null, uuidSet, remote, listener);
			System.out.println("XXXXXXXXX trans " + trans);
		} catch (BluetoothStateException e) {
			System.out.println("device unable to begin service inquiry");
		}
	}

	// implement required listener methods.
	private static RemoteDevice remote = null;
	private static ServiceRecord first = null;

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
		System.out.println("<<<<<<WWW");
		if (distype == INQUIRY_TERMINATED)
			System.out.println("Application Terminated");
		else if (distype == INQUIRY_COMPLETED)
			System.out.println("Inquiry Completed");
		else
			System.out.println("ERROR: Inquiry aborted");
	}

	public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
		first = servRecord[0];
		System.out.println("<<<<<<<<<<< " + servRecord.length);
		String url = first
				.getConnectionURL(first.AUTHENTICATE_NOENCRYPT, false);
		try {
			StreamConnection conn = (StreamConnection) Connector.open(url);
			String message = "What time is it now?";
			InputStream is = conn.openInputStream();
			OutputStream os = conn.openOutputStream();
			byte[] rbuf = new byte[100];
			os.write(message.getBytes());
			is.read(rbuf);
			System.out.println(new String(rbuf));
			is.close();
			os.close();
			conn.close();
		} catch (Exception e) {
			System.out.println("ERROR: connection error");
		}
	}

	public void serviceSearchCompleted(int transID, int respCode) {
		System.out.println("<<<<<<WWWxxxxxxxx");
		if (respCode == SERVICE_SEARCH_COMPLETED)
			System.out.println("Services successfully located");
		else if (respCode == SERVICE_SEARCH_TERMINATED)
			System.out.println("Service inquiry was cancelled");
		else if (respCode == SERVICE_SEARCH_DEVICE_NOT_REACHABLE)
			System.out.println("Service connection cannot be established");
		else if (respCode == SERVICE_SEARCH_NO_RECORDS)
			System.out.println("No service found");
		else
			System.out.println("ERROR: service inquiry failed");
	}
}