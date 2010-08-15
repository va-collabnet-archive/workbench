package org.ihtsdo.arena.context.action;

import java.util.Collection;

import javax.swing.Action;

public class ContextualActions {
	
	
	
	public boolean canDropOnDesc(int userNid, int targetConceptNid, int droppedComponentNid) { throw new UnsupportedOperationException(); };
	public boolean canDropOnRel(int userNid, int targetConceptNid, int droppedComponentNid) { throw new UnsupportedOperationException(); };
	public boolean canDropOnRelGroup(int userNid, int targetConceptNid, int droppedComponentNid) { throw new UnsupportedOperationException(); };	
	public boolean canDropOnTrash(int userNid, int targetConceptNid, int droppedComponentNid)  { throw new UnsupportedOperationException(); };
	
	public Collection<Action> dropOnDesc(int userNid, int targetConceptNid, int droppedComponentNid) { throw new UnsupportedOperationException(); };
	public Collection<Action> dropOnRel(int userNid, int targetConceptNid, int droppedComponentNid) { throw new UnsupportedOperationException(); };
	public Collection<Action> dropOnRelGroup(int userNid, int targetConceptNid, int droppedComponentNid) { throw new UnsupportedOperationException(); };	
	public Collection<Action> dropOnTrash(int userNid, int targetConceptNid, int droppedComponentNid)  { throw new UnsupportedOperationException(); };
	
	
}
