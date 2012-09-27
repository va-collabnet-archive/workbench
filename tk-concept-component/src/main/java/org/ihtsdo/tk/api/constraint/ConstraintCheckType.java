/**
 * Copyright (c) 2012 International Health Terminology Standards Development
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
package org.ihtsdo.tk.api.constraint;

// TODO: Auto-generated Javadoc
/**
 * The Enum ConstraintCheckType.
 */
public enum ConstraintCheckType {
	
	/** The ignore. */
	IGNORE, 
 /** The equals. */
 EQUALS, 
 /** The kind of. */
 KIND_OF, 
 /** The regex. */
 REGEX;
	
	/**
	 * Gets the.
	 *
	 * @param type the type
	 * @return the constraint check type
	 */
	public static ConstraintCheckType get(String type) {
		if (type.equals("x")) {
			return IGNORE;
		}
		if (type.equals("e")) {
			return EQUALS;
		}
		if (type.equals("k")) {
			return KIND_OF;
		}
		if (type.equals("r")) {
			return REGEX;
		}
		throw new UnsupportedOperationException("Can't handle type: " + type);
	}
}
