package org.ihtsdo.tk.api;


public class Coordinate {
	private Precedence precedence;
	private PositionBI position;
	private NidSetBI   allowedStatusNids;
	private NidSetBI   isaTypeNids;
	
	public Coordinate(Precedence precedence, PositionBI position,
			NidSetBI allowedStatusNids, NidSetBI isaTypeNids) {
		super();
		this.precedence = precedence;
		this.position = position;
		this.allowedStatusNids = allowedStatusNids;
		this.isaTypeNids = isaTypeNids;
	}
	
	public PositionBI getPosition() {
		return position;
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

}
