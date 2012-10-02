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
package org.ihtsdo.tk.api;

import java.util.concurrent.CountDownLatch;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;

import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

// TODO: Auto-generated Javadoc
/**
 * The Interface KindOfCacheBI. TODO-javadoc: Alejandro? 
 */
public interface KindOfCacheBI {

	/**
	 * Sets the up.
	 *
	 * @param viewCoordinate the new up
	 * @throws Exception the exception
	 */
	public abstract void setup(ViewCoordinate viewCoordinate) throws Exception;

	/**
	 * Checks if is kind of.
	 *
	 * @param childNid the child nid
	 * @param parentNid the parent nid
	 * @return <code>true</code>, if is kind of
	 * @throws Exception the exception
	 */
	public abstract boolean isKindOf(int childNid, int parentNid)
			throws Exception;
	
	/**
	 * Gets the latch.
	 *
	 * @return the latch
	 */
	public CountDownLatch getLatch();
        
        /**
         * Update cache.
         *
         * @param conceptChronicle the concept chronicle
         * @throws Exception the exception
         */
        public void updateCache(ConceptChronicleBI conceptChronicle) throws Exception;
    

}