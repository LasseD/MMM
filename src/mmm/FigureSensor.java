package mmm;

import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.SensorPort;

public class FigureSensor {
	public static final int INTERVAL_LENGTH_MS = 15 * 1000;
	public static final int NUMBER_OF_INTERVALS = 20;
	public static final int SENSOR_POLLING_SLEEP_MS = 50;
	public static final int SLEEP_POLLING_SLEEP_MS = 100;
	
	private LightSensor light;
	private Interval[] intervals;
	private volatile boolean paused;

	public FigureSensor(SensorPort port) {
		paused = true;
		light = new LightSensor(port);
		intervals = new Interval[NUMBER_OF_INTERVALS];
		for(int i = 0; i < NUMBER_OF_INTERVALS; i++) {
			intervals[i] = new Interval();
		}
		
		// Start a thread to poll the sensor and update the intervals
		new Thread(){
			@Override
			public void run() {
				int time = 0;
				int idx = 0;
				
				while(true) {
					if(!paused) {
						Time.sleep(SENSOR_POLLING_SLEEP_MS);
						time += SENSOR_POLLING_SLEEP_MS;
						if(time > INTERVAL_LENGTH_MS) {
							idx++;
							if(idx >= NUMBER_OF_INTERVALS)
								idx = 0;
							time = 0;
						}
						
						int lightValue = light.getLightValue();
						Interval interval = intervals[idx];
						if(time == 0) {
							interval.reset();
						}
						interval.poll(lightValue);
					}
					else {
						Time.sleep(SLEEP_POLLING_SLEEP_MS);						
					}
				}
			}
		}.start();
	}
	
	public boolean seesMinifig(int timeoutMS) {	
		resume();
		while(timeoutMS > 0) { 
			Time.sleep(SENSOR_POLLING_SLEEP_MS);
			timeoutMS -= SENSOR_POLLING_SLEEP_MS;

			int v = light.getLightValue();
			LCD.drawInt(v, 3, 1);

			int largest = -1;
			for(int i = 0; i < NUMBER_OF_INTERVALS; i++) {
				Interval interval = intervals[i];
				if(!interval.isValid())
					continue;
				if(v > interval.max) {
					return true; // Larger than a full interval!
				}
				if(interval.max > largest) {
					largest = interval.max;
				}
			}
			if(v == largest) {
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
	
	private static class Interval {
		public int min, max, polls;
		
		public Interval() {
			reset();
		}
		
		public void reset() {
			min = max = -1;
			polls = 0;			
		}
		
		public void poll(int i) {
			polls++;
			if(min == -1) {
				min = max = i;
			}
			else if(i < min) {
				min = i;
			}
			else if(i > max) {
				max = i;
			}
		}
		
		public boolean isValid() {
			return polls > 5 && min != max; // Poll at least some times before interval is valid.
		}
	}
}
