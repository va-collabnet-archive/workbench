package org.dwfa.ace.utypes.cs;

import java.io.IOException;

import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.ace.utypes.UniversalAceExtByRefBean;
import org.dwfa.ace.utypes.UniversalAcePath;
import org.dwfa.ace.utypes.UniversalIdList;

public interface I_ProcessUniversalChangeSets {
	
	public void processUniversalAceBean(UniversalAceBean bean, long commitTime) throws IOException;
	
	public void processIdList(UniversalIdList list, long commitTime) throws IOException;
	
	public void processAcePath(UniversalAcePath path, long commitTime) throws IOException;
	
	public void processAceEbr(UniversalAceExtByRefBean bean, long time) throws IOException;
	
}
