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
package org.dwfa.mojo.epicexport.kp;

import java.io.File;
import java.io.IOException;

import org.dwfa.mojo.epicexport.EpicExportManager;
import org.dwfa.mojo.epicexport.I_EpicExportRecordWriter;
import org.dwfa.mojo.epicexport.I_EpicLoadFileBuilder;
import org.dwfa.mojo.epicexport.I_ExportFactory;

public class EpicTermWarehouseFactory implements I_ExportFactory {

    public EpicExportManager getExportManager(String baseDir) {
        return new EpicExportManager(baseDir, this);
    }

    public EpicExportManager getExportManager() {
    	return new EpicExportManager(null, this);
    }

    public I_EpicLoadFileBuilder getLoadFileBuilder(String masterfile, EpicExportManager em) throws Exception {
        I_EpicLoadFileBuilder ret = null;
        /*if (masterfile.equals(EpicExportManager.EPIC_MASTERFILE_NAME_EDG_CLINICAL))
            ret = new EpicExportBuilderEDGClinical(this, em);
        else if (masterfile.equals(EpicExportManager.EPIC_MASTERFILE_NAME_EDG_BILLING))
            ret = new EpicExportBuilderEDGBilling(this, em);
        else
            throw new Exception("Unhandled masterfile: " + masterfile);
        */
        return ret;
    }

    public I_EpicExportRecordWriter getWriter(File fw) throws IOException {
    	throw new UnsupportedOperationException();
    }
    
    public I_EpicExportRecordWriter getWriter(String name) {
    	I_EpicExportRecordWriter ret = new EpicDataWarehouseWriter(name);
    	return ret;
    }

}

