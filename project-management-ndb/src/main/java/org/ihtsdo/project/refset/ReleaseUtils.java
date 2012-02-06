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
package org.ihtsdo.project.refset;

import java.io.IOException;
import java.util.List;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.coordinate.PositionSet;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

/**
 * The Class ReleaseUtils.
 */
public class ReleaseUtils {

	/**
	 * Gets the new config from path.
	 *
	 * @param path the path
	 * @param baseConfig the base config
	 * @return the new config from path
	 */
	public static I_ConfigAceFrame getNewConfigFromPath(PathBI path,I_ConfigAceFrame baseConfig) {
		I_ConfigAceFrame config = null;
		try {
			config = Terms.get().newAceFrameConfig();
			config.addViewPosition(Terms.get().newPosition(
					path, 
					baseConfig.getViewPositionSet().iterator().next().getTime()));
			config.addEditingPath(path);
			config.addPromotionPath( path);

			config.setDescTypes(baseConfig.getDescTypes());
			config.setDefaultStatus(baseConfig.getDefaultStatus());
			config.setAllowedStatus(baseConfig.getAllowedStatus());
			config.setDestRelTypes(baseConfig.getDestRelTypes());
			config.setSourceRelTypes(baseConfig.getSourceRelTypes());
			config.setRoots(baseConfig.getRoots());
			config.setDbConfig(baseConfig.getDbConfig());
			config.setRelAssertionType(baseConfig.getRelAssertionType());
			config.setDefaultRelationshipRefinability(baseConfig.getDefaultRelationshipRefinability());
			config.setDefaultDescriptionType(baseConfig.getDefaultDescriptionType());
			
			config.setPrecedence(baseConfig.getPrecedence());

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return config;
	}
}
