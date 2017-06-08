package me.josephboyle.biometrics;

import java.util.ArrayList;

/**
 * 
 * This class serves as a struct which stores the start and end times of a keystroke, the character entered, and other background data.
 * We use longs to store the times, as they enter the system originally as System times in ms.
 * 
 */

public class CharTimePair {
	
	public char character;
	public long startTime, endTime, start_dev, end_dev;
	public boolean capsLock = false, shift = false;
	
	public CharTimePair(char c, long startTime){
		this.character = c;
		this.startTime = startTime;
	}
	
	public String toString(){
		return startTime + "," + endTime + "," + start_dev + "," + end_dev + "," + capsLock + "," + shift;
	}
	
	public static void normalizeTimes(ArrayList<CharTimePair> pairs){
		if(pairs.size() == 0) return;
		long firstStartTime = pairs.get(0).startTime;

		for(CharTimePair pair : pairs){
			pair.startTime -= firstStartTime;
			pair.endTime -= firstStartTime;
		}
	}

}
