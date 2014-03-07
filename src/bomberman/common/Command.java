/**
 * Author: Vinayak Bansal
 * Dated: Mar 3. 2014
 * 
 * An instance of this class is what the client will be sending to the server.
 * It has a player name, and the operation that the user inputted.
 * This forms the basis for our commincation protocol.
 */
package bomberman.common;

import java.io.Serializable;

public class Command implements Serializable {

	public enum Operation {
		MOVE_LEFT, MOVE_RIGHT, MOVE_UP, MOVE_DOWN, JOIN_GAME, START_GAME, LEAVE_GAME, DROP_BOMB;

		// There are 4 types of move available. They are the first 4
		// of the enum.
		public boolean isMove() {
			return (this.ordinal() <= MOVE_DOWN.ordinal());
		}
	}

	private static final long serialVersionUID = 7342838579737714576L;

	private String player;
	private Operation operation;
	private int identifier;

	public Command(String player, Operation operation) {
		this.player = player;
		this.operation = operation;
	}
	
	public Command(String player, Operation operation, int identifier) {
		this.player = player;
		this.operation = operation;
		this.identifier = identifier;
	}

	public String getPlayer() {
		return player;
	}

	public Operation getOperation() {
		return operation;
	}

	public String toString() {
		return player + ":" + operation;
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Command)) {
			return false;
		}
		
		Command command = (Command) object;
		return command.getOperation().equals(getOperation()) && command.getPlayer().equals(getPlayer());
	}

	public int getIdentifier() {
		return identifier;
	}
}
