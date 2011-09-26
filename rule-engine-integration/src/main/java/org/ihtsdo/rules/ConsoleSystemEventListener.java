package org.ihtsdo.rules;

import org.drools.SystemEventListener;

public class ConsoleSystemEventListener implements SystemEventListener {

	
	public ConsoleSystemEventListener() {
		super();
	}
	
	public void debug(String arg0) {
		System.out.println("DEBUG: " + arg0);
	}

	@Override
	public void debug(String arg0, Object arg1) {
		System.out.println("DEBUG M: " + arg0);
		if (arg1 instanceof Exception) {
			Exception e = (Exception) arg1;
			System.out.println("DEBUG EX: " + e.getMessage());
		}
	}

	@Override
	public void exception(Throwable arg0) {
		if (arg0 instanceof RuntimeException) {
			RuntimeException r = (RuntimeException) arg0;
			System.out.println("EXCEPTION Runtime: " + arg0.getMessage());
			System.out.println("EXCEPTION Cause: " + r.getCause().getMessage());
		}
		System.out.println("EXCEPTION: " + arg0.getMessage());
	}

	@Override
	public void exception(String arg0, Throwable arg1) {
		System.out.println("EXCEPTION M: " + arg0);
		if (arg1 instanceof RuntimeException) {
			RuntimeException r = (RuntimeException) arg1;
			System.out.println("EXCEPTION Runtime: " + arg1.getMessage());
			System.out.println("EXCEPTION Cause: " + r.getCause().getMessage());
		}
		System.out.println("EXCEPTION: " + arg1.getMessage());
	}

	@Override
	public void info(String arg0) {
		System.out.println("INFO: " + arg0);
	}

	@Override
	public void info(String arg0, Object arg1) {
		System.out.println("INFO M: " + arg0);
		if (arg1 instanceof Exception) {
			Exception e = (Exception) arg1;
			System.out.println("INFO EX: " + e.getMessage());
		}
	}

	@Override
	public void warning(String arg0) {
		System.out.println("WARNING: " + arg0);
	}

	@Override
	public void warning(String arg0, Object arg1) {
		System.out.println("WARNING M: " + arg0);
		if (arg1 instanceof Exception) {
			Exception e = (Exception) arg1;
			System.out.println("WARNING EX: " + e.getMessage());
		}
	}

}
