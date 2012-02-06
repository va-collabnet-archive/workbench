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
package org.ihtsdo.rules.test;

/**
 * The Class Concept.
 */
public class Concept implements I_Concept, I_IsLastVersion {
	
	/** The name. */
	String name;
	
	/** The id. */
	Integer id;
	
	/** The last. */
	Boolean last;

	/**
	 * Instantiates a new concept.
	 */
	public Concept() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.rules.test.I_Concept#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.rules.test.I_Concept#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.rules.test.I_Concept#getId()
	 */
	public Integer getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.rules.test.I_Concept#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.rules.test.I_IsLastVersion#isLast()
	 */
	public Boolean isLast() {
		return last;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.rules.test.I_IsLastVersion#setLast(java.lang.Boolean)
	 */
	public void setLast(Boolean last) {
		this.last = last;
	}
	

}
