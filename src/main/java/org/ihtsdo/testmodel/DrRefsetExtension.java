package org.ihtsdo.testmodel;

import java.util.ArrayList;
import java.util.List;

public class DrRefsetExtension extends DrComponent{
	private String primordialUuid;

	private String refsetUuid;
	private String componentUuid;

	private String c1Uuid;
	private String c2Uuid;
	private String c3Uuid;
	private String strValue;
	private boolean booleanValue;
	private float floatValue;
	private int intValue;
	private long longValue;
	private RefsetType refsetType;

	private List<DrIdentifier> identifiers;

	public DrRefsetExtension() {
		identifiers = new ArrayList<DrIdentifier>();
	}

	public String getPrimordialUuid() {
		return primordialUuid;
	}

	public void setPrimordialUuid(String primordialUuid) {
		this.primordialUuid = primordialUuid;
	}

	public String getC1Uuid() {
		return c1Uuid;
	}

	public void setC1Uuid(String uuid) {
		c1Uuid = uuid;
	}

	public String getC2Uuid() {
		return c2Uuid;
	}

	public void setC2Uuid(String uuid) {
		c2Uuid = uuid;
	}

	public String getC3Uuid() {
		return c3Uuid;
	}

	public void setC3Uuid(String uuid) {
		c3Uuid = uuid;
	}

	public String getStrValue() {
		return strValue;
	}

	public void setStrValue(String strValue) {
		this.strValue = strValue;
	}

	public boolean isBooleanValue() {
		return booleanValue;
	}

	public void setBooleanValue(boolean booleanValue) {
		this.booleanValue = booleanValue;
	}

	public float getFloatValue() {
		return floatValue;
	}

	public void setFloatValue(float floatValue) {
		this.floatValue = floatValue;
	}

	public int getIntValue() {
		return intValue;
	}

	public void setIntValue(int intValue) {
		this.intValue = intValue;
	}

	public long getLongValue() {
		return longValue;
	}

	public void setLongValue(long longValue) {
		this.longValue = longValue;
	}

	public List<DrIdentifier> getIdentifiers() {
		return identifiers;
	}

	public void setIdentifiers(List<DrIdentifier> identifiers) {
		this.identifiers = identifiers;
	}

	public RefsetType getRefsetType() {
		return refsetType;
	}

	public void setRefsetType(RefsetType refsetType) {
		this.refsetType = refsetType;
	}

	public String getRefsetUuid() {
		return refsetUuid;
	}

	public void setRefsetUuid(String refsetUuid) {
		this.refsetUuid = refsetUuid;
	}

	public String getComponentUuid() {
		return componentUuid;
	}

	public void setComponentUuid(String componentUuid) {
		this.componentUuid = componentUuid;
	}

}
