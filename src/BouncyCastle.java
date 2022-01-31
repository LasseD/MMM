import mmm.Time;
import lejos.nxt.*;
import lejos.nxt.addon.*;;

/**
 * MMM Module "Bouncy Castle" which is a ride in the video game Theme Park.
 * Building instructions for MMM modules are on brickhub.org
 * @author Lasse Deleuran
 */
public class BouncyCastle {
	public static final BasicMotor motorTrack = new NXTMotor(MotorPort.B);
	public static final BasicMotor motorMatt = new NXTMotor(MotorPort.C);
	public static final NXTRegulatedMotor motorJump = new NXTRegulatedMotor(MotorPort.A);
	public static final BasicMotor motorJumpPowerRegulated = new NXTMotor(MotorPort.A);
	public static final RCXLightSensor sensorTrack = new RCXLightSensor(SensorPort.S4);
	public static final int CAPACITY = 3;
	
	private static int guests = 0;
	private static int valFree, valFig;
	
	public static void main(String[] args) {
		init();
		resetJump();
		
		while(!Button.ENTER.isDown()) {
			motorTrack.setPower(55 + (int)(10*Math.sin((int)System.currentTimeMillis()/900)));
			if(seesMinifig(210)) {
				if(invite()) {
					sensorTrack.setFloodlight(false);

					// Adjust matt a bit to clear the inner wall:
					motorMatt.forward();
					Time.sleep(50);
					motorMatt.flt();

					bounce();
					bounce();
					bounce();
					bounce();
					leave();
					Time.sleep(1000); // Cool down
					sensorTrack.setFloodlight(true);
				}
			}
		}
	}

	public static void init() {
		// Calibrate:
		sensorTrack.setFloodlight(true);
		Time.sleep(200);
		valFree = sensorTrack.getLightValue();

		for(int i = 0; i < 5; i++) {
			sensorTrack.setFloodlight(false);
			Time.sleep(100);
			sensorTrack.setFloodlight(true);
			Time.sleep(100);
		}
		
		while(!Button.ENTER.isDown()) {
			LCD.drawInt(sensorTrack.getLightValue(), 3, 1);
		}
		valFig = sensorTrack.getLightValue();

		LCD.clear();
		LCD.drawInt(valFree, 3, 2);
		LCD.drawInt(valFig, 3, 3);

		motorJump.flt();
		motorJump.setSpeed(110);
		motorJump.setAcceleration(4500);

		motorTrack.setPower(50);
		motorTrack.forward();
	}
	
	public static boolean seesMinifig(int timeout) {	
		while(timeout > 0) { 
			Time.sleep(50);
			timeout -= 50;
			int v = sensorTrack.getLightValue();
			LCD.drawInt(v, 3, 1);

			int diffFig = Math.abs(v-valFig);
			int diffFree = Math.abs(v-valFree);
			if(diffFree > diffFig) {
				return true;
			}
		}
		return false;
	}

	public static boolean invite() {
		if(guests >= CAPACITY) {
			motorTrack.forward();
			motorMatt.flt();
			return true;
		}
		
		guests++;
		Sound.beepSequenceUp();
		
		// Go to gate:
		motorTrack.setPower(55); 
		motorTrack.backward();
		
		// Clear the sensor. If another minifig makes it, then that is alright too:
		Time.sleep(1000);

		if(seesMinifig(700)) { // Magic number to keep matt still while walking to it
			return invite();
		}
		
		// Go onto matt:
		motorMatt.setPower(80);
		motorMatt.backward();
		if(seesMinifig(3000)) {
			return invite();
		}
		
		motorTrack.forward();
		motorMatt.flt();		
		return guests >= CAPACITY;
	}

	public static void bounce() {
		motorJump.resetTachoCount();
		motorJump.rotateTo(75);
		motorJump.rotateTo(0);
	}

	public static void resetJump() {
		motorJump.flt();
		motorJump.suspendRegulation();
		
		motorJumpPowerRegulated.setPower(10);
		motorJumpPowerRegulated.backward();
		Time.sleep(1000);
		motorJumpPowerRegulated.flt();
		motorJump.resetTachoCount();		
	}

	public static void leave() {
		motorJump.rotateTo(37);
		motorTrack.setPower(70);
		motorTrack.forward();

		motorMatt.setPower(70);
		motorMatt.forward();

		Sound.beepSequence();
		Time.sleep(6500); // vacate castle
		
		motorMatt.flt();
		resetJump();
		
		motorTrack.setPower(50);
		guests = 0;
	}
}
