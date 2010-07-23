package org.ihtsdo.tk.helper;

import java.util.UUID;

public abstract class AbstractTransitiveClosureHelper {

	public AbstractTransitiveClosureHelper() {
		super();
	}
	
	abstract public boolean isParentOf(UUID parent, UUID subtype) throws Exception;

	abstract public boolean isParentOfOrEqualTo(UUID parent, UUID subtype) throws Exception;

	abstract public boolean isParentOf(String parents, UUID subtype) throws Exception;

	abstract public boolean isParentOfOrEqualTo(String parents, UUID subtype) throws Exception;

}
