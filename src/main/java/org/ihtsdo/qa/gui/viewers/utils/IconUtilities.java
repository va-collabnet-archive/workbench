package org.ihtsdo.qa.gui.viewers.utils;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.dwfa.cement.ArchitectonicAuxiliary;

public class IconUtilities {

	public static Icon redIcon = new ImageIcon("icons/91.png");
	public static Icon blueIcon = new ImageIcon("icons/93.png");
	public static Icon greenIcon = new ImageIcon("icons/95.png");
	public static Icon attributeIcon = new ImageIcon("icons/ConceptStatus.gif");
	public static Icon orangeIcon = new ImageIcon("icons/90.png");
	public static Icon descriptionIcon = new ImageIcon("icons/Description.gif");
	public static Icon fsnIcon = new ImageIcon("icons/Name.gif");
	public static Icon blackIcon = new ImageIcon("icons/85.png");
	public static Icon preferredIcon = new ImageIcon("icons/Preferred.gif");
	public static Icon roleGroupIcon = new ImageIcon("icons/rolegroup.gif");
	public static Icon folder = new ImageIcon("icons/folder.png");
	public static Icon associationIcon = new ImageIcon("icons/Association.gif");
	public static Icon notAcceptIcon = new ImageIcon("icons/NotAccept.gif");

	public static Icon helpIcon = new ImageIcon("icons/help16.png");

	public static Icon checkIcon = new ImageIcon("icons/check.png");
	public static Icon addIcon = new ImageIcon("icons/add.png");
	public static Icon deleteIcon = new ImageIcon("icons/delete.png");
	public static Icon media_step_forwardIcon = new ImageIcon("icons/media_step_forward.png");

	public static Icon Translation_project = new ImageIcon("icons/branch_element.png");
	public static Icon Worksets = new ImageIcon("icons/cubes_blue.png");
	public static Icon Workset = new ImageIcon("icons/cube_blue.png");
	public static Icon Partition_schemes = new ImageIcon("icons/cubes.png");
	public static Icon Partition_scheme = new ImageIcon("icons/cubes_green.png");
	public static Icon Partition = new ImageIcon("icons/cube_green.png");
	public static Icon Worklists = new ImageIcon("icons/tables.png");
	public static Icon Worklist_statistics = new ImageIcon("icons/column-chart.png");
	public static Icon Worklist = new ImageIcon("icons/table.png");
	public static Icon Source_Language_Refset = new ImageIcon("icons/element_previous.png");
	public static Icon Target_Language_Refset = new ImageIcon("icons/element_next.png");
	public static Icon Exclusion_Refsets = new ImageIcon("icons/cubes_yellow.png");
	public static Icon Exclusion_refset = new ImageIcon("icons/cube_yellow_delete.png");
	public static Icon Inbox = new ImageIcon("icons/inbox_into.png");
	public static Icon Outbox = new ImageIcon("icons/outbox_out.png");
	public static Icon smart_Folder = new ImageIcon("icons/box_white.png");
	public static Icon Custom_folder = new ImageIcon("icons/box_edit.png");
	public static Icon Primitive_parent = new ImageIcon("icons/nav_up_left_blue.png");
	public static Icon Fully_defined_parent = new ImageIcon("icons/nav_up_left_green.png");
	public static Icon Inactive_parent = new ImageIcon("icons/nav_up_left_red.png");
	public static Icon Primitive_concept = new ImageIcon("icons/nav_plain_blue.png");
	public static Icon Fully_defined_concept = new ImageIcon("icons/nav_plain_green.png");
	public static Icon Inactive_concept = new ImageIcon("icons/nav_plain_red.png");
	public static Icon Preferred_acceptability = new ImageIcon("icons/star_yellow.png");
	public static Icon Acceptable_acceptability = new ImageIcon("icons/bookmark.png");
	public static Icon Not_acceptable_acceptability = new ImageIcon("icons/bookmark_delete.png");
	public static Icon Active_FSN = new ImageIcon("icons/bullet_square_green.png");
	public static Icon Inactive_FSN = new ImageIcon("icons/bullet_square_red.png");
	public static Icon Active_description = new ImageIcon("icons/bullet_triangle_green.png");
	public static Icon Inactive_description = new ImageIcon("icons/bullet_triangle_red.png");
	public static Icon Is_matching_guideline = new ImageIcon("icons/bell.png");
	public static Icon Has_associated_active_issues = new ImageIcon("icons/flag_red.png");
	public static Icon Has_associated_resolved_issue = new ImageIcon("icons/flag_green.png");
	public static Icon Has_comments = new ImageIcon("icons/Flag_blue.png");
	public static Icon Comment = new ImageIcon("icons/message.png");
	public static Icon Comments = new ImageIcon("icons/messages.png");
	public static Icon Add_comment = new ImageIcon("icons/message_add.png");
	public static Icon Versions = new ImageIcon("icons/dot-chart.png");
	public static Icon InitialCapitalStatus_significant = new ImageIcon("icons/font.png");
	public static Icon Rolegroup = new ImageIcon("icons/elements_selection.png");
	public static Icon Role = new ImageIcon("icons/element.png");
	public static Icon Qualifier = new ImageIcon("icons/element_into_input.png");
	public static Icon Other_Front = new ImageIcon("icons/graph_edge_directed.png");
	public static Icon ARG = new ImageIcon("icons/flag_argentina.png");
	public static Icon EN_AUS = new ImageIcon("icons/flag_australia.png");
	public static Icon EN_CAN = new ImageIcon("icons/flag_canada.png");
	public static Icon CYP = new ImageIcon("icons/flag_cyprus.png");
	public static Icon DAN = new ImageIcon("icons/flag_denmark.png");
	public static Icon EES = new ImageIcon("icons/flag_estonia.png");
	public static Icon EN_GBR = new ImageIcon("icons/flag_great_britain.png");
	public static Icon LTU = new ImageIcon("icons/flag_lithuania.png");
	public static Icon NLD = new ImageIcon("icons/flag_netherlands.png");
	public static Icon EN_NZD = new ImageIcon("icons/flag_new_zealand.png");
	public static Icon ZH_SGP = new ImageIcon("icons/flag_singapore.png");
	public static Icon SKA = new ImageIcon("icons/flag_slovakia.png");
	public static Icon SIA = new ImageIcon("icons/flag_slovenia.png");
	public static Icon ESP = new ImageIcon("icons/flag_spain.png");
	public static Icon SVE = new ImageIcon("icons/flag_sweden.png");
	public static Icon EN_USA = new ImageIcon("icons/flag_usa.png");

