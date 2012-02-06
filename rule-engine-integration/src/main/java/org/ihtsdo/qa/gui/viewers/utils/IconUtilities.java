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
	public static Icon redIcon = new ImageIcon("icons/91.png");
	
	/** The blue icon. */
	public static Icon blueIcon = new ImageIcon("icons/93.png");
	
	/** The green icon. */
	public static Icon greenIcon = new ImageIcon("icons/95.png");
	
	/** The attribute icon. */
	public static Icon attributeIcon = new ImageIcon("icons/ConceptStatus.gif");
	
	/** The orange icon. */
	public static Icon orangeIcon = new ImageIcon("icons/90.png");
	
	/** The description icon. */
	public static Icon descriptionIcon = new ImageIcon("icons/Description.gif");
	
	/** The fsn icon. */
	public static Icon fsnIcon = new ImageIcon("icons/Name.gif");
	
	/** The black icon. */
	public static Icon blackIcon = new ImageIcon("icons/85.png");
	
	/** The preferred icon. */
	public static Icon preferredIcon = new ImageIcon("icons/Preferred.gif");
	
	/** The role group icon. */
	public static Icon roleGroupIcon = new ImageIcon("icons/rolegroup.gif");
	
	/** The folder. */
	public static Icon folder = new ImageIcon("icons/folder.png");
	
	/** The association icon. */
	public static Icon associationIcon = new ImageIcon("icons/Association.gif");
	
	/** The not accept icon. */
	public static Icon notAcceptIcon = new ImageIcon("icons/NotAccept.gif");

	/** The help icon. */
	public static Icon helpIcon = new ImageIcon("icons/help16.png");

	/** The check icon. */
	public static Icon checkIcon = new ImageIcon("icons/check.png");
	
	/** The add icon. */
	public static Icon addIcon = new ImageIcon("icons/add.png");
	
	/** The delete icon. */
	public static Icon deleteIcon = new ImageIcon("icons/delete.png");
	
	/** The media_step_forward icon. */
	public static Icon media_step_forwardIcon = new ImageIcon("icons/media_step_forward.png");

	/** The Translation_project. */
	public static Icon Translation_project = new ImageIcon("icons/branch_element.png");
	
	/** The Worksets. */
	public static Icon Worksets = new ImageIcon("icons/cubes_blue.png");
	
	/** The Workset. */
	public static Icon Workset = new ImageIcon("icons/cube_blue.png");
	
	/** The Partition_schemes. */
	public static Icon Partition_schemes = new ImageIcon("icons/cubes.png");
	
	/** The Partition_scheme. */
	public static Icon Partition_scheme = new ImageIcon("icons/cubes_green.png");
	
	/** The Partition. */
	public static Icon Partition = new ImageIcon("icons/cube_green.png");
	
	/** The Worklists. */
	public static Icon Worklists = new ImageIcon("icons/tables.png");
	
	/** The Worklist_statistics. */
	public static Icon Worklist_statistics = new ImageIcon("icons/column-chart.png");
	
	/** The Worklist. */
	public static Icon Worklist = new ImageIcon("icons/table.png");
	
	/** The Source_ language_ refset. */
	public static Icon Source_Language_Refset = new ImageIcon("icons/element_previous.png");
	
	/** The Target_ language_ refset. */
	public static Icon Target_Language_Refset = new ImageIcon("icons/element_next.png");
	
	/** The Exclusion_ refsets. */
	public static Icon Exclusion_Refsets = new ImageIcon("icons/cubes_yellow.png");
	
	/** The Exclusion_refset. */
	public static Icon Exclusion_refset = new ImageIcon("icons/cube_yellow_delete.png");
	
	/** The Inbox. */
	public static Icon Inbox = new ImageIcon("icons/inbox_into.png");
	
	/** The Outbox. */
	public static Icon Outbox = new ImageIcon("icons/outbox_out.png");
	
	/** The smart_ folder. */
	public static Icon smart_Folder = new ImageIcon("icons/box_white.png");
	
	/** The Custom_folder. */
	public static Icon Custom_folder = new ImageIcon("icons/box_edit.png");
	
	/** The Primitive_parent. */
	public static Icon Primitive_parent = new ImageIcon("icons/nav_up_left_blue.png");
	
	/** The Fully_defined_parent. */
	public static Icon Fully_defined_parent = new ImageIcon("icons/nav_up_left_green.png");
	
	/** The Inactive_parent. */
	public static Icon Inactive_parent = new ImageIcon("icons/nav_up_left_red.png");
	
	/** The Primitive_concept. */
	public static Icon Primitive_concept = new ImageIcon("icons/nav_plain_blue.png");
	
	/** The Fully_defined_concept. */
	public static Icon Fully_defined_concept = new ImageIcon("icons/nav_plain_green.png");
	
	/** The Inactive_concept. */
	public static Icon Inactive_concept = new ImageIcon("icons/nav_plain_red.png");
	
	/** The Preferred_acceptability. */
	public static Icon Preferred_acceptability = new ImageIcon("icons/star_yellow.png");
	
	/** The Acceptable_acceptability. */
	public static Icon Acceptable_acceptability = new ImageIcon("icons/bookmark.png");
	
	/** The Not_acceptable_acceptability. */
	public static Icon Not_acceptable_acceptability = new ImageIcon("icons/bookmark_delete.png");
	
	/** The Active_ fsn. */
	public static Icon Active_FSN = new ImageIcon("icons/bullet_square_green.png");
	
	/** The Inactive_ fsn. */
	public static Icon Inactive_FSN = new ImageIcon("icons/bullet_square_red.png");
	
	/** The Active_description. */
	public static Icon Active_description = new ImageIcon("icons/bullet_triangle_green.png");
	
	/** The Inactive_description. */
	public static Icon Inactive_description = new ImageIcon("icons/bullet_triangle_red.png");
	
	/** The Is_matching_guideline. */
	public static Icon Is_matching_guideline = new ImageIcon("icons/bell.png");
	
	/** The Has_associated_active_issues. */
	public static Icon Has_associated_active_issues = new ImageIcon("icons/flag_red.png");
	
	/** The Has_associated_resolved_issue. */
	public static Icon Has_associated_resolved_issue = new ImageIcon("icons/flag_green.png");
	
	/** The Has_comments. */
	public static Icon Has_comments = new ImageIcon("icons/Flag_blue.png");
	
	/** The Comment. */
	public static Icon Comment = new ImageIcon("icons/message.png");
	
	/** The Comments. */
	public static Icon Comments = new ImageIcon("icons/messages.png");
	
	/** The Add_comment. */
	public static Icon Add_comment = new ImageIcon("icons/message_add.png");
	
	/** The Versions. */
	public static Icon Versions = new ImageIcon("icons/dot-chart.png");
	
	/** The Initial capital status_significant. */
	public static Icon InitialCapitalStatus_significant = new ImageIcon("icons/font.png");
	
	/** The Rolegroup. */
	public static Icon Rolegroup = new ImageIcon("icons/elements_selection.png");
	
	/** The Role. */
	public static Icon Role = new ImageIcon("icons/element.png");
	
	/** The Qualifier. */
	public static Icon Qualifier = new ImageIcon("icons/element_into_input.png");
	
	/** The Other_ front. */
	public static Icon Other_Front = new ImageIcon("icons/graph_edge_directed.png");
	
	/** The ARG. */
	public static Icon ARG = new ImageIcon("icons/flag_argentina.png");
	
	/** The E n_ aus. */
	public static Icon EN_AUS = new ImageIcon("icons/flag_australia.png");
	
	/** The E n_ can. */
	public static Icon EN_CAN = new ImageIcon("icons/flag_canada.png");
	
	/** The CYP. */
	public static Icon CYP = new ImageIcon("icons/flag_cyprus.png");
	
	/** The DAN. */
	public static Icon DAN = new ImageIcon("icons/flag_denmark.png");
	
	/** The EES. */
	public static Icon EES = new ImageIcon("icons/flag_estonia.png");
	
	/** The E n_ gbr. */
	public static Icon EN_GBR = new ImageIcon("icons/flag_great_britain.png");
	
	/** The LTU. */
	public static Icon LTU = new ImageIcon("icons/flag_lithuania.png");
	
	/** The NLD. */
	public static Icon NLD = new ImageIcon("icons/flag_netherlands.png");
	
	/** The E n_ nzd. */
	public static Icon EN_NZD = new ImageIcon("icons/flag_new_zealand.png");
	
	/** The Z h_ sgp. */
	public static Icon ZH_SGP = new ImageIcon("icons/flag_singapore.png");
	
	/** The SKA. */
	public static Icon SKA = new ImageIcon("icons/flag_slovakia.png");
	
	/** The SIA. */
	public static Icon SIA = new ImageIcon("icons/flag_slovenia.png");
	
	/** The ESP. */
	public static Icon ESP = new ImageIcon("icons/flag_spain.png");
	
	/** The SVE. */
	public static Icon SVE = new ImageIcon("icons/flag_sweden.png");
	
	/** The E n_ usa. */
	public static Icon EN_USA = new ImageIcon("icons/flag_usa.png");

	/** The Constant CUSTOM_NODE. */
	public static final String CUSTOM_NODE = "Custom";
	
	/** The Constant STATUS_NODE. */
	public static final String STATUS_NODE = "Status";
	
	/** The Constant INBOX_NODE. */
	public static final String INBOX_NODE = "Inbox";
	
	/** The Constant CUSTOM_NODE_ROOT. */
	public static final String CUSTOM_NODE_ROOT = "Custom Node Root";
	
	/** The Constant STATUS_NODE_ROOT. */
	public static final String STATUS_NODE_ROOT = "Status Node Root";
	
	/** The Constant WORKLIST_NODE_ROOT. */
	public static final String WORKLIST_NODE_ROOT = "Worklist Node Root";
	
	/** The Constant WORKLIST_NODE. */
	public static final String WORKLIST_NODE = "Worklists";
	
	/** The Constant OUTBOX_NODE. */
	public static final String OUTBOX_NODE = "Outbox";

	/** The Constant CONCEPT. */
	public static final int CONCEPT = 0;
	
	/** The Constant ID. */
	public static final int ID = 1;
	
	/** The Constant CONCEPTID. */
	public static final int CONCEPTID = 2;
	
	/** The Constant ATTRIBUTE. */
	public static final int ATTRIBUTE = 3;
	
	/** The Constant FSNDESCRIPTION. */
	public static final int FSNDESCRIPTION = 4;
	
	/** The Constant PREFERRED. */
	public static final int PREFERRED = 5;
	
	/** The Constant SUPERTYPE. */
	public static final int SUPERTYPE = 6;
	
	/** The Constant ROLE. */
	public static final int ROLE = 7;
	
	/** The Constant DESCRIPTIONINFO. */
	public static final int DESCRIPTIONINFO = 8;
	
	/** The Constant RELATIONSHIPINFO. */
	public static final int RELATIONSHIPINFO = 9;
	
	/** The Constant SYNONYMN. */
	public static final int SYNONYMN = 10;
	
	/** The Constant ROLEGROUP. */
	public static final int ROLEGROUP = 11;
	
	/** The Constant ASSOCIATION. */
	public static final int ASSOCIATION = 12;
	
	/** The Constant FOLDER. */
	public static final int FOLDER = 13;
	
	/** The Constant NOTACCEPTABLE. */
	public static final int NOTACCEPTABLE = 14;
	
	/** The Constant DEFINED. */
	public static final int DEFINED = 15;
	
	/** The Constant PRIMITIVE. */
	public static final int PRIMITIVE = 16;
	
	/** The Constant INACTIVE. */
	public static final int INACTIVE = 17;
	
	/** The Constant PRIMITIVE_PARENT. */
	public static final int PRIMITIVE_PARENT = 18;
	
	/** The Constant DEFINED_PARENT. */
	public static final int DEFINED_PARENT = 19;
	
	/** The Constant INACTIVE_PARENT. */
	public static final int INACTIVE_PARENT = 20;
	
	/** The Constant TRANSLATION_PROJECT. */
	public static final int TRANSLATION_PROJECT = 21;
	
	/** The Constant BLUE_ICON. */
	public static final int BLUE_ICON = 22;
	
	/** The Constant GREEN_ICON. */
	public static final int GREEN_ICON = 23;

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
}
