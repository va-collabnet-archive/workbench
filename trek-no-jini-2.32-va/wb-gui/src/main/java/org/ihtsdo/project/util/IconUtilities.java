/*
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.project.util;

import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.view.ProjectsPanel;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.spec.ValidationException;

/**
 * The Class IconUtilities.
 */
public class IconUtilities {

    /**
     * The red icon.
     */
    public static Icon redIcon = new ImageIcon("icons/91.png");
    /**
     * The attribute icon.
     */
    public static Icon attributeIcon = new ImageIcon("icons/ConceptStatus.gif");
    /**
     * The orange icon.
     */
    public static Icon orangeIcon = new ImageIcon("icons/90.png");
    /**
     * The description icon.
     */
    public static Icon descriptionIcon = new ImageIcon("icons/Description.gif");
    /**
     * The fsn icon.
     */
    public static Icon fsnIcon = new ImageIcon("icons/Name.gif");
    /**
     * The black icon.
     */
    public static Icon blackIcon = new ImageIcon("icons/85.png");
    /**
     * The preferred icon.
     */
    public static Icon preferredIcon = new ImageIcon("icons/Preferred.gif");
    /**
     * The role group icon.
     */
    public static Icon roleGroupIcon = new ImageIcon("icons/rolegroup.gif");
    /**
     * The folder.
     */
    public static Icon folder = new ImageIcon("icons/folder.png");
    /**
     * The association icon.
     */
    public static Icon associationIcon = new ImageIcon("icons/Association.gif");
    /**
     * The not accept icon.
     */
    public static Icon notAcceptIcon = new ImageIcon("icons/NotAccept.gif");
    /**
     * The help icon.
     */
    public static Icon helpIcon = new ImageIcon("icons/help16.png");
    /**
     * The check icon.
     */
    public static Icon checkIcon = new ImageIcon("icons/check.png");
    /**
     * The add icon.
     */
    public static Icon addIcon = new ImageIcon("icons/add.png");
    /**
     * The delete icon.
     */
    public static Icon deleteIcon = new ImageIcon("icons/delete.png");
    /**
     * The media_step_forward icon.
     */
    public static Icon media_step_forwardIcon = new ImageIcon("icons/media_step_forward.png");
    /**
     * The Translation_project.
     */
    public static Icon Translation_project = new ImageIcon("icons/branch_element.png");
    /**
     * The Worksets.
     */
    public static Icon Worksets = new ImageIcon("icons/cubes_blue.png");
    /**
     * The Workset.
     */
    public static Icon Workset = new ImageIcon("icons/cube_blue.png");
    /**
     * The Partition_schemes.
     */
    public static Icon Partition_schemes = new ImageIcon("icons/cubes.png");
    /**
     * The Partition_scheme.
     */
    public static Icon Partition_scheme = new ImageIcon("icons/cubes_green.png");
    /**
     * The Partition.
     */
    public static Icon Partition = new ImageIcon("icons/cube_green.png");
    /**
     * The Worklists.
     */
    public static Icon Worklists = new ImageIcon("icons/tables.png");
    /**
     * The Worklist_statistics.
     */
    public static Icon Worklist_statistics = new ImageIcon("icons/column-chart.png");
    /**
     * The Worklist.
     */
    public static Icon Worklist = new ImageIcon("icons/table.png");
    /**
     * The Source_ language_ refset.
     */
    public static Icon Source_Language_Refset = new ImageIcon("icons/element_previous.png");
    /**
     * The Target_ language_ refset.
     */
    public static Icon Target_Language_Refset = new ImageIcon("icons/element_next.png");
    /**
     * The Exclusion_ refsets.
     */
    public static Icon Exclusion_Refsets = new ImageIcon("icons/cubes_yellow.png");
    /**
     * The Exclusion_refset.
     */
    public static Icon Exclusion_refset = new ImageIcon("icons/cube_yellow_delete.png");
    /**
     * The Inbox.
     */
    public static Icon Inbox = new ImageIcon("icons/inbox_into.png");
    /**
     * The Outbox.
     */
    public static Icon Outbox = new ImageIcon("icons/outbox_out.png");
    /**
     * The smart_ folder.
     */
    public static Icon smart_Folder = new ImageIcon("icons/box_white.png");
    /**
     * The Custom_folder.
     */
    public static Icon Custom_folder = new ImageIcon("icons/box_edit.png");
    /**
     * The Primitive_parent.
     */
    public static Icon Primitive_parent = new ImageIcon("icons/nav_up_left_blue.png");
    /**
     * The Fully_defined_parent.
     */
    public static Icon Fully_defined_parent = new ImageIcon("icons/nav_up_left_green.png");
    /**
     * The Inactive_parent.
     */
    public static Icon Inactive_parent = new ImageIcon("icons/nav_up_left_red.png");
    /**
     * The Primitive_concept.
     */
    public static Icon Primitive_concept = new ImageIcon("icons/nav_plain_blue.png");
    /**
     * The Fully_defined_concept.
     */
    public static Icon Fully_defined_concept = new ImageIcon("icons/nav_plain_green.png");
    /**
     * The Inactive_concept.
     */
    public static Icon Inactive_concept = new ImageIcon("icons/nav_plain_red.png");
    /**
     * The Preferred_acceptability.
     */
    public static Icon Preferred_acceptability = new ImageIcon("icons/star_yellow.png");
    /**
     * The Acceptable_acceptability.
     */
    public static Icon Acceptable_acceptability = new ImageIcon("icons/bookmark.png");
    /**
     * The Not_acceptable_acceptability.
     */
    public static Icon Not_acceptable_acceptability = new ImageIcon("icons/bookmark_delete.png");
    /**
     * The Active_ fsn.
     */
    public static Icon Active_FSN = new ImageIcon("icons/bullet_square_green.png");
    /**
     * The Inactive_ fsn.
     */
    public static Icon Inactive_FSN = new ImageIcon("icons/bullet_square_red.png");
    //
    public static Icon Non_Core_FSN = new ImageIcon("icons/bullet_square_blue.png");
    /**
     * The Active_description.
     */
    public static Icon Active_description = new ImageIcon("icons/bullet_triangle_green.png");
    /**
     * The Inactive_description.
     */
    public static Icon Inactive_description = new ImageIcon("icons/bullet_triangle_red.png");
    public static Icon Non_Core_description = new ImageIcon("icons/bullet_triangle_blue.png");
    /**
     * The Is_matching_guideline.
     */
    public static Icon Is_matching_guideline = new ImageIcon("icons/bell.png");
    /**
     * The Has_associated_active_issues.
     */
    public static Icon Has_associated_active_issues = new ImageIcon("icons/flag_red.png");
    /**
     * The Has_associated_resolved_issue.
     */
    public static Icon Has_associated_resolved_issue = new ImageIcon("icons/flag_green.png");
    /**
     * The Has_comments.
     */
    public static Icon Has_comments = new ImageIcon("icons/Flag_blue.png");
    /**
     * The Comment.
     */
    public static Icon Comment = new ImageIcon("icons/message.png");
    /**
     * The Comments.
     */
    public static Icon Comments = new ImageIcon("icons/messages.png");
    /**
     * The Add_comment.
     */
    public static Icon Add_comment = new ImageIcon("icons/message_add.png");
    /**
     * The Versions.
     */
    public static Icon Versions = new ImageIcon("icons/dot-chart.png");
    /**
     * The Initial capital status_significant.
     */
    public static Icon InitialCapitalStatus_significant = new ImageIcon("icons/front.png");
    /**
     * The Initial capital status_significant.
     */
    public static Icon editingRowIcon = new ImageIcon("icons/Work.png");
    /**
     * The Rolegroup.
     */
    public static Icon Rolegroup = new ImageIcon("icons/elements_selection.png");
    /**
     * The Role.
     */
    public static Icon Role = new ImageIcon("icons/element.png");
    /**
     * The Qualifier.
     */
    public static Icon Qualifier = new ImageIcon("icons/element_into_input.png");
    /**
     * The Other_ front.
     */
    public static Icon Other_Front = new ImageIcon("icons/graph_edge_directed.png");
    /**
     * The ARG.
     */
    public static Icon ARG = new ImageIcon("icons/flag_argentina.png");
    /**
     * The E n_ aus.
     */
    public static Icon EN_AUS = new ImageIcon("icons/flag_australia.png");
    /**
     * The E n_ can.
     */
    public static Icon EN_CAN = new ImageIcon("icons/flag_canada.png");
    /**
     * The CYP.
     */
    public static Icon CYP = new ImageIcon("icons/flag_cyprus.png");
    /**
     * The DAN.
     */
    public static Icon DAN = new ImageIcon("icons/flag_denmark.png");
    /**
     * The EES.
     */
    public static Icon EES = new ImageIcon("icons/flag_estonia.png");
    /**
     * The E n_ gbr.
     */
    public static Icon EN_GBR = new ImageIcon("icons/flag_great_britain.png");
    /**
     * The LTU.
     */
    public static Icon LTU = new ImageIcon("icons/flag_lithuania.png");
    /**
     * The NLD.
     */
    public static Icon NLD = new ImageIcon("icons/flag_netherlands.png");
    /**
     * The E n_ nzd.
     */
    public static Icon EN_NZD = new ImageIcon("icons/flag_new_zealand.png");
    /**
     * The Z h_ sgp.
     */
    public static Icon ZH_SGP = new ImageIcon("icons/flag_singapore.png");
    /**
     * The SKA.
     */
    public static Icon SKA = new ImageIcon("icons/flag_slovakia.png");
    /**
     * The SIA.
     */
    public static Icon SIA = new ImageIcon("icons/flag_slovenia.png");
    /**
     * The ESP.
     */
    public static Icon ESP = new ImageIcon("icons/flag_spain.png");
    /**
     * The SVE.
     */
    public static Icon SVE = new ImageIcon("icons/flag_sweden.png");
    /**
     * The E n_ usa.
     */
    public static Icon EN_USA = new ImageIcon("icons/flag_usa.png");
    /**
     * The FUNNE l_ add.
     */
    public static Icon FUNNEL_ADD = new ImageIcon("icons/funnel_add.png");
    /**
     * The FUNNE l_ delete.
     */
    public static Icon FUNNEL_DELETE = new ImageIcon("icons/funnel_delete.png");
    /**
     * The Constant CUSTOM_NODE.
     */
    public static final String CUSTOM_NODE = "Custom";
    /**
     * The Constant STATUS_NODE.
     */
    public static final String STATUS_NODE = "Status";
    /**
     * The Constant INBOX_NODE.
     */
    public static final String INBOX_NODE = "Inbox";
    /**
     * The Constant CUSTOM_NODE_ROOT.
     */
    public static final String CUSTOM_NODE_ROOT = "Custom Node Root";
    /**
     * The Constant STATUS_NODE_ROOT.
     */
    public static final String STATUS_NODE_ROOT = "Status Node Root";
    /**
     * The Constant WORKLIST_NODE_ROOT.
     */
    public static final String WORKLIST_NODE_ROOT = "Worklist Node Root";
    /**
     * The Constant WORKLIST_NODE.
     */
    public static final String WORKLIST_NODE = "Worklists";
    /**
     * The Constant OUTBOX_NODE.
     */
    public static final String OUTBOX_NODE = "Outbox";
    /**
     * The Constant CONCEPT.
     */
    public static final int CONCEPT = 0;
    /**
     * The Constant ID.
     */
    public static final int ID = 1;
    /**
     * The Constant CONCEPTID.
     */
    public static final int CONCEPTID = 2;
    /**
     * The Constant ATTRIBUTE.
     */
    public static final int ATTRIBUTE = 3;
    /**
     * The Constant FSNDESCRIPTION.
     */
    public static final int FSNDESCRIPTION = 4;
    /**
     * The Constant PREFERRED.
     */
    public static final int PREFERRED = 5;
    /**
     * The Constant SUPERTYPE.
     */
    public static final int SUPERTYPE = 6;
    /**
     * The Constant ROLE.
     */
    public static final int ROLE = 7;
    /**
     * The Constant DESCRIPTIONINFO.
     */
    public static final int DESCRIPTIONINFO = 8;
    /**
     * The Constant RELATIONSHIPINFO.
     */
    public static final int RELATIONSHIPINFO = 9;
    /**
     * The Constant SYNONYMN.
     */
    public static final int SYNONYMN = 10;
    /**
     * The Constant ROLEGROUP.
     */
    public static final int ROLEGROUP = 11;
    /**
     * The Constant ASSOCIATION.
     */
    public static final int ASSOCIATION = 12;
    /**
     * The Constant FOLDER.
     */
    public static final int FOLDER = 13;
    /**
     * The Constant NOTACCEPTABLE.
     */
    public static final int NOTACCEPTABLE = 14;
    /**
     * The Constant DEFINED.
     */
    public static final int DEFINED = 15;
    /**
     * The Constant PRIMITIVE.
     */
    public static final int PRIMITIVE = 16;
    /**
     * The Constant INACTIVE.
     */
    public static final int INACTIVE = 17;
    /**
     * The Constant PRIMITIVE_PARENT.
     */
    public static final int PRIMITIVE_PARENT = 18;
    /**
     * The Constant DEFINED_PARENT.
     */
    public static final int DEFINED_PARENT = 19;
    /**
     * The Constant INACTIVE_PARENT.
     */
    public static final int INACTIVE_PARENT = 20;

