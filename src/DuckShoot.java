import lejos.nxt.*;

/**
 * MMM Module Plane Flyers from the video game Theme Park.
 * Building instructions for MMM modules are on brickhub.org
 * @author Lasse Deleuran
 */
public class DuckShoot {
	private static final NXTRegulatedMotor track = new NXTRegulatedMotor(MotorPort.A);
	private static final NXTRegulatedMotor gun = new NXTRegulatedMotor(MotorPort.C);	
	private static final NXTMotor gunResetter = new NXTMotor(MotorPort.C);
	private static final SoundSensor sound = new SoundSensor(SensorPort.S1);

	public static final int SPEED_TRACK = 100;
	public static final int GUN_TURN = 14;
	
	public static void main(String[] args) {
		track.setSpeed(SPEED_TRACK);
		track.forward();

		resetGun();
		int maxSound = 0;
		
		while(true) {
			int val = sound.readValue();
			if(val > maxSound) {
				maxSound = val;
				LCD.clearDisplay();
				LCD.drawInt(val, 3, 2);
			}
			LCD.drawInt(val, 3, 1);
			if(sound.readValue() > 70) {
				shoot();
			}
		}
	}
	
	private static void shoot() {
		gun.rotate(GUN_TURN);
		gun.rotate(-GUN_TURN);
	}
	
	private static void resetGun() {
		gun.suspendRegulation();
		gunResetter.setPower(10);
		gunResetter.backward();
		sleep(500);
		gunResetter.flt();
		gun.setSpeed(700);
	}
	
	private static void sleep(int time) {
		int start = (int)System.currentTimeMillis();
		while(start+time > (int)System.currentTimeMillis())
			;
	}
}
