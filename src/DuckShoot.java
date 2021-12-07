import lejos.nxt.*;

/**
 * MMM Module Duck Shoot from the video game Theme Park.
 * Building instructions for MMM modules are on brickhub.org
 * @author Lasse Deleuran
 */
public class DuckShoot {
	private static final NXTRegulatedMotor track = new NXTRegulatedMotor(MotorPort.A);
	private static final NXTMotor gun = new NXTMotor(MotorPort.C);	
	private static final NXTMotor extra = new NXTMotor(MotorPort.B);
	private static final SoundSensor sound = new SoundSensor(SensorPort.S1);

	public static final int SPEED_TRACK = 100;
	public static final int GUN_TURN = 12;
	public static final int SHOTS_BEFORE_RECALIBRATION = 10;
	
	public static void main(String[] args) {
		extra.setPower(100);
		extra.forward(); // In case we want to run another module, or light from this module.
		
		track.setSpeed(SPEED_TRACK);
		track.forward();
		
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
		sleep(300);
		gun.flt();
		gun.setPower(100);
		gun.resetTachoCount();
	}
	
	private static void sleep(int time) {
		int start = (int)System.currentTimeMillis();
		while(start+time > (int)System.currentTimeMillis())
			;
	}
}
