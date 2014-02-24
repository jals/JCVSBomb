package bomberman;

import java.util.ArrayList;
import java.util.List;

public class Server {
	
	private List<Player> listOfPlayers;
	private Object[][] grid;

	public Server(){
		setListOfPlayers(new ArrayList<Player>());
	}
	
	public Server(String filename){
		setListOfPlayers(new ArrayList<Player>());
	}

	public List<Player> getListOfPlayers() {
		return listOfPlayers;
	}

	public void setListOfPlayers(List<Player> listOfPlayers) {
		this.listOfPlayers = listOfPlayers;
	}

	public Object[][] getGrid() {
		return grid;
	}

	public void setGrid(Object[][] grid) {
		this.grid = grid;
	}
	
}
