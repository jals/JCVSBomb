package bomberman.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
	private ServerThread s;
	
	public ServerLauncher(){
		frame = new JFrame("Server Launcher");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		textField = new JTextField(20);
		portButton = new JButton("Start Server");
		portButton.addActionListener(this);
		closeButton = new JButton("Shut Down Server");
		closeButton.addActionListener(this);
		
		JTextField tf = new JTextField();
		tf.setText("Enter a port number:");
		tf.setEditable(false);

		JPanel jp = new JPanel();
		jp.add(tf);
		jp.add(textField);
		jp.add(portButton);
		
		frame.add(jp);
		frame.setVisible(true);
		frame.setSize(300, 300);
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
				s = new ServerThread(Integer.parseInt(textField.getText()), false, true);
				s.start();
				changeLauncher();
			} catch (NumberFormatException e1) {
				System.err.println("Bad port number!");
			}
		} else if (e.getSource().equals(closeButton)){
			s.shutdown();
			frame.dispose();
		}
	}

}
