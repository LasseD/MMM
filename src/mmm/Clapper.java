package mmm;

import lejos.nxt.LCD;
import lejos.nxt.SensorPort;
import lejos.nxt.SoundSensor;

public class Clapper {
	private final SoundSensor sound;
	private int maxSound;

	public Clapper(SensorPort port) {
		sound = new SoundSensor(port);
		maxSound = 80; // Ensure value goes up in loud environments.
	}
	
	public boolean hearsClap() {
		int val = sound.readValue();
		if(val > maxSound) {
			maxSound = val;
			LCD.drawInt(val, 3, 3, 4); // Draw max value
		}
		
		LCD.drawInt(val, 3, 3, 5); // Draw current value
		return sound.readValue() > maxSound * 85 / 100;
	}
}
