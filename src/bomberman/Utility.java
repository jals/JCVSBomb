/**
 * Author: Vinayak Bansal
 * Dated: Mar 3, 2014
 * 
 * Handy methods used for all classes.
 */

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
import java.net.SocketException;

import bomberman.Command.Operation;

public class Utility {
	/**
	 * Sends the message over the specified socket.
	 */
	public static void sendMessage(DatagramSocket socket, Object message,
			InetAddress add, int port) {
		try {
			byte[] sendData = new byte[1024 * 100];
			sendData = serialize(message);
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, add, port);
			socket.send(sendPacket);
		} catch (IOException e) {
			if(e instanceof SocketException) {
				// Socket was closed, just return (no error)
				return;
			}
			e.printStackTrace();
		}

	}

	/**
	 * Receives a message object from the specified object and returns it.
	 */
	public static Object receiveMessage(DatagramSocket socket) {
		byte[] receiveData = new byte[1024 * 100];
		DatagramPacket receivePacket = new DatagramPacket(receiveData,
				receiveData.length);
		try {
			socket.receive(receivePacket);
			return deserialize(receiveData);
		} catch (Exception e) {
			if(e instanceof SocketException) {
				// Socket was closed, just return (no error)
				return null;
			}
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * Returns a serial version of the specified object.
	 */
	public static byte[] serialize(Object obj) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(out);
		os.writeObject(obj);
		os.flush();
		return out.toByteArray();
	}

	/**
	 * Returns an object from the list of bytes, after deserializing them.
	 */
	public static Object deserialize(byte[] data) throws IOException,
			ClassNotFoundException {
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		ObjectInput in = null;
		in = new ObjectInputStream(bis);
		return in.readObject();
	}

	/**
	 * Given an operation, and a location, it computes the new location.
	 */
	public static Point getLocation(Operation operation, Point location) {
		int x = (int) location.getX();
		int y = (int) location.getY();
		int newX = 5, newY = 5;

		if (Operation.MOVE_DOWN == operation) {
			newX = Math.min(x + 1, Model.BOARD_SIZE - 1);
			newY = y;
		} else if (Operation.MOVE_UP == operation) {
			newX = Math.max(x - 1, 0);
			newY = y;
		} else if (Operation.MOVE_LEFT == operation) {
			newY = Math.max(y - 1, 0);
			newX = x;
		} else if (Operation.MOVE_RIGHT == operation) {
			newY = Math.min(y + 1, Model.BOARD_SIZE - 1);
			newX = x;
		} else {
			newY = y;
			newX = x;
		}
		return new Point(newX, newY);
	}

}
