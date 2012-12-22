/*
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.issue;

/**
 * The Class IssueAttachmentRef.
 */
public class IssueAttachmentRef {

	/** The issue. */
	private Issue issue;
	
	/** The file name. */
	private String fileName;
	
	/** The file size. */
	private String fileSize;
	
	/** The raw file id. */
	private String rawFileId;
	
	/** The mime type. */
	private String mimeType;
	
	/** The attachment id. */
	private String attachmentId;
	
	/**
	 * Instantiates a new issue attachment ref.
	 */
	public IssueAttachmentRef(){
	}

	/**
	 * Gets the issue.
	 *
	 * @return the issue
	 */
	public Issue getIssue() {
		return issue;
	}

	/**
	 * Sets the issue.
	 *
	 * @param issue the new issue
	 */
	public void setIssue(Issue issue) {
		this.issue = issue;
	}

	/**
	 * Gets the file name.
	 *
	 * @return the file name
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Sets the file name.
	 *
	 * @param fileName the new file name
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Gets the file size.
	 *
	 * @return the file size
	 */
	public String getFileSize() {
		return fileSize;
	}

	/**
	 * Sets the file size.
	 *
	 * @param fileSize the new file size
	 */
	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}

	/**
	 * Gets the raw file id.
	 *
	 * @return the raw file id
	 */
	public String getRawFileId() {
		return rawFileId;
	}

	/**
	 * Sets the raw file id.
	 *
	 * @param rawFileId the new raw file id
	 */
	public void setRawFileId(String rawFileId) {
		this.rawFileId = rawFileId;
	}

	/**
	 * Gets the mime type.
	 *
	 * @return the mime type
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * Sets the mime type.
	 *
	 * @param mimeType the new mime type
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	/**
	 * Gets the attachment id.
	 *
	 * @return the attachment id
	 */
	public String getAttachmentId() {
		return attachmentId;
	}

	/**
	 * Sets the attachment id.
	 *
	 * @param attachmentId the new attachment id
	 */
	public void setAttachmentId(String attachmentId) {
		this.attachmentId = attachmentId;
	}
	
}
