package mmm;

import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.SensorPort;
import lejos.nxt.addon.RCXLightSensor;
import lejos.robotics.LampLightDetector;

public class FigureSensor {
	public static final int MEM_SIZE = 200;
	public static final int SENSOR_POLLING_SLEEP_MS = 50;
	public static final int SENSOR_SLEEP_MS = 150;
	public static final int TRIGGER_SIZE = 3; // Number of measurements where the sensor should see a high value for the seesMinifig to return true
	
	private LampLightDetector light;
	private int[] mem;
	private volatile boolean paused;
	private volatile int currentIdx;

	public FigureSensor(SensorPort port) {
		this(port, false);		
	}
	
	public FigureSensor(SensorPort port, boolean rxcSensor) {
		paused = true;
		light = rxcSensor ? new RCXLightSensor(port) : new LightSensor(port);
		mem = new int[MEM_SIZE];
		for(int i = 0; i < MEM_SIZE; i++) {
			mem[i] = -1;
		}
		
		// Start a thread to poll the sensor and update the intervals
		new Thread(){
			@Override
			public void run() {
				currentIdx = 0;
				
				while(true) {
					if(!paused) {
						Time.sleep(SENSOR_POLLING_SLEEP_MS);
						
						mem[currentIdx] = light.getNormalizedLightValue();
						if(currentIdx == MEM_SIZE-1)
							currentIdx = 0;
						else
							currentIdx++;
					}
					else {
						Time.sleep(SENSOR_SLEEP_MS);						
					}
				}
			}
		}.start();
	}
	
	private boolean seesMinifig() {
		int max = -1, min = 10000, cnt = 0;
		long sum = 0;
		
		for(int i = 0; i < MEM_SIZE; i++) {
			int v = mem[i];
			if(v < 0)
				continue; // Not yet set.
			
			if(v > max) {
				max = v;
			}
			if(v < min) {
				min = v;
			}
			sum += v;
			cnt++;
		}
		if(cnt == 0)
			return false; // Not enough info
		int avg = (int)(sum/cnt);
		
		LCD.drawInt(cnt, 3, 2, 0);
		LCD.drawInt(avg, 3, 2, 1);
		LCD.drawInt(min, 3, 2, 2);
		LCD.drawInt(max, 3, 2, 3);
		
		if(cnt < 10 || min > max-20) {
			return false; // Not enough info
		}

		// Check that the last TRIGGER_SIZE measurements are all OK:
		for(int i = 0; i < TRIGGER_SIZE; i++) {
			int idx = (i + currentIdx - TRIGGER_SIZE + MEM_SIZE) % MEM_SIZE;
			int v = mem[idx];
			if(v < 0)
				return false; // No reading.
			if(v < avg)
				return false;
		}

		return true;
	}
	
	public boolean seesMinifig(int timeoutMS) {	
		resume();
		while(timeoutMS > 0) { 
			Time.sleep(SENSOR_POLLING_SLEEP_MS);
			timeoutMS -= SENSOR_POLLING_SLEEP_MS;
			if(seesMinifig()) {
				return true;
			}
		}
		return false;
	}
	
	public void pause() {
		light.setFloodlight(false);
		paused = true;
	}
	
	public void resume() {
		light.setFloodlight(true);
		paused = false;
	}
	
	public void setFloodlight(boolean on) {
		light.setFloodlight(on);
	}
}
