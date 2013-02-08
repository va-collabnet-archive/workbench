package org.ihtsdo.project.workflow.api.wf2.implementation;

import java.sql.Date;

import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.tk.workflow.api.WfActivityInstanceBI;
import org.ihtsdo.tk.workflow.api.WfStateBI;
import org.ihtsdo.tk.workflow.api.WfUserBI;

public class WfActivityInstance implements WfActivityInstanceBI {

	private Long time;
	private WfStateBI state;
	private WfUserBI author;
	private boolean usedOverride;
	private boolean automaticAction;

	public WfActivityInstance(Long time, WfStateBI state, WfUserBI author, boolean usedOverride, boolean automaticAction) {
		super();
		this.time = time;
		this.state = state;
		this.author = author;
		this.usedOverride = usedOverride;
		this.automaticAction = automaticAction;
	}

	@Override
	public Long getTime() {
		return this.time;
	}

	@Override
	public WfStateBI getState() {
		return this.state;
	}

	@Override
	public WfUserBI getAuthor() {
		return this.author;
	}

	@Override
	public boolean usedOverride() {
		return this.usedOverride;
	}

	@Override
	public boolean automaticAction() {
		return this.automaticAction;
	}

	@Override
	public String toString() {
		return state.getName() + " - " + author.getName() + " - " + TimeHelper.formatDate(time);
	}

}
