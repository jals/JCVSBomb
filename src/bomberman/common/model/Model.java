package bomberman.common.model;

import java.awt.Point;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import bomberman.common.model.PowerUp.Powers;

/**
 * This class creates the grid. If nothing is specified, the grid is created
 * randomly. This can be created from a file, where 0 represents a blank space,
 * 1 represents an inner wall, and 2 represents a door.
 * 
 * @author Jarred Linthorne
 * 
 */
public class Model {
	// The size of the grid, including outer boundary
	public static final int BOARD_SIZE = 12;
	// These values are used when finding the closest spaces
	public static final int RIGHT = 1;
	public static final int LEFT = 2;
	public static final int UP = 3;
	public static final int DOWN = 4;

	private Square[][] board;
	private Random rand = new Random();
	private Boolean[][] emptyBlocks;
	private String input;
	private int a = 0;
	private boolean hasDoor;
	private Door door;
	private List<Box> boxes;
	private ArrayList<Player> playerList;

	/**
	 * 
	 * @param filename
	 *            : The path to the file to load the the grid from
	 * @param grid
	 *            : the grid, used to refresh the internal board
	 */
	public Model(String filename, Square[][] grid) {
		boxes = new ArrayList<Box>();
		playerList = new ArrayList<Player>();
		board = new Square[BOARD_SIZE][BOARD_SIZE];
		if (!filename.isEmpty()) {
			try {
				input = readFile(filename, Charset.defaultCharset()).replaceAll("\\s+", "");
			} catch (IOException e) {
				System.err.println("ERROR: File could not be found/read.");
				return;
			}
			for (int j = 1; j < BOARD_SIZE - 1; j++) {
				for (int k = 1; k < BOARD_SIZE - 1; k++) {
					board[j][k] = new Square();
					if (input.substring(a, a + 1).equals("0")) {
						board[j][k].addObject(0);
					} else if (input.substring(a, a + 1).equals("W")) {
						board[j][k].addObject(new Wall(new Point(j, k)));
					} else if (input.substring(a, a + 1).equals("D")) {
						door = new Door(new Point(j, k), false);
						board[j][k].addObject(door);
						setHasDoor(true);
					} else if (input.substring(a, a + 1).equals("P")) {
						PowerUp p = new PowerUp(new Point(j, k), Powers.HEALTH_UP);
						board[j][k].addObject(p);
					} else if (input.substring(a, a + 1).equals("B")) {
						Box b = new Box(new Point(j, k), null, null);
						board[j][k].addObject(b);
						boxes.add(b);
					} else if (input.substring(a, a+1).equals("E")){ //Door in a box
						door = new Door(new Point(j, k), false);
						setHasDoor(true);
						Box b = new Box(new Point(j, k), door, null);
						board[j][k].addObject(b);
						boxes.add(b);
					} else if (input.substring(a, a+1).equals("Q")){ //Powerup in a box
						PowerUp p = new PowerUp(new Point(j, k), Powers.HEALTH_UP);
						Box b = new Box(new Point(j, k), null, p);
						board[j][k].addObject(b);
						boxes.add(b);
					}
					a++;
				}
				board[0][0] = new Square();
			}
		} else {
			makeMaze();
			fixMaze();
		}
		refreshGrid(grid);
	}

	/**
	 * Refresh the grid, setting the internal board to the passed grid
	 * 
	 * @param grid
	 */
	public void refreshGrid(Square[][] grid) {
		if (grid != null) {
			board = grid;
		}
		playerList.clear();
		for (int j = 1; j < BOARD_SIZE - 1; j++) {
			for (int k = 1; k < BOARD_SIZE - 1; k++) {
				if (board[j][k].getPlayer() != null) {
					if (!board[j][k].getPlayer().getName().contains("Enemy")) {
						playerList.add(board[j][k].getPlayer());
					}
				}
			}
		}
		if(board[0][0] != null){
			if(board[0][0].getPlayer() != null){
				List<Object> objs = board[0][0].getObjects();
				for(int x = 0; x < objs.size(); x++){
					if (objs.get(x) instanceof Player){
						playerList.add((Player) objs.get(x));
					}
				}
			}
		}
		
	}

