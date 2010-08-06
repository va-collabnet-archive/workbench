package org.ihtsdo.tk.api.ext;

import java.util.UUID;

public interface I_RelateExternally {

	public UUID getC1Uuid();

	public UUID getC2Uuid();

	public UUID getCharacteristicUuid();

	public UUID getRefinabilityUuid();

	public int getRelGroup();

	public UUID getTypeUuid();

}