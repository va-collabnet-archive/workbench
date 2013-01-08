package org.ihtsdo.rules.testmodel.filesmodel;

import java.util.List;

public abstract class RF2File {
	
	private  RF2Header header;
	private List<? extends RF2Row> rows;

	public enum RF2FileType {
		CONCEPTS,DESCRIPTIONS,RELATIONSHIPS,ATTRIBUTEVALUE,SIMPLEMAPS,LANGUAGES;
	}
	
	private RF2FileType type;

	

}
