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
package org.dwfa.ace.task.input;

import java.text.DecimalFormat;
import java.text.ParseException;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.NumberFormatter;

/**
 * Number formatter - customised formatting class that can optionally
 * include/exclude
 * negative, zero and non-integer numbers. Insertions/replacements must be
 * validated
 * before they are saved.
 * 
 * @author Christine Hill
 */
public class NumberFormatterImpl extends NumberFormatter {

    public boolean allowNegative = true;
    public boolean allowZero = true;
    public boolean allowDouble = true;
    NumberFilter filter = null;
    private static final long serialVersionUID = 1L;

    public NumberFormatterImpl(DecimalFormat decimalFormat, boolean allowNegative, boolean allowZero,
            boolean allowDouble) {
        super(decimalFormat);
        this.allowNegative = allowNegative;
        this.allowZero = allowZero;
        this.allowDouble = allowDouble;
        filter = new NumberFilter();
    }

    protected DocumentFilter getDocumentFilter() {
        return this.filter;
    }

    private class NumberFilter extends DocumentFilter {

        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {

            // insert the new string into the existing string
            String modified = fb.getDocument().getText(0, offset) + string
                + fb.getDocument().getText(offset, fb.getDocument().getLength() - offset);

            // if it's valid, we'll save the modified string
            if (isValidEntry(modified)) {
                super.insertString(fb, offset, string, attr);
            }
        }

        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {

            // insert the replacement string into the existing string
            String modified = fb.getDocument().getText(0, offset) + text
                + fb.getDocument().getText(offset + length, fb.getDocument().getLength() - (offset + length));

            // if it's valid, we'll save the modified string
            if (isValidEntry(modified)) {
                super.replace(fb, offset, length, text, attrs);
            }
        }

        public boolean isValidEntry(String text) {

            // negative sign is allowed as first char
            if (allowNegative && "-".equals(text)) {
                return true;
            }

            if (allowDouble) {
                Double entry = null;

                // check parses as Double
                try {
                    entry = (Double) Double.valueOf(text);
                } catch (NumberFormatException e) {
                    return false;
                }

                // check non-negative
                if (!allowNegative && entry < 0) {
                    return false;
                }
            } else {
                Integer entry = null;

                // check parses as integer
                try {
                    entry = (Integer) Integer.valueOf(text);
                } catch (NumberFormatException e) {
                    return false;
                }

                // check non-negative
                if (!allowNegative && entry < 0) {
                    return false;
                }

                // check non-zero
                if (!allowZero && entry.intValue() == 0) {
                    return false;
                }
            }
            return true;
        }
    }

    public String valueToString(Object o) throws ParseException {
        if (o == null) {
            return "";
        } else {
            Number entry = (Number) o;
            return entry.toString();
        }
    }

    public Object stringToValue(String s) throws ParseException {
        Number entry = null;
        try {
            if (allowDouble) {
                entry = (Double) Double.valueOf(s);
            } else {
                entry = (Integer) Integer.valueOf(s);
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return entry;
    }
}
