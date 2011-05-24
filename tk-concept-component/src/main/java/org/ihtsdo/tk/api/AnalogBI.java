package org.ihtsdo.tk.api;

import java.beans.PropertyVetoException;

public interface AnalogBI {

    void setNid(int nid) throws PropertyVetoException;
    void setStatusNid(int nid) throws PropertyVetoException;
    void setAuthorNid(int nid) throws PropertyVetoException;
    void setPathNid(int nid) throws PropertyVetoException;
    void setTime(long time) throws PropertyVetoException;

}
