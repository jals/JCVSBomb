package bomberman.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import bomberman.test.ClientThread;

public class ClientLauncher implements ActionListener{

	private JTextField nameTextField;
	private JTextField hostTextField;
	private JTextField portTextField;
	private JButton submitButton;
	private JFrame frame;
	private ClientThread c;
	
	public ClientLauncher(){
		frame = new JFrame("Client Launcher");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		nameTextField = new JTextField(20);
		hostTextField = new JTextField(20);
		portTextField = new JTextField(20);
		submitButton = new JButton("Start Client");
		submitButton.addActionListener(this);
		
		JPanel jp = new JPanel();
		JTextField tf = new JTextField();
		tf.setText("Enter a player name:");
		tf.setEditable(false);
		
		jp.add(tf);
		jp.add(nameTextField);
		
		JTextField tf2 = new JTextField();
		tf2.setText("Enter a host:");
		tf2.setEditable(false);
		
		jp.add(tf2);
		jp.add(hostTextField);
		
		JTextField tf3 = new JTextField();
		tf3.setText("Enter a port number:");
		tf3.setEditable(false);
		
		jp.add(tf3);
		jp.add(portTextField);
		
		jp.add(submitButton);
		
		frame.add(jp);
		frame.setVisible(true);
		frame.setSize(300, 300);
	}
	
	public static void main(String[] args){
		@SuppressWarnings("unused")
		ClientLauncher sl = new ClientLauncher();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			c = new ClientThread(nameTextField.getText(), hostTextField.getText(), Integer.parseInt(portTextField.getText()), true, false);
			c.start();
			frame.dispose();
		} catch (NumberFormatException e1) {
			System.err.println("Bad port number!");
		}
	}

}
