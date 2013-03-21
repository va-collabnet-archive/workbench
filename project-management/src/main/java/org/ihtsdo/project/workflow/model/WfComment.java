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

	public WfComment(Comment commentStructure) {

		date = Long.parseLong(commentStructure.getComment().substring(commentStructure.getComment().trim().lastIndexOf(" ") + 1));
		String comm = commentStructure.getComment().substring(0, commentStructure.getComment().lastIndexOf(" - Time:"));
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
		date = System.currentTimeMillis();
		comment = formatComment(string + " - Time: " + date);
		role = "Commenter";
		try {
			author = new WfUser(Ts.get().getConceptVersion(Terms.get().getActiveAceFrameConfig().getViewCoordinate(), Terms.get().getAuthorNid()).toUserString(), UUID.randomUUID());
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Format comment.
	 * 
	 * @param comment
	 *            the comment
	 * @return the string
	 */
	private String formatComment(String comment) {
		long thickVer;
		SimpleDateFormat formatter = new SimpleDateFormat();
		thickVer = Long.parseLong(comment.substring(comment.trim().lastIndexOf(" ") + 1));
		String strDate = formatter.format(thickVer);
		String tmp = comment.substring(0, comment.lastIndexOf(" - Time:"));
		if (tmp.indexOf(COMMENT_HEADER_SEP) > -1) {
			tmp = tmp.replace(COMMENT_HEADER_SEP, endP + COMMENT_HEADER_SEP) + htmlFooter;
			return htmlHeader + "<I>" + strDate + "</I>" + HEADER_SEPARATOR + tmp;
		}
		return htmlHeader + "<I>" + strDate + "</I>" + COMMENT_HEADER_SEP + tmp;
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
