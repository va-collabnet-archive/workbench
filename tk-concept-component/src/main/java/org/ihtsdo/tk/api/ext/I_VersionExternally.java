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
package org.ihtsdo.tk.api.ext;

import java.util.UUID;

// TODO: Auto-generated Javadoc
/**
 * The Interface I_VersionExternally.
 */
public interface I_VersionExternally {

	/**
	 * Gets the status uuid.
	 *
	 * @return the status uuid
	 */
	public UUID getStatusUuid();
	
	/**
	 * Gets the author uuid.
	 *
	 * @return the author uuid
	 */
	public UUID getAuthorUuid();

	/**
	 * Gets the path uuid.
	 *
	 * @return the path uuid
	 */
	public UUID getPathUuid();
        
        /**
         * Gets the module uuid.
         *
         * @return the module uuid
         */
        public UUID getModuleUuid();

	/**
	 * Gets the time.
	 *
	 * @return the time
	 */
	public long getTime();

}