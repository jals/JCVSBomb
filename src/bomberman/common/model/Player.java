/**
 * Author: Vinayak Bansal
 * Dated: Mar 3, 2014
 * This is part of the model, and the server keeps track of a 
 * list of Player objects. It is very close to a C struct
 */
package bomberman.common.model;

import java.awt.Point;
import java.io.Serializable;
import java.net.InetAddress;

public class Player implements Serializable {

	private static final long serialVersionUID = 1L;
	private String playerName;
	private Point location;
	private Boolean isAlive;
	private InetAddress address;
	private int port;
	private int identifier;
	private int lastDirection = Model.DOWN;
	@SuppressWarnings("unused")
	private PowerUp powerUp;

	public Player(String playerName, int identifier) {
		this.playerName = playerName;
		this.setIdentifier(identifier);
	}

	public String getName() {
		return playerName;
	}

	public Point getLocation() {
		return location;
	}
	
	public void addPowerUp(PowerUp powerUp) {
		this.powerUp = powerUp;
	}
	
	public void setLocation(Point location) {
		if(this.location != null){
			if(this.location.y > location.y){
				setLastDirection(Model.LEFT);
			} else if(this.location.y < location.y){
				setLastDirection(Model.RIGHT);
			} else if(this.location.x > location.x){
				setLastDirection(Model.UP);
			} else {
				setLastDirection(Model.DOWN);
			}
		}
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

	public int getLastDirection() {
		return lastDirection;
	}

	public void setLastDirection(int lastDirection) {
		this.lastDirection = lastDirection;
	}

	public int getIdentifier() {
		return identifier;
	}

	public void setIdentifier(int identifier) {
		this.identifier = identifier;
	}

}
