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
package org.ihtsdo.qa.gui.viewers.utils;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.dwfa.cement.ArchitectonicAuxiliary;

/**
 * The Class IconUtilities.
 */
public class IconUtilities {

	/** The red icon. */
	private static Icon redIcon = new ImageIcon("icons/91.png");
	
	/** The blue icon. */
	private static Icon blueIcon = new ImageIcon("icons/93.png");
	
	/** The green icon. */
	private static Icon greenIcon = new ImageIcon("icons/95.png");
	
	/** The attribute icon. */
	private static Icon attributeIcon = new ImageIcon("icons/ConceptStatus.gif");
	
	/** The orange icon. */
	private static Icon orangeIcon = new ImageIcon("icons/90.png");
	
	/** The description icon. */
	private static Icon descriptionIcon = new ImageIcon("icons/Description.gif");
	
	/** The fsn icon. */
	private static Icon fsnIcon = new ImageIcon("icons/Name.gif");
	
	/** The black icon. */
	private static Icon blackIcon = new ImageIcon("icons/85.png");
	
	/** The preferred icon. */
	private static Icon preferredIcon = new ImageIcon("icons/Preferred.gif");
	
	/** The role group icon. */
	private static Icon roleGroupIcon = new ImageIcon("icons/rolegroup.gif");
	
	/** The folder. */
	private static Icon folder = new ImageIcon("icons/folder.png");
	
	/** The association icon. */
	private static Icon associationIcon = new ImageIcon("icons/Association.gif");
	
	/** The not accept icon. */
	private static Icon notAcceptIcon = new ImageIcon("icons/NotAccept.gif");

	/** The help icon. */
	private static Icon helpIcon = new ImageIcon("icons/help16.png");

	/** The check icon. */
	private static Icon checkIcon = new ImageIcon("icons/check.png");
	
	/** The add icon. */
	private static Icon addIcon = new ImageIcon("icons/add.png");
	
	/** The delete icon. */
	private static Icon deleteIcon = new ImageIcon("icons/delete.png");
	
	/** The media_step_forward icon. */
	private static Icon media_step_forwardIcon = new ImageIcon("icons/media_step_forward.png");

	/** The Translation_project. */
	private static Icon Translation_project = new ImageIcon("icons/branch_element.png");
	
	/** The Worksets. */
	private static Icon Worksets = new ImageIcon("icons/cubes_blue.png");
	
	/** The Workset. */
	private static Icon Workset = new ImageIcon("icons/cube_blue.png");
	
	/** The Partition_schemes. */
	private static Icon Partition_schemes = new ImageIcon("icons/cubes.png");
	
	/** The Partition_scheme. */
	private static Icon Partition_scheme = new ImageIcon("icons/cubes_green.png");
	
	/** The Partition. */
	private static Icon Partition = new ImageIcon("icons/cube_green.png");
	
	/** The Worklists. */
	private static Icon Worklists = new ImageIcon("icons/tables.png");
	
	/** The Worklist_statistics. */
	private static Icon Worklist_statistics = new ImageIcon("icons/column-chart.png");
	
	/** The Worklist. */
	private static Icon Worklist = new ImageIcon("icons/table.png");
	
	/** The Source_ language_ refset. */
	private static Icon Source_Language_Refset = new ImageIcon("icons/element_previous.png");
	
	/** The Target_ language_ refset. */
	private static Icon Target_Language_Refset = new ImageIcon("icons/element_next.png");
	
	/** The Exclusion_ refsets. */
	private static Icon Exclusion_Refsets = new ImageIcon("icons/cubes_yellow.png");
	
	/** The Exclusion_refset. */
	private static Icon Exclusion_refset = new ImageIcon("icons/cube_yellow_delete.png");
	
	/** The Inbox. */
	private static Icon Inbox = new ImageIcon("icons/inbox_into.png");
	
	/** The Outbox. */
	private static Icon Outbox = new ImageIcon("icons/outbox_out.png");
	
	/** The smart_ folder. */
	private static Icon smart_Folder = new ImageIcon("icons/box_white.png");
	
	/** The Custom_folder. */
	private static Icon Custom_folder = new ImageIcon("icons/box_edit.png");
	
