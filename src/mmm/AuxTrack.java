package mmm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import lejos.nxt.*;;

public class AuxTrack  implements ButtonListener {
	public static final String FILE_NAME = "TRACK";

	private File file;
	private boolean in;
	private Track track;
	
	public static boolean shouldStartTrack(MotorPort port) {
		NXTRegulatedMotor motor = new NXTRegulatedMotor(port);
		int pos = motor.getTachoCount();
		motor.forward();
		motor.setSpeed(100);
		Time.sleep(100);
		motor.flt();
		return pos != motor.getTachoCount(); // There is a NXT motor attached - treat it like a track.
	}
	
	public AuxTrack(MotorPort motorPort) {
		file = new File(FILE_NAME);
		track = new Track(motorPort);

		try {
			// Read file with power level:
			if(!file.exists()) {
				file.createNewFile();
				in = true;
				writeFile();
			}
			else {
				FileInputStream s = new FileInputStream(file);
				in = s.read()==1;
				s.close();
			}
		}
		catch(IOException e) {
			LCD.drawString("IOException", 0, 1);
			LCD.drawString(e.getMessage(), 0, 2);
		}
		
		updateMotor();
		Button.RIGHT.addButtonListener(this);
	}
	
	@Override
	public void buttonReleased(Button b) {
		// NOP
	}
	
	@Override
	public void buttonPressed(Button b) {
		in = !in;
		writeFile();
		updateMotor();
	}
	
	private void writeFile() {
		try {
			FileOutputStream s = new FileOutputStream(file, false);
			s.write(in ? 1 : 0);
			s.flush();
			s.close();
		}
		catch(IOException e) {
			LCD.drawString("IOException W", 0, 1);
			LCD.drawString(e.getMessage(), 0, 2);
		}
	}
		
	private void updateMotor() {
		if(in) {
			track.in();	
		}
		else {
			track.out();
		}
	}
}
