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
package org.ihtsdo.project.view.dnd;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.image.FilteredImageSource;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;

import org.dwfa.ace.TermLabelMaker;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.dnd.ConceptTransferable;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.issue.issuerepository.IssueRepository;
import org.ihtsdo.project.model.PartitionMember;

/**
 * The Class ListDragGestureListenerWithImage.
 */
public class ListDragGestureListenerWithImage implements DragGestureListener{
	
	/** The dsl. */
	DragSourceListener dsl;
	
	/** The j list. */
	JList jList;
	
	/** The config. */
	private I_ConfigAceFrame config;

	/**
	 * Instantiates a new list drag gesture listener with image.
	 *
	 * @param dsl the dsl
	 * @param jList the j list
	 * @param config the config
	 */
	public ListDragGestureListenerWithImage(DragSourceListener dsl, JList jList,I_ConfigAceFrame config) {

		super();
		this.jList=jList;
		this.dsl = dsl;
		this.config=config;
	}

	/* (non-Javadoc)
	 * @see java.awt.dnd.DragGestureListener#dragGestureRecognized(java.awt.dnd.DragGestureEvent)
	 */
	public void dragGestureRecognized(DragGestureEvent dge) {

		Object[] values = jList.getSelectedValues();

		if (values.length>0){
			try {
				I_GetConceptData obj=null;
				if (values[0] instanceof IssueRepository){
					obj = Terms.get().getConcept(((IssueRepository)values[0]).getConceptId());
					
				}else if (values[0] instanceof PartitionMember){

					obj = ((PartitionMember) values[0]).getConcept();
				}else if (values[0] instanceof I_GetConceptData){

					obj = (I_GetConceptData) values[0];
				}
				if (obj!=null){
					Image dragImage = getDragImage(obj);
					Point imageOffset = new Point(-10, -(dragImage.getHeight(jList) + 1));
					dge.startDrag(DragSource.DefaultMoveDrop, dragImage, imageOffset, getTransferable(obj), dsl);
				}
			} catch (InvalidDnDOperationException e) {
				AceLog.getAppLog().info(e.toString());
			} catch (Exception ex) {
				AceLog.getAppLog().alertAndLogException(ex);
			}
		}
	}

	/**
	 * Gets the transferable.
	 *
	 * @param obj the obj
	 * @return the transferable
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private Transferable getTransferable(I_GetConceptData obj) throws TerminologyException, IOException {
		return new ConceptTransferable(obj);
	}

	/**
	 * Gets the drag image.
	 *
	 * @param obj the obj
	 * @return the drag image
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public Image getDragImage(I_GetConceptData obj) throws IOException {

		I_DescriptionTuple desc = obj.getDescTuple(config.getTreeDescPreferenceList(), config);
		if (desc == null) {
			desc = obj.getDescs().iterator().next().getFirstTuple();
		}
		JLabel dragLabel = TermLabelMaker.newLabel(desc, false, false).getLabel();
		dragLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		Image dragImage = jList.createImage(dragLabel.getWidth(), dragLabel.getHeight());
		dragLabel.setVisible(true);
		Graphics og = dragImage.getGraphics();
		og.setClip(dragLabel.getBounds());
		dragLabel.paint(og);
		og.dispose();
		FilteredImageSource fis = new FilteredImageSource(dragImage.getSource(),
				TermLabelMaker.getTransparentFilter());
		dragImage = Toolkit.getDefaultToolkit().createImage(fis);
		return dragImage;
	}

}
