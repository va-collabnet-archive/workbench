/**
 *
 */
package org.dwfa.ace.gui.concept;

//~--- non-JDK imports --------------------------------------------------------
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.worker.Worker;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;
import org.ihtsdo.helper.dto.DtoExtract;
import org.ihtsdo.helper.dto.DtoToText;
import org.ihtsdo.helper.export.ActiveOnlyExport;
import org.ihtsdo.helper.io.FileIO;
import org.dwfa.ace.reporting.AuthorReporter;
import org.ihtsdo.helper.bdb.NidDuplicateFinder;
import org.ihtsdo.helper.bdb.NidDuplicateReporter;
import org.ihtsdo.helper.bdb.UuidDupFinder;
import org.ihtsdo.helper.bdb.UuidDupReporter;
import org.ihtsdo.project.workflow.api.wf2.implementation.WorkflowStore;
import org.ihtsdo.request.uscrs.UscrsContentRequestHandler;
import org.ihtsdo.rules.RulesLibrary;
import org.ihtsdo.rules.testmodel.TerminologyHelperDroolsWorkbench;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.dto.concept.TkConcept;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

public class ProgrammersPopupListener extends MouseAdapter implements ActionListener, ClipboardOwner {

    private static Map<String, MENU_OPTIONS> optionMap = new HashMap<String, MENU_OPTIONS>(MENU_OPTIONS.values().length);

    //~--- static initializers -------------------------------------------------
    static {
        for (MENU_OPTIONS option : MENU_OPTIONS.values()) {
            optionMap.put(option.menuText, option);
        }
    }
    //~--- fields --------------------------------------------------------------
    JPopupMenu popup = new JPopupMenu();
    /**
     *
     */
    private final ConceptPanel conceptPanel;

    //~--- constructors --------------------------------------------------------
    public ProgrammersPopupListener(ConceptPanel conceptPanel) {
        for (MENU_OPTIONS option : MENU_OPTIONS.values()) {
            option.addToMenu(popup, this);
        }

        this.conceptPanel = conceptPanel;
    }

    private void launchBp() {
        FileDialog dialog =
                new FileDialog(new Frame(), "Select process to launch...");

        dialog.setMode(FileDialog.LOAD);
        dialog.setDirectory(System.getProperty("user.dir"));
        dialog.setVisible(true);

        if (dialog.getFile() != null) {
            ObjectInputStream inStream = null;
            try {
                File processFile = new File(dialog.getDirectory(), dialog.getFile());
                inStream = new ObjectInputStream(new FileInputStream(processFile));
                I_EncodeBusinessProcess processToLaunch = (I_EncodeBusinessProcess) inStream.readObject();
                Worker worker = this.conceptPanel.getConfig().getWorker();

                // Launch Process...
                Stack<I_EncodeBusinessProcess> ps = worker.getProcessStack();
                worker.setProcessStack(new Stack<I_EncodeBusinessProcess>());
                processToLaunch.execute(worker);
                worker.setProcessStack(ps);

            } catch (Exception ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            } finally {
                try {
                    inStream.close();
                } catch (IOException ex) {
                    AceLog.getAppLog().alertAndLogException(ex);
                }
            }
        }
    }

    //~--- enums ---------------------------------------------------------------
    private enum MENU_OPTIONS {
        //J-

        WRITE_LONG_FORM_TO_CLIPBOARD("Write long form to clipboard"),
        SET_FROM_NID("Set from nid"),
        ADD_TO_WATCH_LIST("Add to watch list"),
        REMOVE_FROM_WATCH_LIST("Remove from watch list"),
        GET_CONCEPT_ATTRIBUTES("Get concept attributes..."),
        SET_CACHE_SIZE("Set cache size"),
        SET_CACHE_PERCENT("Set cache percent"),
        CHANGE_SET_TO_TEXT("Change set to text..."),
        PROCESS_ECONCEPT("Process eConcept file..."),
        ALL_CHANGE_SET_TO_TEXT("All change sets in profiles to text"),
        EXTRACT_CHANGE_SETS_FOR_CONCEPT("Extract change sets for concept..."),
        EXTRACT_CHANGE_SETS_FOR_CONCEPT_AND_ASSIGN_NEW_NIDS(
        "Extract change sets for concept and assign new nids..."),
        DTO_TO_TEXT("DTO to text..."),
        IMPORT_CHANGE_SET("Import change set..."),
        TOGGLE_QA("Toggle QA"),
        REINDEX_LUCENE("Recreate Lucene index"),
        EXPORT_ACTIVE_ONLY("Export active only from view"),
        PATCH_MSMITH("Patch msmith#80#"),
        TEST_ISA_CACHE("Test isa cache"),
        LAUNCH_WF_CHANGES_INIT("Launch workflow initiator on changes..."),
        LAUNCH_BP("Launch Business Process..."),
        USCRS_EXPORT("USCRS Export"),
        AUTHOR_REPORT("All changes by current user to text"),
        DUP_NID_CHECKER("Check database for duplicate nids"),
        DUP_UUID_CHECKER("Check database for duplicate uuids");
        //J+
        String menuText;

