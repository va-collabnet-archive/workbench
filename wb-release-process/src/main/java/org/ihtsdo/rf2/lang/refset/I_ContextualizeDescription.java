package org.ihtsdo.rf2.lang.refset;

import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;

public interface I_ContextualizeDescription {

	public abstract boolean persistChanges() throws Exception;

	public abstract boolean persistChangesNoChecks() throws Exception;

	public abstract I_ContextualizeDescription contextualizeThisDescription(int newLanguageRefsetId, int acceptabilityId) throws Exception;

	public abstract int getDescId();

	public abstract int getConceptId();

	public abstract int getTypeId();

	public abstract void setTypeId(int typeId);

	public abstract String getLang();

	public abstract void setLang(String lang);

	public abstract String getText();

	public abstract void setText(String text);

	public abstract int getAcceptabilityId();

	public abstract void setAcceptabilityId(int acceptabilityId);

	public abstract int getLanguageRefsetId();

	public abstract I_DescriptionVersioned getDescriptionVersioned();

	public abstract I_DescriptionPart getDescriptionPart();

	public abstract I_ExtendByRef getLanguageExtension();

	public abstract I_ExtendByRefPartCid getLanguageExtensionPart();

	public abstract I_GetConceptData getConcept();

	public abstract int getExtensionStatusId();

	public abstract void setExtensionStatusId(int extensionStatusId);

	public abstract Collection<UUID> getUuids();

	public abstract int getDescriptionStatusId();

	public abstract void setDescriptionStatusId(int descriptionStatusId);

	public abstract void setInitialCaseSignificant(boolean isInitialCaseSignificant);

	public abstract boolean isInitialCaseSignificant();

	public abstract String toString();

}