import mmm.*;
import lejos.nxt.*;

/**
 * MMM Module Plane Flyers from the video game Theme Park.
 * Building instructions for MMM modules are on brickhub.org
 * @author Lasse Deleuran
 */
public class PlaneFlyer {
	private static final Track track = new Track(MotorPort.C, true);
	private static final NXTRegulatedMotor lifter = new NXTRegulatedMotor(MotorPort.A);
	private static final NXTRegulatedMotor turner = new NXTRegulatedMotor(MotorPort.B);
	private static final NXTMotor turnResetter = new NXTMotor(MotorPort.B);
	
	private static final FigureSensor figureSensor = new FigureSensor(SensorPort.S3);
	private static final TouchSensor touch = new TouchSensor(SensorPort.S4);

	public static final int CAPACITY = 4;

	// Speed and acceleration:
	public static final int ACCELERATION = 300;	
	public static final int SPEED_TURN_SLOW = 150;
	public static final int SPEED_TURN_FAST = 400;
	public static final int SPEED_LIFT = 230;

	// Movements:
	public static final int LIFT_CLEAR = -300;
	public static final int LIFT_TOP = -250;
	
	public static final int TURN_DOOR = -120;
	
	private static int guests = 0;
	
	public static void main(String[] args) {
		init();
		
		while(true) {
			for(int i = 0; i < CAPACITY && pickup(); i++)
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
			Time.sleep(15*1000); // Wait 15 seconds until next round
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
		track.in();
		track.boost(180);

		// Wait for guest. If no guest arrives, then return false:
		if(!figureSensor.seesMinifig(20*1000)) {
			track.resetSpeed();
			track.out();
			
			if(guests > 0) { // Only close airplane if there are guests:
				liftToDoor();
				closeDoor();
			}
			return false;
		}
		
		// Get passenger into plane:
		track.boost(350);
		guests++;
		
		// Close the door:
		track.stop();
		liftToDoor();		
		track.out();
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
		
		track.boost(-720);
		guests--;

		if(guests > 0) {
			liftToDoor();
			closeDoor();
		}
	}
	
	private static boolean recentlyAdjusted = false;
	private static void init() {
		track.out();
		
		Button.RIGHT.addButtonListener(new ButtonListener() {			
			@Override
			public void buttonReleased(Button b) {
				// NOP
			}
			
			@Override
			public void buttonPressed(Button b) {
				recentlyAdjusted = true;
				turner.rotate(-10);
			}
		});
		
		Button.LEFT.addButtonListener(new ButtonListener() {			
			@Override
			public void buttonReleased(Button b) {
				// NOP
			}
			
			@Override
			public void buttonPressed(Button b) {
				recentlyAdjusted = true;
				turner.rotate(10);
			}
		});
		
		do {
			recentlyAdjusted = false;
			Time.sleep(15000);
		}
		while(recentlyAdjusted);
		
		lifter.setSpeed(SPEED_LIFT);
		if(touch.isPressed()) {
			lifter.backward();
			while(touch.isPressed())
				;
		}
		downAndReset();
		KillSwitch.enable();
		
		// Assume turn motor is placed with a plane over the entrance:
		turner.setAcceleration(ACCELERATION);
		turner.flt();
		
		// Set track:
		track.in();
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
		lifter.rotate(-525);
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
		lifter.rotate(125);
		
		// Reset turner:
		turner.flt();
		turner.suspendRegulation();
		
		turnResetter.setPower(15);
		turnResetter.forward();
		Time.sleep(150);
		turnResetter.backward();
		Time.sleep(150);
		turnResetter.flt();
		
		// Restore turner:
		turner.setSpeed(SPEED_LIFT);
		turner.resetTachoCount();
	}
}
