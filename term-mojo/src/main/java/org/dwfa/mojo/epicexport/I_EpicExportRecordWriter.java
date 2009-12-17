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

import java.io.IOException;

public interface I_EpicExportRecordWriter {

    public int getRecordsWrittenCount();

    public void newRecord();

    public void saveRecord() throws IOException;

    public void writeLine(String str) throws IOException;

    public void addItemValue(String itemNumber, Object value, int pos);

    public void addItemValue(String itemNumber, Object value);

    public String getSummary();

    public void close() throws IOException;

    public class EpicItem {
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
