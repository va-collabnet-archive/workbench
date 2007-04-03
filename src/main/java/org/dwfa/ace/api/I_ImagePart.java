package org.dwfa.ace.api;

import org.dwfa.vodb.jar.I_MapNativeToNative;
import org.dwfa.vodb.types.ThinImagePart;

public interface I_ImagePart {

	public abstract int getPathId();

	public abstract int getStatusId();

	public abstract int getVersion();

	public abstract void setPathId(int pathId);

	public abstract void setStatusId(int status);

	public abstract void setVersion(int version);

	public abstract String getTextDescription();

	public abstract void setTextDescription(String name);

	public abstract int getTypeId();

	public abstract void setTypeId(int type);

	public abstract boolean hasNewData(ThinImagePart another);

	public abstract void convertIds(I_MapNativeToNative jarToDbNativeMap);

}