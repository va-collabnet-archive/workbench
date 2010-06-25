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
package org.dwfa.gui.toggle;

import javax.swing.Action;
import javax.swing.Icon;

public class Toggle32x32 extends Toggle24x24 {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    protected int getPixels() {
        return 44;
    }

    public Toggle32x32() {
        super();
    }

    public Toggle32x32(Action arg0) {
        super(arg0);
    }

    public Toggle32x32(Icon arg0, boolean arg1) {
        super(arg0, arg1);
    }

    public Toggle32x32(Icon arg0) {
        super(arg0);
    }

    public Toggle32x32(String arg0, boolean arg1) {
        super(arg0, arg1);
    }

    public Toggle32x32(String arg0, Icon arg1, boolean arg2) {
        super(arg0, arg1, arg2);
    }

    public Toggle32x32(String arg0, Icon arg1) {
        super(arg0, arg1);
    }

    public Toggle32x32(String arg0) {
        super(arg0);
    }

}
