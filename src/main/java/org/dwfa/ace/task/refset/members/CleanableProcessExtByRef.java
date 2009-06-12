package org.dwfa.ace.task.refset.members;

import org.dwfa.ace.api.I_ProcessExtByRef;

//composite interface.
public interface CleanableProcessExtByRef extends I_ProcessExtByRef {
	
	void clean() throws Exception;
	
}
