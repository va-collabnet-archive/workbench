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
package org.dwfa.vodb.types;

import java.util.Comparator;

public class ThinDescVersionedComparator implements Comparator<ThinDescVersioned> {

	public int compare(ThinDescVersioned o1, ThinDescVersioned o2) {
		return o1.getVersions().get(0).getText().compareTo(o2.getVersions().get(0).getText());
	}

}
