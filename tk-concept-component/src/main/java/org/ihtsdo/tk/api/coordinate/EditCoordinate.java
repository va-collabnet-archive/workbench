package org.ihtsdo.tk.api.coordinate;

import org.ihtsdo.tk.api.NidSet;
import org.ihtsdo.tk.api.NidSetBI;


public class EditCoordinate {
	private int authorNid;
        private int moduleNid;
	private NidSetBI editPaths;
        
        public EditCoordinate(int authorNid, int moduleNid, NidSetBI editPathNids) {
		super();
		assert editPathNids != null;
		assert authorNid != Integer.MIN_VALUE;
		this.authorNid = authorNid;
                this.moduleNid = moduleNid;
		this.editPaths = editPathNids;
	}
	
	public EditCoordinate(int authorNid, int moduleNid, int... editPathNids) {
		super();
		assert editPathNids != null;
		assert authorNid != Integer.MIN_VALUE;
		this.authorNid = authorNid;
                this.moduleNid = moduleNid;
		this.editPaths = new NidSet(editPathNids);
	}

	public int getAuthorNid() {
		return authorNid;
	}
        
        public int getModuleNid() {
		return moduleNid;
	}

	public int[] getEditPaths() {
		return editPaths.getSetValues();
	}
        
        public NidSetBI getEditPathsSet(){
            return editPaths;
        }
        
   
   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("authorNid: ").append(authorNid);
      sb.append("moduleNid: ").append(moduleNid);
      sb.append("editPaths: ").append(editPaths);           
      return sb.toString();
   }

}
