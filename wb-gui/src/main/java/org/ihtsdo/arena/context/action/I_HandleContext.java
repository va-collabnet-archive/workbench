package org.ihtsdo.arena.context.action;

import java.util.Collection;

import javax.swing.Action;

import org.dwfa.ace.api.I_ConfigAceFrame;

public interface I_HandleContext {

	boolean canDropOnDesc(int targetConceptNid,
			int droppedComponentNid);

	boolean canDropOnRel(int targetConceptNid,
			int droppedComponentNid);

	boolean canDropOnRelGroup(
			int targetConceptNid, int droppedComponentNid);

	boolean canDropOnTrash(int targetConceptNid,
			int droppedComponentNid);

	Collection<Action> dropOnDesc(
			int targetConceptNid, int droppedComponentNid);

	Collection<Action> dropOnRel(
			int targetConceptNid, int droppedComponentNid);

	Collection<Action> dropOnRelGroup(
			int targetConceptNid, int droppedComponentNid);

	Collection<Action> dropOnTrash(
			int targetConceptNid, int droppedComponentNid);

	public I_ConfigAceFrame getConfig();

}