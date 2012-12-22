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
package org.ihtsdo.project.refset;

import java.text.SimpleDateFormat;

import org.dwfa.ace.api.ebr.I_ExtendByRef;

/**
 * The Class Comment.
 */
public class Comment implements Comparable<Comment> {

    /**
     * The type cid.
     */
    private int typeCid;
    /**
     * The sub type cid.
     */
    private int subTypeCid;
    /**
     * The comment.
     */
    private String comment;
    /**
     * The time.
     */
    private Long time;
    /**
     * The extension.
     */
    private I_ExtendByRef extension;
    /**
     * The formatter.
     */
    private SimpleDateFormat formatter;
    /**
     * The Constant HEADER_SEPARATOR.
     */
    private static final String HEADER_SEPARATOR = " // ";
    /**
     * The Constant COMMENT_HEADER_SEP.
     */
    private static final String COMMENT_HEADER_SEP = ": -";
    /**
     * The Constant REFSET_COMMENT_NAME.
     */
    private static final Object REFSET_COMMENT_NAME = "Language comment";
    /**
     * The Constant WORKLIST_COMMENT_NAME.
     */
    private static final Object WORKLIST_COMMENT_NAME = "Worklist comment";
    /**
     * The html footer.
     */
    private String htmlFooter = "</body></html>";
    /**
     * The html header.
     */
    private String htmlHeader = "<html><body><font style='color:blue'>";
    /**
     * The end p.
     */
    private String endP = "</font>";
    private String commentName;

    /**
     * Instantiates a new comment.
     *
     * @param typeCid the type cid
     * @param subTypeCid the sub type cid
     * @param comment the comment
     * @param time the time
     * @param extension the extension
     */
    public Comment(int typeCid, int subTypeCid, String comment, Long time, I_ExtendByRef extension) {
        super();
        this.typeCid = typeCid;
        this.subTypeCid = subTypeCid;
        this.comment = comment;
        this.time = time;
        this.extension = extension;
        this.formatter = new SimpleDateFormat();
    }

    /**
     * Gets the type cid.
     *
     * @return the type cid
     */
    public int getTypeCid() {
        return typeCid;
    }

    /**
     * Sets the type cid.
     *
     * @param typeCid the new type cid
     */
    public void setTypeCid(int typeCid) {
        this.typeCid = typeCid;
    }

    /**
     * Gets the sub type cid.
     *
     * @return the sub type cid
     */
    public int getSubTypeCid() {
        return subTypeCid;
    }

    /**
     * Sets the sub type cid.
     *
     * @param subTypeCid the new sub type cid
     */
    public void setSubTypeCid(int subTypeCid) {
        this.subTypeCid = subTypeCid;
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the comment.
     *
     * @param comment the new comment
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Gets the time.
     *
     * @return the time
     */
    public Long getTime() {
        return time;
    }

    /**
     * Sets the time.
     *
     * @param time the new time
     */
    public void setTime(Long time) {
        this.time = time;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Comment comment2) {
        return this.getTime().compareTo(comment2.getTime());
    }

    /**
     * Gets the extension.
     *
     * @return the extension
     */
    public I_ExtendByRef getExtension() {
        return extension;
    }

    /**
     * Sets the extension.
     *
     * @param extension the new extension
     */
    public void setExtension(I_ExtendByRef extension) {
        this.extension = extension;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return formatComment(comment);
    }

    /**
     * Format comment.
     *
     * @param comment the comment
     * @return the string
     */
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

    public String getCommentName() {
        return commentName;
    }

    public void setCommentName(String commentName) {
        this.commentName = commentName;
    }
}
