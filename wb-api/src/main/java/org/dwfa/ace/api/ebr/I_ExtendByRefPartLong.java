package org.dwfa.ace.api.ebr;

import org.ihtsdo.tk.api.refex.type_long.RefexLongAnalogBI;

public interface I_ExtendByRefPartLong<A extends RefexLongAnalogBI<A>> 
	extends I_ExtendByRefPart<A> {

    public long getLongValue();

    public void setLongValue(long value);

}
