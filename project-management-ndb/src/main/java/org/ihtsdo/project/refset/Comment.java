package org.ihtsdo.project.refset;

import java.text.SimpleDateFormat;

import org.dwfa.ace.api.ebr.I_ExtendByRef;

public class Comment implements Comparable<Comment> {
	private int typeCid;
	private int subTypeCid;
	private String comment;
	private Long time;
	private I_ExtendByRef extension;
	private SimpleDateFormat formatter;
	
	private static final String HEADER_SEPARATOR = " // ";
	private static final String COMMENT_HEADER_SEP = ": -";
	private static final Object REFSET_COMMENT_NAME = "Language comment";
	private static final Object WORKLIST_COMMENT_NAME = "Worklist comment";
	private String htmlFooter = "</body></html>";
	private String htmlHeader = "<html><body><font style='color:blue'>";
	private String endP = "</font>";

	public Comment(int typeCid, int subTypeCid, String comment, Long time, I_ExtendByRef extension) {
		super();
		this.typeCid = typeCid;
		this.subTypeCid = subTypeCid;
		this.comment = comment;
		this.time = time;
		this.extension = extension;
		this.formatter = new SimpleDateFormat();
	}

	public int getTypeCid() {
		return typeCid;
	}

	public void setTypeCid(int typeCid) {
		this.typeCid = typeCid;
	}

	public int getSubTypeCid() {
		return subTypeCid;
	}

	public void setSubTypeCid(int subTypeCid) {
		this.subTypeCid = subTypeCid;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public int compareTo(Comment comment2) {
		return this.getTime().compareTo(comment2.getTime());
	}

	public I_ExtendByRef getExtension() {
		return extension;
	}

	public void setExtension(I_ExtendByRef extension) {
		this.extension = extension;
	}
	
	public String toString() {
		return formatComment(comment);
	}
	
	private String formatComment(String comment) {
		long thickVer;
		thickVer = Long.parseLong(comment.substring(comment.trim().lastIndexOf(" ") + 1));
		String strDate = formatter.format(thickVer);
		String tmp = comment.substring(0, comment.lastIndexOf(" - Time:"));
		if (tmp.indexOf(COMMENT_HEADER_SEP) > -1) {
			tmp = tmp.replace(COMMENT_HEADER_SEP, endP + COMMENT_HEADER_SEP) + htmlFooter;
			return htmlHeader + "<I>" + strDate + "</I>" + HEADER_SEPARATOR + tmp;
		}
		return htmlHeader + "<I>" + strDate + "</I>" + COMMENT_HEADER_SEP + tmp;

	}

}
