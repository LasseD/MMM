package mmm;

public class Time {
	public static void sleep(int time) {
		try {
			Thread.sleep(time);
		}
		catch(InterruptedException e) {
			// Can't happen on NXT.
		}
	}
}