    /**
     * Gets the icon for project tree.
     *
     * @param nodeType the node type
     * @return the icon for project tree
     */
    public static Icon getIconForProjectTree(String nodeType) {
        if (nodeType == null) {
            return blackIcon;
        }
        if (nodeType.equals(ProjectsPanel.PROJECTNODE)) {
            return Translation_project;
        }

        if (nodeType.equals(ProjectsPanel.WORKSETNODE)) {
            return Workset;
        }

        if (nodeType.equals(ProjectsPanel.WORKLISTNODE)) {
            return Worklist;
        }

        if (nodeType.equals(ProjectsPanel.PROJECTROOTNODE)) {
            return folder;
        }

        if (nodeType.equals(ProjectsPanel.WORKSETROOTNODE)) {
            return Worksets;
        }

        if (nodeType.equals(ProjectsPanel.EXCREFSETROOTNODE)) {
            return Exclusion_Refsets;
        }


        if (nodeType.equals(ProjectsPanel.EXCREFSETNODE)) {
            return Exclusion_refset;
        }


        if (nodeType.equals(ProjectsPanel.LNKREFSETROOTNODE)) {
            return folder;
        }

        if (nodeType.equals(ProjectsPanel.LNKREFSETNODE)) {
            return folder;
        }


        if (nodeType.equals(ProjectsPanel.SRCREFSETROOTNODE)) {
            return folder;
        }

        if (nodeType.equals(ProjectsPanel.SRCREFSETNODE)) {
            return Source_Language_Refset;
        }

        if (nodeType.equals(ProjectsPanel.TGTREFSETROOTNODE)) {
            return folder;
        }


        if (nodeType.equals(ProjectsPanel.TGTREFSETNODE)) {
            return Target_Language_Refset;
        }


        if (nodeType.equals(ProjectsPanel.PARTSCHEMEROOTNODE)) {
            return Partition_schemes;
        }


        if (nodeType.equals(ProjectsPanel.PARTSCHEMENODE)) {
            return Partition_scheme;
        }


        if (nodeType.equals(ProjectsPanel.PARTITIONNODE)) {
            return Partition;
        }
        return blackIcon;
    }

