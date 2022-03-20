package mmm;

import lejos.nxt.*;

public class KillSwitch {
	private static boolean enabled = false;
	
	public static void enable() {
		if(enabled) {
			return; // Already enabled.
		}
		while(Button.ENTER.isDown()) {
			; // Ensure no immediate return.
		}
		enabled = true;
		
		Button.ENTER.addButtonListener(new ButtonListener() {
			@Override
			public void buttonPressed(Button b) {
				System.exit(0);
			}

			@Override
			public void buttonReleased(Button b) {
				System.exit(0);
			}			
		});
	}
}
