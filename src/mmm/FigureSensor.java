package mmm;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.SensorPort;

public class FigureSensor {
	private LightSensor light;
	private int sensorValFree, sensorValFigure; // Light sensor values after initial calibration.

	public FigureSensor(SensorPort port) {
		light = new LightSensor(port);
	}
	
	public void calibrate() {
		this.calibrate(new ButtonAdjustable() {
			@Override
			public void onRight() {
			}			
			@Override
			public void onLeft() {
			}
		});
	}
	
	public void calibrate(ButtonAdjustable adjuster) {
		light.setFloodlight(true);
		Time.sleep(100);
		sensorValFree = light.getLightValue();

		for(int i = 0; i < 5; i++) {
			light.setFloodlight(false);
			Time.sleep(100);
			light.setFloodlight(true);
			Time.sleep(100);
		}
		
		while(true) {
			if(Button.ENTER.isDown()) {
				LCD.drawInt(light.getLightValue(), 3, 1);
				break;
			}
			else if(Button.RIGHT.isDown()) {
				adjuster.onRight();
			}
			else if(Button.LEFT.isDown()) {
				adjuster.onLeft();
			}
			Time.sleep(200);
		}
		while(Button.ENTER.isDown()) {
			// Release the button!
		}
		sensorValFigure = light.getLightValue();

		LCD.clear();		
		LCD.drawInt(sensorValFree, 3, 2);
		LCD.drawInt(sensorValFigure, 3, 3);
		light.setFloodlight(false);
	}
	
	public boolean seesMinifig(int timeoutMS) {	
		light.setFloodlight(true);
		while(timeoutMS > 0 && !Button.ENTER.isDown()) { 
			Time.sleep(50);
			timeoutMS -= 50;
			int v = light.getLightValue();
			LCD.drawInt(v, 3, 1);

			int diffFig = Math.abs(v-sensorValFigure);
			int diffFree = Math.abs(v-sensorValFree);
			if(diffFree > diffFig) {
				light.setFloodlight(false);
				return true;
			}
		}
		light.setFloodlight(false);
		return false;
	}
}
