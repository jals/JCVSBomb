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
import java.util.ArrayList;

import bomberman.common.model.PowerUp.Powers;

public class Player implements Serializable {

	private static final long serialVersionUID = 1L;
	private String playerName;
	private Point location;
	private Boolean isAlive;
	private InetAddress address;
	private int port;
	private int identifier;
	private int lastDirection = Model.DOWN;
	private int health = 1;
	private ArrayList<PowerUp> powerUps;
	private boolean hasWon = false;

	public Player(String playerName, int identifier) {
		powerUps = new ArrayList<PowerUp>();
		this.playerName = playerName;
		this.setIdentifier(identifier);
	}

	public String getName() {
		return playerName;
	}

	public Point getLocation() {
		return location;
	}
	
	public void takeHit() {
		health--;
		if (health <= 0){
			isAlive = false;
		}
	}
	
	public int getHealth(){
		return health;
	}
	
	public void addPowerUp(PowerUp powerUp) {
		powerUps.add(powerUp);
		if(powerUp.getPower().equals(Powers.HEALTH_UP)){
			health++;
		}
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

	public Boolean isAlive() {
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

	public ArrayList<PowerUp> getPowerUps() {
		return powerUps;
	}

	public void setPowerUp(ArrayList<PowerUp> powerUps) {
		this.powerUps = powerUps;
	}

	public boolean hasWon() {
		return hasWon;
	}

	public void setWon(boolean hasWon) {
		this.hasWon = hasWon;
	}

}
