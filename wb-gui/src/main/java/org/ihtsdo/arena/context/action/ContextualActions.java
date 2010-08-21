package org.ihtsdo.arena.context.action;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Action;

import org.dwfa.ace.api.I_ConfigAceFrame;

public class ContextualActions implements I_HandleContext {
	
	I_ConfigAceFrame config;
	
	public ContextualActions(I_ConfigAceFrame config) {
		this.config = config;
	}
	
	/* (non-Javadoc)
	 * @see org.ihtsdo.arena.context.action.I_HandleContext#canDropOnDesc(int, int, int)
	 */
	public boolean canDropOnDesc(int targetConceptNid, int droppedComponentNid) { return false; };
	/* (non-Javadoc)
	 * @see org.ihtsdo.arena.context.action.I_HandleContext#canDropOnRel(int, int, int)
	 */
	public boolean canDropOnRel(int targetConceptNid, int droppedComponentNid) { return false; };
	/* (non-Javadoc)
	 * @see org.ihtsdo.arena.context.action.I_HandleContext#canDropOnRelGroup(int, int, int)
	 */
	public boolean canDropOnRelGroup(int targetConceptNid, int droppedComponentNid) { return false; };	
	/* (non-Javadoc)
	 * @see org.ihtsdo.arena.context.action.I_HandleContext#canDropOnTrash(int, int, int)
	 */
	public boolean canDropOnTrash(int targetConceptNid, int droppedComponentNid)  { return false; };
	
	/* (non-Javadoc)
	 * @see org.ihtsdo.arena.context.action.I_HandleContext#dropOnDesc(int, int, int)
	 */
	public Collection<Action> dropOnDesc(int targetConceptNid, int droppedComponentNid) {
		ArrayList<Action> actions = new ArrayList<Action>();
		actions.add(new CopyDescAction("Copy to Concept"));
		actions.add(new MoveDescAction("Move to Concept"));
        return actions;

	};
	/* (non-Javadoc)
	 * @see org.ihtsdo.arena.context.action.I_HandleContext#dropOnRel(int, int, int)
	 */
	public Collection<Action> dropOnRel(int targetConceptNid, int droppedComponentNid) {
		ArrayList<Action> actions = new ArrayList<Action>();
		actions.add(new CopyDescAction("Copy to Concept"));
		actions.add(new MoveDescAction("Move to Concept"));
        return actions;
	};
	/* (non-Javadoc)
	 * @see org.ihtsdo.arena.context.action.I_HandleContext#dropOnRelGroup(int, int, int)
	 */
	public Collection<Action> dropOnRelGroup(int targetConceptNid, int droppedComponentNid) {
		ArrayList<Action> actions = new ArrayList<Action>();
		actions.add(new CopyDescAction("Copy to Concept"));
		actions.add(new MoveDescAction("Move to Concept"));
        return actions;
	};	
	/* (non-Javadoc)
	 * @see org.ihtsdo.arena.context.action.I_HandleContext#dropOnTrash(int, int, int)
	 */
	public Collection<Action> dropOnTrash(int targetConceptNid, int droppedComponentNid)  { return new ArrayList<Action>(); };
	
	public I_ConfigAceFrame getConfig() {
		return config;
	}


}
