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
package org.dwfa.maven;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * http://java.sun.com/developer/technicalArticles/releases/1.4regex/
 * 
 * @author kec
 * 
 */
public class RegexReplace {

    private Pattern pattern;
    String replacementStr;

    public RegexReplace(String patternStr, String replacementStr) {
        super();
        pattern = Pattern.compile(patternStr);
        this.replacementStr = replacementStr;
    }

    public String execute(CharSequence inputStr) {
        Matcher matcher = pattern.matcher(inputStr);
        return matcher.replaceAll(replacementStr);
    }

}