	/** The Primitive_parent. */
	private static Icon Primitive_parent = new ImageIcon("icons/nav_up_left_blue.png");
	
	/** The Fully_defined_parent. */
	private static Icon Fully_defined_parent = new ImageIcon("icons/nav_up_left_green.png");
	
	/** The Inactive_parent. */
	private static Icon Inactive_parent = new ImageIcon("icons/nav_up_left_red.png");
	
	/** The Primitive_concept. */
	private static Icon Primitive_concept = new ImageIcon("icons/nav_plain_blue.png");
	
	/** The Fully_defined_concept. */
	private static Icon Fully_defined_concept = new ImageIcon("icons/nav_plain_green.png");
	
	/** The Inactive_concept. */
	private static Icon Inactive_concept = new ImageIcon("icons/nav_plain_red.png");
	
	/** The Preferred_acceptability. */
	private static Icon Preferred_acceptability = new ImageIcon("icons/star_yellow.png");
	
	/** The Acceptable_acceptability. */
	private static Icon Acceptable_acceptability = new ImageIcon("icons/bookmark.png");
	
	/** The Not_acceptable_acceptability. */
	private static Icon Not_acceptable_acceptability = new ImageIcon("icons/bookmark_delete.png");
	
	/** The Active_ fsn. */
	private static Icon Active_FSN = new ImageIcon("icons/bullet_square_green.png");
	
	/** The Inactive_ fsn. */
	private static Icon Inactive_FSN = new ImageIcon("icons/bullet_square_red.png");
	
	/** The Active_description. */
	private static Icon Active_description = new ImageIcon("icons/bullet_triangle_green.png");
	
	/** The Inactive_description. */
	private static Icon Inactive_description = new ImageIcon("icons/bullet_triangle_red.png");
	
	/** The Is_matching_guideline. */
	private static Icon Is_matching_guideline = new ImageIcon("icons/bell.png");
	
	/** The Has_associated_active_issues. */
	private static Icon Has_associated_active_issues = new ImageIcon("icons/flag_red.png");
	
	/** The Has_associated_resolved_issue. */
	private static Icon Has_associated_resolved_issue = new ImageIcon("icons/flag_green.png");
	
	/** The Has_comments. */
	private static Icon Has_comments = new ImageIcon("icons/Flag_blue.png");
	
	/** The Comment. */
	private static Icon Comment = new ImageIcon("icons/message.png");
	
	/** The Comments. */
	private static Icon Comments = new ImageIcon("icons/messages.png");
	
	/** The Add_comment. */
	private static Icon Add_comment = new ImageIcon("icons/message_add.png");
	
	/** The Versions. */
	private static Icon Versions = new ImageIcon("icons/dot-chart.png");
	
	/** The Initial capital status_significant. */
	private static Icon InitialCapitalStatus_significant = new ImageIcon("icons/font.png");
	
	/** The Rolegroup. */
	private static Icon Rolegroup = new ImageIcon("icons/elements_selection.png");
	
	/** The Role. */
	private static Icon Role = new ImageIcon("icons/element.png");
	
	/** The Qualifier. */
	private static Icon Qualifier = new ImageIcon("icons/element_into_input.png");
	
	/** The Other_ front. */
	private static Icon Other_Front = new ImageIcon("icons/graph_edge_directed.png");
	
	/** The ARG. */
	private static Icon ARG = new ImageIcon("icons/flag_argentina.png");
	
	/** The E n_ aus. */
	private static Icon EN_AUS = new ImageIcon("icons/flag_australia.png");
	
	/** The E n_ can. */
	private static Icon EN_CAN = new ImageIcon("icons/flag_canada.png");
	
	/** The CYP. */
	private static Icon CYP = new ImageIcon("icons/flag_cyprus.png");
	
	/** The DAN. */
	private static Icon DAN = new ImageIcon("icons/flag_denmark.png");
	
	/** The EES. */
	private static Icon EES = new ImageIcon("icons/flag_estonia.png");
	
	/** The E n_ gbr. */
	private static Icon EN_GBR = new ImageIcon("icons/flag_great_britain.png");
	
	/** The LTU. */
	private static Icon LTU = new ImageIcon("icons/flag_lithuania.png");
	
	/** The NLD. */
	private static Icon NLD = new ImageIcon("icons/flag_netherlands.png");
	
