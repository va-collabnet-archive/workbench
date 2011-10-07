package org.ihtsdo.tk.api;

import java.beans.PropertyVetoException;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;

public interface AnalogBI {

    void setNid(int nid) throws PropertyVetoException;
    void setStatusNid(int nid) throws PropertyVetoException;
    void setAuthorNid(int nid) throws PropertyVetoException;
    void setPathNid(int nid) throws PropertyVetoException;
    void setTime(long time) throws PropertyVetoException;
    public boolean addLongId(Long longId, int authorityNid, int statusNid, EditCoordinate ec,
                            long time);

}
