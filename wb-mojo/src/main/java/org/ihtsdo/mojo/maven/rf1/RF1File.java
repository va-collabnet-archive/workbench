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
package org.ihtsdo.mojo.maven.rf1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.maven.plugin.MojoFailureException;

public class RF1File implements Comparable<Object> {
    File file;
    String revDateStr;
    Date revDate;

    public RF1File(File f, String wDir, String subDir, String dateStr, Rf1Dir rf1Dir)
            throws MojoFailureException {
        this.file = f;
        this.revDateStr = dateStr;

        String pattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        try {
            this.revDate = formatter.parse(revDateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new MojoFailureException("SimpleDateFormat parse error");
        }
    }

    @Override
    public int compareTo(Object o) {
        RF1File tmp = (RF1File) o;

        if (this.revDate.before(tmp.revDate)) {
            return -1; // instance less than received
        } else if (this.revDate.after(tmp.revDate)) {
            return 1; // instance greater than received
        } else {
            return 0; // instance == received
        }
    }
    
    public static int countFileLines(RF1File rf1) throws MojoFailureException {
        int lineCount = 0;
        BufferedReader br = null;
    
        try {
            br = new BufferedReader(new FileReader(rf1.file));
            try {
                while (br.readLine() != null) {
                    lineCount++;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                throw new MojoFailureException("FAILED: error counting lines in " + rf1.file, ex);
            } finally {
                br.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new MojoFailureException("FAILED: error open BufferedReader for " + rf1.file, ex);
        }
    
        // lineCount NOTE: COUNT -1 BECAUSE FIRST LINE SKIPPED
        // lineCount NOTE: REQUIRES THAT LAST LINE IS VALID RECORD
        return lineCount - 1;
    }


}
