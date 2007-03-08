package org.dwfa.vodb.types;

import org.dwfa.vodb.jar.I_MapNativeToNative;

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
}
