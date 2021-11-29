import lejos.nxt.*;
import lejos.nxt.addon.*;;

/**
 * MMM Module Plane Flyers from the video game Theme Park.
 * Building instructions for MMM modules are on brickhub.org
 * @author Lasse Deleuran
 */
public class PlaneFlyers {
	private static final NXTRegulatedMotor track = new NXTRegulatedMotor(MotorPort.A);
	private static final NXTRegulatedMotor lifter = new NXTRegulatedMotor(MotorPort.B);
	private static final NXTRegulatedMotor turner = new NXTRegulatedMotor(MotorPort.C);
	private static final NXTMotor turnResetter = new NXTMotor(MotorPort.C);
	
	private static final LightSensor light = new LightSensor(SensorPort.S4);
	private static final TouchSensor touch = new TouchSensor(SensorPort.S1);

	public static final int CAPACITY = 4;

	// Speed and acceleration:
	public static final int ACCELERATION = 300;	
	public static final int SPEED_TRACK_SLOW = 200;
	public static final int SPEED_TRACK_FAST = 400;
	public static final int SPEED_TURN_SLOW = 150;
	public static final int SPEED_TURN_FAST = 400;
	public static final int SPEED_LIFT = 230;
	
	// Movements:
	public static final int LIFT_TOP = -400;
	public static final int LIFT_CLEAR = -300;
	
	public static final int TURN_DOOR = -120;
	
	private static int guests = 0;
	private static int sensorValFree, sensorValFig; // Light sensor values after initial calibration.
	
	public static void main(String[] args) {
		init();
		
		while(true) {
			for(int i = 0; i < 4 && pickup(); i++)
				;
			if(guests > 0) {
				lifter.rotate(LIFT_TOP);
				turner.setSpeed(SPEED_TURN_FAST);
				turn(3 * 360 - (90 + 90*guests));
				turner.setSpeed(SPEED_TURN_SLOW);
				lifter.rotate(-LIFT_TOP);
				while(guests > 0) {
					leave();
				}
			}
			sleep(15*1000); // Wait 15 seconds until next round
		}
	}
	
	/**
	 * Attempt to pick up a guest.
	 * At return, a plane is over the entrance of the track.
	 * This plane might be empty if no passenger has arrived.
	 * @return true if another guest should be picked up.
	 */
	private static boolean pickup() {
		turner.setSpeed(SPEED_TURN_SLOW); // Slow for pickups.
		track.forward();

		// Wait for guest. If no guest arrives, then return false:
		if(!seesMinifig(20*1000)) {
			track.setSpeed(SPEED_TRACK_SLOW);
			track.backward();
			
			if(guests > 0) { // Only close airplane if there are guests:
				liftToDoor();
				closeDoor();
			}
			return false;
		}
		
		// Get passenger into plane:
		track.setSpeed(SPEED_TRACK_FAST);
		track.rotate(1200);
		track.setSpeed(SPEED_TRACK_SLOW);
		guests++;
		
		// Close the door:
		liftToDoor();		
		track.backward();
		closeDoor();

		if(guests < CAPACITY) {
			// Position next plane:
			openDoor();
			track.forward();
			downAndReset();
			return true;
		}
		else {			
			return false;
		}
	}
	
	private static void leave() {
		turner.setSpeed(SPEED_TURN_SLOW); // Slow for pickups.

		openDoor();
		
		downAndReset();
		
		track.setSpeed(SPEED_TRACK_FAST);
		sleep(1000); // clear the plane
		track.setSpeed(SPEED_TRACK_SLOW);
		guests--;

		if(guests > 0) {
			liftToDoor();
			closeDoor();
		}
	}
	
	private static void calibrate() {
		light.setFloodlight(true);
		sleep(200);
		sensorValFree = light.getLightValue();

		for(int i = 0; i < 5; i++) {
			light.setFloodlight(false);
			sleep(100);
			light.setFloodlight(true);
			sleep(100);
		}
		
		LCD.clear();
		LCD.drawString("BLOCK", 0, 0, true);
		while(!Button.ENTER.isDown()) {
			LCD.drawInt(light.getLightValue(), 3, 1);
		}
		sensorValFig = light.getLightValue();

		LCD.clear();		
		LCD.drawInt(sensorValFree, 3, 2);
		LCD.drawInt(sensorValFig, 3, 3);
		light.setFloodlight(false);
	}

	private static void init() {
		calibrate();

		lifter.setSpeed(SPEED_LIFT);
		if(touch.isPressed()) {
			lifter.backward();
			while(touch.isPressed())
				;
		}
		downAndReset();
		
		// Assume turn motor is placed with a plane over the entrance:
		turner.setAcceleration(ACCELERATION);
		turner.flt();
		
		// Set track:
		track.setAcceleration(ACCELERATION);
		track.setSpeed(SPEED_TRACK_SLOW);
		track.forward();
	}
	
	private static void turn(int planeAngle) {
		planeAngle = -planeAngle; // Turn counter clockwise, as that is forward for planes.
		//public static final int TURN_QUARTER = -450; // 90*60/12
		lifter.setSpeed(turner.getSpeed()*12/60);
		lifter.rotate(planeAngle, true);
		turner.rotate(planeAngle*60/12, true);

		while(lifter.isMoving() || turner.isMoving())
			;
		
		lifter.setSpeed(SPEED_LIFT);
	}
	
	private static void liftToDoor() {
		lifter.backward();
		while(touch.isPressed())
			;
		lifter.stop();
		sleep(50);
		lifter.rotate(-360);
	}
	
	private static void closeDoor() {
		turner.rotate(-TURN_DOOR);		
		// Clear the pin:
		lifter.rotate(LIFT_CLEAR);
		turner.rotate(TURN_DOOR);
	}
	
	private static void openDoor() {
		turn(60);
		lifter.rotate(-LIFT_CLEAR+15);
		turn(30);		
	}
	
	/*¨
	 * Ensure lifter is reset, so that it does not get out of calibration.
	 */
	private static void downAndReset() {		
		lifter.forward();
		while(!touch.isPressed())
			;
		lifter.stop();
		lifter.rotate(120);
		
		// Reset turner:
		turner.flt();
		turner.suspendRegulation();
		
		turnResetter.setPower(15);
		turnResetter.forward();
		sleep(150);
		turnResetter.backward();
		sleep(150);
		turnResetter.flt();
		
		// Restore turner:
		turner.setSpeed(SPEED_LIFT);
		turner.resetTachoCount();
	}
	
	public static boolean seesMinifig(int timeoutMS) {	
		light.setFloodlight(true);
		while(timeoutMS > 0) { 
			sleep(50);
			timeoutMS -= 50;
			int v = light.getLightValue();
			LCD.drawInt(v, 3, 1);

			int diffFig = Math.abs(v-sensorValFig);
			int diffFree = Math.abs(v-sensorValFree);
			if(diffFree > diffFig) {
				light.setFloodlight(false);
				return true;
			}
		}
		light.setFloodlight(false);
		return false;
	}
	
	private static void sleep(int time) {
		int start = (int)System.currentTimeMillis();
		while(start+time > (int)System.currentTimeMillis())
			;
	}
}
