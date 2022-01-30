import mmm.*;
import lejos.nxt.*;

/**
 * MMM Module Coconut Shy, which is a shop in the video game Theme Park.
 * Building instructions for MMM modules are on brickhub.org
 * @author Lasse Deleuran
 */
public class CoconutShy {
	private static final NXTRegulatedMotor track = new NXTRegulatedMotor(MotorPort.A);
	private static final NXTMotor gun = new NXTMotor(MotorPort.B);	
	private static final UltrasonicSensor distance = new UltrasonicSensor(SensorPort.S1);
	private static final AdjustablePowerOutput aux = new AdjustablePowerOutput(MotorPort.C, 3);

	public static final int SPEED_TRACK = 100;
	public static final int GUN_TURN = 8;
	public static final int TRIGGER_DISTANCE = 3;
	public static final int SHOTS_BEFORE_RECALIBRATION = 10;
	
	public static void main(String[] args) {
		aux.start(); // Extra power output.
		
		track.setSpeed(SPEED_TRACK);
		track.forward();
		
		resetGun(); // Ensure gun is ready to shoot
		int lastDistance = distance.getDistance();
		int shots = 0;
		
		while(!Button.ENTER.isDown()) {
			Time.sleep(250);

			int val = distance.getDistance();
			LCD.drawInt(val, 2, 3, 1); // Draw current value
			
			if(val > lastDistance || lastDistance >= 255) {
				// Don't do anything.
			}
			else if(val <= lastDistance - TRIGGER_DISTANCE) { // Moved more than TRIGGER_DISTANCE cm closer: Shoot.
				shots++;
				shoot();			
			}
			lastDistance = val;

			if(shots >= 5) {
				reset();
				shots = 0;
			}
		}
		aux.stop();
	}
	
	private static void reset() {
		track.stop();
		track.setSpeed(2*SPEED_TRACK);
		track.rotate(-145);
		track.rotate(180); // Ensure the lifters are down
		track.setSpeed(SPEED_TRACK);
		track.forward();
	}
	
	private static void shoot() {
		gun.resetTachoCount();
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
		gun.setPower(60);
	}
}