        //~--- constructors -----------------------------------------------------
        private MENU_OPTIONS(String menuText) {
            this.menuText = menuText;
        }

        //~--- methods ----------------------------------------------------------
        public void addToMenu(JPopupMenu popup, ActionListener l) {
            JMenuItem menuItem = new JMenuItem(menuText);

            menuItem.addActionListener(l);
            popup.add(menuItem);
        }

        //~--- get methods ------------------------------------------------------
        public String getText() {
            return this.menuText;
        }

        //~--- set methods ------------------------------------------------------
        public void setText(String menuText) {
            this.menuText = menuText;
        }
    }

    //~--- methods -------------------------------------------------------------
    @Override
    public void actionPerformed(ActionEvent e) {
        switch (optionMap.get(e.getActionCommand())) {
            case ADD_TO_WATCH_LIST:
                addToWatch();

                break;

            case REMOVE_FROM_WATCH_LIST:
                removeFromWatch();

                break;

            case SET_FROM_NID:
                setFromNid();

                break;

            case WRITE_LONG_FORM_TO_CLIPBOARD:
                writeLongFormToClipboard();

                break;

            case GET_CONCEPT_ATTRIBUTES:
                getConceptAttributes();

                break;

            case SET_CACHE_PERCENT:
                setCachePercent();

                break;

            case SET_CACHE_SIZE:
                setCacheSize();

                break;

            case CHANGE_SET_TO_TEXT:
            case DTO_TO_TEXT:
            case ALL_CHANGE_SET_TO_TEXT:
                toText(optionMap.get(e.getActionCommand()));

                break;

            case PROCESS_ECONCEPT:
                processEConcept();
                break;

            case IMPORT_CHANGE_SET:
                importEccs();

                break;

            case TOGGLE_QA:
                toggleQa();

                break;

            case REINDEX_LUCENE:
                recreateLuceneIndex();

                break;

            case EXTRACT_CHANGE_SETS_FOR_CONCEPT:
                extractChangeSets();

                break;

            case EXTRACT_CHANGE_SETS_FOR_CONCEPT_AND_ASSIGN_NEW_NIDS:
                extractChangeSetsAndAssignNewNids();

                break;

            case EXPORT_ACTIVE_ONLY:
                exportActiveOnly();

                break;

            case PATCH_MSMITH:
                patchMSmith();

                break;

            case TEST_ISA_CACHE:
                testIsaCache();

                break;

            case LAUNCH_BP:
                launchBp();
                break;
                
            case LAUNCH_WF_CHANGES_INIT:
                lauchWfChangesInit();
                break;
                
            case AUTHOR_REPORT:
                generateAuthorReport();
                break;
            
            case DUP_NID_CHECKER:
                checkForDuplicateNids();
                break;
            
            case DUP_UUID_CHECKER:
                checkForDuplicateUuids();
                break;
                
            case USCRS_EXPORT:
                exportForUSCRS();
                break;
            default:
                AceLog.getAppLog().alertAndLogException(new Exception("No support for: "
                        + optionMap.get(e.getActionCommand())));
        }
    }

    private void lauchWfChangesInit() {
        WorkflowStore wf = new WorkflowStore();
        try {
            String start = (String) JOptionPane.showInputDialog(
                    null,
                    "Enter start date as MM/dd/yy hh:mm:ss",
                    "Start Date",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    "");
            
            if (start == null || start.trim().isEmpty()) {
                start = "02/01/13 00:00:00";
            }
            
            String end = (String) JOptionPane.showInputDialog(
                    null,
                    "Enter end date as MM/dd/yy hh:mm:ss",
                    "End Date",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    "");
            
            if (end == null || end.trim().isEmpty()) {
                end = "12/31/13 00:00:00";
            }

            wf.sendAllChangesInTimeRangeToDefaultWorkflow(start, end);
        } catch (Exception e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }
        
    }

    private void addToWatch() {
        I_GetConceptData igcd = (I_GetConceptData) this.conceptPanel.getTermComponent();

        Terms.get().addToWatchList(igcd);
    }

    public String askQuestion(String realm, String question, String defaultAnswer) {
        return (String) JOptionPane.showInputDialog(this.conceptPanel, question, realm,
                JOptionPane.PLAIN_MESSAGE, null, null, defaultAnswer);
    }

