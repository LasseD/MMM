package mmm;

import lejos.nxt.MotorPort;
import lejos.nxt.NXTRegulatedMotor;

public class Track {
	public static final int SPEED_TRACK = 100;	
	public static final int SPEED_TRACK_BOOST = 250;	
	
	private NXTRegulatedMotor track;
	private boolean forwardIsIn;
	
	public Track(MotorPort port) {
		this(port, false);
	}

	public Track(MotorPort port, boolean forwardIsIn) {
		track = new NXTRegulatedMotor(port);
		track.setSpeed(SPEED_TRACK);
		this.forwardIsIn = forwardIsIn;
	}
	
	public void boost() {
		track.setSpeed(SPEED_TRACK_BOOST);
	}

	public void boost(int angle) {
		boost();
		track.rotate(angle);
		resetSpeed();
		if(angle > 0)
			track.forward();
		else
			track.backward();
	}

	public void resetSpeed() {
		track.setSpeed(SPEED_TRACK);
	}
	
	public void stop() {
		track.stop();
	}
	
	public void rotate(int angle) {
		track.rotate(angle);
	}

	public void in() {
		if(forwardIsIn) {
			track.forward();
		}
		else {
			track.backward();
		}
	}

	public void out() {
		if(forwardIsIn) {
			track.backward();
		}
		else {
			track.forward();
		}
	}
}
