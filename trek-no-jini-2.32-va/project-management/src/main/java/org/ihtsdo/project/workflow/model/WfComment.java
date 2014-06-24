package org.ihtsdo.project.workflow.model;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.UUID;

import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.project.refset.Comment;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.workflow.api.WfCommentBI;

public class WfComment implements WfCommentBI {

	/** The Constant HEADER_SEPARATOR. */
	private static final String HEADER_SEPARATOR = " // ";
	/** The Constant COMMENT_HEADER_SEP. */
	private static final String COMMENT_HEADER_SEP = ": -";

	/** The Constant WORKLIST_COMMENT_NAME. */
	private static final Object WORKLIST_COMMENT_NAME = "Worklist comment";

	/** The html footer. */
	private String htmlFooter = "</body></html>";

	/** The html header. */
	private String htmlHeader = "<html><body><font style='color:blue'>";

	/** The end p. */
	private String endP = "</font>";

	private String role;

	private WfUser author;

	private Long date;

	private String comment;
	private SimpleDateFormat formatter;

	public WfComment(Comment commentStructure) {

		this.formatter = new SimpleDateFormat();
		String oldComment = commentStructure.getComment();
		String datestr = oldComment.substring(oldComment.trim().lastIndexOf(" ") + 1);
		date = Long.parseLong(datestr);
		String comm = oldComment.substring(0, oldComment.lastIndexOf(" - Time:"));
		comm = comm.replace(htmlHeader, "");
		comm = comm.replace(htmlFooter, "");
		comm = comm.replace(endP, "");
		String[] arrComm = comm.split(COMMENT_HEADER_SEP);
		String header = arrComm[0];
		String[] headerComp = header.split(HEADER_SEPARATOR);
		String authorStr = "";
		role = "";
		if (headerComp.length > 0) {
			role = headerComp[0];
			if (headerComp.length > 1) {
				authorStr = headerComp[1];
			}
		}
		comment = arrComm[1];
		author = new WfUser(authorStr, UUID.randomUUID());
	}

	public WfComment(String string) {
		try {
			this.formatter = new SimpleDateFormat();
			date = System.currentTimeMillis();
			String username = Terms.get().getActiveAceFrameConfig().getDbConfig().getUsername();
			this.author = new WfUser(username,UUID.randomUUID());
			role = "Author";
			comment = role.toString() + HEADER_SEPARATOR  + username  + COMMENT_HEADER_SEP + string;
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public WfUser getAuthor() {
		return author;
	}

	@Override
	public Long getDate() {
		return date;
	}

	@Override
	public String getComment() {
		return comment;
	}

	public String getRole() {
		return role;
	}

	@Override
	public int compareTo(WfCommentBI comment2) {
		return this.getDate().compareTo(comment2.getDate());
	}

	@Override
	public String toString() {
		return comment + " - author: " + author + " - " + TimeHelper.formatDate(date);
	}
}