    private void testIsaCache() {
        try {
            TerminologyHelperDroolsWorkbench thdw = new TerminologyHelperDroolsWorkbench();

            List<I_GetConceptData> parents = new ArrayList<I_GetConceptData>();
            parents.add(Terms.get().getConcept(UUID.fromString("0bab48ac-3030-3568-93d8-aee0f63bf072")));
            parents.add(Terms.get().getConcept(UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8")));
            parents.add(Terms.get().getConcept(UUID.fromString("5adbed85-55d8-3304-a404-4bebab660fff")));
            parents.add(Terms.get().getConcept(UUID.fromString("bd83b1dd-5a82-34fa-bb52-06f666420a1c")));
            parents.add(Terms.get().getConcept(UUID.fromString("a5db42d4-6d94-33b7-92e7-d4a1d0f0d814")));
            parents.add(Terms.get().getConcept(UUID.fromString("d4227098-db7a-331e-8f00-9d1e27626fc5")));
            parents.add(Terms.get().getConcept(UUID.fromString("f267fc6f-7c4d-3a79-9f17-88b82b42709a")));
            parents.add(Terms.get().getConcept(UUID.fromString("0c7b717a-3e41-372b-be57-621befb9b3ee")));
            parents.add(Terms.get().getConcept(UUID.fromString("e730d11f-e155-3482-a423-9637db3bc1a2")));
            parents.add(Terms.get().getConcept(UUID.fromString("d96c5d7d-3314-3048-919e-4b866225c6c6")));
            parents.add(Terms.get().getConcept(UUID.fromString("d8a42cc5-05dd-3fcf-a1f7-62856e38874a")));
            parents.add(Terms.get().getConcept(UUID.fromString("bfbced4b-ad7d-30aa-ae5c-f848ccebd45b")));
            parents.add(Terms.get().getConcept(UUID.fromString("a0db7e17-c6b2-3acc-811d-8a523274e869")));

            List<I_GetConceptData> domains = new ArrayList<I_GetConceptData>();
            domains.add(Terms.get().getConcept(UUID.fromString("0acd09e5-a5f1-4880-82b2-2a5c273dca29")));
            domains.add(Terms.get().getConcept(UUID.fromString("a8883b08-9920-4531-9eb5-2ea578402157")));
            domains.add(Terms.get().getConcept(UUID.fromString("46c55be1-5a10-48e4-ab5a-ef579e1990b8")));
            domains.add(Terms.get().getConcept(UUID.fromString("272bcda1-766d-41e7-a711-b8d8e75fa5b2")));
            domains.add(Terms.get().getConcept(UUID.fromString("767e3525-ebd4-43f9-9640-952e31589e47")));
            domains.add(Terms.get().getConcept(UUID.fromString("674f7913-a5c3-4d92-834c-6ba879dc7423")));
            domains.add(Terms.get().getConcept(UUID.fromString("2e6d715a-aa77-4473-9720-93455d34c64c")));
            domains.add(Terms.get().getConcept(UUID.fromString("2465beac-65b5-47ef-8082-6e42934a4b18")));
            domains.add(Terms.get().getConcept(UUID.fromString("8d0f383e-fa0e-4492-843e-bc2003037dfa")));
            domains.add(Terms.get().getConcept(UUID.fromString("aebf2ccd-181d-45b9-bea8-8511c07a7fb7")));
            domains.add(Terms.get().getConcept(UUID.fromString("a0ff836c-bf65-4c06-a11b-f5ebe0eea5e9")));

            List<I_GetConceptData> ranges = new ArrayList<I_GetConceptData>();
            ranges.add(Terms.get().getConcept(UUID.fromString("af9e9019-136e-4816-80ab-801df9c61a6b")));
            ranges.add(Terms.get().getConcept(UUID.fromString("7785e4d0-ef89-411e-bb53-a39cb1a7dd01")));
            ranges.add(Terms.get().getConcept(UUID.fromString("a985589e-5ecb-42ad-a04d-9cb5be711979")));
            ranges.add(Terms.get().getConcept(UUID.fromString("3b753d9c-8ded-4def-9318-07b3b80a1a4e")));
            ranges.add(Terms.get().getConcept(UUID.fromString("efe80992-2c82-4adc-b91a-aa259772a370")));
            ranges.add(Terms.get().getConcept(UUID.fromString("329228ca-5f69-44d3-aa7e-91c798a6a717")));
            ranges.add(Terms.get().getConcept(UUID.fromString("de0ba6ce-bfc6-44ea-bfe8-c8adc5ff4f54")));
            ranges.add(Terms.get().getConcept(UUID.fromString("8a6f1b4d-0965-4216-b7b3-a38eaf364acf")));
            ranges.add(Terms.get().getConcept(UUID.fromString("034c1f09-2356-497a-94cf-57401fdf0210")));
            ranges.add(Terms.get().getConcept(UUID.fromString("27baf8b8-0012-4132-95b3-2089d89e1622")));
            ranges.add(Terms.get().getConcept(UUID.fromString("98582c9c-8a08-4a12-aaf3-48c90b38f6c1")));
            ranges.add(Terms.get().getConcept(UUID.fromString("b3c2919f-4cc9-45e6-a499-437e94bbb452")));
            ranges.add(Terms.get().getConcept(UUID.fromString("f0133e8f-547f-4fc6-8c10-6c6d1be0f531")));
            ranges.add(Terms.get().getConcept(UUID.fromString("c658f166-301f-45f6-b5aa-c52989e8e829")));
            ranges.add(Terms.get().getConcept(UUID.fromString("4f3404b7-5da3-4092-bb90-778c65df4f31")));
            ranges.add(Terms.get().getConcept(UUID.fromString("bedc628b-1e0b-4359-aa7d-7a3078e3bee0")));
            ranges.add(Terms.get().getConcept(UUID.fromString("81a14dc2-ae8f-48fc-a43b-b445ae89df44")));
            ranges.add(Terms.get().getConcept(UUID.fromString("f76ece36-c3f6-49fd-90ec-7ec57e5cafb9")));
            ranges.add(Terms.get().getConcept(UUID.fromString("e9fc0f33-0d5c-46f4-bd39-2807cb04080b")));
            ranges.add(Terms.get().getConcept(UUID.fromString("87fe35ac-458d-41c1-8588-129d1bab3951")));
            ranges.add(Terms.get().getConcept(UUID.fromString("3d2a28e3-67bd-470e-a570-991bbce52d1d")));
            ranges.add(Terms.get().getConcept(UUID.fromString("753e3b8f-14de-40e8-aad3-d07aaa6ebf18")));
            ranges.add(Terms.get().getConcept(UUID.fromString("32318346-fbe8-467c-90b5-9ae8f65a348b")));
            ranges.add(Terms.get().getConcept(UUID.fromString("c8ab369c-fe56-4737-be32-5e5859a16d79")));
            ranges.add(Terms.get().getConcept(UUID.fromString("6f169a0b-36fc-4341-b355-65a2f293eccf")));
            ranges.add(Terms.get().getConcept(UUID.fromString("66d14afd-7457-488c-9ae4-47697b1700eb")));
            ranges.add(Terms.get().getConcept(UUID.fromString("128946c4-9181-4f69-97f4-03d50625737d")));
            ranges.add(Terms.get().getConcept(UUID.fromString("24d21983-2158-4c58-bd6c-59ac73417096")));
            ranges.add(Terms.get().getConcept(UUID.fromString("c6559269-cdec-478c-80f8-a69a6c979b05")));
            ranges.add(Terms.get().getConcept(UUID.fromString("593dae07-8e86-40f4-8c04-fc5804d1ac45")));
            ranges.add(Terms.get().getConcept(UUID.fromString("c0111487-cb37-4afb-b2c3-ec8d09850b78")));
            ranges.add(Terms.get().getConcept(UUID.fromString("cb7b1a32-f558-406b-9348-c0dcaa039051")));
            ranges.add(Terms.get().getConcept(UUID.fromString("91359120-2b7f-40ad-bc68-5520ed5ae4e0")));
            ranges.add(Terms.get().getConcept(UUID.fromString("e0806cb8-62dd-4680-a2c4-6838bd619f3c")));
            ranges.add(Terms.get().getConcept(UUID.fromString("255226b9-4fe8-4947-af3f-3003f53d0a22")));
            ranges.add(Terms.get().getConcept(UUID.fromString("26d816df-bfb0-47b7-aa56-d4f516356f07")));
            ranges.add(Terms.get().getConcept(UUID.fromString("c79b5385-ec6d-44ed-ad6a-ba71d67dfaf1")));
            ranges.add(Terms.get().getConcept(UUID.fromString("1dbed7d0-b2be-4435-9487-28e1b8981a03")));
            ranges.add(Terms.get().getConcept(UUID.fromString("ac3f9c9c-1f6e-4b31-972d-8a99286df03a")));
            ranges.add(Terms.get().getConcept(UUID.fromString("a9e4c5a7-a753-42ad-b395-d5e5f41fb326")));
            ranges.add(Terms.get().getConcept(UUID.fromString("9a7c4424-cdd8-4442-a570-55a1da760854")));
            ranges.add(Terms.get().getConcept(UUID.fromString("cf2df8a2-88e4-417d-ba5c-cd33a1292add")));
            ranges.add(Terms.get().getConcept(UUID.fromString("78d5f6ea-f5db-429f-9d4f-3fd291eb4302")));
            ranges.add(Terms.get().getConcept(UUID.fromString("b3d62f01-cae1-4a4d-8a1e-1302cca565d9")));
            ranges.add(Terms.get().getConcept(UUID.fromString("256875d1-f29b-4ea4-ad10-811ecd87a74d")));
            ranges.add(Terms.get().getConcept(UUID.fromString("a744e178-ec29-45e8-ae0a-63d85927da2b")));
            ranges.add(Terms.get().getConcept(UUID.fromString("0c41774c-14af-4195-853f-29e8e3826ae3")));
            ranges.add(Terms.get().getConcept(UUID.fromString("224770fd-4c0b-486c-b053-1b07ade4293b")));
            ranges.add(Terms.get().getConcept(UUID.fromString("59e7e1be-9b91-431d-b1c8-479bf536e56a")));
            ranges.add(Terms.get().getConcept(UUID.fromString("abbbb4cf-8072-4348-8252-fd8f2ef1c039")));
            ranges.add(Terms.get().getConcept(UUID.fromString("7325a456-9d3d-4c9c-8964-467893c72f2b")));
            ranges.add(Terms.get().getConcept(UUID.fromString("6d682c29-346e-40f5-b3af-6bd4cb6fa3ee")));
            ranges.add(Terms.get().getConcept(UUID.fromString("2a190f65-9905-4b15-9f84-8bcb2538e947")));
            ranges.add(Terms.get().getConcept(UUID.fromString("91e9272a-182f-44cf-b788-44bb654c37b5")));
            ranges.add(Terms.get().getConcept(UUID.fromString("0e65d464-c379-42b4-999f-5448580e61a2")));
            ranges.add(Terms.get().getConcept(UUID.fromString("f57ad8fc-2ca7-4632-8c9e-74b7e71914d7")));
            ranges.add(Terms.get().getConcept(UUID.fromString("5734a7a3-3926-43d0-a005-32ee4bddc423")));
            ranges.add(Terms.get().getConcept(UUID.fromString("9058754a-4262-4f07-9096-9b8f92fa8503")));

            I_GetConceptData descendant = (I_GetConceptData) this.conceptPanel.getTermComponent();
            I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
            for (I_GetConceptData loopParent : parents) {
                if (thdw.isParentOfOrEqualTo(loopParent.getPrimUuid().toString(),
                        descendant.getPrimUuid().toString())) {
                    System.out.println("Parent: " + loopParent + " Descendant: " + descendant + " *** result = TRUE");
                } else {
                    System.out.println("Parent: " + loopParent + " Descendant: " + descendant + " *** result = FALSE");
                }
            }
            System.out.println("");
            System.out.println("");
            System.out.println("");
            for (I_GetConceptData loopdomain : domains) {
                if (thdw.isMemberOf(descendant.getPrimUuid().toString(),
                        loopdomain.getPrimUuid().toString())) {
                    System.out.println("Domain: " + loopdomain + " Member: " + descendant + " *** result = TRUE");
                } else {
                    System.out.println("Domain: " + loopdomain + " Member: " + descendant + " *** result = FALSE");
                }
            }
            System.out.println("");
            System.out.println("");
            System.out.println("");
            for (I_GetConceptData looprange : ranges) {
                if (thdw.isMemberOf(descendant.getPrimUuid().toString(),
                        looprange.getPrimUuid().toString())) {
                    System.out.println("Range: " + looprange + " Member: " + descendant + " *** result = TRUE");
                } else {
                    System.out.println("Range: " + looprange + " Member: " + descendant + " *** result = FALSE");
                }
            }
            System.out.println("");
            System.out.println("");
            System.out.println("");

            String uuidDesc = JOptionPane.showInputDialog("Enter descendant UUID:");

            if (uuidDesc != null && !uuidDesc.isEmpty()) {

                UUID descUuid = UUID.fromString(uuidDesc);
                I_GetConceptData newDescenant = Terms.get().getConcept(descUuid);

                for (I_GetConceptData loopParent : parents) {
                    if (loopParent.isParentOfOrEqualTo(newDescenant,
                            config.getAllowedStatus(),
                            config.getDestRelTypes(),
                            config.getViewPositionSetReadOnly(),
                            config.getPrecedence(), config.getConflictResolutionStrategy())) {
                        System.out.println("Parent: " + loopParent + " Descendant: " + newDescenant + " *** result = TRUE");
                    } else {
                        System.out.println("Parent: " + loopParent + " Descendant: " + newDescenant + " *** result = FALSE");
                    }
                }
                System.out.println("");
                System.out.println("");
                System.out.println("");
            }

        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }

    }

    private boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();

            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }

        return (path.delete());
    }

