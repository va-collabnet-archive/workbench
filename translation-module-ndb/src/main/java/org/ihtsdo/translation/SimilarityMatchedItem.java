/**
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
package org.ihtsdo.translation;

import java.io.Serializable;

/**
 * The Class SimilarityMatchedItem.
 */
public class SimilarityMatchedItem implements Serializable{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The concept id. */
	private Integer conceptId;
	
	/** The source description id. */
	private Integer sourceDescriptionId;
	
	/** The source text. */
	private String sourceText;
	
	/** The target description id. */
	private Integer targetDescriptionId;
	
	/** The target text. */
	private String targetText;
	
	/** The score. */
	private float score;
	
	/** The query. */
	private String query;
	
	/**
	 * Instantiates a new similarity matched item.
	 * 
	 * @param conceptId the concept id
	 * @param sourceDescriptionId the source description id
	 * @param sourceText the source text
	 * @param targetDescriptionId the target description id
	 * @param targetText the target text
	 * @param score the score
	 * @param originalTerm the original term
	 */
	public SimilarityMatchedItem(Integer conceptId,
			Integer sourceDescriptionId, String sourceText,
			Integer targetDescriptionId, String targetText, float score,
			String originalTerm) {
		super();
		this.conceptId = conceptId;
		this.sourceDescriptionId = sourceDescriptionId;
		this.sourceText = sourceText;
		this.targetDescriptionId = targetDescriptionId;
		this.targetText = targetText;
		this.score = score;
		this.query = originalTerm;
	}
	
	/**
	 * Gets the concept id.
	 * 
	 * @return the concept id
	 */
	public Integer getConceptId() {
		return conceptId;
	}
	
	/**
	 * Sets the concept id.
	 * 
	 * @param conceptId the new concept id
	 */
	public void setConceptId(Integer conceptId) {
		this.conceptId = conceptId;
	}
	
	/**
	 * Gets the source description id.
	 * 
	 * @return the source description id
	 */
	public Integer getSourceDescriptionId() {
		return sourceDescriptionId;
	}
	
	/**
	 * Sets the source description id.
	 * 
	 * @param sourceDescriptionId the new source description id
	 */
	public void setSourceDescriptionId(Integer sourceDescriptionId) {
		this.sourceDescriptionId = sourceDescriptionId;
	}
	
	/**
	 * Gets the source text.
	 * 
	 * @return the source text
	 */
	public String getSourceText() {
		return sourceText;
	}
	
	/**
	 * Sets the source text.
	 * 
	 * @param sourceText the new source text
	 */
	public void setSourceText(String sourceText) {
		this.sourceText = sourceText;
	}
	
	/**
	 * Gets the target description id.
	 * 
	 * @return the target description id
	 */
	public Integer getTargetDescriptionId() {
		return targetDescriptionId;
	}
	
	/**
	 * Sets the target description id.
	 * 
	 * @param targetDescriptionId the new target description id
	 */
	public void setTargetDescriptionId(Integer targetDescriptionId) {
		this.targetDescriptionId = targetDescriptionId;
	}
	
	/**
	 * Gets the target text.
	 * 
	 * @return the target text
	 */
	public String getTargetText() {
		return targetText;
	}
	
	/**
	 * Sets the target text.
	 * 
	 * @param targetText the new target text
	 */
	public void setTargetText(String targetText) {
		this.targetText = targetText;
	}
	
	/**
	 * Gets the score.
	 * 
	 * @return the score
	 */
	public float getScore() {
		return score;
	}
	
	/**
	 * Sets the score.
	 * 
	 * @param score the new score
	 */
	public void setScore(float score) {
		this.score = score;
	}
	
	/**
	 * Gets the query.
	 * 
	 * @return the query
	 */
	public String getQuery() {
		return query;
	}
	
	/**
	 * Sets the query.
	 * 
	 * @param query the new query
	 */
	public void setQuery(String query) {
		this.query = query;
	}
	
	
}
