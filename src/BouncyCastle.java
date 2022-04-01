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
		jump.setSpeed(110);
		jump.setAcceleration(4500);
		matt.setPower(70);
		matt.flt();

		track.out();

		resetJump();
		
		while(true) {
			if(sensor.seesMinifig(30000)) {
				if(invite()) {
					// Adjust matt a bit to clear the inner wall:
					matt.forward();
					Time.sleep(50);
					matt.flt();

					bounce();
					bounce();
					bounce();
					bounce();
					leave();
					Time.sleep(10000); // Cool down					
				}
			}
		}
	}

	public static boolean invite() {
		if(guests >= CAPACITY) {
			track.out();
			matt.flt();
			return true;
		}
		
		guests++;
		Sound.beepSequenceUp();
		
		// Go to entrance:
		track.in();
		track.boost();
		
		// Clear the sensor. If another minifig makes it, then that is alright too:
		Time.sleep(800);

		// Go onto matt:
		matt.backward();
		if(sensor.seesMinifig(3000)) {
			return invite();
		}
		
		track.resetSpeed();
		track.out();
		matt.flt();		
		return guests >= CAPACITY;
	}

	public static void bounce() {
		jump.resetTachoCount();
		jump.rotateTo(65);
		jump.rotateTo(0);
	}

	public static void resetJump() {
		jump.flt();
		jump.suspendRegulation(); // Let the resetter take over.
		
		jumpResetter.setPower(20);
		jumpResetter.backward();
		while(true) {
			int pos = jumpResetter.getTachoCount();
			Time.sleep(100);
			if(jumpResetter.getTachoCount() == pos)
				break;
		}
		jumpResetter.flt();
		jump.resetTachoCount();		
	}

	public static void leave() {
		jump.rotateTo(30);
		track.boost();

		matt.forward();

		Time.sleep(6500); // vacate castle
		
		matt.flt();
		resetJump();
		
		track.resetSpeed();
		guests = 0;
	}
}
