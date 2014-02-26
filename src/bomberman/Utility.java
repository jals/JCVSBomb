package bomberman;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Utility {
	public static void sendMessage(DatagramSocket socket, Object message, InetAddress add, int port) {
		try {
			byte[] sendData = new byte[1024 * 100];
			sendData = serialize(message);
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, add, port);
			socket.send(sendPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static Object receiveMessage(DatagramSocket socket){
		byte[] receiveData = new byte[1024 * 100];
		DatagramPacket receivePacket = new DatagramPacket(receiveData,
				receiveData.length);
		try {
			socket.receive(receivePacket);
			return deserialize(receiveData);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	public static byte[] serialize(Object obj) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(out);
		os.writeObject(obj);
		os.flush();
		return out.toByteArray();
	}

	public static Object deserialize(byte[] data) throws IOException,
			ClassNotFoundException {
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		ObjectInput in = null;
		in = new ObjectInputStream(bis);
		return in.readObject();
	}

}
