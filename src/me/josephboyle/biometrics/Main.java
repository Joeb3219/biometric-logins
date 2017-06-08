package me.josephboyle.biometrics;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Main {

	public static int _HEIGHT = 400;
	public static int _WIDTH = 600;
	
	// We keep a static arraylist of all of the users for access within the Register and Login classes.
	public static ArrayList<User> users;
	
	public static void main(String[] args){
		File usersFile = new File("users.txt");
		users = FileUtil.createUserList(usersFile);
		
		JFrame window = new JFrame("Biometric Typing Demo");
		JFrame.setDefaultLookAndFeelDecorated(true);
		window.setPreferredSize(new Dimension(_WIDTH, _HEIGHT));
		window.add(new Login(_WIDTH / 2 - 8, _HEIGHT), BorderLayout.WEST);
		window.add(new Register(_WIDTH / 2 - 8, _HEIGHT, usersFile), BorderLayout.EAST);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.pack();
		window.setResizable(false);
		window.setVisible(true);
	}
		
	public static void popupBox(String title, String message){
		JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
	}
	
}
