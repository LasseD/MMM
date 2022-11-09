import mmm.*;
import lejos.nxt.*;

/**
 * MMM Module Big Wheel from the video game Theme Park.
 * Building instructions for MMM modules are on brickhub.org
 * @author Lasse Deleuran
 */
public class BigWheel {
	private static final Track track = new Track(MotorPort.A, true);
	private static final NXTMotor lifter = new NXTMotor(MotorPort.B);
	private static final NXTRegulatedMotor turner = new NXTRegulatedMotor(MotorPort.C);
	
	private static final FigureSensor figureSensor = new FigureSensor(SensorPort.S4);

	// Speed and acceleration:
	public static final int ACCELERATION = 150;	
	public static final int SPEED_TURN = 80;
	public static final int SPEED_LIFT = 130;
		
	public static void main(String[] args) {
		init();
		
		/*while(guests < CAPACITY) {
			if(pickup() && guests < CAPACITY) {
				turn(guests%2 == 1 ? 3 : 1);
			}
		}//*/

		while(true) {
			turn(-7);
			leave();
			turn(-6);
			while(!pickup()) {
				turn(-6);
			}
		}
	}
	
	private static void init() {
		KillSwitch.enable();
		turner.setSpeed(SPEED_TURN);
		turner.setAcceleration(ACCELERATION);
		lifterDown();

		track.out();
		Time.sleep(5000);
	}
	
	private static void turn(int slots) {
		turner.rotate(slots * 3*360/6);
	}

	/**
	 * Attempt to pick up a guest.
	 * At return, a plane is over the entrance of the track.
	 * This plane might be empty if no passenger has arrived.
	 * @return true if another guest should be picked up.
	 */
	private static boolean pickup() {
		track.boost(100);

		if(!figureSensor.seesMinifig(15000)) {
			track.out();
			return false; // No figure detected
		}
		
		track.stop();
		track.rotate(485); // Clear corner
		lifterUp();
		track.rotate(380); // Get into pod.
		track.in();
		clear();

		return true;
	}
	
	private static void leave() {
		lifterUp();
		track.rotate(-250);
		track.out();
		clear();
	}
	
	private static void clear() {
		lifterDown();
		track.out();
		Time.sleep(5000);
		int tries = 0;
		while(figureSensor.seesMinifig(2000)) {
			// Try to get minifig out if stuck:
			Time.sleep(4000);
			track.in();
			Time.sleep(850);
			track.out();
			Time.sleep(4000);
			tries++;
			if(tries == 3) {
				track.boost(); // People can't get out!
			}
		}
	}
	
	private static void lifterDown() {
		lifter.setPower(14);
		lifter.forward();
		while(true) {
			int from = lifter.getTachoCount();
			Time.sleep(200);
			if(Math.abs(lifter.getTachoCount() - from) < 2) {
				break;
			}
		}
		lifter.flt();
	}
	
	private static void lifterUp() {
		lifter.setPower(35);
		lifter.backward();
		while(true) {
			int from = lifter.getTachoCount();
			Time.sleep(200);
			if(Math.abs(lifter.getTachoCount() - from) < 2) {
				break;
			}
		}
		lifter.flt();
	}
}
