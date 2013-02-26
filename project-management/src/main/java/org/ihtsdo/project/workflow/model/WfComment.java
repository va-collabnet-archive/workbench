package org.ihtsdo.project.workflow.model;

import java.text.SimpleDateFormat;

import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.project.refset.Comment;
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
	
	private String author;
	
	private Long date;
	
	private String comment;
	public WfComment(Comment commentStructure){

		date = Long.parseLong(commentStructure.getComment().substring(commentStructure.getComment().trim().lastIndexOf(" ") + 1));
		String comm = commentStructure.getComment().substring(0, commentStructure.getComment().lastIndexOf(" - Time:"));
		comm = comm.replace(htmlHeader, "");
		comm = comm.replace(htmlFooter, "");
		comm = comm.replace(endP, "");
		String[] arrComm = comm.split(COMMENT_HEADER_SEP);
		String header = arrComm[0];
		String[] headerComp = header.split(HEADER_SEPARATOR);
		author = "";
		role = "";
		if (headerComp.length > 0) {
			role = headerComp[0];
			if (headerComp.length > 1) {
				author = headerComp[1];
			}
		}
		comment=arrComm[1];

	}
	@Override
	public String getAuthor() {
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
	@Override
	public String getRole() {
		return role;
	}
	@Override
	public int compareTo(WfCommentBI comment2) {
		 return this.getDate().compareTo(comment2.getDate());
	}


	@Override
	public String toString() {
		return (role==null || role.equals("null") || role.equals("")? "author":role) + ": " +  author + " - " + comment + " - " + TimeHelper.formatDate(date);
	}
}
