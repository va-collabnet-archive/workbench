package org.ihtsdo.rf2.refset.factory;

public class SctidUuid{
	private String sctid;
	private String uuid;

	public SctidUuid() {
		super();
	}

	public SctidUuid(String sctid, String uuid) {
		super();
		this.sctid = sctid;
		this.uuid = uuid;
	}

	public String getSctid() {
		return sctid;
	}

	public void setSctid(String sctid) {
		this.sctid = sctid;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

}
