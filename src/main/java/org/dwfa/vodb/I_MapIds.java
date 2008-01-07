package org.dwfa.vodb;

import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.api.I_Path;

public interface I_MapIds {
    public int getIntId(Collection<UUID> uids, I_Path idPath, int version) throws Exception;
    
    public int getIntId(UUID uid, I_Path idPath, int version) throws Exception;
    
    public void flushIdBuffer() throws Exception;
    
}