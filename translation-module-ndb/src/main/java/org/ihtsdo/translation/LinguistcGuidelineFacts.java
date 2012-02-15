package org.ihtsdo.translation;

import java.util.UUID;

public class LinguistcGuidelineFacts {
	private String sourceFsn;
	private String sourcePreferred;
	private UUID uuid;

	public LinguistcGuidelineFacts(String sourceFsn, String sourcePreferred, UUID uuid) {
		super();
		this.sourceFsn = sourceFsn;
		this.sourcePreferred = sourcePreferred;
		this.uuid = uuid;
	}

	public String getSourceFsn() {
		return sourceFsn;
	}

	public void setSourceFsn(String sourceFsn) {
		this.sourceFsn = sourceFsn;
	}

	public String getSourcePreferred() {
		return sourcePreferred;
	}

	public void setSourcePreferred(String sourcePreferred) {
		this.sourcePreferred = sourcePreferred;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
}