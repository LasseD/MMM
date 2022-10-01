import mmm.*;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTMotor;
import lejos.nxt.SensorPort;

/**
 * MMM Module Coconut Shy, which is a shop in the video game Theme Park.
 * Building instructions for MMM modules are on brickhub.org
 * @author Lasse Deleuran
 */
public class CoconutShy {
	private static final Track track = new Track(MotorPort.A);
	private static final NXTMotor gun = new NXTMotor(MotorPort.B);	
	private static final Clapper clapper = new Clapper(SensorPort.S1);

	public static final int GUN_TURN = 8;
	public static final int TRIGGER_DISTANCE = 3;
	
	public static void main(String[] args) {
		AuxController.setupAuxController(MotorPort.C, SensorPort.S2);
		KillSwitch.enable();
		
		track.out();
		
		resetGun(); // Ensure gun is ready to shoot
		int shots = 0;
		
		while(true) {
			Time.sleep(25);

			if(clapper.hearsClap()) {
				shots++;
				shoot();			
			}

			if(shots >= 3) {
				reset();
				shots = 0;
			}
		}
	}
	
	private static void reset() {
		track.stop();
		track.boost();
		track.rotate(-150);
		track.rotate(180); // Ensure the lifters are down
		track.resetSpeed();
		track.out();
	}
	
	private static void shoot() {
		gun.resetTachoCount();
		gun.forward();
		while(gun.getTachoCount() < GUN_TURN)
			;
		gun.flt();
		resetGun();
	}
	
	private static void resetGun() {
		gun.setPower(10);
		gun.backward();
		Time.sleep(300);
		gun.flt();
		gun.setPower(60);
	}
}
