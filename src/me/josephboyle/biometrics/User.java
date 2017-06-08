package me.josephboyle.biometrics;

import java.util.ArrayList;

public class User {

	// _STD_DEV_WEIGHT controls by how many standard deviations of the training data we allow points to be considered valid.
	// A larger value allows for larger deviations.
	// IE: if the average is 15, and the deviation is 4, a weight of 2 will allow values in [15-4*2, 15+4*2] => [7,23].
	// A smaller value is more "correct", but less flexible.
	private static final double _STD_DEV_WEIGHT = 2.5;
	
	public String username;
	private String password;
	private CharTimePair[] charTimePairs;
	
	public User(String username, String password, CharTimePair[] pairs){
		this.username = username;
		this.password = password;
		this.charTimePairs = pairs;
		// If we import a user with a password and set of pairs such that the number of pairs isn't the same as the number of
		// characters in the password, this is a clear logical error, indicitive of data loss.
		// We don't throw any errors or crash the program, but rather alert the console. This is an error if it occurs.
		if(charTimePairs.length != password.length()){
			System.out.println("User " + toString() + " has an error! ");
			System.out.println("Password length is " + password.length() + ", but have " + charTimePairs.length + " character time entries.");
		}
	}
	
	// The arguments are all supplied as a login attempt.
	// This method verifies that the username and password are correct, and that the supplied keystrokes data is within an 
	// acceptable range of the trained biometric data.
	boolean isValidLogin(String username, String password, ArrayList<CharTimePair> pairs){
		if(!this.username.equals(username)) return false;	// Check that the username is correct
		if(!this.password.equals(password)) return false;	// Check that the password is correct
		if(pairs.size() != password.length()) return false;	// Check that the pairs sent have the correct number of character entries.
		for(int i = 0; i < pairs.size(); i ++){
			CharTimePair current = pairs.get(i);			// Current is the character pair that the user provided.
			CharTimePair reference = charTimePairs[i];		// Reference is the character pair that the registration has saved.
			if(current.capsLock != reference.capsLock || current.shift != reference.shift) return false;		// Checks that the shift/capslock status is correct.
			if(current.startTime > reference.startTime + reference.start_dev * _STD_DEV_WEIGHT) return false;	// Checks that the start time of the key is within _STD_DEV_WEIGHT deviations of the reference.
			if(current.endTime > reference.endTime + reference.end_dev * _STD_DEV_WEIGHT) return false;			// Checks that the end time of the key is within _STD_DEV_WEIGHT deviations of the reference.
			if(current.startTime < reference.startTime - reference.start_dev * _STD_DEV_WEIGHT) return false;	// Checks that the start time of the key is within _STD_DEV_WEIGHT deviations of the reference.
			if(current.endTime < reference.endTime - reference.end_dev * _STD_DEV_WEIGHT) return false;			// Checks that the end time of the key is within _STD_DEV_WEIGHT deviations of the reference.
		}
		return true;
	}
	
	// User's toString is used in saving the user into a save file.
	// It is a Comma Separated Value (CSV) list of the username, password, and all of the time pair entries.
	// See CharTimePair.toString().
	public String toString(){
		String str =  username + "," + password + ",";
		for(CharTimePair pair : charTimePairs){
			if(pair != null) str += pair.toString() + ",";
		}
		return str.substring(0, str.length() - 1);
	}
	
}
