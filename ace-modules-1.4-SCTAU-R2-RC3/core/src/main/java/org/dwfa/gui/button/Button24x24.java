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
package org.dwfa.gui.button;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;

public class Button24x24 extends JButton {

    /**
    * 
    */
    private static final long serialVersionUID = 1L;

    protected int getPixels() {
        return 36;
    }

    protected Dimension getPixelDim() {
        return new Dimension(getPixels(), getPixels());
    }

    private static void setSize(JButton button, int pixels) {
        Dimension size = new Dimension(pixels, pixels);
        button.setMaximumSize(size);
        button.setSize(size);
        button.setMinimumSize(size);
    }

    public Button24x24() {
        super();
        setSize(this, getPixels());
    }

    public Button24x24(Action arg0) {
        super(arg0);
        setSize(this, getPixels());
    }

    public Button24x24(Icon arg0) {
        super(arg0);
        setSize(this, getPixels());
    }

    public Button24x24(String arg0, Icon arg1) {
        super(arg0, arg1);
        setSize(this, getPixels());
    }

    public Button24x24(String arg0) {
        super(arg0);
        setSize(this, getPixels());
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, getPixels(), getPixels());
    }

    @Override
    public void setBounds(Rectangle r) {
        super.setBounds(new Rectangle(r.x, r.y, getPixels(), getPixels()));
    }

    @Override
    public void setSize(Dimension arg0) {
        super.setSize(getPixelDim());
    }

    @Override
    public void setSize(int arg0, int arg1) {
        super.setSize(getPixels(), getPixels());
    }

    @Override
    public void setMaximumSize(Dimension arg0) {
        super.setMaximumSize(getPixelDim());
    }

    @Override
    public void setMinimumSize(Dimension arg0) {
        super.setMinimumSize(getPixelDim());
    }

    @Override
    public void setPreferredSize(Dimension arg0) {
        super.setPreferredSize(getPixelDim());
    }

}
