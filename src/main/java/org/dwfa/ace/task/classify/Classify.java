package org.dwfa.ace.task.classify;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.profile.NewDefaultProfile;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.NoMappingException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.util.id.Type3UuidFactory;

@BeanList(specs = { @Spec(directory = "tasks/ace/classify", type = BeanType.TASK_BEAN) })
public class Classify extends AbstractTask {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String factoryClass = "java.lang.String";

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(factoryClass);
    }

    private void readObject(ObjectInputStream in) throws IOException,
    ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            factoryClass = (String) in.readObject();
        } else {
            throw new IOException(
                    "Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker)
    throws TaskFailedException {
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
    throws TaskFailedException {
        worker.getLogger().info("classify stub");
        
        final I_TermFactory termFactory = LocalVersionedTerminology.get();
        try {
            // GET ROOT
            I_GetConceptData snomedRoot = termFactory.getConcept(new UUID[] {Type3UuidFactory.SNOMED_ROOT_UUID});
            worker.getLogger().info("SNOMED ROOT: " + snomedRoot);
            
            // GET CHILDREN
            I_IntSet relTypes = termFactory.newIntSet();
            relTypes.add(termFactory.uuidToNative(Type3UuidFactory.SNOMED_ISA_REL_UUID));
            
            I_IntSet statusType = termFactory.newIntSet();
            statusType.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()).getConceptId());
            statusType.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()).getConceptId());
            statusType.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.PENDING_MOVE.getUids()).getConceptId());
            
            I_Path statedPath = termFactory.getPath(ArchitectonicAuxiliary.Concept.SNOMED_CORE.getUids());
            Set<I_Position> latestStated = new HashSet<I_Position>();
            latestStated.add(termFactory.newPosition(statedPath, Integer.MAX_VALUE));
            
            boolean addUncommitted = false;
            
            // get rels that point at snomedRoot
            List<I_RelTuple> tuples = snomedRoot.getDestRelTuples(statusType, relTypes, latestStated , addUncommitted);
            
            worker.getLogger().info("Tuples: " + tuples);
            
            // CREATE CONCEPT
            
            I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
            
            final UUID pathUUID = UUID.fromString("0f71239a-a796-11dc-8314-0800200c9a66");
            I_Path inferredPath;
            try {
                inferredPath = termFactory.getPath(new UUID[] {pathUUID});
            } catch (NoMappingException e) {
                // path doesn't exist - create it
                I_GetConceptData pathConcept = termFactory.newConcept(pathUUID, false,config);

                termFactory.newDescription(UUID.randomUUID(), pathConcept, "en", "SNOMED Inferred Path",
                        ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize(),
                        config);

                termFactory.newDescription(UUID.randomUUID(), pathConcept, "en", "SNOMED Inferred Path",
                        ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize(), config);

                termFactory.newRelationship(UUID.randomUUID(),
                        pathConcept,
                        termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()),
                        termFactory.getConcept(ArchitectonicAuxiliary.Concept.RELEASE.getUids()),
                        termFactory.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()),
                        termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
                        termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()),
                        0, config);

                termFactory.commit();
                
                Set<I_Position> origins = new HashSet<I_Position>(statedPath.getOrigins());

                inferredPath = termFactory.newPath(origins, pathConcept);
                
                termFactory.commit();
            }

            
            I_ConfigAceFrame newConfig = NewDefaultProfile.newProfile("", "", "", "");
            newConfig.getEditingPathSet().clear();
            newConfig.addEditingPath(inferredPath);
            
            I_GetConceptData newConcept = termFactory.newConcept(UUID.randomUUID(), false, newConfig);

            termFactory.newDescription(UUID.randomUUID(), newConcept, "en", "New Fully Specified Description",
                    ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize(),
                    newConfig);

            termFactory.newDescription(UUID.randomUUID(), newConcept, "en", "New Preferred Description",
                    ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize(), newConfig);

            UUID newRelUid = UUID.randomUUID();
            I_GetConceptData relType = termFactory.getConcept(new UUID[] {Type3UuidFactory.SNOMED_ISA_REL_UUID});
            I_GetConceptData relDestination = snomedRoot;
            I_GetConceptData relCharacteristic = termFactory.getConcept(ArchitectonicAuxiliary.Concept.INFERRED_RELATIONSHIP.getUids());
            I_GetConceptData relRefinability = termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids());
            I_GetConceptData relStatus = termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
            int relGroup = 0;
            termFactory.newRelationship(newRelUid, newConcept, relType, relDestination, relCharacteristic, relRefinability, relStatus, relGroup, newConfig);

            termFactory.commit();
        } catch (TerminologyException e) {
            handleException(e);
        } catch (IOException e) {
            handleException(e);
        } catch (Exception e) {
            handleException(e);
        }

        return Condition.CONTINUE;
    }

    private void handleException(Exception e) throws TaskFailedException {
        throw new TaskFailedException(e);
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public String getFactoryClass() {
        return factoryClass;
    }

    public void setFactoryClass(String factoryClass) {
        this.factoryClass = factoryClass;
    }

}
