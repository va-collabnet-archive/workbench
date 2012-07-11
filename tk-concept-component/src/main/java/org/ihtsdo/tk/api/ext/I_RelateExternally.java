package org.ihtsdo.tk.api.ext;

import java.util.UUID;

public interface I_RelateExternally {

	public UUID getRelationshipSourceUuid();

	public UUID getRelationshipTargetUuid();

	public UUID getCharacteristicUuid();

	public UUID getRefinabilityUuid();

	public int getRelationshipGroup();

	public UUID getTypeUuid();

}