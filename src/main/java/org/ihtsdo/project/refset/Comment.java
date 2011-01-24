package org.ihtsdo.project.refset;

public class Comment {
	private int typeCid;
	private int subTypeCid;
	private String comment;

	public Comment(int typeCid, int subTypeCid, String comment) {
		super();
		this.typeCid = typeCid;
		this.subTypeCid = subTypeCid;
		this.comment = comment;
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
	
}
	
class CommentComparator implements java.util.Comparator<Comment> {

	@Override
	public int compare(Comment comment1, Comment comment2) {
		String[] splitedComment1 = comment1.getComment().split(" ");
		String[] splitedComment2 = comment2.getComment().split(" ");
		
		Long commentTime1 = Long.valueOf(splitedComment1[splitedComment1.length -1]);
		Long commentTime2 = Long.valueOf(splitedComment2[splitedComment2.length -1]);
		
		return commentTime1.compareTo(commentTime2);
		
	}
	
}
