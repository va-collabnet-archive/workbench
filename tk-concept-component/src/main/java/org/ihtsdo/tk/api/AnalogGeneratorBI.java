package org.ihtsdo.tk.api;

public interface AnalogGeneratorBI <T extends AnalogBI> {

	T makeAnalog(int statusNid, int authorNid, int pathNid, long time);
}
