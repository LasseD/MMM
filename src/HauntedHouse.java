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
	private static final LightSensor light = new LightSensor(SensorPort.S2);

	public static final int LIFT_SPEED = 200;
	public static final int LIFT_DIST = 1380;
	public static final int LIFT_CLEAR = 120;
	
	public static void main(String[] args) {
		AuxController.setupAuxController(MotorPort.C, SensorPort.S3);
		KillSwitch.enable();
		light.setFloodlight(false);

		reset();

		track.out();
		sensor.resume();
		Time.sleep(5000); // Calibrate sensor

		while(true) {
			track.boost(200);
			if(sensor.seesMinifig(60000)) {
				track.boost(450); // Get figure completely inside
				
				sensor.pause();

				lift.rotate(LIFT_CLEAR);
				track.boost(-900); // Ensure alone.

				lift.rotate(LIFT_DIST-LIFT_CLEAR);
				
				flicker();
				lift.rotate(-LIFT_DIST+20*5/3);		
				
				reset();					
				sensor.resume();

				track.boost(-200);

				// Cool down:
				Time.sleep(15000);
			}
		}
	}
	
	private static final int[] FLICKER_WAIT = {
		30, 30,
		60, 60,
		90, 90,
		120, 120,
		150, 150,
		180, 180,
		2000, 50
		};
	private static void flicker() {
		for(int i = 0; i < FLICKER_WAIT.length; i+= 2) {
			light.setFloodlight(true);
			Time.sleep(FLICKER_WAIT[i]);
			light.setFloodlight(false);
			Time.sleep(FLICKER_WAIT[i+1]);
		}
	}
	
	private static void reset() {
		sensor.pause();

		lift.setSpeed(LIFT_SPEED);
		lift.suspendRegulation();

		liftResetter.setPower(55);
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
		
		lift.flt();
		lift.resetTachoCount();
		
		sensor.resume();
	}
}
