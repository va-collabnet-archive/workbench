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
package org.dwfa.mojo.compare.operators;

import java.util.List;

import org.dwfa.mojo.compare.CompareOperator;
import org.dwfa.mojo.compare.Match;

public class AndOperator implements CompareOperator {

	public List<CompareOperator> operators;
	
	public boolean compare(List<Match> matches) {		
		if (!(operators==null)) {
			boolean result = true;
			for (CompareOperator co : operators) {
				result = result && co.compare(matches);
			}
			return result;
		}
		else {
			return false;			
		}
	}

}
