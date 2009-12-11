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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ImagePart;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.ImageTableModel.StringWithImageTuple;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.ConceptBean;

public class ImagePopupListener extends MouseAdapter {

    enum FieldToChange { TYPE, STATUS};

	private ImageTableModel model;

	private class ChangeActionListener implements ActionListener {

		public ChangeActionListener() {
			super();
		}

		public void actionPerformed(ActionEvent e) {
			ConceptBean sourceBean = ConceptBean.get(selectedObject
					.getTuple().getConceptId());
			for (I_Path p : config.getEditingPathSet()) {
				I_ImagePart newPart = selectedObject.getTuple()
						.duplicatePart();
				newPart.setPathId(p.getConceptId());
				newPart.setVersion(Integer.MAX_VALUE);
				newPart.setStatusId(config.getDefaultStatus().getConceptId());
				selectedObject.getTuple().getVersioned().addVersion(newPart);
			}
			ACE.addUncommitted(sourceBean);
			model.allImageTuples = null;
			model.allImages = null;
			model.fireTableDataChanged();
		}
	}

	private class ChangeFieldActionListener implements ActionListener {
		private Collection<UUID> ids;

		private FieldToChange field;

		public ChangeFieldActionListener(Collection<UUID> ids,
				FieldToChange field) {
			super();
			this.ids = ids;
			this.field = field;
		}

		@SuppressWarnings("unchecked")
		public void actionPerformed(ActionEvent e) {
			try {
				ConceptBean sourceBean = ConceptBean.get(selectedObject
						.getTuple().getConceptId());
				for (I_Path p : config.getEditingPathSet()) {
					I_ImagePart newPart = selectedObject.getTuple()
					.duplicatePart();
					newPart.setPathId(p.getConceptId());
					newPart.setVersion(Integer.MAX_VALUE);
					switch (field) {
					case STATUS:
						newPart.setStatusId((AceConfig.getVodb()
								.uuidToNative(ids)));
						break;
					case TYPE:
						newPart.setTypeId((AceConfig.getVodb()
								.uuidToNative(ids)));
						newPart.setStatusId(config.getDefaultStatus().getConceptId());
						break;

					default:
						throw new Exception("Don't know how to handle: "
								+ field);
					}

					model.referencedConcepts.put(newPart.getStatusId(),
							ConceptBean.get(newPart.getStatusId()));
					model.referencedConcepts.put(newPart.getTypeId(),
							ConceptBean.get(newPart.getTypeId()));
					selectedObject.getTuple().getVersioned().getVersions()
							.add(newPart);
				}
				ACE.addUncommitted(sourceBean);
				model.allImageTuples = null;
				model.allImages = null;
				
				model.fireTableDataChanged();
				model.propertyChange(new PropertyChangeEvent(this, I_ContainTermComponent.TERM_COMPONENT, null, model.host.getTermComponent()));
			} catch (Exception ex) {
				AceLog.getAppLog().alertAndLogException(ex);
			}
		}
	}

	JPopupMenu popup;

	JTable table;

	ActionListener change;

	StringWithImageTuple selectedObject;

	I_ConfigAceFrame config;

	public ImagePopupListener(JTable table, I_ConfigAceFrame config,
			ImageTableModel model) {
		super();
		this.table = table;
		this.config = config;
		this.model = model;
		change = new ChangeActionListener();
	}

	private void makePopup(MouseEvent e) {
		try {
			popup = null;
			int column = table.columnAtPoint(e.getPoint());
			int row = table.rowAtPoint(e.getPoint());
			if ((row != -1) && (column != -1)) {
				popup = new JPopupMenu();
				JMenuItem noActionItem = new JMenuItem("");
				popup.add(noActionItem);
				selectedObject = (StringWithImageTuple) table.getValueAt(row,
						column);
				JMenuItem changeItem = new JMenuItem("Change");
				popup.add(changeItem);
				changeItem.addActionListener(change);
				/*
				JMenuItem retireItem = new JMenuItem("Retire");
				retireItem.addActionListener(new ChangeFieldActionListener(
						ArchitectonicAuxiliary.Concept.RETIRED.getUids(),
						FieldToChange.STATUS));
				popup.add(retireItem);
				*/

				JMenu changeType = new JMenu("Change Type");
				popup.add(changeType);
				addSubmenuItems(changeType, FieldToChange.TYPE, model.host
						.getConfig().getEditImageTypePopup());
				JMenu changeStatus = new JMenu("Change Status");
				popup.add(changeStatus);
				addSubmenuItems(changeStatus, FieldToChange.STATUS, model.host
						.getConfig().getEditStatusTypePopup());
			}
		} catch (TerminologyException e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		} catch (IOException e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		}
	}

	private void addSubmenuItems(JMenu menu, FieldToChange field,
			I_IntList possibleValues) throws TerminologyException, IOException {
		for (int id : possibleValues.getListValues()) {
			I_GetConceptData possibleValue = LocalVersionedTerminology.get()
					.getConcept(id);
			JMenuItem changeStatusItem = new JMenuItem(possibleValue.toString());
			changeStatusItem.addActionListener(new ChangeFieldActionListener(
					possibleValue.getUids(), field));
			menu.add(changeStatusItem);
		}
	}

	public void mousePressed(MouseEvent e) {
		maybeShowPopup(e);
	}

	public void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
	}

	private void maybeShowPopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			if (config.getEditingPathSet().size() > 0) {
				int column = table.columnAtPoint(e.getPoint());
				int row = table.rowAtPoint(e.getPoint());
				selectedObject = (StringWithImageTuple) table.getValueAt(row,
						column);
				if (selectedObject.getTuple().getVersion() == Integer.MAX_VALUE) {
					JOptionPane
							.showMessageDialog(
									table.getTopLevelAncestor(),
									"<html>To change an uncommitted description, <br>use the cancel button, or change the value<br>directly on the uncommitted description...");
				} else {
					makePopup(e);
					if (popup != null) {
						popup.show(e.getComponent(), e.getX(), e.getY());
					}
				}
			} else {
				JOptionPane.showMessageDialog(table.getTopLevelAncestor(),
						"You must select at least one path to edit on...");
			}
		}
	}
}
