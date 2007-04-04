package org.dwfa.ace.api;

import java.io.IOException;

public interface I_MapNativeToNative {

	public void add(int jarId, int dbId) throws IOException;

	public int get(int jarId);

}