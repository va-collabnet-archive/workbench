package org.ihtsdo.tk.dto.concept.component;

import java.util.List;
import java.util.UUID;

import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifier;

public interface I_AmComponent<V extends TkRevision> {

	public abstract int getIdComponentCount();

	public abstract List<TkIdentifier> getEIdentifiers();

	public abstract List<UUID> getUuids();

	public abstract int getVersionCount();

	public abstract List<? extends TkRevision> getRevisionList();

	public abstract UUID getPrimordialComponentUuid();

	public abstract List<TkIdentifier> getAdditionalIdComponents();

	public abstract List<V> getRevisions();

}