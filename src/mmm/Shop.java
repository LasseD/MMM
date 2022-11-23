package mmm;

import lejos.nxt.*;
import java.io.File;

public class Shop extends Thread {
	public static final File file = new File("bell.wav"); // Upload to NXT: "nxjupload.bat -u sounds/bell.wav"
	
	public static boolean shouldStartShop(SensorPort port) {
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
		track.out();
		Time.sleep(8000); // Ensure sensor is calibrated.
		while(true) {
			track.in();
			boolean purchase = false;
			if(sensor.seesMinifig(1000*60*4)) { // 4 minutes
				Sound.playSample(file, 100);
				purchase = true;
			}
			track.out();
			Time.sleep(purchase ? 10000 : 4000); // Move out 10 seconds to get guest out, 4 seconds to potentially miscalibrated sensor.
		}
	}
}