	public static final String CUSTOM_NODE = "Custom";
	public static final String STATUS_NODE = "Status";
	public static final String INBOX_NODE = "Inbox";
	public static final String CUSTOM_NODE_ROOT = "Custom Node Root";
	public static final String STATUS_NODE_ROOT = "Status Node Root";
	public static final String WORKLIST_NODE_ROOT = "Worklist Node Root";
	public static final String WORKLIST_NODE = "Worklists";
	public static final String OUTBOX_NODE = "Outbox";

	public static final int CONCEPT = 0;
	public static final int ID = 1;
	public static final int CONCEPTID = 2;
	public static final int ATTRIBUTE = 3;
	public static final int FSNDESCRIPTION = 4;
	public static final int PREFERRED = 5;
	public static final int SUPERTYPE = 6;
	public static final int ROLE = 7;
	public static final int DESCRIPTIONINFO = 8;
	public static final int RELATIONSHIPINFO = 9;
	public static final int SYNONYMN = 10;
	public static final int ROLEGROUP = 11;
	public static final int ASSOCIATION = 12;
	public static final int FOLDER = 13;
	public static final int NOTACCEPTABLE = 14;
	public static final int DEFINED = 15;
	public static final int PRIMITIVE = 16;
	public static final int INACTIVE = 17;
	public static final int PRIMITIVE_PARENT = 18;
	public static final int DEFINED_PARENT = 19;
	public static final int INACTIVE_PARENT = 20;
	public static final int TRANSLATION_PROJECT = 21;
	public static final int BLUE_ICON = 22;
	public static final int GREEN_ICON = 23;

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

	public static Icon getIconForICS(Boolean type) {
		if (type) {
			return InitialCapitalStatus_significant;
		}
		return null;
	}

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
