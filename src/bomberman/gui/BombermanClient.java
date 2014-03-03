package bomberman.gui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import bomberman.Model;
import bomberman.Square;

/**
 * This class instantiates the GUI. 
 * This can be done from a file, or from a passed in grid, otherwise it will be created randomly.
 * @author Jarred Linthorne
 *
 */
public class BombermanClient extends JFrame {
	private static final long serialVersionUID = -5111494396046772840L;

	private Model model;
	private BoardDisplay board;

	/**
	 * The constructor instantiates the GUI and adds all elements to it.
	 * @param filename : The path to the grid to load from a file, empty is not loading from file
	 * @param grid : The grid that should be used, null if not loading from grid
	 */
	public BombermanClient(String filename, Square[][] grid) {
		model = new Model(filename, grid);
		board = new BoardDisplay(model);

		JPanel content = new JPanel();
		content.setLayout(new BorderLayout());

		content.add(board, BorderLayout.CENTER);

		setContentPane(content);
		setTitle("Bomberman");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false); // Don't let user resize it.
		pack();
		setLocationRelativeTo(null); // Center it.
	}

	public BombermanClient(Square[][] grid) {
		this("", grid);
	}

	public BombermanClient() {
		this("", null);
	}

	/**
	 * Refresh method, used to refresh the grid, and repaint what is seen by a client
	 * @param grid
	 */
	public void refresh(Square[][] grid) {
		model.refreshGrid(grid);
		repaint();
	}

	/**
	 * Main method, used to view the grid without having any extra information
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 0) {
			new BombermanClient(args[0], null).setVisible(true);
		} else {
			new BombermanClient().setVisible(true);
		}
	}
}