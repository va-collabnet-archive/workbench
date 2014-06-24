package org.dwfa.ace.api.ebr;

import java.beans.PropertyVetoException;
import org.ihtsdo.tk.api.refex.type_nid_long.RefexNidLongAnalogBI;

public interface I_ExtendByRefPartCidLong<A extends RefexNidLongAnalogBI<A>> 
	extends I_ExtendByRefPartCid<A>, I_ExtendByRefPartLong<A> {

    public long getLongValue();

    public void setLongValue(long longValue) throws PropertyVetoException;

}