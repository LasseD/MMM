package mmm;

import lejos.nxt.*;

public class AuxController {
	public static void setupAuxController(MotorPort motorPort, SensorPort sensorPort) {
		if(Shop.shouldStartShop(sensorPort)) {
			Shop shop = new Shop(sensorPort, motorPort);
			shop.start();
		}
		else {
			AdjustablePowerOutput aux = new AdjustablePowerOutput(motorPort, 3);
			aux.start();
		}
	}
}
