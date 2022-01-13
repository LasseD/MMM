package mmm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import lejos.nxt.*;

public class AdjustablePowerOutput extends Thread {
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
	}
	
	// returns first when button is released.
	private boolean isPressed(Button b) {
		boolean pressed = false;

		int start = (int)System.currentTimeMillis();
		
		while(b.isDown() && start+100 > (int)System.currentTimeMillis()) {
			pressed = true;
		}
		return pressed;
	}
	
	private void handleButtons() throws IOException {		
		if(isPressed(Button.LEFT)) {
			speed -= 5;
			if(speed < -100)
				speed = -100;
			writeFile();
		}
		else if(isPressed(Button.RIGHT)) {
			speed += 5;
			if(speed > 100)
				speed = 100;
			writeFile();
		}		
	}
	
	public void run() {
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
			
			// Run forever:
			while(true) {
				LCD.drawInt(speed, 4, 2, lineNumber);
				Time.sleep(100);			
				handleButtons();
				updateMotor();
			}
		}
		catch(IOException e) {
			LCD.drawString("IOException", 0, 1);
			LCD.drawString(e.getMessage(), 0, 2);
		}
	}
	
	private void writeFile() throws IOException {
		FileOutputStream s = new FileOutputStream(file, false);
		s.write(speed+100);
		s.flush();
		s.close();
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