	/**
	 * This method is used to read a file from a file path and return the string
	 * representation of the contents.
	 * 
	 * @param path
	 * @param encoding
	 * @throws IOException
	 */
	private static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return encoding.decode(ByteBuffer.wrap(encoded)).toString();
	}

	/**
	 * Make the maze randomly, filling in the grid with 0's and 1's. Add the
	 * objects to the board while creating them.
	 */
	private void makeMaze() {
		int[][] maze = new int[BOARD_SIZE][BOARD_SIZE];
		emptyBlocks = new Boolean[BOARD_SIZE][BOARD_SIZE];
		for (int i = 1; i < BOARD_SIZE - 1; i++) {
			for (int j = 1; j < BOARD_SIZE - 1; j++) {
				// increasing this random number factor makes more walls spawn
				maze[i][j] = rand.nextInt(8);
				if (maze[i][j] == 0) {
					maze[i][j] = 2;
					emptyBlocks[i][j] = false;
				} else if (maze[i][j] == 1) {
					maze[i][j] = 1;
					emptyBlocks[i][j] = true;
				} else {
					emptyBlocks[i][j] = true;
				}
				board[i][j] = new Square();
				board[i][j].addObject(maze[i][j]);
				if (maze[i][j] == 1) {
					Box b = new Box(new Point(i, j), null, null);
					board[i][j].addObject(b);
					boxes.add(b);
				} else if (maze[i][j] == 2) {
					board[i][j].addObject(new Wall(new Point(i, j)));
				}
			}
		}
	}

	/**
	 * This method is used to create paths between the randomly generated areas.
	 * The algorithm looks for the closest block in each of the four
	 * directions(up, down, right, left) and returns that value. It then looks
	 * for the next closest block in the other three directions. If both of
	 * these values are 1, then the square is already connected to two blocks,
	 * otherwise, those blocks should be changed to be open space instead of a
	 * wall.
	 */
	private void fixMaze() {
		for (int i = 1; i < BOARD_SIZE - 1; i++) {
			for (int j = 1; j < BOARD_SIZE - 1; j++) {
				if (emptyBlocks[i][j] == true) {
					Point x = getClosestBlock(i, j, null);
					// check point x to make sure you don't get same point again
					Point y = getClosestBlock(i, j, x);
					if (x.x != BOARD_SIZE) {
						if (x.y == RIGHT) {
							adjustRight(i, j, x.x);
						} else if (x.y == LEFT) {
							adjustLeft(i, j, x.x);
						} else if (x.y == UP) {
							adjustUp(i, j, x.x);
						} else if (x.y == DOWN) {
							adjustDown(i, j, x.x);
						}
					}
					if (y.x != BOARD_SIZE) {
						if (y.y == RIGHT) {
							adjustRight(i, j, y.x);
						} else if (y.y == LEFT) {
							adjustLeft(i, j, y.x);
						} else if (y.y == UP) {
							adjustUp(i, j, y.x);
						} else if (y.y == DOWN) {
							adjustDown(i, j, y.x);
						}
					}
				}
			}
		}
	}

	/**
	 * These 4 methods are used to remove the blocks specified, given the x or y
	 * position and number of spaces the closest block is.
	 * 
	 * @param x
	 * @param y
	 * @param spaces
	 */
	private void adjustRight(int x, int y, int spaces) {
		for (int i = y; i < y + spaces; i++) {
			board[x][i + 1].removeLast();
			if (!board[x][i + 1].hasBox()) {
				board[x][i + 1].addObject(0);
			}
		}
	}

	private void adjustLeft(int x, int y, int spaces) {
		for (int i = y; i > y - spaces; i--) {
			board[x][i - 1].removeLast();
			if (!board[x][i - 1].hasBox()) {
				board[x][i - 1].addObject(0);
			}
		}
	}

	private void adjustUp(int x, int y, int spaces) {
		for (int i = x; i > x - spaces; i--) {
			board[i - 1][y].removeLast();
			if (!board[i - 1][y].hasBox()) {
				board[i - 1][y].addObject(0);
			}
		}
	}

	private void adjustDown(int x, int y, int spaces) {
		for (int i = x; i < x + spaces; i++) {
			board[i + 1][y].removeLast();
			if (!board[i + 1][y].hasBox()) {
				board[i + 1][y].addObject(0);
			}
		}
	}

	/**
	 * This method returns the closest point in the form of a Point. Where,
	 * Point.x = the closest value as the number of spaces between them, and,
	 * Point.y = the direction as an integer, ex. RIGHT
	 * 
	 * @param x
	 * @param y
	 * @param p
	 * @return
	 */
	private Point getClosestBlock(int x, int y, Point p) {
		int right;
		int left;
		int up;
		int down;
		if (p != null) {
			// This is the case when the method is called for the second time
			if (p.y != RIGHT) {
				right = closestRight(x, y);
			} else {
				// default as highest value to ensure same point doesn't get
				// chosen as closest twice
				right = BOARD_SIZE;
			}
			if (p.y != LEFT) {
				left = closestLeft(x, y);
			} else {
				left = BOARD_SIZE;
			}
			if (p.y != UP) {
				up = closestUp(x, y);
			} else {
				up = BOARD_SIZE;
			}
			if (p.y != DOWN) {
				down = closestDown(x, y);
			} else {
				down = BOARD_SIZE;
			}
		} else {
			// This is the case when the method is called for the first time
			right = closestRight(x, y);
			left = closestLeft(x, y);
			up = closestUp(x, y);
			down = closestDown(x, y);
		}

		// check for the lowest value, and return it as a point with the
		// direction
		if (right <= left && right <= up && right <= down) {
			Point q = new Point();
			q.x = right;
			q.y = RIGHT;
			return q;
		} else if (left <= right && left <= up && left <= down) {
			Point q = new Point();
			q.x = left;
			q.y = LEFT;
			return q;
		} else if (up <= right && up <= left && up <= down) {
			Point q = new Point();
			q.x = up;
			q.y = UP;
			return q;
		} else if (down <= left && down <= up && down <= right) {
			Point q = new Point();
			q.x = down;
			q.y = DOWN;
			return q;
		}
		return null; // this should never happen.
	}

	/**
	 * Find the closest space from the given square to the next open square
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private int closestRight(int x, int y) {
		for (int i = y + 1; i < BOARD_SIZE - 1; i++) {
			if ((Integer) board[x][i].getObjects().get(0) == 0 && board[x][i].getBox() != null) {
				return i - y;
			}
		}
		return BOARD_SIZE; // return the largest value if no empty square is
							// found
	}

	private int closestLeft(int x, int y) {
		for (int i = y - 1; i > 0; i--) {
			if ((Integer) board[x][i].getObjects().get(0) == 0 && board[x][i].getBox() != null) {
				return Math.abs(i - y);
			}
		}
		return BOARD_SIZE;
	}

	private int closestUp(int x, int y) {
		for (int i = x - 1; i > 0; i--) {
			if ((Integer) board[i][y].getObjects().get(0) == 0 && board[x][i].getBox() != null) {
				return Math.abs(i - x);
			}
		}
		return BOARD_SIZE;
	}

	private int closestDown(int x, int y) {
		for (int i = x + 1; i < BOARD_SIZE - 1; i++) {
			if ((Integer) board[i][y].getObjects().get(0) == 0 && board[x][i].getBox() != null) {
				return i - x;
			}
		}
		return BOARD_SIZE;
	}

	public void setVal(int x, int y, int v) {
		board[x][y].addObject(v);
	}

	public Object getVal(int row, int col) {
		return board[row][col];
	}

	public Square[][] getBoard() {
		return board;
	}

	public void setBoard(Square[][] board) {
		this.board = board;
	}

	public boolean hasDoor() {
		return hasDoor;
	}

	public void setHasDoor(boolean hasDoor) {
		this.hasDoor = hasDoor;
	}

	public Door getDoor() {
		return door;
	}

	public void setDoor(Door door) {
		this.door = door;
	}

	public List<Box> getBoxes() {
		return boxes;
	}

	public Box getFreeBox() {
		Random r = new Random();
		int i = r.nextInt(boxes.size() + 1);
		if (boxes != null) {
			// don't return a box that already contains a door or a powerup
			while (boxes.get(i).getDoor() != null || boxes.get(i).getPowerUp() != null) {
				i = r.nextInt(boxes.size() + 1);
			}
		}

		return boxes.get(i);
	}

	public void setBoxes(List<Box> boxes) {
		this.boxes = boxes;
	}

	public ArrayList<Player> getPlayerList() {
		return playerList;
	}

}
