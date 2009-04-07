package org.dwfa.ace.timer;

import java.util.Date;
import java.util.TimerTask;

public class UpdateAlertsTimer {
	
	static java.util.Timer timer = new java.util.Timer("updateDataAlertsTimer");

	public static void schedule(TimerTask arg0, Date arg1, long arg2) {
		timer.schedule(arg0, arg1, arg2);
	}

	public static void schedule(TimerTask arg0, Date arg1) {
		timer.schedule(arg0, arg1);
	}

	public static void schedule(TimerTask arg0, long arg1, long arg2) {
		timer.schedule(arg0, arg1, arg2);
	}

	public static void schedule(TimerTask arg0, long arg1) {
		timer.schedule(arg0, arg1);
	}

	public static void scheduleAtFixedRate(TimerTask arg0, Date arg1, long arg2) {
		timer.scheduleAtFixedRate(arg0, arg1, arg2);
	}

	public static void scheduleAtFixedRate(TimerTask arg0, long arg1, long arg2) {
		timer.scheduleAtFixedRate(arg0, arg1, arg2);
	}


}
