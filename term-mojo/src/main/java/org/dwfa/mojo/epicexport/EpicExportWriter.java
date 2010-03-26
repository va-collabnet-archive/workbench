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
package org.dwfa.mojo.epicexport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.dwfa.ace.log.AceLog;
import org.dwfa.mojo.epicexport.kp.EpicSoftDeleteWriter;
import org.dwfa.mojo.epicexport.kp.RegionalHibernationBuilder;

/**
 * Class used to store values to be written to a load file, and write to that
 * load file.
 * From an export builder,(see AbstractEpicLoadFileBuilder), the method
 * addItemValue
 * is called to store the values.
 * When the saveRecord() method is called, the stored values are written to the
 * load
 * file in the format 'n,"value"'.
 * 
 * @author Steven Neiner
 * 
 */
public class EpicExportWriter implements I_EpicExportRecordWriter {
    private BufferedWriter writer;
    private String masterfile;
    private String version;
    private String contactType;
    private String filename;
    private int recordsWrittenCount = 0;

    public int getRecordsWrittenCount() {
        return recordsWrittenCount;
    }

    private List<EpicItem> values = new ArrayList<EpicItem>();;

    public EpicExportWriter(BufferedWriter wrtr) {
    	newRecord();
        this.writer = wrtr;
    }

    public EpicExportWriter(BufferedWriter wrtr, String filename) {
        this.writer = wrtr;
        this.filename = filename;
    }

    public void newRecord() {
        values = new ArrayList<EpicItem>();
    }

    public void saveRecord() throws IOException {
        Collections.sort(values, new Comparator<Object>() {
            public int compare(Object e1, Object e2) {
                return ((EpicItem) e1).compareTo((EpicItem) e2);
            }
        });
        for (Iterator<EpicItem> i = values.listIterator(); i.hasNext();) {
            EpicItem e = (EpicItem) i.next();
            this.writer.write(e.toExportLine());
            this.writer.write("\r\n");
        }
        recordsWrittenCount++;
    }

    public void writeLine(String str) throws IOException {
        this.writer.write(str);
        this.writer.write("\r\n");
    }

    public void addItemValue(String itemNumber, Object value, int pos) {
        values.add(new EpicItem(itemNumber, value, pos));
    }

    public void addItemValue(String itemNumber, Object value) {
        addItemValue(itemNumber, value, 1);
    }

    public BufferedWriter getWriter() {
        return writer;
    }

    public void close() throws IOException {
        writer.close();
    }

    public void setWriter(BufferedWriter writer) {
        this.writer = writer;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMasterfile() {
        return masterfile;
    }

    public void setMasterfile(String masterfile) {
        this.masterfile = masterfile;
    }

    public String getContactType() {
        return contactType;
    }

    public void setContactType(String contactType) {
        this.contactType = contactType;
    }

    public String getSummary() {
        String ret = "Wrote " + this.getRecordsWrittenCount() + " records";
        if (this.filename != null)
            ret = ret.concat(" to " + this.filename);
        return ret;
    }

    public static void writeSoftDeleteFile(EpicExportManager em, String writerName, 
    		String cid, String masterfile) throws IOException {
    	I_EpicExportRecordWriter w;
    	HashMap<String, I_EpicExportRecordWriter> writers = em.getWriters();
    	if ((w = writers.get(writerName)) == null) {
            StringBuffer filename = new StringBuffer(em.getBaseDir());
            filename.append(writerName);
            filename.append(".txt");
            System.out.println("Creating " + filename.toString());
            File fw = new File(filename.toString());
            fw.mkdirs();
            if (fw.exists())
                fw.delete();

        	BufferedWriter bw = new BufferedWriter(new FileWriter(fw));
        	w = new EpicSoftDeleteWriter(bw, writerName);
        	AceLog.getAppLog().info("Creating " + filename);
        	em.getWriters().put(writerName, w);
    	}
    	w.newRecord();
    	w.addItemValue("11", cid);
    	w.addItemValue("ini", masterfile.toUpperCase().substring(0, 3));
    	w.saveRecord();
    }

    private class EpicItem {
        private String itemNumber;
        private Object value;
        private int position;

        public EpicItem(String num, Object val, int pos) {
            itemNumber = num;
            value = val;
            position = pos;
        }

        public String getItemNumber() {
            return itemNumber;
        }

        public int getPosition() {
            return position;
        }

        public String toExportLine() {
            if (value == null)
                return "";
            else
                return itemNumber.concat(",\"").concat(value.toString()).concat("\"");
        }

        public int compareTo(EpicItem x) {
            int ret = this.itemNumber.compareTo(x.getItemNumber());
            if (ret == 0)
                ret = this.position - x.getPosition();
            return ret;
        }
    }
}
