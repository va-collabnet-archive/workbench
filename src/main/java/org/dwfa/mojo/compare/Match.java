/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.dwfa.mojo.compare;

import java.util.LinkedList;
import java.util.List;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;

public class Match {

	public I_Position path1;
	public I_Position path2;
	public Match(I_Position path1, I_Position path2) {
		super();
		this.path1 = path1;
		this.path2 = path2;
	}
	public I_Position getPath1() {
		return path1;
	}
	public void setPath1(I_Position path1) {
		this.path1 = path1;
	}
	public I_Position getPath2() {
		return path2;
	}
	public void setPath2(I_Position path2) {
		this.path2 = path2;
	}

	public List<I_ConceptAttributeTuple> matchConceptAttributeTuples = new LinkedList<I_ConceptAttributeTuple>();
	public List<I_DescriptionTuple> matchDescriptionTuples = new LinkedList<I_DescriptionTuple>();
	public List<I_RelTuple> matchRelationshipTuples = new LinkedList<I_RelTuple>();

}
