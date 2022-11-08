import mmm.*;
import lejos.nxt.*;

/**
 * MMM Module "Bouncy Castle" which is a ride in the video game Theme Park.
 * Building instructions for MMM modules are on brickhub.org
 * @author Lasse Deleuran
 */
public class BouncyCastle {
	public static final Track track = new Track(MotorPort.C, true);
	public static final BasicMotor matt = new NXTMotor(MotorPort.B);
	public static final NXTRegulatedMotor jump = new NXTRegulatedMotor(MotorPort.A);
	public static final NXTMotor jumpResetter = new NXTMotor(MotorPort.A);
	public static final FigureSensor sensor = new FigureSensor(SensorPort.S4, true); // "true" for RCX light sensor.

	public static final int CAPACITY = 3;	
	private static int guests = 0;
	
	public static void main(String[] args) {
		KillSwitch.enable();
		
		jump.flt();
		jump.setSpeed(75);
		jump.setAcceleration(2500);
		matt.setPower(65);
		matt.flt();

		track.out();
		sensor.resume();
		resetJump();
		Time.sleep(5000);

		while(true) {
			invite();
			if(guests >= CAPACITY) {
				// Adjust matt a bit to clear the inner wall:
				matt.forward();
				Time.sleep(90);
				matt.flt();

				bounce();
				bounce();
				bounce();
				bounce();
				leave();
				Time.sleep(10000); // Cool down before inviting again.
			}
		}
	}

	public static void invite() {
		if(!sensor.seesMinifig(30000)) {
			return; // No minifig. No change.
		}
		long lastMinifig = System.currentTimeMillis();
		guests++;
		Sound.beepSequenceUp();

		// Go to entrance:
		track.in();
		track.boost();
		Time.sleep(1500); // Clear the sensor.

		// Go onto matt:
		matt.backward();
		
		while(guests < CAPACITY && sensor.seesMinifig(4000)) {
			guests++;
			Sound.beepSequenceUp();
			lastMinifig = System.currentTimeMillis();
			Time.sleep(1500); // Clear the sensor.
		}
		Time.sleep(5000 - (int)(System.currentTimeMillis()-lastMinifig));
		
		// Back to normal until we see another minifig:
		track.resetSpeed();
		track.out();
		matt.flt();		
	}

	public static void bounce() {
		jump.rotateTo(43);
		jump.rotateTo(0);
	}

	public static void resetJump() {
		jump.flt();
		jump.suspendRegulation(); // Let the resetter take over.
		
		jumpResetter.setPower(20);
		jumpResetter.backward();
		while(true) {
			int pos = jumpResetter.getTachoCount();
			Time.sleep(150);
			if(Math.abs(jumpResetter.getTachoCount() - pos) < 2)
				break;
		}
		jumpResetter.flt();
		jump.resetTachoCount();		
		jump.rotateTo(20);
	}

	public static void leave() {
		jump.rotateTo(30);
		matt.forward();

		track.boost(-1300);
		// Shuffle matt in case people are stuck:
		matt.backward();
		track.boost(80);
		matt.forward();
		track.boost(-1500);

		matt.flt();
		resetJump();
		
		guests = 0;
	}
}
