package org.dwfa.ace.api;

public class DatabaseSetupConfig {
	
	public enum CORE_DB_TYPE { LAZY, CON_DESC, CON_DESC_REL, CON_COMPDESC_REL, CON_DESCMAP_REL };
	public enum ID_DB_TYPE { UUID_MAP_PRIMARY, UUID_MAP_SECONDARY, UUID_MAP_PRIMARY_WITH_CORES };

	private CORE_DB_TYPE coreDbType = CORE_DB_TYPE.CON_DESCMAP_REL;
	
	private ID_DB_TYPE idDbType = ID_DB_TYPE.UUID_MAP_PRIMARY_WITH_CORES;

	public CORE_DB_TYPE getCoreDbType() {
		return coreDbType;
	}

	public void setCoreDbType(CORE_DB_TYPE coreDbType) {
		this.coreDbType = coreDbType;
	}

	public void setCoreDbTypeStr(String coreDbTypeStr) {
		this.coreDbType = CORE_DB_TYPE.valueOf(coreDbTypeStr);
	}

	public String getCoreDbTypeStr() {
		return coreDbType.name();
	}

	public ID_DB_TYPE getIdDbType() {
		return idDbType;
	}

	public void setIdDbType(ID_DB_TYPE idDbType) {
		this.idDbType = idDbType;
	}
	
	public void setIdDbTypeStr(String idDbTypeStr) {
		this.idDbType = ID_DB_TYPE.valueOf(idDbTypeStr);
	}

	public String getIdDbTypeStr() {
		return idDbType.name();
	}

}
