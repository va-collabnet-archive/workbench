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
package org.dwfa.vodb.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.zip.DataFormatException;

import junit.framework.TestCase;

import org.dwfa.ace.api.I_DescriptionVersioned;

public class ConDescRelBdbTest extends TestCase {

    public void testRoundTrip() {
        List<I_DescriptionVersioned> descriptions = null;
        try {
            ConDescRelBdb.DescriptionCompressionMap map1 = new ConDescRelBdb.DescriptionCompressionMap(descriptions);
            byte[] compressedMapBytes = map1.getBytes();
            ConDescRelBdb.DescriptionCompressionMap map2 = new ConDescRelBdb.DescriptionCompressionMap(
                compressedMapBytes);
            map2.call();

        } catch (UnsupportedEncodingException e) {
            fail(e.toString());
        } catch (DataFormatException e) {
            fail(e.toString());
        } catch (IOException e) {
            fail(e.toString());
        }
    }
}
