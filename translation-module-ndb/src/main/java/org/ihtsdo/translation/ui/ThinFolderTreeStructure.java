package org.ihtsdo.translation.ui;

import java.io.Serializable;

public class ThinFolderTreeStructure implements Serializable {

	private String folderName;
	private ThinFolderTreeStructure[] children;
	public String getFolderName() {
		return folderName;
	}
	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}
	public ThinFolderTreeStructure[] getChildren() {
		return children;
	}
	public void setChildren(ThinFolderTreeStructure[] children) {
		this.children = children;
	}
	public String toString(){
		return folderName;
	}
}
