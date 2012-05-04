package org.ihtsdo.tk.api;

public interface AnalogGeneratorBI <T extends AnalogBI> {
        
        T makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid);
}
