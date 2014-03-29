package bomberman.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import bomberman.test.ServerThread;

public class ServerLauncher implements ActionListener{

	private JTextField textField;
	private JButton portButton;
	private JButton closeButton;
	private JFrame frame;
	private ArrayList<ServerThread> servers = new ArrayList<ServerThread>();
	
	
	public ServerLauncher(){
		frame = new JFrame("Server Launcher");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		textField = new JTextField(20);
		portButton = new JButton("Start Server(s)");
		portButton.addActionListener(this);
		closeButton = new JButton("Shut Down Server(s)");
		closeButton.addActionListener(this);
		
		JTextField tf = new JTextField();
		tf.setText("Enter one or more port numbers seperated by a space:");
		tf.setEditable(false);

		JPanel jp = new JPanel();
		jp.add(tf);
		jp.add(textField);
		jp.add(portButton);
		
		frame.add(jp);
		frame.setVisible(true);
		frame.setSize(320, 320);
		frame.setLocationRelativeTo(null);
	}
	
	public void changeLauncher(){
		frame.getContentPane().removeAll();
		JPanel jp = new JPanel();
		jp.add(closeButton);
		frame.add(jp);
		frame.revalidate();
		frame.repaint();
	}
	
	public static void main(String[] args){
		@SuppressWarnings("unused")
		ServerLauncher sl = new ServerLauncher();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(portButton)){
			try {
				ServerThread s;
				String[] splitInput = textField.getText().split(" ");
				for(int i=0; i<splitInput.length ; i++){
					s = new ServerThread(Integer.parseInt(splitInput[i]), false, true);
					s.start();
					servers.add(s);
				}
				changeLauncher();
			} catch (NumberFormatException e1) {
				System.err.println("Bad port number!");
			}
		} else if (e.getSource().equals(closeButton)){
			for(ServerThread s: servers){
				s.shutdown();
			}
			frame.dispose();
		}
	}

}
