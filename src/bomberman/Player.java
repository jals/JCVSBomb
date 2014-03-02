package bomberman;

import java.awt.Point;
import java.io.Serializable;
import java.net.InetAddress;

public class Player implements Serializable{

	private static final long serialVersionUID = 1L;
	private String playerName;
	private Point location;
	private Boolean isAlive;
	private InetAddress address;
	private int port;
	
	public Player(String playerName){
		this.playerName = playerName;
	}
	
	public String getName() {
		return playerName;
	}
	
	public Point getLocation() {
		return location;
	}
	
	public void setLocation(Point location) {
		this.location = location;
	}
	
	public Boolean getIsAlive() {
		return isAlive;
	}
	
	public void setIsAlive(Boolean isAlive) {
		this.isAlive = isAlive;
	}

	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	
}
