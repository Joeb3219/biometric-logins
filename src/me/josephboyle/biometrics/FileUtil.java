package me.josephboyle.biometrics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class FileUtil {
	
	/*
	 * The file format goes as follows:
	 * - user,password,p1start,p1end,p1start_dev,p1end_dev,p1capsLock,p1shift,p2start,p2end,p2start_dev,p2end_dev,p2capsLock,p2shift...
	 * - user2,password2,p1start,p1end,p1start_dev,p1end_dev,p1capsLock,p1shift,p2start,p2end,p2start_dev,p2end_dev,p2capsLock,p2shift...
	 * The number of entries following password will be equal to the number of characters in password * 6 (as we store 6 pieces of data per pair).
	 */
	
	public static ArrayList<User> createUserList(File file){
		ArrayList<User> users = new ArrayList<User>();
		
		try{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			while( (line = reader.readLine()) != null){
				String[] parts = line.split(",");
				if(parts.length < 2) continue;	// We must have at least a username and password.
				String username = parts[0];
				String password = parts[1];
				if(password.length() > (parts.length - 2)/6) continue;	// Ensure that there's the correct number of pairs for the password.
				CharTimePair[] pairs = new CharTimePair[(parts.length - 2) / 6];
				int j = 0;
				// For each entry after the password, we create a pair using the six consecutive values.
				for(int i = 2; i < parts.length; i += 6){
					pairs[j] = new CharTimePair(password.charAt(j), Long.parseLong(parts[i]));
					pairs[j].endTime = Long.parseLong(parts[i + 1]);
					pairs[j].start_dev = Long.parseLong(parts[i + 2]);
					pairs[j].end_dev = Long.parseLong(parts[i + 3]);
					pairs[j].capsLock = Boolean.parseBoolean(parts[i + 4]);
					pairs[j].shift = Boolean.parseBoolean(parts[i + 5]);
					j ++;
				}
				users.add(new User(username, password, pairs));
			}
			reader.close();
		}catch(IOException e){e.printStackTrace();}
		
		return users;
	}
	
	public static void writeUsersToFile(ArrayList<User> users, File file){
		try{
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			for(User u : users){
				writer.write(u.toString());	// Write the user object as a string.
				writer.newLine();			// New line
			}
			writer.close();
		}catch(IOException e){e.printStackTrace();}
	}
	
}
