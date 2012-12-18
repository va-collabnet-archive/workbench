/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.drools.facts.ComponentFact;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.spec.ConceptSpec;

/**
 * Adds component to string refex.
 *
 */
public class AddToNidFloatRefexAction extends AbstractAction {

    private I_ConfigAceFrame config;
    private ComponentVersionBI component;
    private ConceptSpec nidIntRefex;

    public AddToNidFloatRefexAction(String actionName, ComponentFact fact,
            ConceptSpec integerRefex, I_ConfigAceFrame config) {
        super(actionName);
        this.component = (ComponentVersionBI) fact.getComponent();
        this.nidIntRefex = integerRefex;
        this.config = config;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(config.getEditCoordinate(), config.getViewCoordinate());
            ConceptSpec ml = new ConceptSpec("milligram", UUID.fromString("89cb8d09-3a3c-31e6-94ea-05fe8ff17551"));
            if (RelationshipVersionBI.class.isAssignableFrom(component.getClass())) {

                RelationshipVersionBI rel = (RelationshipVersionBI) component;
                RefexCAB refexBp = new RefexCAB(TK_REFEX_TYPE.CID_FLOAT,
                        rel.getNid(),
                        nidIntRefex.getLenient().getConceptNid());
                refexBp.put(RefexCAB.RefexProperty.FLOAT1, -1);
                refexBp.put(RefexCAB.RefexProperty.CNID1, 
                        ml.getLenient().getConceptNid());
                builder.construct(refexBp);
                if(!Ts.get().getConceptForNid(nidIntRefex.getLenient().getNid()).isAnnotationStyleRefex()){
                    Ts.get().addUncommitted(Ts.get().getConceptForNid(nidIntRefex.getLenient().getNid()));
                }
                Ts.get().addUncommitted(Ts.get().getConcept(rel.getSourceNid()));

            } else {
                throw new UnsupportedOperationException("Only relationships are supported at this time.");
            }
        } catch (IOException ex) {
            Logger.getLogger(AddToNidFloatRefexAction.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidCAB ex) {
            Logger.getLogger(AddToNidFloatRefexAction.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ContradictionException ex) {
            Logger.getLogger(AddToNidFloatRefexAction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
