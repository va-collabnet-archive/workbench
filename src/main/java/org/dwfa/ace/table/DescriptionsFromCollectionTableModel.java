/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.dwfa.ace.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.search.LuceneMatch;
import org.dwfa.vodb.types.ConceptBean;

public class DescriptionsFromCollectionTableModel extends DescriptionTableModel {
    public DescriptionsFromCollectionTableModel(DESC_FIELD[] columns, I_ConfigAceFrame config) {
        super(columns, config);
    }

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    private List<I_DescriptionVersioned> descriptionList = new ArrayList<I_DescriptionVersioned>();
    private List<Float> scoreList = new ArrayList<Float>();

    @Override
    public I_DescriptionTuple getDescription(int rowIndex) {
        if (rowIndex < 0 || rowIndex == descriptionList.size()) {
            return null;
        }
        return descriptionList.get(rowIndex).getLastTuple();
    }

    public int getRowCount() {
        return descriptionList.size();
    }

    public void setDescriptions(Collection<I_DescriptionVersioned> descriptions) {
        descriptionList = new ArrayList<I_DescriptionVersioned>(descriptions);
        scoreList = null;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                fireTableChanged(new TableModelEvent(DescriptionsFromCollectionTableModel.this));
            }
        });
    }

    public void setLuceneMatches(Collection<LuceneMatch> matches) {
        descriptionList = new ArrayList<I_DescriptionVersioned>(matches.size());
        scoreList = new ArrayList<Float>(matches.size());
        synchronized (matches) {
            for (LuceneMatch m : matches) {
                descriptionList.add(m.getDesc());
                scoreList.add(m.getScore());
            }
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                fireTableChanged(new TableModelEvent(DescriptionsFromCollectionTableModel.this));
            }
        });
    }

    public String getScore(int rowIndex) {
        if (scoreList != null) {
            return scoreList.get(rowIndex).toString();
        }
        return "";
    }

    @Override
    public Map<Integer, ConceptBean> getReferencedConcepts() {
        Map<Integer, ConceptBean> referencedConcept = new HashMap<Integer, ConceptBean>();
        List<I_DescriptionVersioned> descriptionListCopy;
        synchronized (descriptionList) {
            descriptionListCopy = new ArrayList<I_DescriptionVersioned>(descriptionList);
        }
        for (I_DescriptionVersioned desc : descriptionListCopy) {
            for (I_DescriptionPart part : desc.getVersions()) {
                referencedConcept.put(part.getTypeId(), ConceptBean.get(part.getTypeId()));
                referencedConcept.put(part.getStatusId(), ConceptBean.get(part.getStatusId()));
            }

        }

        return referencedConcept;
    }
}
