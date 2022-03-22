import mmm.*;
import lejos.nxt.*;

/**
 * MMM Module Hounted House is a ride from the video game Theme Park.
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
	public static final int LIFT_DIST = 500;
	
	public static void main(String[] args) {
		AuxController.setupAuxController(MotorPort.C, SensorPort.S3);
		KillSwitch.enable();

		lift.setSpeed(LIFT_SPEED);
		reset();

		while(true) {
			track.out();
			Time.sleep(15000);

			track.in();
			if(sensor.seesMinifig(60000)) {
				Time.sleep(300); // Ensure figure is fully in
				sensor.pause();
				lift.rotate(LIFT_DIST/10);
				track.out();
				lift.rotate(9*LIFT_DIST/10);
				Time.sleep(2000);
				lift.rotate(-LIFT_DIST+20);
				reset();
			}
		}
	}
	
	private static void reset() {
		sensor.pause();

		lift.suspendRegulation();

		liftResetter.setPower(30);
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
