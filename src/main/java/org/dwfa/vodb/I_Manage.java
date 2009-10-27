package org.dwfa.vodb;

import java.util.Set;

import org.dwfa.tapi.TerminologyException;

/**
 * Generic template for persistent object management.
 * 
 * This is an alternative to interfaces which extend {@link I_StoreInBdb}
 * 
 * Defines methods for retrieving and persisting a specific type of object 
 * without being logically bound to a specific database store.
 * 
 */
public interface I_Manage<T> {

    public T get(int nid) throws TerminologyException;
    
    public Set<T> getAll() throws TerminologyException;
    
    public boolean exists(int nid) throws TerminologyException;
    
    public void write(T object) throws TerminologyException;
    
}