    /**
     * Gets the icon for term type_ status.
     *
     * @param type the type
     * @param status the status
     * @return the icon for term type_ status
     */
    public static Icon getIconForTermType_Status(String type, String status, Boolean isCoreDesc) {
        try {
            I_GetConceptData fsn = Terms.get().getConcept(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());
            I_GetConceptData active = Terms.get().getConcept(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
            if (status.equalsIgnoreCase(active.getInitialText()) || status.equalsIgnoreCase("current") || status.equalsIgnoreCase("active")) {
                if ((type.equalsIgnoreCase(fsn.getInitialText()) || type.equalsIgnoreCase("fully specified name")) && isCoreDesc) {
                    return Active_FSN;
                } else if ((type.equalsIgnoreCase(fsn.getInitialText()) || type.equalsIgnoreCase("fully specified name")) && !isCoreDesc) {
                    return Non_Core_FSN;
                }
                if (isCoreDesc) {
                    return Active_description;
                } else {
                    return Non_Core_description;
                }
            }
            if (type.equalsIgnoreCase(fsn.getInitialText()) || type.equalsIgnoreCase("fully specified name")) {
                return Inactive_FSN;
            }
            return Inactive_description;
        } catch (ValidationException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (TerminologyException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return Inactive_description;
    }

    /**
     * Gets the icon for acceptability.
     *
     * @param type the type
     * @return the icon for acceptability
     */
    public static Icon getIconForAcceptability(String type) {
        try {
            I_GetConceptData preferred = Terms.get().getConcept(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid());
            I_GetConceptData acceptable = Terms.get().getConcept(SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getNid());
            if (type.equalsIgnoreCase("acceptable") || type.equalsIgnoreCase(acceptable.getInitialText())) {
                return Acceptable_acceptability;
            }
            if (type.equalsIgnoreCase("preferred term") || type.equalsIgnoreCase(preferred.getInitialText())) {
                return Preferred_acceptability;
            }
            if (type.equalsIgnoreCase("not acceptable")) {
                return Not_acceptable_acceptability;
            }
        } catch (ValidationException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (TerminologyException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return blackIcon;
    }

    /**
     * Gets the icon for ics.
     *
     * @param type the type
     * @return the icon for ics
     */
    public static Icon getIconForICS(Boolean type) {
        if (type != null && type) {
            return InitialCapitalStatus_significant;
        }
        return null;
    }

    public static Icon getIconForEditingRow(Boolean value) {
        if (value) {
            return editingRowIcon;
        }
        return null;
    }

    /**
     * Gets the icon for language.
     *
     * @param type the type
     * @return the icon for language
     */
    public static Icon getIconForLanguage(String type) {
        try {
            if (type.equals("bca0a686-3516-3daf-8fcf-fe396d13cfad")) {
                return EN_USA;
            } else if (type.equals("eb9a5e42-3cba-356d-b623-3ed472e20b30")) {
                return EN_GBR;
            } else if (type.equals("03615ef2-aa56-336d-89c5-a1b5c4cee8f6")) {
                return ESP;
            } else if (type.equals("be446b41-0eda-3d36-84e4-ae196f05858b")) {
                return DAN;
            }else if (type.equals("e57ec728-742f-56b3-9b53-9613670fb24d")){
            	return SVE;
            }

        } catch (Exception e) {
            return EN_USA;
        }
        return EN_USA;

    }

    /**
     * Gets the icon for concept details.
     *
     * @param type the type
     * @return the icon for concept details
     */
    public static Icon getIconForConceptDetails(Integer type) {
        if (type == null) {
            return blackIcon;
        }
        switch (type) {
            case ATTRIBUTE:
                return attributeIcon;
            case FSNDESCRIPTION:
                return Active_FSN;
            case PREFERRED:
                return Preferred_acceptability;
            case ROLE:
                return Role;
            case SUPERTYPE:
                return redIcon;
            case DESCRIPTIONINFO:
                return Active_description;
            case RELATIONSHIPINFO:
                return descriptionIcon;
            case SYNONYMN:
                return Active_description;
            case ROLEGROUP:
                return Rolegroup;
            case FOLDER:
                return folder;
            case ASSOCIATION:
                return Qualifier;
            case NOTACCEPTABLE:
                return Not_acceptable_acceptability;
            case DEFINED:
                return Fully_defined_concept;
            case PRIMITIVE:
                return Primitive_concept;
            case INACTIVE:
                return Inactive_concept;
            case PRIMITIVE_PARENT:
                return Primitive_parent;
            case DEFINED_PARENT:
                return Fully_defined_parent;
            case INACTIVE_PARENT:
                return Inactive_parent;

            default:
                return blackIcon;
        }
    }

    /**
     * Gets the icon for inbox tree.
     *
     * @param nodeType the node type
     * @return the icon for inbox tree
     */
    public static Icon getIconForInboxTree(String nodeType) {
        if (nodeType == null) {
            return blackIcon;
        }
        if (nodeType.equals(CUSTOM_NODE)) {
            return Custom_folder;
        }
        if (nodeType.equals(STATUS_NODE)) {
            return smart_Folder;
        }
        if (nodeType.equals(INBOX_NODE)) {
            return Inbox;
        }
        if (nodeType.equals(CUSTOM_NODE_ROOT)) {
            return Custom_folder;
        }
        if (nodeType.equals(STATUS_NODE_ROOT)) {
            return folder;
        }
        if (nodeType.equals(WORKLIST_NODE_ROOT)) {
            return folder;
        }
        if (nodeType.equals(WORKLIST_NODE)) {
            return smart_Folder;
        }
        if (nodeType.equals(OUTBOX_NODE)) {
            return Outbox;
        }
        return blackIcon;
    }
//	public static HashMap<UUID,Icon> uuidToIcon;
//	public static HashMap<Class,Icon> classToIcon;
//	public static enum ICON_REPOSITORY{ 
//		Translation_project (ArchitectonicAuxiliary.Concept.ACTIVE.getUids().iterator().next(.png");attributeIcon.png");
//		Worksets (WorkSet[],blackIcon.png");
//		Workset  ,
//		Partition_schemes  ,
//		Partition_scheme  ,
//		Partition ,
//		Worklists ,
//		Worklist_statistics,
//		Worklist  ,
//		Source_Language_Refset ,
//		Target_Language_RefSet  ,
//		Exclusion_RefSets ,
//		Exclusion_refset ,
//		Inbox ,
//		Outbox ,
//		smart_Folder ,
//		Custom_folder,
//		Primitive_parent ,
//		Fully_defined_parent ,
//		Inactive_parent ,
//		Primitive_concept (nav_plain_blue)  ,
//		Fully_defined_concept (nav_plain_green.png");  
//		Inactive_concept (nav_plain_red.png");  
//		Preferred_acceptability (star_yellow) , 
//		Acceptable_acceptability (bookmark.png");   
//		Not_acceptable_acceptability (bookmark_delete.png");
//		Active_FSN (bullet_square_green.png");  
//		Inactive_FSN (bullet_square_red.png");   
//		Active_description  (bullet_triangle_green.png"); 
//		Inactive_description (bullet_triangle_red.png"); 
//		Is_matching_guideline (bell.png");  
//		Has_associated_active_issues (flag_red) , 
//		Has_associated_resolved_issue (flag_green)  ,
//		Has_comments  (Flag_blue.png");  
//		Comment (message.png"); 
//		Comments (messages) ,
//		Add_comment (message_add.png");  
//		Versions (dot-chart.png"); 
//		InitialCapitalStatus_significant (font.png");   
//		Rolegroup (elements_selection)  ,
//		Role (element)  ,
//		Qualifier (element_into_input.png");    
//		Other_Front (graph_edge_directed);
//	
//		public ICON_REPOSITORY(UUID uuid,Icon icon){
//			uuidToIcon.put(uuid, icon);
//		}
//		public ICON_REPOSITORY(Class objectClass,Icon icon){
//			classToIcon.put(objectClass, icon);
//		}
//	};
//
//		
//		
//		
//	
//	public static Icon getIconForType(Integer type){
//		if (type==null){
//			return blackIcon;
//		}
//		ArchitectonicAuxiliary.Concept.STATUS.ACTIVE.get.getUids();
//		switch (type) {
//		case 3: return attributeIcon; 
//		case 4: return fsnIcon; 
//		case 5: return preferredIcon; 
//		case 6: return orangeIcon; 
//		case 7: return redIcon; 
//		case 8: return descriptionIcon; 
//		case 10: return descriptionIcon; 
//		case 11: return roleGroupIcon; 
//		case 12: return folder; 
//		case 13: return associationIcon; 
//		case 14: return notAcceptIcon; 
//		default: return blackIcon;
//		}
//		notAcceptIcon.
//	}
}
