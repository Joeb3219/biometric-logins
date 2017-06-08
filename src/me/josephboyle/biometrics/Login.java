package me.josephboyle.biometrics;

import java.awt.Button;
import java.awt.Dimension;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

public class Login extends JPanel{
	
	private ArrayList<CharTimePair> pairs = new ArrayList<CharTimePair>();
	
	// Width and height are the dimensions of the display.
	public Login(int width, int height){
		// Display information to build the JPanel.
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setPreferredSize(new Dimension(width, height));
		TitledBorder border = new TitledBorder("Login");
		border.setTitleJustification(TitledBorder.CENTER);
		border.setTitleJustification(TitledBorder.TOP);
		setBorder(border);
		
		TextField field_username = new TextField();
		TextField field_password = new TextField();
		
		add(new Label("Username")); // Adds the username label before the input box.
		add(field_username);		// Adds the username box.
		
		BiometricListener listener = new BiometricListener();	// Creates a biometric listener, which implements KeyListener.
		field_password.addKeyListener(listener);
		add(new Label("Password"));	// Adds the password label before the input box.
		add(field_password);		// Adds the password box.
		
		// As in the Register class, the button handles all of the processing.
		Button button_login = new Button("Login");
		button_login.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				pairs = listener.charTimes;			// Grab all of the input data.
				CharTimePair.normalizeTimes(pairs);	// Normalize them (see CharTimePair.normalizeTimes) so that they're scaled (not absurdly big).
				listener.reset();					// Reset the listener for the next use.
				
				// We create a User variable, and set it to null.
				// We then check each user to see if it's a valid login.
				// If any are valid, it will set loggedInAs to that user.
				User loggedInAs = null;
				for(User u : Main.users){
					if(u.isValidLogin(field_username.getText(), field_password.getText(), pairs)) loggedInAs = u;
				}
				
				if(loggedInAs != null) Main.popupBox("Login Successful", "Hello, " + loggedInAs.username + "! You have successfully logged in.");
				else  Main.popupBox("Login Failed", "We were unable to log you in with your credentials.");
				
				field_password.setText(" ");	// Reset the password field. Due to Java 8 bugs, we have to do it twice.
				field_password.setText("");		// Reset the password field. Due to Java 8 bugs, we have to do it twice.
			}
		});
		add(button_login);
	}
	
}
