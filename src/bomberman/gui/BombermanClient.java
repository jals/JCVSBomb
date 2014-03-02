package bomberman.gui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import bomberman.Model;
import bomberman.Square;

public class BombermanClient extends JFrame {
	private static final long serialVersionUID = -5111494396046772840L;

	private Model model;
	private BoardDisplay board;

	public BombermanClient(String filename, Square[][] grid){
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
	
	public BombermanClient(Square[][] grid){
		this("", grid);
	}
	
	public BombermanClient() {
		this("", null);
	}
	
	public void refresh(Square[][] grid){
		model.refreshGrid(grid);
		repaint();
	}

	public static void main(String[] args) {
		if(args.length != 0){
			new BombermanClient(args[0], null).setVisible(true);
		} else {
			new BombermanClient().setVisible(true);
		}
	}
}