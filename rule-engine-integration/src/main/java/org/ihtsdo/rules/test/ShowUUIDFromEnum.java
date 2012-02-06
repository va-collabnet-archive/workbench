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

import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.util.id.Type3UuidFactory;

/**
 * The Class ShowUUIDFromEnum.
 */
public class ShowUUIDFromEnum {

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		AceLog.getAppLog().info("PreCommit: " + 
				Type3UuidFactory.fromEnum(RefsetAuxiliary.Concept.REALTIME_PRECOMMIT_QA_CONTEXT));
		AceLog.getAppLog().info("Realtime: " + 
				Type3UuidFactory.fromEnum(RefsetAuxiliary.Concept.REALTIME_QA_CONTEXT));
		AceLog.getAppLog().info("Batch: " + 
				Type3UuidFactory.fromEnum(RefsetAuxiliary.Concept.BATCH_QA_CONTEXT));

	}

}
