import mmm.*;
import lejos.nxt.*;

/**
 * MMM Module Haunted House is a ride from the video game Theme Park.
 * Building instructions for MMM modules are on brickhub.org
 * 
 * @author Lasse Deleuran
 */
public class HauntedHouse {
	private static final Track track = new Track(MotorPort.A, true);
	private static final NXTRegulatedMotor lift = new NXTRegulatedMotor(MotorPort.B);
	private static final NXTMotor liftResetter = new NXTMotor(MotorPort.B);
	private static final FigureSensor sensor = new FigureSensor(SensorPort.S1);

	public static final int LIFT_SPEED = 200;
	public static final int LIFT_DIST = 800;
	
	public static void main(String[] args) {
		AuxController.setupAuxController(MotorPort.C, SensorPort.S3);
		KillSwitch.enable();

		reset();

		track.out();
		sensor.resume();
		Time.sleep(5000); // Calibrate sensor

		while(true) {
			track.boost(200);
			if(sensor.seesMinifig(60000)) {
				track.boost(450); // Get figure completely inside
				
				sensor.pause();

				lift.rotate(LIFT_DIST/10);
				track.boost(-600);

				lift.rotate(9*LIFT_DIST/10);
				
				ride();
				
				reset();					
				sensor.resume();

				track.boost(-200);

				// Cool down:
				Time.sleep(15000);
			}
		}
	}
	
	private static void ride() {
		if(Math.random() < 0.1) {
			ride2();
		}
		else {
			ride1();
		}
	}
	
	private static void ride1() {
		Time.sleep(3000);
		lift.rotate(-LIFT_DIST+20);		
	}
	
	private static void ride2() {
		final int D5 = 5*LIFT_DIST/10;
		
		Time.sleep(3000);

		lift.rotate(-D5);
		Time.sleep(500);
		flicker();
		lift.rotate(D5);
		lift.setSpeed(800);
		
		lift.rotate(-LIFT_DIST+80);
	}
	
	private static final int[] FLICKER_WAIT = {
		30, 150, 
		20, 50, 
		40, 200,
		30, 50,
		20, 100, 
		40, 150,
		30, 50,
		20, 150, 
		50, 10,
		};
	private static void flicker() {
		for(int i = 0; i < FLICKER_WAIT.length; i+= 2) {
			sensor.setFloodlight(true);
			Time.sleep(FLICKER_WAIT[i]);
			sensor.setFloodlight(false);
			Time.sleep(FLICKER_WAIT[i+1]);
		}
	}
	
	private static void reset() {
		sensor.pause();

		lift.setSpeed(LIFT_SPEED);
		lift.suspendRegulation();

		liftResetter.setPower(40);
		liftResetter.backward();
		while(true) {
			int from = liftResetter.getTachoCount();
			Time.sleep(150);
			int to = liftResetter.getTachoCount();
			if(from - to < 2) {
				break;
			}
		}
		liftResetter.flt();
		
		lift.stop();
		lift.resetTachoCount();
		
		sensor.resume();
	}
}
