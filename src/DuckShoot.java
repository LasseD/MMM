import mmm.*;
import lejos.nxt.*;

/**
 * MMM Module Duck Shoot is a shop from the video game Theme Park.
 * Building instructions for MMM modules are on brickhub.org
 * 
 * @author Lasse Deleuran
 */
public class DuckShoot {
	private static final Track track = new Track(MotorPort.A);
	private static final NXTMotor gun = new NXTMotor(MotorPort.B);
	private static final Clapper clapper = new Clapper(SensorPort.S1);

	public static final int GUN_TURN = 12, COOLDOWN_MS = 600;
	
	public static void main(String[] args) {
		AuxController.setupAuxController(MotorPort.C, SensorPort.S3);
		KillSwitch.enable();
		
		track.out();
		
		resetGun(); // Ensure gun is loaded
		
		while(true) {
			Time.sleep(25);
			if(clapper.hearsClap()) {
				shoot();
			}
		}
	}
	
	private static void shoot() {
		track.slow();
		gun.forward();
		while(gun.getTachoCount() < GUN_TURN)
			;
		gun.flt();
		resetGun();
		Time.sleep(COOLDOWN_MS); // Small cooldown.
		track.resetSpeed();
	}
	
	private static void resetGun() {
		gun.setPower(10);
		gun.backward();
		Time.sleep(300);
		gun.flt();
		gun.setPower(100);
		gun.resetTachoCount();
	}	
}
