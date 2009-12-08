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
package org.dwfa.ace.url.tiuid;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class Handler extends URLStreamHandler {

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        return new BdbImageConnection(u);
    }

    @Override
    protected void parseURL(URL u, String spec, int start, int limit) {

        this.setURL(u, ExtendedUrlStreamHandlerFactory.PROTOCOL, "", -1, "", "", spec.substring(start),
            spec.substring(start), "");
    }

    @Override
    protected String toExternalForm(URL u) {
        return ExtendedUrlStreamHandlerFactory.PROTOCOL + ":" + u.getQuery();
    }

}
