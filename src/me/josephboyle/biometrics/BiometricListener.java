package me.josephboyle.biometrics;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class BiometricListener implements KeyListener{

	// A record of which characters are currently pressed, so we don't add entries too many times (see keyPressed).
	// keyPressed will continue to trigger the event the longer you hold it, but we only want the one character actually typed,
	// so we ignore subsequent calls until the character is released.
	boolean[] charPressed = new boolean[255];
	// The actual data generated for our use later.
	public ArrayList<CharTimePair> charTimes;
	
	private boolean shiftHeld = false;
	
	public BiometricListener(){
		reset();
	}
	
	// Resets for use in another trial/login.
	// Replaces the need to create a new listener.
	public void reset(){
		charTimes = new ArrayList<CharTimePair>();
		for(int i = 0; i < charPressed.length; i ++) charPressed[i] = false;
	}
	
	// Iterates through all of the entries in charTimes and finds the most recent occurence of character.
	// It then sets that instance's endTime to the specified endTime.
	// This is called in keyRelease, to set the time at which the character was released.
	private void setTimesOfLastOccurence(char character, long endTime){
		// We start from the end, so that we can update the most recent occurence.
		for(int i = charTimes.size() - 1; i >= 0; i --){
			CharTimePair pair = charTimes.get(i);
			if(pair.character != character) continue;	// If characters don't match, we aren't at the right pair.
			pair.endTime = endTime;
			break;	// Once we update one, we can stop so as to not update all occurences.
		}
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE){
			if(charTimes.size() > 0) charTimes.remove(charTimes.size() - 1);
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// We don't handle caps lock implicitly -- they're handled through a Toolkit call for higher accuracy.
		// Thus, we consume the entry here so it doesn't affect our pairs.
		// We also consume backspaces here. See keyTyped for its usage.
		if(e.getKeyCode() == KeyEvent.VK_SHIFT) shiftHeld = true; // Updates shiftHeld once shift is pressed.
		else if(e.getKeyCode() == KeyEvent.VK_CAPS_LOCK || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) return;
		else{
			char key = e.getKeyChar();	//key is the character pressed.
			if(!charPressed[key]){		// Proceed iff we aren't currently pressing the key.
				CharTimePair pair = new CharTimePair(key, System.currentTimeMillis());	// Creates a new pair with the current time.
				pair.capsLock = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);	// Grab the current system state of the caps lock button.
				pair.shift = shiftHeld;	// Get the current shift state.
				charTimes.add(pair);
				charPressed[key] = true;	// Lock the character for future press events.
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// We ignore releases of caps lock and backspace, as they're handled in the keyPressed and keyTyped methods, respectively.
		if(e.getKeyCode() == KeyEvent.VK_CAPS_LOCK || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) return;
		// Updates shiftHeld once shift is let go. See keyPressed for when it's set.
		else if(e.getKeyCode() == KeyEvent.VK_SHIFT) shiftHeld = false;
		else{
			char key = e.getKeyChar();	// The key is the current character.
			charPressed[key] = false;	// Update the record to show that we're not pressing the character, to allow future presses of it.
			setTimesOfLastOccurence(key, System.currentTimeMillis());	// Set the end time of the most recent occurence.
		}
	}

}
