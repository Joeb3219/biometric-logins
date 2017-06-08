package me.josephboyle.biometrics;

import java.awt.Button;
import java.awt.Dimension;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

public class Register extends JPanel{

	// The number of training sessions required.
	// This is the number of *successful* training sessions.
	private static int _NUM_TRAINING_SESSIONS = 10;
	
	private ArrayList<ArrayList<CharTimePair>> training_pairs = new ArrayList<ArrayList<CharTimePair>>();
	private int trainNumber = 0;
	
	// Width and height are the dimensions of the display.
	// usersFile is the File which holds all of the user data.
	public Register(int width, int height, File usersFile){
		// Display information to build the JPanel.
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setPreferredSize(new Dimension(width, height));
		TitledBorder border = new TitledBorder("Register");
		border.setTitleJustification(TitledBorder.CENTER);
		border.setTitleJustification(TitledBorder.TOP);
		setBorder(border);
		
		TextField field_username = new TextField();
		TextField field_password = new TextField();

		// We add a focusListener which, upon the user exiting the username field, will reset the 
		// fields if the username is already taken to prevent duplicate registrations.
		field_username.addFocusListener(new FocusListener(){
			public void focusGained(FocusEvent e) {} // Unused.
			public void focusLost(FocusEvent e) {
				for(User u : Main.users){
					// Check if any users have the entered username.
					if(u.username.equals(field_username.getText())){
						Main.popupBox("Username Taken", "Uh oh, the username " + field_username.getText() + " is already taken.");
						field_username.setText(" ");
						field_username.setText("");
						field_password.setText(" ");
						field_password.setText("");
						return;
					}
				}
			}
		});
		add(new Label("Username"));	// Adds the label before the username box.
		add(field_username);		// Adds the username box.

		
		BiometricListener listener = new BiometricListener();	// Creates an instance of BiometricListener, which implements KeyListener.
		field_password.addKeyListener(listener);
		add(new Label("Password"));	// Adds the label before the password box
		add(field_password);		// Adds the password box
		
		Button button_train = new Button("Train");
		// The action listener for the train/register button does all the heavy work for generating the training data.
		// Whenever the button is pressed, we will handle a load of processes which, depending on the current training number,
		// will either ask for another training set, or actually register the user.
		button_train.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				// Checks that the password is actually entered (IE: didn't just click Train w/ no data).
				// Empty passwords make our user login system crash.
				if(field_password.getText().length() == 0){
					Main.popupBox("Invalid Password", "You cannot have a password of length 0.");
					return;
				}
				
				// After the first entry, all subsequent entries should contain the same password
				// as the first entry. Thus, we perform a check iff the training number isn't 0.
				if(trainNumber != 0){
					String originalPassword = convertPairsToString(training_pairs.get(0));	// training_pairs.get(0) is the first entered set, which will have all of the needed characters.
					if(!originalPassword.equals(field_password.getText())){
						Main.popupBox("Password Invalid", "The entered password did not match the previous entry. Please try again.");
						field_password.setText(" ");	// Reset the password field. This needs to be done twice due to bugs in Java 8.
						field_password.setText("");	// Reset the password field. This needs to be done twice due to bugs in Java 8.
						listener.reset();			// Resets the listener. This is important, or subsequent trials will have bad data preloaded from the previous trial.
						return;
					}
				}
				
				training_pairs.add(trainNumber, listener.charTimes);	// Adds the accumulated data in the listener to the training sets.
				listener.reset();	// Resets the listener. This is important, or subsequent trials will have bad data preloaded from the previous trial.
				trainNumber ++;
				
				if(trainNumber < _NUM_TRAINING_SESSIONS - 1){
					// If this isn't the last training session, we change the train button to have the correct number of completed trials.
					button_train.setLabel("Train (" + (trainNumber + 1) + "/" + _NUM_TRAINING_SESSIONS + ")");
				}else if(trainNumber == _NUM_TRAINING_SESSIONS - 1){
					// If this is the last training session, change the button to say "Register".
					button_train.setLabel("Register");
				}else if(trainNumber == _NUM_TRAINING_SESSIONS){
					// If we've now completed all training sessions, attempt to register the user.
					Main.users.add(new User(field_username.getText(), field_password.getText(), getUserCharTimes(training_pairs) ));
					Main.popupBox("Registration successful", "Congrats, " + field_username.getText() + ", you have registerd!");
					field_username.setText(" ");	// Reset the username field. This needs to be done twice due to bugs in Java 8.
					field_username.setText("");		// Reset the username field. This needs to be done twice due to bugs in Java 8.
					trainNumber = 0;	// Reset the trainNumber for another registration.
					FileUtil.writeUsersToFile(Main.users, usersFile);	// Saves the users to the file now that we've added a new user to the list.
					button_train.setLabel("Train");	// Reset the button text to the original text.
					training_pairs = new ArrayList<ArrayList<CharTimePair>>();
				}
				field_password.setText(" ");	// Reset the password field. This needs to be done twice due to bugs in Java 8.
				field_password.setText("");		// Reset the password field. This needs to be done twice due to bugs in Java 8.
			}
		});
		add(button_train);
	}
	
	// Given an arraylist of CharTimePair, will grab the characters, in order, to construct the message typed.
	private String convertPairsToString(ArrayList<CharTimePair> pairs){
		String s = "";
		for(CharTimePair pair : pairs){
			s += pair.character;
		}
		return s;
	}
	
	// Given all of the training sets, this method is responsible for generating a single array of CharTimePair
	// such that each pair, p, is the average start/end of all of pairs at its given index, with standard deviations included.
	// This requires several steps: normalization of pairs, finding the mean, finding the standard deviation.
	// We keep a count of "meaningful entries" in the event that a set doesn't have the right number of characters.
	private CharTimePair[] getUserCharTimes(ArrayList<ArrayList<CharTimePair>> sets){
		if(sets.size() == 0) return null;
		
		int passwordLength = sets.get(0).size(), numMeaningfulEntries = 0;
		CharTimePair[] result = new CharTimePair[passwordLength];
		
		// We begin by normalizing all of the sets.
		// This shifts all the pairs such that the first pair begins at t = 0.
		// See CharTimePair.normalizeTimes
		for(ArrayList<CharTimePair> set : sets){
			CharTimePair.normalizeTimes(set);
		}
		
		// We next populate result such that for each index i, result[i] is a new CharTimePair object
		// with the character at index i in the password.
		// All data of the resulting pair is set to 0.
		for(int i = 0; i < passwordLength; i ++){
			result[i] = new CharTimePair(sets.get(0).get(i).character, 0);
		}
		
		// Next, we add the start and end times of each pair (avg = total/num ... we're computing total).
		for(ArrayList<CharTimePair> set : sets){
			if(set.size() != passwordLength) continue;
			numMeaningfulEntries ++;
			for(int i = 0; i < passwordLength; i ++){
				result[i].startTime += set.get(i).startTime;
				result[i].endTime += set.get(i).endTime;
			}
		}
		
		// Go through each pair and find the standard deviation.
		// Std deviation = sqrt (sum( (x - avg)^2 )) for all x which contributed to the average.
		for(ArrayList<CharTimePair> set : sets){
			if(set.size() != passwordLength) continue;
			for(int i = 0; i < passwordLength; i ++){
				result[i].start_dev += Math.pow((result[i].startTime / numMeaningfulEntries) - set.get(i).startTime, 2);
				result[i].end_dev += Math.pow((result[i].endTime / numMeaningfulEntries) - set.get(i).endTime, 2);
			}
		}
		
		// Go through each pair and divide by the total number of pairs, therein giving us an average.
		for(int i = 0; i < passwordLength; i ++){
			result[i].startTime /= numMeaningfulEntries;
			result[i].endTime /= numMeaningfulEntries;
			result[i].start_dev = (long) Math.sqrt(result[i].start_dev / numMeaningfulEntries);
			result[i].end_dev = (long) Math.sqrt(result[i].end_dev / numMeaningfulEntries);
		}
		
		// We now check what the user mostly in terms of capsLock/shift.
		// If at least half of the pairs used shift/capsLock, we set shift/capsLock to true.
		for(int i = 0; i < passwordLength; i ++){
			int numShift = 0, numCapsLock = 0;
			for(ArrayList<CharTimePair> set : sets){
				if(set.size() != passwordLength) continue;
				if(set.get(i).capsLock) numCapsLock ++;
				if(set.get(i).shift) numShift ++;				
			}
			if(numShift >= numMeaningfulEntries / 2) result[i].shift = true;
			if(numCapsLock >= numMeaningfulEntries / 2) result[i].capsLock = true;
		}
		
		return result;
	}
	
}
