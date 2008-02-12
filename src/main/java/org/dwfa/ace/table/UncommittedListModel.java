package org.dwfa.ace.table;

import java.util.Collection;

import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.bpa.data.ArrayListModel;

public class UncommittedListModel extends ArrayListModel<AlertToDataConstraintFailure> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UncommittedListModel() {
		super();
	}

	public UncommittedListModel(Collection<AlertToDataConstraintFailure> data) {
		super(data);
	}

}
