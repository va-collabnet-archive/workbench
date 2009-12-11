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
package org.dwfa.mojo;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import org.dwfa.vodb.bind.ThinVersionHelper;

public class SimpleUniversalAcePosition {
	
	private static SimpleDateFormat dateParser = new SimpleDateFormat();
	private ConceptDescriptor pathConcept;
	private String timeStr;
	
	public SimpleUniversalAcePosition(ConceptDescriptor pathConcept, String timeStr) {
		super();
		this.pathConcept = pathConcept;
		this.timeStr = timeStr;
	}
	
	public SimpleUniversalAcePosition() {
		super();
	}
	public String getTimeStr() {
		return timeStr;
	}
	public void setTimeStr(String timeStr) {
		this.timeStr = timeStr;
	}
	
	public int getTime() throws ParseException {
		if (timeStr == null) {
			return Integer.MAX_VALUE;
		}
		Date date = dateParser.parse(timeStr);
		return ThinVersionHelper.convert(date.getTime());
	}

	public Collection<UUID> getPathId() throws IOException, Exception {
		return pathConcept.getVerifiedConcept().getUids();
	}

	public ConceptDescriptor getPathConcept() {
		return pathConcept;
	}

	public void setPathConcept(ConceptDescriptor pathConcept) {
		this.pathConcept = pathConcept;
	}

}
