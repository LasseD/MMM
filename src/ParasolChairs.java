import mmm.*;
import lejos.nxt.*;

/**
 * MMM Module Parasol Chairs from the video game Theme Park.
 * Building instructions for MMM modules are on brickhub.org
 * @author Lasse Deleuran
 */
public class ParasolChairs {
	private static final Track track = new Track(MotorPort.C, true);
	private static final NXTMotor lifter = new NXTMotor(MotorPort.A);
	private static final NXTRegulatedMotor turner = new NXTRegulatedMotor(MotorPort.B);
	private static final NXTMotor fun = new NXTMotor(MotorPort.B);
	
	private static final FigureSensor figureSensor = new FigureSensor(SensorPort.S1);
	private static final LightSensor turnerLightSensor = new LightSensor(SensorPort.S2);

	public static final int CAPACITY = 6;

	// Speed and acceleration:
	public static final int ACCELERATION = 100;
	public static final int SPEED_TURN_SLOW = 150;
	public static final int SPEED_TURN_FAST = 1100;
	public static final int SPEED_LIFT = 230;

	// Position and state:
	public static final int BANANA = 140;
	public static final int COG_40 = 40;	
	public static final int ANGLE_SIXTH = 60*BANANA/COG_40;
	
	private static int guests = 0;
	
	public static void main(String[] args) {
		turner.setAcceleration(ACCELERATION);
		reset();
		KillSwitch.enable();
		
		while(true) {
			for(int i = 0; i < CAPACITY && pickup(); i++)
				;
			if(guests > 0) {
				turner.suspendRegulation();
				fun.forward();
				int p = 25;
				for(; p < 95; p += 5) {
					fun.setPower(p);
					Time.sleep(500);
				}
				fun.setPower(p);
				Time.sleep(5000);
				for(; p >= 10; p -= 3) {
					fun.setPower(p);
					Time.sleep(500);
				}
				fun.flt();
				Time.sleep(500);
				fun.stop();
				
				reset();
				offloadAll();
			}
			Time.sleep(15000); // Cooldown
		}
	}
	
	/**
	 * Attempt to pick up a guest.
	 * At return, a seat is over the lift.
	 * @return true if another guest should be picked up.
	 */
	private static boolean pickup() {		
		turner.setSpeed(SPEED_TURN_SLOW); // Slow for pickups.
		track.in();
		track.boost(120);

		// Wait for guest. If no guest arrives, then return false:
		if(!figureSensor.seesMinifig(20*1000)) {
			track.out();			
			return false;
		}

		// Get passenger into seat:
		track.boost(620);
		track.boost(-30);
		track.stop();
		lifter.setPower(25);
		lifter.backward();
		Time.sleep(2500);
		track.boost(300);
		track.stop();
		liftDown();
		track.out();

		guests++;

		if(guests < CAPACITY) {
			// Position next seat:
			rotate(guests%2==0 ? 1 : 3);
			return true;
		}
		else {			
			return false;
		}
	}
	
	private static void rotate(int seats) {
		turner.rotate(ANGLE_SIXTH*seats);
	}
	
	// Offload CAPACITY times, since we don't know where our guests are...
	private static void offloadAll() {
		offload();
		for(int i = 0; i < CAPACITY-1; i++) {
			rotate(1);
			offload();
		}
		guests = 0;
	}
	
	private static void offload() {
		lifter.setPower(25);
		lifter.backward();
		track.out();
		Time.sleep(4000);
		liftDown();
	}
	
	private static void liftDown() {
		lifter.setPower(10);
		lifter.forward();
		for(int i = 0; true; i++) {
			int a = lifter.getTachoCount();
			Time.sleep(200);
			int b = lifter.getTachoCount();
			if(a == b)
				break;
			if(i == 10)
				track.out(); // Don't stop the module too much
		}
		lifter.flt();	
	}
		
	private static void reset() {
		track.out();
		
		liftDown();
		
		// Reset the turner:
		int bestVal = 100;
		int bestPosiiton = 0;
		
		turner.resetTachoCount();
		turner.setSpeed(SPEED_TURN_SLOW);
		turner.rotate(ANGLE_SIXTH*3, true);
		while(turner.isMoving()) {
			int val = turnerLightSensor.readValue();
			if(val < bestVal) {
				bestVal = val;
				bestPosiiton = turner.getTachoCount();
			}
		}
		turner.rotate(bestPosiiton+15); // + slack
	}
}
