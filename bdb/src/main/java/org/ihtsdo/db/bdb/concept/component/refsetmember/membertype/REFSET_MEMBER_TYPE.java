package org.ihtsdo.db.bdb.concept.component.refsetmember.membertype;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.dwfa.ace.config.AceConfig;
import org.dwfa.cement.RefsetAuxiliary;

/**
 * 
 * @author kec
 *
 */
public enum REFSET_MEMBER_TYPE {
    MEMBER("member", null, RefsetAuxiliary.Concept.MEMBER_EXTENSION.getUids()), 
    BOOLEAN("boolean", null, RefsetAuxiliary.Concept.BOOLEAN_EXTENSION.getUids()), 
    CONCEPT("concept", null, RefsetAuxiliary.Concept.STRING_EXTENSION.getUids()), 
    CON_INT("con int", null, RefsetAuxiliary.Concept.CONCEPT_EXTENSION.getUids()),
    STRING("string", null, RefsetAuxiliary.Concept.INT_EXTENSION.getUids()), 
    INTEGER("integer", null, RefsetAuxiliary.Concept.CONCEPT_INT_EXTENSION.getUids()), 
    MEASUREMENT("measurement", null, RefsetAuxiliary.Concept.LANGUAGE_EXTENSION.getUids()), 
    LANGUAGE("language", null, RefsetAuxiliary.Concept.SCOPED_LANGUAGE_EXTENSION.getUids()), 
    SCOPED_LANGUAGE("scoped language", null, RefsetAuxiliary.Concept.MEASUREMENT_EXTENSION.getUids()), 
    TEMPLATE_FOR_REL("template for rel", null, RefsetAuxiliary.Concept.CROSS_MAP_EXTENSION.getUids()),
    TEMPLATE("template", null, RefsetAuxiliary.Concept.CROSS_MAP_REL_EXTENSION.getUids()),
    CROSS_MAP_FOR_REL("cross map for rel", null, RefsetAuxiliary.Concept.TEMPLATE_EXTENSION.getUids()),
    CROSS_MAP("cross map", null, RefsetAuxiliary.Concept.TEMPLATE_REL_EXTENSION.getUids()),
    CONCEPT_CONCEPT("concept-concept", null, RefsetAuxiliary.Concept.CONCEPT_CONCEPT_EXTENSION.getUids()),
    CONCEPT_CONCEPT_CONCEPT("concept-concept-concept", null, RefsetAuxiliary.Concept.CONCEPT_CONCEPT_CONCEPT_EXTENSION.getUids()),
    CONCEPT_CONCEPT_STRING("concept-concept-string", null, RefsetAuxiliary.Concept.CONCEPT_CONCEPT_STRING_EXTENSION.getUids()),
    CONCEPT_STRING("concept-string", null, RefsetAuxiliary.Concept.CONCEPT_STRING_EXTENSION.getUids()),
    	;
    
    private String interfaceName;
    private I_MakeRefsetMemberParts<?> partFactory;
    private int typeNid;
    
    
    private REFSET_MEMBER_TYPE(String interfaceName, 
    		I_MakeRefsetMemberParts<?> partFactory, Collection<UUID> uids) {
        this.interfaceName = interfaceName;
        this.partFactory = partFactory;
        this.typeNid = AceConfig.getVodb().uuidToNative(uids);
    }

    private static Map<Integer, REFSET_MEMBER_TYPE> typeNidToRefsetMemberTypeMap = 
    	new HashMap<Integer, REFSET_MEMBER_TYPE>();
    	
    public static REFSET_MEMBER_TYPE fromEnumId(int id) {
    	if (typeNidToRefsetMemberTypeMap == null) {
    		typeNidToRefsetMemberTypeMap = new HashMap<Integer, REFSET_MEMBER_TYPE>();
    		for (REFSET_MEMBER_TYPE type: REFSET_MEMBER_TYPE.values()) {
    			typeNidToRefsetMemberTypeMap.put(type.typeNid, type);
    		}
    	}
    	return typeNidToRefsetMemberTypeMap.get(id);
    }

	public int getTypeNid() {
		return typeNid;
	}

	public String getInterfaceName() {
		return interfaceName;
	}

	public I_MakeRefsetMemberParts<?> getPartFactory() {
		return  partFactory;
	}
}
