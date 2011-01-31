package org.ihtsdo.qadb.data;


public class QADatabase {
	
	private String databaseUuid;
	private String name;
	
	public QADatabase(){
		super();
	}
	
	public QADatabase(String databaseUuid, String name) {
		super();
		this.databaseUuid = databaseUuid;
		this.name = name;
	}

	public String getDatabaseUuid() {
		return databaseUuid;
	}

	public void setDatabaseUuid(String databaseUuid) {
		this.databaseUuid = databaseUuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String toString() {
		return name;
	}

}
