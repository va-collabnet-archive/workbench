package org.dwfa.ace.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartInt;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

public class RefsetPropertyMap {

	private EConcept.REFSET_TYPES memberType;
	
	public RefsetPropertyMap() {
		super();
	}
	
	

	public RefsetPropertyMap(REFSET_TYPES memberType) {
		super();
		this.memberType = memberType;
	}



	public enum REFSET_PROPERTY {
	    STATUS, 
	    VERSION, 
	    TIME, 
	    PATH,
	    CID_ONE, 
	    CID_TWO, 
	    CID_THREE,
	    BOOLEAN_VALUE, 
	    INTEGER_VALUE, 
	    STRING_VALUE,
	}
	protected HashMap<REFSET_PROPERTY, Object> properties = new HashMap<REFSET_PROPERTY, Object>();
	public boolean containsKey(Object key) {
		return properties.containsKey(key);
	}
	public Set<Entry<REFSET_PROPERTY, Object>> entrySet() {
		return properties.entrySet();
	}
	public Set<REFSET_PROPERTY> keySet() {
		return properties.keySet();
	}
	public Object put(REFSET_PROPERTY key, Number value) {
		return properties.put(key, value);
	}
	public Object put(REFSET_PROPERTY key, String value) {
		assert key == REFSET_PROPERTY.STRING_VALUE;
		return properties.put(key, value);
	}
	public Object put(REFSET_PROPERTY key, Boolean value) {
		assert key == REFSET_PROPERTY.BOOLEAN_VALUE;
		return properties.put(key, value);
	}
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " " + properties;
	}
	
	public RefsetPropertyMap with(REFSET_PROPERTY key, Number value) {
		put(key, value);
		return this;
	}
	public RefsetPropertyMap with(REFSET_PROPERTY key, String value) {
		assert key == REFSET_PROPERTY.STRING_VALUE;
		properties.put(key, value);
		return this;
	}
	public RefsetPropertyMap with(REFSET_PROPERTY key, Boolean value) {
		assert key == REFSET_PROPERTY.BOOLEAN_VALUE;
		properties.put(key, value);
		return this;
	}
	
	public boolean hasProperty(REFSET_PROPERTY key) {
		return properties.containsKey(key);
	}
	public void writeTo(I_ExtendByRefPart part) {
		setProperties(part);
	}
	public void setProperties(I_ExtendByRefPart part) {
		for (Entry<REFSET_PROPERTY, Object> entry: properties.entrySet()) {
			switch (entry.getKey()) {
			case BOOLEAN_VALUE:
				I_ExtendByRefPartBoolean booleanPart = (I_ExtendByRefPartBoolean) part;
				booleanPart.setBooleanValue((Boolean) entry.getValue());
				break;
			case CID_ONE:
				I_ExtendByRefPartCid c1part = (I_ExtendByRefPartCid) part;
				c1part.setC1id((Integer) entry.getValue());
				break;
			case CID_THREE:
				I_ExtendByRefPartCidCidCid c3part = (I_ExtendByRefPartCidCidCid) part;
				c3part.setC3id((Integer) entry.getValue());
				break;
			case CID_TWO:
				I_ExtendByRefPartCidCid c2part = (I_ExtendByRefPartCidCid) part;
				c2part.setC2id((Integer) entry.getValue());
				break;
			case INTEGER_VALUE:
				I_ExtendByRefPartInt intPart = (I_ExtendByRefPartInt) part;
				intPart.setIntValue((Integer) entry.getValue());
				break;
			case PATH:
				part.setPathId((Integer) entry.getValue());
				break;
			case STATUS:
				// done during makeAnalog;
				break;
			case STRING_VALUE:
				I_ExtendByRefPartStr strPart = (I_ExtendByRefPartStr) part;
				strPart.setStringValue((String) entry.getValue());
				break;
			case VERSION:	
				part.setTime(Terms.get().convertToThickVersion((Integer) entry.getValue()));
				break;
			case TIME:
				part.setTime((Long) entry.getValue());
				break;

			default:
				throw new RuntimeException("Can't handle: " + entry.getKey());
			}
		}
	}
	
    public boolean validate(I_ExtendByRefPart part) {
    	if (memberType != null) {
    		if (REFSET_TYPES.classToType(part.getClass()) != memberType) {
    			return false;
    		}
    	}
    	for (Entry<REFSET_PROPERTY, Object> entry: properties.entrySet()) {
			switch (entry.getKey()) {
			case BOOLEAN_VALUE:
				if (!I_ExtendByRefPartBoolean.class.isAssignableFrom(part.getClass())) {
					return false;
				}
				I_ExtendByRefPartBoolean booleanPart = (I_ExtendByRefPartBoolean) part;
				if (!entry.getValue().equals(booleanPart.getBooleanValue())) {
					return false;
				}
				break;
			case CID_ONE:
				if (!I_ExtendByRefPartCid.class.isAssignableFrom(part.getClass())) {
					return false;
				}
				I_ExtendByRefPartCid c1part = (I_ExtendByRefPartCid) part;
				if (!entry.getValue().equals(c1part.getC1id())) {
					return false;
				}
				break;
			case CID_THREE:
				if (!I_ExtendByRefPartCidCidCid.class.isAssignableFrom(part.getClass())) {
					return false;
				}
				I_ExtendByRefPartCidCidCid c3part = (I_ExtendByRefPartCidCidCid) part;
				if (!entry.getValue().equals(c3part.getC3id())) {
					return false;
				}
				break;
			case CID_TWO:
				if (!I_ExtendByRefPartCidCid.class.isAssignableFrom(part.getClass())) {
					return false;
				}
				I_ExtendByRefPartCidCid c2part = (I_ExtendByRefPartCidCid) part;
				if (!entry.getValue().equals(c2part.getC2id())) {
					return false;
				}
				break;
			case INTEGER_VALUE:
				if (!I_ExtendByRefPartInt.class.isAssignableFrom(part.getClass())) {
					return false;
				}
				I_ExtendByRefPartInt intPart = (I_ExtendByRefPartInt) part;
				if (!entry.getValue().equals(intPart.getIntValue())) {
					return false;
				}
				break;
			case PATH:
				if (!entry.getValue().equals(part.getPathId())) {
					return false;
				}
				break;
			case STATUS:
				// done during makeAnalog;
				break;
			case STRING_VALUE:
				if (!I_ExtendByRefPartStr.class.isAssignableFrom(part.getClass())) {
					return false;
				}
				I_ExtendByRefPartStr strPart = (I_ExtendByRefPartStr) part;
				if (!entry.getValue().equals(strPart.getStringValue())) {
					return false;
				}
				break;
			case VERSION:	
				if (part.getVersion() != (Integer) entry.getValue()) {
				    return false;
				}
				break;
            case TIME:   
                if (part.getTime() != (Long) entry.getValue()) {
                    return false;
                }
                break;

			default:
				throw new RuntimeException("Can't handle: " + entry.getKey());
			}
		}
        return true;
    }
    
	public int getInt(REFSET_PROPERTY key) {
		return (Integer) properties.get(key);
	}
	public long getLong(REFSET_PROPERTY key) {
		assert key == REFSET_PROPERTY.TIME;
		return (Long) properties.get(key);
	}
	public String getString(REFSET_PROPERTY key) {
		assert key == REFSET_PROPERTY.STRING_VALUE;
		return (String) properties.get(key);
	}
	public boolean getBoolean(REFSET_PROPERTY key) {
		assert key == REFSET_PROPERTY.BOOLEAN_VALUE;
		return (Boolean) properties.get(key);
	}
	public EConcept.REFSET_TYPES getMemberType() {
		return memberType;
	}
	public void setMemberType(EConcept.REFSET_TYPES memberType) {
		this.memberType = memberType;
	}


}
