package org.dwfa.vodb.types;

import java.io.IOException;
import java.util.Date;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinDescPartCore {
	
	private int pathId;
	private int version;
	private int statusId;
	private boolean initialCaseSignificant;
	private int typeId; 
	private String lang;
	
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionPart#hasNewData(org.dwfa.vodb.types.ThinDescPart)
	 */
	public boolean hasNewData(I_DescriptionPart another) {
		return ((this.pathId != another.getPathId()) ||
				(this.statusId != another.getStatusId()) ||
				(this.initialCaseSignificant != another.getInitialCaseSignificant()) ||
				(this.typeId != another.getTypeId()) ||
				((this.lang.equals(another.getLang()) == false)));
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionPart#getPathId()
	 */
	public int getPathId() {
		return pathId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionPart#setPathId(int)
	 */
	public void setPathId(int pathId) {
		if (pathId == -2147483393) {
			throw new RuntimeException("Invaid path: " + pathId);
		}
		this.pathId = pathId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionPart#getInitialCaseSignificant()
	 */
	public boolean getInitialCaseSignificant() {
		return initialCaseSignificant;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionPart#setInitialCaseSignificant(boolean)
	 */
	public void setInitialCaseSignificant(boolean capStatus) {
		this.initialCaseSignificant = capStatus;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionPart#getLang()
	 */
	public String getLang() {
		return lang;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionPart#setLang(java.lang.String)
	 */
	public void setLang(String lang) {
		this.lang = lang;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionPart#getStatusId()
	 */
	public int getStatusId() {
		return statusId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionPart#setStatusId(int)
	 */
	public void setStatusId(int status) {
		this.statusId = status;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionPart#getTypeId()
	 */
	public int getTypeId() {
		return typeId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionPart#setTypeId(int)
	 */
	public void setTypeId(int typeInt) {
		this.typeId = typeInt;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionPart#getVersion()
	 */
	public int getVersion() {
		return version;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionPart#setVersion(int)
	 */
	public void setVersion(int version) {
		this.version = version;
	}
   public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append(" statusId: ");
      buff.append(nidToString(statusId));
      buff.append(" typeId: ");
      buff.append(nidToString(typeId));
      buff.append(" init case sig: ");
      buff.append(initialCaseSignificant);
      buff.append(" lang: ");
      buff.append(lang);
      buff.append(" pathId: ");
      buff.append(nidToString(pathId));
      buff.append(" version: ");
      buff.append(version);
      buff.append(" (");
      buff.append(new Date(ThinVersionHelper.convert(version)));
      buff.append(")");
      
      return buff.toString();
   }
   
   private String nidToString(int nid)  {
      try {
         return ConceptBean.get(nid).getInitialText();
      } catch (IOException e) {
         return Integer.toString(nid);
      }
   }

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionPart#convertIds(org.dwfa.vodb.jar.I_MapNativeToNative)
	 */
	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		pathId = jarToDbNativeMap.get(pathId);
		statusId = jarToDbNativeMap.get(statusId);
		typeId = jarToDbNativeMap.get(typeId);
	}

	@Override
	public boolean equals(Object obj) {
		ThinDescPartCore another = (ThinDescPartCore) obj;
		return ((initialCaseSignificant == another.initialCaseSignificant) &&
				(lang.equals(another.lang)) && 
				(pathId == another.pathId) && 
				(typeId == another.typeId) &&
				(version == another.version));
	}

	@Override
	public int hashCode() {
		int bhash = 0;
		if (initialCaseSignificant) {
			bhash = 1;
		}
		return HashFunction.hashCode(new int[] {
				bhash, lang.hashCode(), pathId, 
				statusId,
				typeId, version
		});
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionPart#duplicate()
	 */
	public ThinDescPartCore duplicate() {
		ThinDescPartCore newPart = new ThinDescPartCore();
		newPart.pathId = pathId;
		newPart.version = version;
		newPart.statusId = statusId;
		newPart.initialCaseSignificant = initialCaseSignificant;
		newPart.typeId = typeId; 
		newPart.lang = lang;
		return newPart;
	}
	
	
}
