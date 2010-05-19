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
/*
 * Created on Feb 14, 2005
 */
package org.dwfa.bpa.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author kec
 * 
 */
public class FilterInputStreamWithProgress extends FilterInputStream implements MonitorableProcess {
    private int nread = 0;

    private int size = 0;

    private boolean done = false;

    private boolean stop = false;

    /**
     * @param in
     */
    public FilterInputStreamWithProgress(InputStream in) {
        super(in);
        try {
            size = in.available();
        } catch (IOException ioe) {
            size = 0;
        }
    }

    public int read() throws IOException {
        if (stop) {
            throw new IOException("Process canceled");
        }
        int c = in.read();
        if (c >= 0) {
            nread++;
        }
        return c;
    }

    public int read(byte b[]) throws IOException {
        if (stop) {
            throw new IOException("Process canceled");
        }
        int nr = in.read(b);
        if (nr > 0) {
            nread += nr;
        }
        return nr;
    }

    public int read(byte b[], int off, int len) throws IOException {
        if (stop) {
            throw new IOException("Process canceled");
        }
        int nr = in.read(b, off, len);
        if (nr > 0) {
            nread += nr;
        }
        return nr;
    }

    public long skip(long n) throws IOException {
        if (stop) {
            throw new IOException("Process canceled");
        }
        long nr = in.skip(n);
        if (nr > 0) {
            nread += nr;
        }
        return nr;
    }

    public void close() throws IOException {
        in.close();
        setDone(true);
    }

    /**
     * 
     */
    private synchronized void setDone(boolean done) {
        this.done = done;
        this.notifyAll();
    }

    public synchronized void reset() throws IOException {
        if (stop) {
            throw new IOException("Process canceled");
        }
        in.reset();
        nread = size - in.available();
    }

    /**
     * @see org.dwfa.bpa.util.TaskWithProgress#getLengthOfTask()
     */
    public int getLengthOfTask() {

        return size;
    }

    /**
     * @see org.dwfa.bpa.util.TaskWithProgress#getCurrent()
     */
    public int getCurrent() {
        return nread;
    }

    /**
     * @see org.dwfa.bpa.util.TaskWithProgress#stop()
     */
    public void stop() {
        this.stop = true;

    }

    /**
     * @see org.dwfa.bpa.util.TaskWithProgress#isDone()
     */
    public boolean isDone() {
        return done;
    }

    /**
     * @see org.dwfa.bpa.util.MonitorableProcess#waitTillDone()
     */
    public void waitTillDone() throws InterruptedException {
        while (!this.isDone())
            wait();

    }

}
