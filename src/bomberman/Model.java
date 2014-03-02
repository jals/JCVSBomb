package bomberman;

import java.awt.Point;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

public class Model {
	public static final int BOARD_SIZE = 12;
	public static final int RIGHT = 1;
	public static final int LEFT = 2;
	public static final int UP = 3;
	public static final int DOWN = 4;

	private Square[][] board;
	private Random rand = new Random();
	private Boolean[][] emptyBlocks;
	private String input;
	private int a = 0;

	public Model(String filename, Square[][] grid) {
		board = new Square[BOARD_SIZE][BOARD_SIZE];
		if (!filename.isEmpty()) {
			try {
				input = readFile(filename, Charset.defaultCharset())
						.replaceAll("\\s+", "");
			} catch (IOException e) {
				e.printStackTrace();
			}
			for (int j = 1; j < BOARD_SIZE - 1; j++) {
				for (int k = 1; k < BOARD_SIZE - 1; k++) {
					board[j][k] = new Square();
					if (Integer.parseInt(input.substring(a, a + 1)) == 0) {
						board[j][k].addObject(0);
					} else {
						board[j][k].addObject(1);
					}
					a++;
				}
			}
		} else {
			makeMaze();
			fixMaze();
		}
		refreshGrid(grid);
	}

	public void refreshGrid(Square[][] grid) {
		if (grid != null) {
			board = grid;
		}
	}

	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return encoding.decode(ByteBuffer.wrap(encoded)).toString();
	}

	private void makeMaze() {
		int[][] maze = new int[BOARD_SIZE][BOARD_SIZE];
		emptyBlocks = new Boolean[BOARD_SIZE][BOARD_SIZE];
		for (int i = 1; i < BOARD_SIZE - 1; i++) {
			for (int j = 1; j < BOARD_SIZE - 1; j++) {
				maze[i][j] = rand.nextInt(4); // increasing this factor makes
												// more walls spawn
				if (maze[i][j] == 0) {
					emptyBlocks[i][j] = true;
				} else {
					maze[i][j] = 1;
					emptyBlocks[i][j] = false;
				}
				board[i][j] = new Square();
				board[i][j].addObject(maze[i][j]);
			}
		}
	}

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

	private void adjustRight(int x, int y, int spaces) {
		for (int i = y; i < y + spaces; i++) {
			board[x][i + 1].removeLast();
			board[x][i + 1].addObject(0);
		}
	}

	private void adjustLeft(int x, int y, int spaces) {
		for (int i = y; i > y - spaces; i--) {
			board[x][i - 1].removeLast();
			board[x][i - 1].addObject(0);
		}
	}

	private void adjustUp(int x, int y, int spaces) {
		for (int i = x; i > x - spaces; i--) {
			board[i - 1][y].removeLast();
			board[i - 1][y].addObject(0);
		}
	}

	private void adjustDown(int x, int y, int spaces) {
		for (int i = x; i < x + spaces; i++) {
			board[i + 1][y].removeLast();
			board[i + 1][y].addObject(0);
		}
	}

	// where Point.x = closest value, Point.y = direction as integer
	private Point getClosestBlock(int x, int y, Point p) {
		int right;
		int left;
		int up;
		int down;
		if (p != null) {
			if (p.y != RIGHT) {
				right = closestRight(x, y);
			} else {
				right = BOARD_SIZE; // default as highest value to ensure same
									// point doesn't get chosen as closest twice
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
			right = closestRight(x, y);
			left = closestLeft(x, y);
			up = closestUp(x, y);
			down = closestDown(x, y);
		}

		// System.out.println("At x:" + x + ", y:" + y + " R:" + right + " L:" +
		// left + " U:" + up + " D:" + down + "\n");
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
		return null;
	}

	private int closestRight(int x, int y) {
		for (int i = y + 1; i < BOARD_SIZE - 1; i++) {
			if ((Integer) board[x][i].getObjects().get(0) == 0) {
				return i - y;
			}
		}
		return BOARD_SIZE; // return the largest value
	}

	private int closestLeft(int x, int y) {
		for (int i = y - 1; i > 0; i--) {
			if ((Integer) board[x][i].getObjects().get(0) == 0) {
				return Math.abs(i - y);
			}
		}
		return BOARD_SIZE; // return the largest value
	}

	private int closestUp(int x, int y) {
		for (int i = x - 1; i > 0; i--) {
			if ((int) board[i][y].getObjects().get(0) == 0) {
				return Math.abs(i - x);
			}
		}
		return BOARD_SIZE; // return the largest value
	}

	private int closestDown(int x, int y) {
		for (int i = x + 1; i < BOARD_SIZE - 1; i++) {
			if ((Integer) board[i][y].getObjects().get(0) == 0) {
				return i - x;
			}
		}
		return BOARD_SIZE; // return the largest value
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
}