package org.dwfa.ace.api;

import java.io.IOException;
import java.util.Date;

import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;


public class TimePathId {
	int time;
	int pathId;
	public int getPathId() {
		return pathId;
	}
	public int getTime() {
		return time;
	}
	public TimePathId(int time, int pathId) {
		super();
		this.time = time;
		this.pathId = pathId;
	}
	@Override
	public boolean equals(Object obj) {
		TimePathId another = (TimePathId) obj;
		return time == another.time && pathId == another.pathId;
	}
	@Override
	public int hashCode() {
		return time;
	}
	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		pathId  = jarToDbNativeMap.get(pathId);
	}
	
	public String toString() {
		long thickTime = LocalVersionedTerminology.get().convertToThickVersion(time);
		try {
			I_GetConceptData path = LocalVersionedTerminology.get().getConcept(pathId);
			return new Date(thickTime) + " on path " + path;
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return new Date(thickTime) + " on path " + pathId;
	}
}
