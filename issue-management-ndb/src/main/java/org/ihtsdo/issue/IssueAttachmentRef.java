package org.ihtsdo.issue;

public class IssueAttachmentRef {

	private Issue issue;
	
	private String fileName;
	
	private String fileSize;
	
	private String rawFileId;
	
	private String mimeType;
	
	private String attachmentId;
	
	public IssueAttachmentRef(){
	}

	public Issue getIssue() {
		return issue;
	}

	public void setIssue(Issue issue) {
		this.issue = issue;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileSize() {
		return fileSize;
	}

	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}

	public String getRawFileId() {
		return rawFileId;
	}

	public void setRawFileId(String rawFileId) {
		this.rawFileId = rawFileId;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getAttachmentId() {
		return attachmentId;
	}

	public void setAttachmentId(String attachmentId) {
		this.attachmentId = attachmentId;
	}
	
}
