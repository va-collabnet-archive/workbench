package org.ihtsdo.mojo.mojo.exporter.publishers;

import java.util.Properties;

public class PublisherDTO {
	
	public String id_uuidS;
	public int id_int;
	public String name;
	public String publisherClassName;
	public String outputDirectory;
	
	public String errMsg = "";
	public boolean pdtoOK = true;
	
	public Properties configProps;

	public String getId_uuidS() {
		return id_uuidS;
	}

	public void setId_uuidS(String idUuidS) {
		id_uuidS = idUuidS;
	}

	public int getId_int() {
		return id_int;
	}

	public void setId_int(int idInt) {
		id_int = idInt;
	}

	public Properties getConfigProps() {
		//configProps.setProperty(key, value)
		return configProps;
	}

	public void setConfigProps(Properties configProps) {
		this.configProps = configProps;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPublisherClassName() {
		return publisherClassName;
	}

	public void setPublisherClassName(String publisherClassName) {
		this.publisherClassName = publisherClassName;
	}

	public String getOutputDirectory() {
		return outputDirectory;
	}

	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}
	
	public void addMsg(String msg){
		errMsg = errMsg +msg+ " \n";
		
		
	}

	public boolean isPdtoOK() {
		if(name == null || name.length() == 0){
			addMsg("no name set");
			pdtoOK = false;
		}
		if(outputDirectory == null || outputDirectory.length() == 0){
			addMsg("no outputDirectory set");
			pdtoOK = false;
		}
		if(publisherClassName == null || publisherClassName.length() == 0){
			addMsg("no publisherClassName set");
			pdtoOK = false;
		}
		if(id_uuidS == null || id_uuidS.length() == 0){
			addMsg("no id_uuidS set");
			pdtoOK = false;
		}
		
		return pdtoOK;
	}

	public void setPdtoOK(boolean pdtoOK) {
		this.pdtoOK = pdtoOK;
	}
	
	

}
