package org.dwfa.ace.api;

import org.ihtsdo.tk.api.Precedence;

/**
 * Left as a transitional compatibility class. Eventually we need 
 * to migrate to org.ihtsdo.tk.api.Precedence in all api calls.  
 * @author kec
 *
 */
public enum PRECEDENCE {
    
    TIME(Precedence.TIME),
    PATH(Precedence.PATH);
    
    private Precedence tkp;
    
    private PRECEDENCE(Precedence tkp) {
    	this.tkp = tkp;
    }

    public String getDescription() {
        return tkp.getDescription();
    }

    @Override
    public String toString() {
        return tkp.toString();
    }
    
    public Precedence getTkPrecedence() {
    	return tkp;
    }

    public static PRECEDENCE get(Precedence p) {
    	switch (p) {
		case PATH:
			return PATH;
		case TIME:
			return TIME;
		default:
			throw new RuntimeException("can't handle " + p);
		}
    }
}
