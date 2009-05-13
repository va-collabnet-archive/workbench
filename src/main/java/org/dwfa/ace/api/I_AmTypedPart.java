package org.dwfa.ace.api;

public interface I_AmTypedPart extends I_AmPart {

	public int getTypeId();
	public void setTypeId(int type);
	
	public void convertIds(I_MapNativeToNative jarToDbNativeMap);
}
