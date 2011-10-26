package org.ihtsdo.translation;

public class TreeEditorObjectWrapper {
	private String name;
	private int type;
	private Object userObject;
	
	public static int CONCEPT = 0;
	public static int ID = 1;
	public static int CONCEPTID = 2;
	public static int ATTRIBUTE = 3;
	public static int FSNDESCRIPTION = 4;
	public static int PREFERRED = 5;
	public static int SYNONYMN = 10;
	public static int SUPERTYPE = 6;
	public static int ROLE = 7;
	public static int DESCRIPTIONINFO = 8;
	public static int RELATIONSHIPINFO = 9;
	public static int ROLEGROUP = 11;
	public static int ASSOCIATION=12;
	public static int FOLDER=13;
	public static int NOTACCEPTABLE=14;
	
	/**
	 * @param name
	 * @param type
	 * @param userObject
	 */
	public TreeEditorObjectWrapper(String name, int type, Object userObject) {
		super();
		this.name = name;
		this.type = type;
		this.userObject = userObject;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}
	/**
	 * @return the userObject
	 */
	public Object getUserObject() {
		return userObject;
	}
	/**
	 * @param userObject the userObject to set
	 */
	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}
	
	public String toString() {
		String colouredName = name;
		
		if (type == TreeEditorObjectWrapper.DESCRIPTIONINFO || type == TreeEditorObjectWrapper.RELATIONSHIPINFO) {
			colouredName = "<HTML><FONT color = blue size = '-2'>" + colouredName;
		}
		return colouredName;
	}
}
