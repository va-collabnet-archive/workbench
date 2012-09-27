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
package org.ihtsdo.mojo.maven.derby;

import java.io.IOException;
import java.io.OutputStream;

public final class NullOuputStream extends OutputStream {

    public NullOuputStream() {
        // do nothing.
    }

    @Override
    public void write(final byte[] b) throws IOException {
        // do nothing.
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        // do nothing.
    }

    @Override
    public void flush() throws IOException {
        // do nothing.
    }

    @Override
    public void close() throws IOException {
        // do nothing.
    }

    @Override
    public void write(final int b) throws IOException {
        // do nothing.
    }

    @Override
    public String toString() {
        return "[NullOuputStream - Data is not collected in this OutputStream]";
    }
}
