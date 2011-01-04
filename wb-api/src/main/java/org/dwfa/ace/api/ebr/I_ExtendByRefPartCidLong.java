package org.dwfa.ace.api.ebr;

import org.ihtsdo.tk.api.refex.type_cnid_long.RefexCnidLongAnalogBI;

public interface I_ExtendByRefPartCidLong<A extends RefexCnidLongAnalogBI<A>> 
	extends I_ExtendByRefPartCid<A>, I_ExtendByRefPartLong<A> {

    public long getLongValue();

    public void setLongValue(long longValue);

}