	/** The E n_ nzd. */
	private static Icon EN_NZD = new ImageIcon("icons/flag_new_zealand.png");
	
	/** The Z h_ sgp. */
	private static Icon ZH_SGP = new ImageIcon("icons/flag_singapore.png");
	
	/** The SKA. */
	private static Icon SKA = new ImageIcon("icons/flag_slovakia.png");
	
	/** The SIA. */
	private static Icon SIA = new ImageIcon("icons/flag_slovenia.png");
	
	/** The ESP. */
	private static Icon ESP = new ImageIcon("icons/flag_spain.png");
	
	/** The SVE. */
	private static Icon SVE = new ImageIcon("icons/flag_sweden.png");
	
	/** The E n_ usa. */
	private static Icon EN_USA = new ImageIcon("icons/flag_usa.png");

	/** The Constant CUSTOM_NODE. */
	private static final String CUSTOM_NODE = "Custom";
	
	/** The Constant STATUS_NODE. */
	private static final String STATUS_NODE = "Status";
	
	/** The Constant INBOX_NODE. */
	private static final String INBOX_NODE = "Inbox";
	
	/** The Constant CUSTOM_NODE_ROOT. */
	private static final String CUSTOM_NODE_ROOT = "Custom Node Root";
	
	/** The Constant STATUS_NODE_ROOT. */
	private static final String STATUS_NODE_ROOT = "Status Node Root";
	
	/** The Constant WORKLIST_NODE_ROOT. */
	private static final String WORKLIST_NODE_ROOT = "Worklist Node Root";
	
	/** The Constant WORKLIST_NODE. */
	private static final String WORKLIST_NODE = "Worklists";
	
	/** The Constant OUTBOX_NODE. */
	private static final String OUTBOX_NODE = "Outbox";

	/** The Constant CONCEPT. */
	private static final int CONCEPT = 0;
	
	/** The Constant ID. */
	private static final int ID = 1;
	
	/** The Constant CONCEPTID. */
	private static final int CONCEPTID = 2;
	
	/** The Constant ATTRIBUTE. */
	private static final int ATTRIBUTE = 3;
	
	/** The Constant FSNDESCRIPTION. */
	private static final int FSNDESCRIPTION = 4;
	
	/** The Constant PREFERRED. */
	private static final int PREFERRED = 5;
	
	/** The Constant SUPERTYPE. */
	private static final int SUPERTYPE = 6;
	
	/** The Constant ROLE. */
	private static final int ROLE = 7;
	
	/** The Constant DESCRIPTIONINFO. */
	private static final int DESCRIPTIONINFO = 8;
	
	/** The Constant RELATIONSHIPINFO. */
	private static final int RELATIONSHIPINFO = 9;
	
	/** The Constant SYNONYMN. */
	private static final int SYNONYMN = 10;
	
	/** The Constant ROLEGROUP. */
	private static final int ROLEGROUP = 11;
	
	/** The Constant ASSOCIATION. */
	private static final int ASSOCIATION = 12;
	
	/** The Constant FOLDER. */
	private static final int FOLDER = 13;
	
	/** The Constant NOTACCEPTABLE. */
	private static final int NOTACCEPTABLE = 14;
	
	/** The Constant DEFINED. */
	private static final int DEFINED = 15;
	
	/** The Constant PRIMITIVE. */
	private static final int PRIMITIVE = 16;
	
	/** The Constant INACTIVE. */
	private static final int INACTIVE = 17;
	
	/** The Constant PRIMITIVE_PARENT. */
	private static final int PRIMITIVE_PARENT = 18;
	
	/** The Constant DEFINED_PARENT. */
	private static final int DEFINED_PARENT = 19;
	
	/** The Constant INACTIVE_PARENT. */
	private static final int INACTIVE_PARENT = 20;
	
	/** The Constant TRANSLATION_PROJECT. */
	private static final int TRANSLATION_PROJECT = 21;
	
	/** The Constant BLUE_ICON. */
	private static final int BLUE_ICON = 22;
	
	/** The Constant GREEN_ICON. */
	private static final int GREEN_ICON = 23;

