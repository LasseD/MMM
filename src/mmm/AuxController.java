package mmm;

import lejos.nxt.*;

public class AuxController {
	public static void setupAuxController(MotorPort motorPort, SensorPort sensorPort) {
		if(Shop.shouldStartShop(sensorPort)) {
			Shop shop = new Shop(sensorPort, motorPort);
			shop.start();
		}
		else if(AuxTrack.shouldStartTrack(motorPort)) {
			new AuxTrack(motorPort);
		}
		else {
			new AdjustablePowerOutput(motorPort, 3);
		}
	}
}
