package mmm;

import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.SensorPort;
import lejos.nxt.addon.RCXLightSensor;
import lejos.robotics.LampLightDetector;

public class FigureSensor {
	public static final int INTERVAL_LENGTH_MS = 4 * 1000;
	public static final int NUMBER_OF_INTERVALS = 36;
	public static final int SENSOR_POLLING_SLEEP_MS = 50;
	public static final int SLEEP_POLLING_SLEEP_MS = 150;
	
	private LampLightDetector light;
	private Interval[] intervals;
	private volatile boolean paused;

	public FigureSensor(SensorPort port) {
		this(port, false);		
	}
	
	public FigureSensor(SensorPort port, boolean rxcSensor) {
		paused = true;
		light = rxcSensor ? new RCXLightSensor(port) : new LightSensor(port);
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
						
						int lightValue = light.getNormalizedLightValue();
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

			int v = light.getNormalizedLightValue();
			LCD.drawInt(v, 3, 1);

			int max = -1, min = 100;
			int validIntervals = 0;
			boolean largerThanAnInterval = false;
			for(int i = 0; i < NUMBER_OF_INTERVALS; i++) {
				Interval interval = intervals[i];
				if(!interval.isValid())
					continue;
				validIntervals++;
				if(v > interval.max + 1) {
					largerThanAnInterval = true;
				}
				if(interval.max > max) {
					max = interval.max;
				}
				if(interval.min < min) {
					min = interval.min;
				}
			}
			if(validIntervals > 3 && largerThanAnInterval) {
				return true; // There has to be enough intervals and light value must be higher than one of them.
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
			return polls > 5; // Poll at least some times before interval is valid.
		}
	}
}