	/**
	 * Gets the icon for term type_ status.
	 *
	 * @param type the type
	 * @param status the status
	 * @return the icon for term type_ status
	 */
	public static Icon getIconForTermType_Status(String type, String status) {
		if (status.equalsIgnoreCase("current") || status.equalsIgnoreCase("active")) {
			if (type.equalsIgnoreCase("fully specified name")) {
				return Active_FSN;
			}
			return Active_description;
		}
		if (type.equalsIgnoreCase("fully specified name")) {
			return Inactive_FSN;
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
		if (type.equalsIgnoreCase("acceptable")) {
			return Acceptable_acceptability;
		}
		if (type.equalsIgnoreCase("preferred term")) {
			return Preferred_acceptability;
		}
		if (type.equalsIgnoreCase("not acceptable")) {
			return Not_acceptable_acceptability;
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
		if (type) {
			return InitialCapitalStatus_significant;
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
		String normalizedLangCode = type.toUpperCase().replace('-', '_');
		switch (ArchitectonicAuxiliary.LANG_CODE.valueOf(normalizedLangCode)) {
		case DA:
			return DAN;
		case DA_DK:
			return DAN;
		case EN:
			return EN_USA;
		case EN_AU:
			return EN_AUS;
		case EN_BZ:
			return EN_USA;
		case EN_CA:
			return EN_CAN;
		case EN_GB:
			return EN_GBR;
		case EN_IE:
			return EN_USA;
		case EN_JM:
			return EN_USA;
		case EN_NZ:
			return EN_NZD;
		case EN_TT:
			return EN_USA;
		case EN_US:
			return EN_USA;
		case EN_ZA:
			return EN_USA;
		case ES:
			return ESP;
		case ES_AR:
			return ARG;
		case ES_BO:
			return ESP;
		case ES_CL:
			return ESP;
		case ES_CO:
			return ESP;
		case ES_CR:
			return ESP;
		case ES_DO:
			return ESP;
		case ES_EC:
			return ESP;
		case ES_ES:
			return ESP;
		case ES_GT:
			return ESP;
		case ES_HN:
			return ESP;
		case ES_NI:
			return ESP;
		case ES_PA:
			return ESP;
		case ES_PE:
			return ESP;
		case ES_PY:
			return ESP;
		case ES_SV:
			return ESP;
		case ES_UY:
			return ESP;
		case ES_VE:
			return ESP;
		case FR:
			return ESP;
		case FR_BE:
			return ESP;
		case FR_CA:
			return EN_CAN;
		case FR_CH:
			return ESP;
		case FR_FR:
			return ESP;
		case FR_LU:
			return ESP;
		case FR_MC:
			return ESP;
		case LIT:
			return LTU;
		case LT:
			return LTU;
		case LT_LT:
			return LTU;
		case SV:
			return SVE;
		case SV_FI:
			return SVE;
		case SV_SE:
			return SVE;
		case ZH:
			return ZH_SGP;
		case ZH_CHS:
			return ZH_SGP;
		case ZH_CHT:
			return ZH_SGP;
		case ZH_CN:
			return ZH_SGP;
		case ZH_HK:
			return ZH_SGP;
		case ZH_MO:
			return ZH_SGP;
		case ZH_SG:
			return ZH_SGP;
		case ZH_TW:
			return ZH_SGP;
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
		case TRANSLATION_PROJECT:
			return Translation_project;
		case BLUE_ICON:
			return blueIcon;
		case GREEN_ICON:
			return greenIcon;
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

	/**
	 * @return the redIcon
	 */
	public static Icon getRedIcon() {
		return redIcon;
	}

	/**
	 * @return the blueIcon
	 */
	public static Icon getBlueIcon() {
		return blueIcon;
	}

	/**
	 * @return the greenIcon
	 */
	public static Icon getGreenIcon() {
		return greenIcon;
	}

	/**
	 * @return the attributeIcon
	 */
	public static Icon getAttributeIcon() {
		return attributeIcon;
	}

	/**
	 * @return the orangeIcon
	 */
	public static Icon getOrangeIcon() {
		return orangeIcon;
	}

	/**
	 * @return the descriptionIcon
	 */
	public static Icon getDescriptionIcon() {
		return descriptionIcon;
	}

	/**
	 * @return the fsnIcon
	 */
	public static Icon getFsnIcon() {
		return fsnIcon;
	}

	/**
	 * @return the blackIcon
	 */
	public static Icon getBlackIcon() {
		return blackIcon;
	}

	/**
	 * @return the preferredIcon
	 */
	public static Icon getPreferredIcon() {
		return preferredIcon;
	}

	/**
	 * @return the roleGroupIcon
	 */
	public static Icon getRoleGroupIcon() {
		return roleGroupIcon;
	}

	/**
	 * @return the folder
	 */
	public static Icon getFolder() {
		return folder;
	}

	/**
	 * @return the associationIcon
	 */
	public static Icon getAssociationIcon() {
		return associationIcon;
	}

	/**
	 * @return the notAcceptIcon
	 */
	public static Icon getNotAcceptIcon() {
		return notAcceptIcon;
	}

	/**
	 * @return the helpIcon
	 */
	public static Icon getHelpIcon() {
		return helpIcon;
	}

	/**
	 * @return the checkIcon
	 */
	public static Icon getCheckIcon() {
		return checkIcon;
	}

	/**
	 * @return the addIcon
	 */
	public static Icon getAddIcon() {
		return addIcon;
	}

	/**
	 * @return the deleteIcon
	 */
	public static Icon getDeleteIcon() {
		return deleteIcon;
	}

	/**
	 * @return the media_step_forwardIcon
	 */
	public static Icon getMedia_step_forwardIcon() {
		return media_step_forwardIcon;
	}

	/**
	 * @return the translation_project
	 */
	public static Icon getTranslation_project() {
		return Translation_project;
	}

	/**
	 * @return the worksets
	 */
	public static Icon getWorksets() {
		return Worksets;
	}

	/**
	 * @return the workset
	 */
	public static Icon getWorkset() {
		return Workset;
	}

	/**
	 * @return the partition_schemes
	 */
	public static Icon getPartition_schemes() {
		return Partition_schemes;
	}

	/**
	 * @return the partition_scheme
	 */
	public static Icon getPartition_scheme() {
		return Partition_scheme;
	}

	/**
	 * @return the partition
	 */
	public static Icon getPartition() {
		return Partition;
	}

	/**
	 * @return the worklists
	 */
	public static Icon getWorklists() {
		return Worklists;
	}

	/**
	 * @return the worklist_statistics
	 */
	public static Icon getWorklist_statistics() {
		return Worklist_statistics;
	}

	/**
	 * @return the worklist
	 */
	public static Icon getWorklist() {
		return Worklist;
	}

	/**
	 * @return the source_Language_Refset
	 */
	public static Icon getSource_Language_Refset() {
		return Source_Language_Refset;
	}

	/**
	 * @return the target_Language_Refset
	 */
	public static Icon getTarget_Language_Refset() {
		return Target_Language_Refset;
	}

	/**
	 * @return the exclusion_Refsets
	 */
	public static Icon getExclusion_Refsets() {
		return Exclusion_Refsets;
	}

	/**
	 * @return the exclusion_refset
	 */
	public static Icon getExclusion_refset() {
		return Exclusion_refset;
	}

	/**
	 * @return the inbox
	 */
	public static Icon getInbox() {
		return Inbox;
	}

	/**
	 * @return the outbox
	 */
	public static Icon getOutbox() {
		return Outbox;
	}

	/**
	 * @return the smart_Folder
	 */
	public static Icon getSmart_Folder() {
		return smart_Folder;
	}

	/**
	 * @return the custom_folder
	 */
	public static Icon getCustom_folder() {
		return Custom_folder;
	}

	/**
	 * @return the primitive_parent
	 */
	public static Icon getPrimitive_parent() {
		return Primitive_parent;
	}

	/**
	 * @return the fully_defined_parent
	 */
	public static Icon getFully_defined_parent() {
		return Fully_defined_parent;
	}

	/**
	 * @return the inactive_parent
	 */
	public static Icon getInactive_parent() {
		return Inactive_parent;
	}

	/**
	 * @return the primitive_concept
	 */
	public static Icon getPrimitive_concept() {
		return Primitive_concept;
	}

	/**
	 * @return the fully_defined_concept
	 */
	public static Icon getFully_defined_concept() {
		return Fully_defined_concept;
	}

	/**
	 * @return the inactive_concept
	 */
	public static Icon getInactive_concept() {
		return Inactive_concept;
	}

	/**
	 * @return the preferred_acceptability
	 */
	public static Icon getPreferred_acceptability() {
		return Preferred_acceptability;
	}

	/**
	 * @return the acceptable_acceptability
	 */
	public static Icon getAcceptable_acceptability() {
		return Acceptable_acceptability;
	}

	/**
	 * @return the not_acceptable_acceptability
	 */
	public static Icon getNot_acceptable_acceptability() {
		return Not_acceptable_acceptability;
	}

	/**
	 * @return the active_FSN
	 */
	public static Icon getActive_FSN() {
		return Active_FSN;
	}

	/**
	 * @return the inactive_FSN
	 */
	public static Icon getInactive_FSN() {
		return Inactive_FSN;
	}

	/**
	 * @return the active_description
	 */
	public static Icon getActive_description() {
		return Active_description;
	}

	/**
	 * @return the inactive_description
	 */
	public static Icon getInactive_description() {
		return Inactive_description;
	}

	/**
	 * @return the is_matching_guideline
	 */
	public static Icon getIs_matching_guideline() {
		return Is_matching_guideline;
	}

	/**
	 * @return the has_associated_active_issues
	 */
	public static Icon getHas_associated_active_issues() {
		return Has_associated_active_issues;
	}

	/**
	 * @return the has_associated_resolved_issue
	 */
	public static Icon getHas_associated_resolved_issue() {
		return Has_associated_resolved_issue;
	}

	/**
	 * @return the has_comments
	 */
	public static Icon getHas_comments() {
		return Has_comments;
	}

	/**
	 * @return the comment
	 */
	public static Icon getComment() {
		return Comment;
	}

	/**
	 * @return the comments
	 */
	public static Icon getComments() {
		return Comments;
	}

	/**
	 * @return the add_comment
	 */
	public static Icon getAdd_comment() {
		return Add_comment;
	}

	/**
	 * @return the versions
	 */
	public static Icon getVersions() {
		return Versions;
	}

	/**
	 * @return the initialCapitalStatus_significant
	 */
	public static Icon getInitialCapitalStatus_significant() {
		return InitialCapitalStatus_significant;
	}

	/**
	 * @return the rolegroup
	 */
	public static Icon getRolegroup() {
		return Rolegroup;
	}

	/**
	 * @return the role
	 */
	public static Icon getRole() {
		return Role;
	}

	/**
	 * @return the qualifier
	 */
	public static Icon getQualifier() {
		return Qualifier;
	}

	/**
	 * @return the other_Front
	 */
	public static Icon getOther_Front() {
		return Other_Front;
	}

	/**
	 * @return the aRG
	 */
	public static Icon getARG() {
		return ARG;
	}

	/**
	 * @return the eN_AUS
	 */
	public static Icon getEN_AUS() {
		return EN_AUS;
	}

	/**
	 * @return the eN_CAN
	 */
	public static Icon getEN_CAN() {
		return EN_CAN;
	}

	/**
	 * @return the cYP
	 */
	public static Icon getCYP() {
		return CYP;
	}

	/**
	 * @return the dAN
	 */
	public static Icon getDAN() {
		return DAN;
	}

	/**
	 * @return the eES
	 */
	public static Icon getEES() {
		return EES;
	}

	/**
	 * @return the eN_GBR
	 */
	public static Icon getEN_GBR() {
		return EN_GBR;
	}

	/**
	 * @return the lTU
	 */
	public static Icon getLTU() {
		return LTU;
	}

	/**
	 * @return the nLD
	 */
	public static Icon getNLD() {
		return NLD;
	}

	/**
	 * @return the eN_NZD
	 */
	public static Icon getEN_NZD() {
		return EN_NZD;
	}

	/**
	 * @return the zH_SGP
	 */
	public static Icon getZH_SGP() {
		return ZH_SGP;
	}

	/**
	 * @return the sKA
	 */
	public static Icon getSKA() {
		return SKA;
	}

	/**
	 * @return the sIA
	 */
	public static Icon getSIA() {
		return SIA;
	}

	/**
	 * @return the eSP
	 */
	public static Icon getESP() {
		return ESP;
	}

	/**
	 * @return the sVE
	 */
	public static Icon getSVE() {
		return SVE;
	}

	/**
	 * @return the eN_USA
	 */
	public static Icon getEN_USA() {
		return EN_USA;
	}

	/**
	 * @return the customNode
	 */
	public static String getCustomNode() {
		return CUSTOM_NODE;
	}

	/**
	 * @return the statusNode
	 */
	public static String getStatusNode() {
		return STATUS_NODE;
	}

	/**
	 * @return the inboxNode
	 */
	public static String getInboxNode() {
		return INBOX_NODE;
	}

	/**
	 * @return the customNodeRoot
	 */
	public static String getCustomNodeRoot() {
		return CUSTOM_NODE_ROOT;
	}

	/**
	 * @return the statusNodeRoot
	 */
	public static String getStatusNodeRoot() {
		return STATUS_NODE_ROOT;
	}

	/**
	 * @return the worklistNodeRoot
	 */
	public static String getWorklistNodeRoot() {
		return WORKLIST_NODE_ROOT;
	}

	/**
	 * @return the worklistNode
	 */
	public static String getWorklistNode() {
		return WORKLIST_NODE;
	}

	/**
	 * @return the outboxNode
	 */
	public static String getOutboxNode() {
		return OUTBOX_NODE;
	}

	/**
	 * @return the concept
	 */
	public static int getConcept() {
		return CONCEPT;
	}

	/**
	 * @return the id
	 */
	public static int getId() {
		return ID;
	}

	/**
	 * @return the conceptid
	 */
	public static int getConceptid() {
		return CONCEPTID;
	}

	/**
	 * @return the attribute
	 */
	public static int getAttribute() {
		return ATTRIBUTE;
	}

	/**
	 * @return the fsndescription
	 */
	public static int getFsndescription() {
		return FSNDESCRIPTION;
	}

	/**
	 * @return the preferred
	 */
	public static int getPreferred() {
		return PREFERRED;
	}

	/**
	 * @return the supertype
	 */
	public static int getSupertype() {
		return SUPERTYPE;
	}

	/**
	 * @return the role
	 */
	public static int getRoleInt() {
		return ROLE;
	}

	/**
	 * @return the descriptioninfo
	 */
	public static int getDescriptioninfo() {
		return DESCRIPTIONINFO;
	}

	/**
	 * @return the relationshipinfo
	 */
	public static int getRelationshipinfo() {
		return RELATIONSHIPINFO;
	}

	/**
	 * @return the synonymn
	 */
	public static int getSynonymn() {
		return SYNONYMN;
	}

	/**
	 * @return the rolegroup
	 */
	public static int getRolegroupInt() {
		return ROLEGROUP;
	}

	/**
	 * @return the association
	 */
	public static int getAssociation() {
		return ASSOCIATION;
	}

	/**
	 * @return the folder
	 */
	public static int getFolderInt() {
		return FOLDER;
	}

	/**
	 * @return the notacceptable
	 */
	public static int getNotacceptable() {
		return NOTACCEPTABLE;
	}

	/**
	 * @return the defined
	 */
	public static int getDefined() {
		return DEFINED;
	}

	/**
	 * @return the primitive
	 */
	public static int getPrimitive() {
		return PRIMITIVE;
	}

	/**
	 * @return the inactive
	 */
	public static int getInactive() {
		return INACTIVE;
	}

	/**
	 * @return the primitiveParent
	 */
	public static int getPrimitiveParent() {
		return PRIMITIVE_PARENT;
	}

	/**
	 * @return the definedParent
	 */
	public static int getDefinedParent() {
		return DEFINED_PARENT;
	}

	/**
	 * @return the inactiveParent
	 */
	public static int getInactiveParent() {
		return INACTIVE_PARENT;
	}

	/**
	 * @return the translationProject
	 */
	public static int getTranslationProject() {
		return TRANSLATION_PROJECT;
	}

	/**
	 * @return the blueIcon
	 */
	public static int getBlueIconInt() {
		return BLUE_ICON;
	}

	/**
	 * @return the greenIcon
	 */
	public static int getGreenIconInt() {
		return GREEN_ICON;
	}
}
