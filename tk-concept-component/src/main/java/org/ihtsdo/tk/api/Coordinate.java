package org.ihtsdo.tk.api;


public class Coordinate {
	private Precedence precedence;
	private PositionSetBI positionSet;
	private NidSetBI   allowedStatusNids;
	private NidSetBI   isaTypeNids;
	private ContradictionManagerBI contradictionManager;
	
	public Coordinate(Precedence precedence, PositionSetBI positionSet,
			NidSetBI allowedStatusNids, NidSetBI isaTypeNids, 
			ContradictionManagerBI contradictionManager) {
		super();
		this.precedence = precedence;
		this.positionSet = positionSet;
		this.allowedStatusNids = allowedStatusNids;
		this.isaTypeNids = isaTypeNids;
		this.contradictionManager = contradictionManager;
	}
	
	public PositionSetBI getPositionSet() {
		return positionSet;
	}

	public NidSetBI getAllowedStatusNids() {
		return allowedStatusNids;
	}

	public Precedence getPrecedence() {
		return precedence;
	}
	
	public NidSetBI getIsaTypeNids() {
		return isaTypeNids;
	}

	public ContradictionManagerBI getContradictionManager() {
		return contradictionManager;
	}

}
