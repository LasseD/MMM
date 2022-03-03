import mmm.*;
import lejos.nxt.*;

/**
 * MMM Module Duck Shoot is a shop from the video game Theme Park.
 * Building instructions for MMM modules are on brickhub.org
 * 
 * @author Lasse Deleuran
 */
public class DuckShoot {
	private static final Track track = new Track(MotorPort.A);
	private static final NXTMotor gun = new NXTMotor(MotorPort.B);
	private static final SoundSensor sound = new SoundSensor(SensorPort.S1);

	public static final int GUN_TURN = 12;
	
	public static void main(String[] args) {
		AuxController.setupAuxController(MotorPort.C, SensorPort.S3);
		KillSwitch.enable();
		
		track.out();
		
		resetGun(); // Ensure gun is loaded
		int maxSound = 80; // Ensure value goes up in loud environments.
		int shots = 0;
		
		while(true) {
			int val = sound.readValue();
			if(val > maxSound) {
				maxSound = val;
				LCD.clearDisplay();
				LCD.drawInt(val, 3, 2); // Draw max value
			}
			
			LCD.drawInt(val, 2, 3, 1); // Draw current value
			LCD.drawInt(shots, 3, 0);
			if(sound.readValue() > maxSound*85/100) {
				shots++;
				shoot();
			}
		}
	}
	
	private static void shoot() {
		gun.forward();
		while(gun.getTachoCount() < GUN_TURN)
			;
		gun.flt();
		resetGun();
	}
	
	private static void resetGun() {
		gun.setPower(10);
		gun.backward();
		Time.sleep(300);
		gun.flt();
		gun.setPower(100);
		gun.resetTachoCount();
	}	
}
