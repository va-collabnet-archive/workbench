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

import com.mysql.jdbc.Connection;

public interface I_ExportFactory {
	public EpicExportManager getExportManager(String baseDir);
	
	public EpicExportManager getExportManager(String dburl, String user, String pw)  throws Exception ;
	
	public I_EpicLoadFileBuilder getLoadFileBuilder(String masterfile, EpicExportManager em) throws Exception;
	
	public I_EpicExportRecordWriter getWriter(String writerName, String baseDir, Connection conn) throws Exception;
	
	public I_RefsetUsageInterpreter getInterpreter();

	public I_ExportValueConverter getValueConverter(int startingVersion);
}
