import lejos.nxt.*;

/**
 * MMM Module Plane Flyers from the video game Theme Park.
 * Building instructions for MMM modules are on brickhub.org
 * @author Lasse Deleuran
 */
public class PlaneFlyers {
	private static final NXTMotor track = new NXTMotor(MotorPort.C);
	private static final NXTRegulatedMotor lifter = new NXTRegulatedMotor(MotorPort.A);
	private static final NXTRegulatedMotor turner = new NXTRegulatedMotor(MotorPort.B);
	private static final NXTMotor turnResetter = new NXTMotor(MotorPort.B);
	
	private static final LightSensor light = new LightSensor(SensorPort.S3);
	private static final TouchSensor touch = new TouchSensor(SensorPort.S4);

	public static final int CAPACITY = 4;

	// Speed and acceleration:
	public static final int ACCELERATION = 300;	
	public static final int SPEED_TURN_SLOW = 150;
	public static final int SPEED_TURN_FAST = 400;
	public static final int SPEED_LIFT = 230;
	public static final int POWER_TRACK_SLOW = 25;
	public static final int POWER_TRACK_FAST = 65;

	// Movements:
	public static final int LIFT_CLEAR = -300;
	public static final int LIFT_TOP = -250;
	
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
		in();

		// Wait for guest. If no guest arrives, then return false:
		if(!seesMinifig(20*1000)) {
			track.setPower(POWER_TRACK_SLOW);
			out();
			
			if(guests > 0) { // Only close airplane if there are guests:
				liftToDoor();
				closeDoor();
			}
			return false;
		}
		
		// Get passenger into plane:
		track.setPower(POWER_TRACK_FAST);
		sleep(1800);
		track.setPower(POWER_TRACK_SLOW);
		guests++;
		
		// Close the door:
		track.flt();
		liftToDoor();		
		out();
		closeDoor();

		if(guests < CAPACITY) {
			// Position next plane:
			openDoor();
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
		
		track.setPower(POWER_TRACK_FAST);
		sleep(800); // clear the plane
		track.setPower(POWER_TRACK_SLOW);
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
		while(true) {
			if(Button.ENTER.isDown()) {
				LCD.drawInt(light.getLightValue(), 3, 1);
				break;
			}
			else if(Button.RIGHT.isDown()) {
				turner.rotate(-10);
			}
			else if(Button.LEFT.isDown()) {
				turner.rotate(10);
			}
			sleep(200);
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
		track.setPower(POWER_TRACK_SLOW);
		in();
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
		lifter.rotate(-530);
	}
	
	private static void closeDoor() {
		turner.rotate(-TURN_DOOR);		
		// Clear the pin:
		lifter.rotate(LIFT_CLEAR);
		turner.rotate(TURN_DOOR);
	}
	
	private static void openDoor() {
		turn(70);
		lifter.rotate(-LIFT_CLEAR+15);
		turn(20);
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

	private static boolean forward = true;
	private static void reverseTrack() {
		if(forward) {
			track.backward();
			forward = false;
		}
		else {
			track.forward();
			forward = true;
		}
	}
	
	public static void ensureTrackRuns() {
		final int WAIT_FOR_TEST = 300;

		while(true) {
			track.resetTachoCount();
			sleep(WAIT_FOR_TEST);
			if(Math.abs(track.getTachoCount()) > 30) { // stalled?
				return;
			}
			reverseTrack();
			sleep(WAIT_FOR_TEST);
			reverseTrack();
			sleep(WAIT_FOR_TEST);
		}
	}

	public static void in() {
		track.forward();
		ensureTrackRuns();
	}
	
	public static void out() {
		track.backward();
		ensureTrackRuns();
	}
	
	private static void sleep(int time) {
		int start = (int)System.currentTimeMillis();
		while(start+time > (int)System.currentTimeMillis())
			;
	}
}
