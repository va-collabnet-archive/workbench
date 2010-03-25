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
package org.dwfa.ace.api;

import java.util.Collection;
import java.util.List;

public interface I_Position {

    public I_Path getPath();

    public int getVersion();

    public long getTime();

    public boolean isSubsequentOrEqualTo(int version, int pathId);

    public boolean isAntecedentOrEqualTo(int version, int pathId);

    public boolean isAntecedentOrEqualTo(I_Position another);

    public boolean checkAntecedentOrEqualToOrigins(List<I_Position> origins);

    public boolean isSubsequentOrEqualTo(I_Position another);

    public boolean equals(int version, int pathId);

    public Collection<I_Position> getAllOrigins();

}
