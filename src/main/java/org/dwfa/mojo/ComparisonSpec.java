package org.dwfa.mojo;

/**
 * <h1>ComparisonSpec</h1>
 * <p>This class is used to create an object to hold configuration details 
 * used by the {@link CompareFileContent} mojo.</p>
 * 
 * @see org.dwfa.mojo.CompareFileContent
 * 
 *
 */
public class ComparisonSpec{
	private String comparisonBase = null;
	private String delimeter = null;
	private String excludedFields = null;
	
	public void setComparisonBase( String base){
		this.comparisonBase = base;
	}
	public String getComparisonBase(){
		return this.comparisonBase;
	}
	
	public void setDelimeter( String delimeter ){
		this.delimeter = delimeter;
	}
	public String getDelimeter(){
		return this.delimeter;
	}
	
	public void setExcludedFields( String excludedFields ){
		this.excludedFields = excludedFields;
	}
	public String getExcludedFields(){
		return this.excludedFields;
	}
	
	public ComparisonSpec(){
		super();
	}
	
}//End class ComparisonSpec