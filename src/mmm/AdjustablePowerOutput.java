package mmm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import lejos.nxt.*;

public class AdjustablePowerOutput implements ButtonListener {
	public static final String FILE_NAME = "POWER";

	private int lineNumber;
	private File file;
	private int speed; // -100 to 100
	private BasicMotor motor;

	public AdjustablePowerOutput(MotorPort motorPort) {
		this(motorPort, 0);
	}
	
	public AdjustablePowerOutput(MotorPort motorPort, int lineNumber) {
		file = new File(FILE_NAME);
		motor = new NXTMotor(motorPort);
		this.lineNumber = lineNumber;

		try {
			// Read file with power level:
			if(!file.exists()) {
				file.createNewFile();
				speed = 100;
				writeFile();
			}
			else {
				FileInputStream s = new FileInputStream(file);
				speed = s.read()-100;
				s.close();
			}
		}
		catch(IOException e) {
			LCD.drawString("IOException", 0, 1);
			LCD.drawString(e.getMessage(), 0, 2);
		}
		
		Button.LEFT.addButtonListener(this);
		Button.RIGHT.addButtonListener(this);
		updateMotor();
	}
	
	@Override
	public void buttonReleased(Button b) {
		// NOP
	}
	
	@Override
	public void buttonPressed(Button b) {
		if(b.getId() == Button.LEFT.getId()) {
			speed -= 5;
			if(speed < -100)
				speed = -100;
			writeFile();
		}
		else if(b.getId() == Button.RIGHT.getId()) {
			speed += 5;
			if(speed > 100)
				speed = 100;
			writeFile();
		}		
		LCD.drawInt(speed, 4, 2, lineNumber);
		updateMotor();
	}
	
	private void writeFile() {
		try {
			FileOutputStream s = new FileOutputStream(file, false);
			s.write(speed+100);
			s.flush();
			s.close();
		}
		catch(IOException e) {
			LCD.drawString("IOException W", 0, 1);
			LCD.drawString(e.getMessage(), 0, 2);
		}
	}
		
	private void updateMotor() {
		if(speed == 0)
			motor.flt();
		else if(speed > 0) {
			motor.setPower(speed);
			motor.forward();								
		}
		else {
			motor.setPower(-speed);
			motor.backward();								
		}
	}
}
