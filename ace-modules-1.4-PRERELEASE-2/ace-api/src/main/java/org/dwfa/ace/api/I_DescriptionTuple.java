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

public interface I_DescriptionTuple extends I_AmTypedTuple {

    public abstract boolean getInitialCaseSignificant();

    public abstract String getLang();

    public abstract String getText();

    public abstract int getConceptId();

    public abstract int getDescId();

    public abstract void setInitialCaseSignificant(boolean capStatus);

    public abstract void setLang(String lang);

    public abstract void setText(String text);

    /**
     * @deprecated Use {@link #duplicate()}
     */
    @Deprecated
    public abstract I_DescriptionPart duplicatePart();

    public I_DescriptionPart duplicate();

    public abstract I_DescriptionPart getPart();

    public abstract I_DescriptionVersioned getDescVersioned();

}
