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

import org.ihtsdo.tk.api.description.DescriptionAnalogBI;


public interface I_DescriptionTuple<A extends DescriptionAnalogBI>
        extends I_AmTypedTuple<A>, DescriptionAnalogBI<A> {

    @Override
    public boolean isInitialCaseSignificant();

    @Override
    public String getLang();

    @Override
    public String getText();

    @Override
    public int getConceptNid();

    public int getDescId();

    @Override
    public void setInitialCaseSignificant(boolean capStatus);

    @Override
    public void setLang(String lang);

    @Override
    public void setText(String text);

    /**
     * @deprecated
     */
    public I_DescriptionPart duplicate();

    @Override
    public I_DescriptionPart getMutablePart();

    public I_DescriptionVersioned getDescVersioned();

}
