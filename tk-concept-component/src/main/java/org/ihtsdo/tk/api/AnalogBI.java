package org.ihtsdo.tk.api;

import java.beans.PropertyVetoException;

public interface AnalogBI {

    public void setStatusNid(int nid) throws PropertyVetoException;
    public void setAuthorNid(int nid) throws PropertyVetoException;
    public void setPathNid(int nid) throws PropertyVetoException;
    public void setTime(long time) throws PropertyVetoException;

}
