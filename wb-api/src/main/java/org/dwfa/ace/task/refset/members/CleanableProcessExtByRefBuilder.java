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
package org.dwfa.ace.task.refset.members;

import java.io.File;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.select.DescriptionSelector;
import org.dwfa.ace.task.util.Logger;

public interface CleanableProcessExtByRefBuilder {

    CleanableProcessExtByRefBuilder withSelectedDir(File selectedDirectory);

    CleanableProcessExtByRefBuilder withLogger(Logger logger);

    CleanableProcessExtByRef build();

    CleanableProcessExtByRefBuilder withTermFactory(I_TermFactory termFactory);

    CleanableProcessExtByRefBuilder withLanguagePreference(DescriptionSelector descriptionSelector);

}
