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
	
	private static final FigureSensor figureSensor = new FigureSensor(SensorPort.S1);

	public static final int CAPACITY = 6;

	// Speed and acceleration:
	public static final int ACCELERATION = 300;	
	public static final int SPEED_TURN = 80;
	public static final int SPEED_LIFT = 130;
	
	private static int guests = 0;
	
	public static void main(String[] args) {
		init();
		
		track.out();
		Time.sleep(1500);
		
		while(guests < CAPACITY) {
			if(pickup() && guests < CAPACITY) {
				turn(guests%2 == 1 ? 3 : 1);
			}
		}

		while(!Button.ENTER.isDown()) {
			turn(-7);
			leave();
			while(!pickup()) {
				turn(-6);
			}
		}
	}
	
	private static void init() {
		turner.setSpeed(SPEED_TURN);
		turner.setAcceleration(ACCELERATION);
		lifterDown();
		
		figureSensor.calibrate();
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
		track.boost();
		track.in();
		Time.sleep(750);
		track.resetSpeed();

		if(!figureSensor.seesMinifig(20*1000)) {
			track.out();
			return false; // No figure detected
		}
		
		track.stop();
		track.rotate(580);
		lifterUp();
		track.rotate(220);
		track.in();
		lifterDown();
		track.out();
		track.boost();
		Time.sleep(2500); // If someone else is at the wheel, then give them time to leave before it turns.
		track.resetSpeed();

		guests++;
		return true;
	}
	
	private static void leave() {
		lifterUp();
		track.rotate(-250);
		track.out();
		lifterDown();
		track.boost();
		Time.sleep(4500);
		track.resetSpeed();
	}
	
	private static void lifterDown() {
		lifter.setPower(12);
		lifter.forward();
		while(true) {
			int from = lifter.getTachoCount();
			Time.sleep(100);
			int to = lifter.getTachoCount();
			if(to - from < 2) {
				break;
			}
		}
		lifter.flt();
	}
	
	private static void lifterUp() {
		lifter.setPower(25);
		lifter.backward();
		while(true) {
			int from = lifter.getTachoCount();
			Time.sleep(100);
			int to = lifter.getTachoCount();
			if(from - to < 2) {
				break;
			}
		}
		lifter.flt();
	}
}
