package bomberman;

import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

import bomberman.Command.Operation;

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
	
	public static String getGridString(Square[][] grid) {
		String toReturn = "";
		for (int x = 0; x < 10; x++) {
			for (int y = 0; y < 10; y++) {
				List<Object> objects = grid[x][y].getObjects();
				if (objects.size() > 0 && objects.get(0) instanceof Player) {
					toReturn += ((Player) grid[x][y].getObjects().get(0)).getName();
				} else {
					toReturn += "0";
				}
			}
			toReturn+="\n";
		}
		return toReturn;

	}
	
	public static Point getLocation(Operation operation, Point location) {
		int x = (int) location.getX();
		int y = (int) location.getY();
		int newX = 5, newY = 5;

		if (Operation.MOVE_DOWN == operation) {
			newX = Math.min(x + 1, 9);
			newY = y;
		} else if (Operation.MOVE_UP == operation) {
			newX = Math.max(x - 1, 0);
			newY = y;
		} else if (Operation.MOVE_LEFT == operation) {
			newY = Math.max(y - 1, 0);
			newX = x;
		} else if (Operation.MOVE_RIGHT == operation) {
			newY = Math.min(y + 1, 9);
			newX = x;
		} else {
			newY = y;
			newX = x;
		}
		return new Point(newX, newY);
	}





}
