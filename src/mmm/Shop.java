package mmm;

import lejos.nxt.*;
import java.io.File;

public class Shop extends Thread {
	public static final String BELL_FILE = "bell.wav"; // Upload to NXT: "nxjupload.bat -u sounds/bell.wav"

	
	public static boolean shouldStartShop(ADSensorPort port) {
		LightSensor sensor = new LightSensor(port);
		return sensor.readValue() > 0; // There is a light sensor, so most likely a shop is attached.
	}
	
	private FigureSensor sensor;
	private Track track;
	
	public Shop(SensorPort sensorPort, MotorPort motorPort) {
		sensor = new FigureSensor(sensorPort);
		track = new Track(motorPort, true);
	}
	
	@Override
	public void run() {
		File file = new File(BELL_FILE);
		
		track.out();
		Time.sleep(8000);
		while(true) {
			track.in();
			if(sensor.seesMinifig(10000)) {
				Sound.playSample(file, 100);

				track.out();
				Time.sleep(10000);
			}
		}
	}
}
