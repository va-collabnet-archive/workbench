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


import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.log.AceLog;
import org.dwfa.mojo.epicexport.I_EpicExportRecordWriter;


/** 
 * Class used to build a data warehouse of terms used as master files in the Epic system.
 * Also, associated values that are used to populate master file items from a refset are 
 * passed to this class.
 * When the saveRecord() method is called, the stored values are written to the load
 * file in the format 'n,"value"'.
 * 
 * @author Steven Neiner
 *
 */
public class EpicDataWarehouseWriter implements I_EpicExportRecordWriter {
	private static final String SOURCE_IDENTIFIER = "et";
	private static final String DD_SOURCE_IDENTIFIER = "cs";
	private BufferedWriter writer;
	private String masterfile;
	private String version;
	private String contactType;
	private String database = "test";
	private String masterTable = null;
	private String detailTable = null;
	private String masterTableDD = null;
	private String detailTableDD = null;
	
	private int recordsWrittenCount = 0;
	private int masterId = 0;
	private Connection connection;
	private List<EpicItem> values = new ArrayList<EpicItem>();
	//private List<EpicItem> multiValues;
	
	private List<DatabaseColumn> singleValueItems;
	private List<DatabaseColumn> multiValueItems;
	
	
	public EpicDataWarehouseWriter(String masterfile, Connection conn) {
		try {
			AceLog.getAppLog().info("Initializing writer with masterfile name: " + masterfile);
			this.setMasterfile(masterfile);
			this.connection = conn;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public int getRecordsWrittenCount() {
		return recordsWrittenCount;
	}


	
	

	public void newRecord() {
		values = new ArrayList<EpicItem>();
	}
	
	public void saveRecord() throws IOException {
		/*Collections.sort(values, new Comparator<Object>() {
			public int compare(Object e1, Object e2){
					return ((EpicItem) e1).compareTo((EpicItem) e2);
				}
			}
		); */
		++this.masterId;
		// List<EpicItem> singleItems = seperateMultiValueItems();
		Connection conn = null;
		try {
			conn = this.getConnection();
			if (this.recordsWrittenCount == 0)
				createTable(conn);
			Statement st = (Statement) conn.createStatement();
			StringBuffer vals = new StringBuffer();
			vals.append(masterId);
			vals.append(", ");
			StringBuffer names = new StringBuffer("master_id, ");
			int i = 0;
			for (DatabaseColumn singleItem : this.singleValueItems) {
				names.append("item_value_");
				names.append(singleItem.getName());
				++i;
				Object v = null;
				int j = 0;
				for (EpicItem e: this.values) {
					if (e.itemNumber.equals(singleItem.getName())) {
						if (j++ > 1)
							AceLog.getAppLog().warning("Multiple values for single value item: " + e.itemNumber);
						v = e.value;
					}
				}
				if (v == null)
					vals.append("null");
				else {
					vals.append("\"");
					vals.append(v.toString());
					vals.append("\"");
				}
				if (i < singleValueItems.size()) {
					vals.append(",");
					names.append(",");
				}
			}
			StringBuffer sql = new StringBuffer("insert into ");
			sql.append(this.getMasterTable());
			sql.append(" (");
			sql.append(names);
			sql.append(") values (");
			sql.append(vals);
			sql.append(");");
			// System.out.println(sql.toString());			
			st.execute(sql.toString());

			/**
			 * Save the multi-value items
			 */
			
			for (DatabaseColumn singleItem : this.multiValueItems) {
				i = 0;
				for (EpicItem e: this.values) {
					if (e.itemNumber.equals(singleItem.getName())) {
						sql = new StringBuffer("insert into ");
						names.append(e.getFieldName());
						sql.append(this.getDetailTable());
						sql.append(" (master_id, item_number, item_order, item_value) values (");
						sql.append(masterId);
						sql.append(", '");
						sql.append(e.getItemNumber());
						sql.append("', ");
						sql.append(++i);
						sql.append(", '");
						sql.append(e.value.toString());
						sql.append("');");
						// System.out.println(sql.toString());
						st = (Statement) conn.createStatement();
						st.execute(sql.toString());
					}
				}
			}
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		values = new ArrayList<EpicItem>();
		recordsWrittenCount++;
	}
	
	public void createTable(Connection conn) throws Exception {
		Statement st = (Statement) conn.createStatement();
		try {
			st.execute("drop table if exists " + this.getMasterTable());
		} catch (SQLException se) {
			//Maybe the table doesn't exist, so what
			System.out.println("Error in deleting: " + se.getMessage());
		}
		StringBuffer sql = new StringBuffer("create table ");
		sql.append(this.getMasterTable());
		sql.append(" (");
		// int i = 0;
		sql.append("`master_id` int(11) NOT NULL, ");
		for (DatabaseColumn fname : this.singleValueItems) {
			sql.append("item_value_");
			sql.append(fname.getName());
			sql.append(" ");
			sql.append(fname.getDatatype());
			sql.append(", ");
				
		}
		sql.append("KEY `item_11` (`item_value_11`), ");
		sql.append("KEY `item_2` (`item_value_2`) ");
		sql.append(") ENGINE=MyISAM DEFAULT CHARSET=latin1");
		AceLog.getAppLog().info("Creating table: " + sql.toString());
		st.execute(sql.toString());
		
		/**
		 * Now create the multi value table 
		 */
		try {
			st.execute("drop table if exists " + this.getDetailTable() );
		} catch (SQLException se) {
			//Maybe the table doesn't exist, so what
			System.out.println("Error in deleting: " + se.getMessage());
		}
		
		sql = new StringBuffer("create table ");
		sql.append(this.getDetailTable());
		sql.append(" (`master_id` int(11) NOT NULL, ");
		sql.append("`item_number` int(11) NOT NULL, ");
		sql.append("`item_order` int(11) NOT NULL, ");
		sql.append("`item_value` varchar(255) NOT NULL, ");
		sql.append("KEY `master_id` (`master_id`), ");
		sql.append("KEY `item_number` (`item_number`), ");
		sql.append("KEY `item_order` (`item_order`) ");
		sql.append(") ENGINE=MyISAM DEFAULT CHARSET=latin1");

		AceLog.getAppLog().info("Creating table: " + sql.toString());
		st.execute(sql.toString());

		/* CREATE TABLE  `contextset_qa`.`edc_cs_wsd_detail` (
		  `master_id` int(11) NOT NULL,
		  `item_number` int(11) NOT NULL,
		  `item_order` int(11) NOT NULL,
		  `item_value` varchar(255) NOT NULL,
		  KEY `edc_cs_wsd_detail_idx1` (`master_id`),
		  KEY `edc_cs_wsd_detail_idx2` (`item_number`),
		  KEY `edc_cs_wsd_detail_idx3` (`item_order`)
		) ENGINE=MyISAM DEFAULT CHARSET=latin1; */
	}
	
	public void writeLine(String str) throws IOException {
		throw new UnsupportedOperationException();
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
		// Nothing to do
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

	public void setMasterfile(String masterfile) throws Exception {
		this.masterfile = masterfile;
		String usage = null;
		if (this.masterfile.contains("billing") || this.masterfile.contains("clinical") || 
				this.masterfile.contains("charge"));
			usage = masterfile.substring(3);
		
		String ini = this.masterfile.substring(0, 3);
		this.masterTable = buildMasterFileName(ini, EpicDataWarehouseWriter.SOURCE_IDENTIFIER, usage, "master");
		this.detailTable = buildMasterFileName(ini, EpicDataWarehouseWriter.SOURCE_IDENTIFIER, usage, "detail");
		this.masterTableDD = buildMasterFileName(ini, EpicDataWarehouseWriter.DD_SOURCE_IDENTIFIER, usage, "master");
		this.detailTableDD = buildMasterFileName(ini, EpicDataWarehouseWriter.DD_SOURCE_IDENTIFIER, usage, "detail");
		buildItemLists();
	}
	
	public String getMasterTable() {
		return getTableName(this.masterTable);
	}
	
	public String getDetailTable() {
		return getTableName(this.detailTable);
	}

	public String getTableName(String base) {
		StringBuffer ret = new StringBuffer();
		if (this.database != null) {
			ret.append(database);
			ret.append('.');
		}
		ret.append(base);
		return ret.toString();
	}

	private void buildItemLists() throws Exception {
		this.singleValueItems = new ArrayList<DatabaseColumn>();
		this.multiValueItems = new ArrayList<DatabaseColumn>();
		
		Connection conn = this.getConnection();
		Statement st = (Statement) conn.createStatement();
		String baseSql = "select cs_item_num from contextset_qa.data_dict_cs_pn_xref where cs_extract = ";
		ResultSet r = st.executeQuery(baseSql + "'" + this.masterTableDD + "'");
		while (r.next()) {
			this.singleValueItems.add(new DatabaseColumn(r.getString("cs_item_num"), "varchar(80)"));
		}
		r = st.executeQuery(baseSql + "'" + this.detailTableDD + "'");
		while (r.next()) {
			this.multiValueItems.add(new DatabaseColumn(r.getString("cs_item_num"), "varchar(80)"));
		}
		assureColumnIsAdded("5");
		assureColumnIsAdded("icd9");
		assureColumnIsAdded("icd10");
		assureColumnIsAdded("snomed");
		
	}
	
	private void assureColumnIsAdded(String name) {
		DatabaseColumn d = this.getColumn(name);
		if (d == null)
			this.singleValueItems.add(new DatabaseColumn(name, "varchar(20)"));
	}
	
	private DatabaseColumn getColumn(String name) {
		for (DatabaseColumn d: this.singleValueItems) {
			if (d.getName().equalsIgnoreCase(name))
				return d;
		}
		return null;
	}
	
	private String buildMasterFileName(String ini, String identifier, String usage, String type) {
		StringBuffer ret = new StringBuffer(ini);
		ret.append('_');
		ret.append(identifier);
		if (usage != null) {
			ret.append('_');
			ret.append(usage);
		}
		ret.append('_');
		ret.append(type);
		return ret.toString();
	}
	public String getContactType() {
		return contactType;
	}

	public void setContactType(String contactType) {
		this.contactType = contactType;
	}

	public String getSummary() {
		String ret = "Wrote " + this.getRecordsWrittenCount() + " records";
		if (this.masterTable != null)
			ret = ret.concat(" to " + this.masterTable);
		return ret;
	}

	public Connection getConnection() throws Exception {
		if (this.connection == null) {
			 Class.forName("com.mysql.jdbc.Driver").newInstance();
		     String url = "jdbc:mysql://cmtprod-web01.dwny.ca.kp.org";
		     this.connection = (Connection) DriverManager.getConnection(url, "relgen", "relgen");

		}
		return this.connection;
	}

	public void clearRecordContents() {
		values = new ArrayList<EpicItem>();
	}

	/*public List<EpicItem> seperateMultiValueItems() {
		int i = 0;
		List<EpicItem> singles = new ArrayList<EpicItem>();
		this.multiValues = new ArrayList<EpicItem>();
		for (EpicItem e: values) {
			int n = 1;
			boolean duplicate = false;
			while (i + n < values.size() && e.itemNumber == values.get(i + n).itemNumber) {
				this.multiValues.add(values.get(i + n));
				n++;
				duplicate = true;
			}
			if  (duplicate) {
				this.multiValues.add(values.get(i));
			}
			else
				singles.add(e);
			i++;
		}
		System.out.println("Single value items:");
		for (EpicItem e: singles)
			System.out.println(e.itemNumber);
		System.out.println("Multi value items:");
		for (EpicItem e: multiValues)
			System.out.println(e.itemNumber + ": " + e.value);
		return singles;
		
	}
	*/
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

		@SuppressWarnings("unused")
		public String toExportLine() {
			return itemNumber.concat(",\"").concat(value.toString()).concat("\"");
		}
		
		@SuppressWarnings("unused")
		public int compareTo(EpicItem x) {
			int ret = this.itemNumber.compareTo(x.getItemNumber());
			if (ret == 0)
				ret = this.position - x.getPosition();
			return ret;
		}
		
		public String getFieldName() {
			return "item_value_".concat(itemNumber);
		}
	}
	
	public class DatabaseColumn {
		private String name;
		private String datatype;
		
		public DatabaseColumn(String name, String datatype) {
			this.name = name;
			this.datatype = datatype;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getDatatype() {
			return datatype;
		}
		public void setDatatype(String datatype) {
			this.datatype = datatype;
		}
		
	}
}