    private void doExport(DataOutputStream out) {
        try {
            NidBitSetBI exclusionSet = Ts.get().getEmptyNidSet();

            exclusionSet.setMember(RefsetAuxiliary.Concept.WORKFLOW_HISTORY.localize().getNid());
            exclusionSet.setMember(RefsetAuxiliary.Concept.WORKFLOW.localize().getNid());
            exclusionSet.setMember(ArchitectonicAuxiliary.Concept.CTV3_ID.localize().getNid());
            exclusionSet.setMember(ArchitectonicAuxiliary.Concept.SNOMED_RT_ID.localize().getNid());

            Map<UUID, UUID> conversionMap = new HashMap<UUID, UUID>() {

                @Override
                public UUID get(Object o) {
                    UUID returnUuid = super.get(o);

                    if (returnUuid == null) {
                        returnUuid = (UUID) o;
                    }

                    return returnUuid;
                }
            };
            ViewCoordinate vc = conceptPanel.getConfig().getViewCoordinate();
            ActiveOnlyExport exporter = new ActiveOnlyExport(vc, vc, vc,
                    exclusionSet, out, conversionMap);

            Ts.get().iterateConceptDataInSequence(exporter);
        } catch (Exception ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }
    }

    private void exportActiveOnly() {
        FileDialog dialog = new FileDialog(new Frame(), "Select name and location for Active Only Export...");

        dialog.setMode(FileDialog.SAVE);
        dialog.setDirectory(System.getProperty("user.dir"));
        dialog.setFile("activeOnly.jbin");
        dialog.setVisible(true);

        if (dialog.getFile() != null) {
            File outputFile = new File(dialog.getDirectory(), dialog.getFile());

            outputFile.getParentFile().mkdirs();

            try {
                FileOutputStream fos = new FileOutputStream(outputFile);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                DataOutputStream out = new DataOutputStream(bos);

                ACE.threadPool.execute(new RunnableImpl(out));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ProgrammersPopupListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void extractChangeSets() {
        File rootFile = new File("profiles");
        String prefix = null;
        String suffix = ".eccs";
        List<File> changeSetFiles = FileIO.recursiveGetFiles(rootFile, prefix, suffix, true);
        I_GetConceptData igcd = (I_GetConceptData) this.conceptPanel.getTermComponent();
        FileDialog dialog = new FileDialog(new Frame(),
                "Select name and location for new directory...");

        dialog.setMode(FileDialog.SAVE);
        dialog.setDirectory(System.getProperty("user.dir"));
        dialog.setFile(igcd.toUserString() + " cs extract");

        Set<UUID> concepts = new TreeSet<UUID>();

        concepts.add(igcd.getPrimUuid());
        dialog.setVisible(true);

        if (dialog.getFile() != null) {
            File csfd = new File(dialog.getDirectory(), dialog.getFile());

            csfd.mkdir();

            for (File csf : changeSetFiles) {
                try {
                    File extractFile = new File(csfd, "ex-" + csf.getName());

                    DtoExtract.extract(csf, concepts, extractFile);
                } catch (IOException ex) {
                    AceLog.getAppLog().alertAndLogException(ex);
                } catch (ClassNotFoundException ex) {
                    AceLog.getAppLog().alertAndLogException(ex);
                }
            }
        }
    }

    private void extractChangeSetsAndAssignNewNids() {
        File rootFile = new File("profiles");
        String prefix = null;
        String suffix = ".eccs";
        List<File> changeSetFiles = FileIO.recursiveGetFiles(rootFile, prefix, suffix, true);
        I_GetConceptData igcd = (I_GetConceptData) this.conceptPanel.getTermComponent();
        FileDialog dialog =
                new FileDialog(new Frame(), "Select name and location for new cs extract and map directory...");

        dialog.setMode(FileDialog.SAVE);
        dialog.setDirectory(System.getProperty("user.dir"));
        dialog.setFile(igcd.toUserString() + " cs extract and map");

        Set<UUID> concepts = new TreeSet<UUID>();

        concepts.add(igcd.getPrimUuid());
        dialog.setVisible(true);

        if (dialog.getFile() != null) {
            File csfd = new File(dialog.getDirectory(), dialog.getFile());

            csfd.mkdir();

            DtoExtract.DynamicMap map = new DtoExtract.DynamicMap();

            for (File csf : changeSetFiles) {
                try {
                    File extractFile = new File(csfd, "ex-newnid-" + csf.getName());

                    DtoExtract.extractChangeSetsAndAssignNewNids(csf, concepts, extractFile, map);
                } catch (IOException ex) {
                    AceLog.getAppLog().alertAndLogException(ex);
                } catch (ClassNotFoundException ex) {
                    AceLog.getAppLog().alertAndLogException(ex);
                }
            }
        }
    }

    private void importEccs() {
        try {
            FileDialog dialog = new FileDialog(new Frame(), "Select change set file...");

            dialog.setMode(FileDialog.LOAD);
            dialog.setDirectory(System.getProperty("user.dir"));
            dialog.setFilenameFilter(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".eccs");
                }
            });
            dialog.setVisible(true);

            if (dialog.getFile() != null) {
                File csf = new File(dialog.getDirectory(), dialog.getFile());

                try {
                    Terms.get().suspendChangeSetWriters();

                    I_ReadChangeSet csr = Terms.get().newBinaryChangeSetReader(csf);
                    HashSet<ConceptChronicleBI> annotatedIndexes = new HashSet<>();

                    csr.read(annotatedIndexes);

                    if (WorkflowHelper.isWorkflowCapabilityAvailable()) {
                        I_ReadChangeSet wcsr = Terms.get().newWfHxLuceneChangeSetReader(csf);

                        wcsr.read(annotatedIndexes);
                    }
                    for (ConceptChronicleBI concept: annotatedIndexes) {
                        Ts.get().addUncommittedNoChecks(concept);
                    }
                } finally {
                    Terms.get().resumeChangeSetWriters();
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(ProgrammersPopupListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        // nothing to do...
    }

    private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            if (e.isAltDown() || e.isControlDown()) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    private void patchMSmith() {
        try {
            FileDialog dialog = new FileDialog(new Frame(), "Select change set file...");

            dialog.setMode(FileDialog.LOAD);
            dialog.setDirectory(System.getProperty("user.dir"));
            dialog.setFilenameFilter(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".eccs");
                }
            });
            dialog.setVisible(true);

            if (dialog.getFile() != null) {
                File csf = new File(dialog.getDirectory(), dialog.getFile());
                FileInputStream fis = new FileInputStream(csf);
                BufferedInputStream bis = new BufferedInputStream(fis);
                DataInputStream dataStream = new DataInputStream(bis);
                File ocsf = new File(dialog.getDirectory(), dialog.getFile() + ".fixed");
                FileOutputStream fos = new FileOutputStream(ocsf);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                DataOutputStream dataOutStream = new DataOutputStream(bos);

                try {
                    int count = 0;

                    while (dataStream.available() > 0) {
                        long nextCommit = dataStream.readLong();
                        TkConcept eConcept = new TkConcept(dataStream);

                        if (eConcept.getPrimordialUuid().equals(
                                UUID.fromString("629a6c24-1f0a-3941-90df-8a7103a98ec7"))) {
                            for (TkRefexAbstractMember member : eConcept.getRefsetMembers()) {
                                if (member.getPrimordialComponentUuid().equals(
                                        UUID.fromString("bbde54c9-fc58-41e7-90b2-52f7029068e1"))) {
                                    member.setPrimordialComponentUuid(UUID.randomUUID());
                                }
                            }
                        }

                        dataOutStream.writeLong(nextCommit);
                        eConcept.writeExternal(dataOutStream);
                        count++;
                    }
                } catch (EOFException ex) {
                    // Nothing to do...
                } finally {
                    dataStream.close();
                    dataOutStream.close();
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(ProgrammersPopupListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void recreateLuceneIndex() {
        AceLog.getAppLog().info("Deleting lindexes");
        deleteDirectory(new File("berkeley-db/mutable/lucene"));
        deleteDirectory(new File("berkeley-db/read-only/lucene"));

        try {
            Terms.get().doLuceneSearch("something");
        } catch (IOException ex) {
            Logger.getLogger(ProgrammersPopupListener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (java.text.ParseException ex) {
            Logger.getLogger(ProgrammersPopupListener.class.getName()).log(Level.SEVERE, null, ex);
        } 

        JOptionPane.showMessageDialog(null, "Descriptions Lucene indexes removed! Please restart Workbench.",
                "Warning", JOptionPane.WARNING_MESSAGE);
    }

    private void removeFromWatch() {
        I_GetConceptData igcd = (I_GetConceptData) this.conceptPanel.getTermComponent();

        Terms.get().removeFromWatchList(igcd);
    }

    private void processEConcept() {
        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(null);

        HashSet<UUID> conceptsToFind = new HashSet<UUID>();
        conceptsToFind.add(UUID.fromString("7560feb1-0778-314d-bc76-2d5071def2fa"));
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                DtoToText.searchForDto(fc.getSelectedFile(), conceptsToFind);
            } catch (Exception ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }
    }

    private void toText(MENU_OPTIONS option) {
        try {
            switch (option) {
                case ALL_CHANGE_SET_TO_TEXT:
                    File rootFile = new File("profiles");
                    String prefix = null;
                    String suffix = ".eccs";
                    List<File> changeSetFiles = FileIO.recursiveGetFiles(rootFile, prefix, suffix, true);

                    for (File csf : changeSetFiles) {
                        DtoToText.convertChangeSet(csf);
                    }

                    break;

                default:
                    JFileChooser fc = new JFileChooser();
                    int returnVal = fc.showOpenDialog(null);

                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        switch (option) {
                            case CHANGE_SET_TO_TEXT:
                                DtoToText.convertChangeSet(fc.getSelectedFile());

                                break;

                            case DTO_TO_TEXT:
                                DtoToText.convertDto(fc.getSelectedFile());

                                break;
                        }
                    }
            }
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (ClassNotFoundException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }

    private void toggleQa() {
        if (RulesLibrary.rulesDisabled == false) {
            RulesLibrary.rulesDisabled = true;
            JOptionPane.showMessageDialog(null, "QA is Disabled!.", "Warning", JOptionPane.WARNING_MESSAGE);
        } else {
            RulesLibrary.rulesDisabled = false;
            JOptionPane.showMessageDialog(null, "QA is Enabled!.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void writeLongFormToClipboard() {
        I_GetConceptData igcd = (I_GetConceptData) this.conceptPanel.getTermComponent();
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection contents = new StringSelection(igcd.toLongString());

        clip.setContents(contents, this);
    }
    
    private void exportForUSCRS() {
        try {
            I_GetConceptData igcd = (I_GetConceptData) this.conceptPanel.getTermComponent();
            new UscrsContentRequestHandler(igcd.getConceptNid());
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
            JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "Unexpected error performing export: " + e,
                "USCRS Content Request", JOptionPane.ERROR_MESSAGE);
        } 
    }
    
    private void generateAuthorReport() {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogTitle("Select report destination.");
            int returnVal = chooser.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                AuthorReporter reporter = new AuthorReporter(
                        conceptPanel.getConfig().getEditCoordinate().getAuthorNid(),
                        chooser.getSelectedFile(),
                        conceptPanel.getConfig().getViewCoordinate());
                Ts.get().iterateConceptDataInParallel(reporter);
                reporter.report();
            }
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
    }
    
    private void checkForDuplicateNids() {
        Ts.get().disableComponentsCRHM();
        try{
        AceLog.getAppLog().info("Testing for duplicate nids.");
        NidDuplicateFinder dupFinder = new NidDuplicateFinder();

        Ts.get().iterateConceptDataInParallel(dupFinder);
        System.out.println();

        if (dupFinder.getDupNids().isEmpty()) {
            AceLog.getAppLog().info("No duplicate nids found.");
        } else {
            dupFinder.writeDupFile();
            AceLog.getAppLog().severe("\n\nDuplicate nids found: " + dupFinder.getDupNids().size() + "\n"
                    + dupFinder.getDupNids() + "\n");

            NidDuplicateReporter reporter = new NidDuplicateReporter(dupFinder.getDupNids());

            Ts.get().iterateConceptDataInParallel(reporter);
            reporter.reportDupClasses();
        }
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        AceLog.getAppLog().info("Finished testing for duplicate nids.");
        Ts.get().enableComponentsCRHM();
    }
    
    private void checkForDuplicateUuids() {
        Ts.get().disableComponentsCRHM();
        try{
        AceLog.getAppLog().info("Testing for duplicate uuids.");
        UuidDupFinder dupFinder = new UuidDupFinder();

        Ts.get().iterateConceptDataInParallel(dupFinder);
        System.out.println();

        if (dupFinder.getDupUuids().isEmpty()) {
            AceLog.getAppLog().info("No duplicate uuids found.");
        } else {
            dupFinder.writeDupFile();
            AceLog.getAppLog().severe("\n\nDuplicate uuids found: " + dupFinder.getDupUuids().size() + "\n"
                    + dupFinder.getDupUuids() + "\n");

            UuidDupReporter reporter = new UuidDupReporter(dupFinder.getDupUuids());

            Ts.get().iterateConceptDataInParallel(reporter);
            reporter.reportDupClasses();
        }
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        AceLog.getAppLog().info("Finished testing for duplicate uuids.");
        Ts.get().enableComponentsCRHM();
    }

    //~--- get methods ---------------------------------------------------------
    private void getConceptAttributes() {
        try {
            I_GetConceptData igcd =
                    (I_GetConceptData) this.conceptPanel.getTermComponent();
            I_ConceptAttributeVersioned attr = igcd.getConAttrs();
            List<? extends I_ConceptAttributeTuple> tuples = attr.getTuples();
            List<? extends I_ConceptAttributeTuple> tuples2 =
                    attr.getTuples(conceptPanel.getConfig().getAllowedStatus(),
                    conceptPanel.getConfig().getViewPositionSetReadOnly());
            List<? extends I_ConceptAttributeTuple> tuples3 =
                    igcd.getConceptAttributeTuples(null, conceptPanel.getConfig().getViewPositionSetReadOnly(),
                    conceptPanel.getConfig().getPrecedence(),
                    conceptPanel.getConfig().getConflictResolutionStrategy());
            List<? extends I_ConceptAttributeTuple> tuples4 =
                    igcd.getConceptAttributeTuples(null, conceptPanel.getConfig().getViewPositionSetReadOnly(),
                    conceptPanel.getConfig().getPrecedence(),
                    conceptPanel.getConfig().getConflictResolutionStrategy());

            AceLog.getAppLog().info("attr: " + attr);
            AceLog.getAppLog().info("tuples 1: " + tuples);
            AceLog.getAppLog().info("tuples 2: " + tuples2);
            AceLog.getAppLog().info("tuples 3: " + tuples3);
            AceLog.getAppLog().info("tuples 4: " + tuples4);
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (TerminologyException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }

    //~--- set methods ---------------------------------------------------------
    private void setCachePercent() {
        String percentString = askQuestion("Set bdb cache percent:", "Enter percent[1..99]:",
                "" + Terms.get().getCachePercent());

        if (percentString != null) {
            Terms.get().setCachePercent(percentString);
        }
    }

    private void setCacheSize() {
        String sizeString = askQuestion("Set bdb cache size:", "Enter size[XXXXm|XXg]:",
                "" + Terms.get().getCacheSize());

        if (sizeString != null) {
            Terms.get().setCacheSize(sizeString);
        }
    }

    private void setFromNid() {
        String nidString = askQuestion("Set panel to new concept:", "Enter nid:", "-2142075612");
        int nid = Integer.parseInt(nidString);

        try {
            I_GetConceptData concept = Terms.get().getConceptForNid(nid);

            this.conceptPanel.setTermComponent(concept);
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }

    //~--- inner classes -------------------------------------------------------
    private class RunnableImpl implements Runnable {

        private final DataOutputStream out;

        //~--- constructors -----------------------------------------------------
        public RunnableImpl(DataOutputStream out) {
            this.out = out;
        }

        //~--- methods ----------------------------------------------------------
        @Override
        public void run() {
            doExport(out);
        }
    }
}
