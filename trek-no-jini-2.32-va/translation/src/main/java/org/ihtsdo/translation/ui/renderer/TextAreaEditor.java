package org.ihtsdo.translation.ui.renderer;

import javax.swing.DefaultCellEditor;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class TextAreaEditor extends DefaultCellEditor {
	private static final long serialVersionUID = -7843406197841039745L;

	public TextAreaEditor() {
		super(new JTextField());
		final JTextArea textArea = new JTextArea();
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setBorder(null);
		editorComponent = scrollPane;

		delegate = new DefaultCellEditor.EditorDelegate() {
			private static final long serialVersionUID = 7358294190383089216L;

			public void setValue(Object value) {
				textArea.setText((value != null) ? value.toString() : "");
			}

			public Object getCellEditorValue() {
				return textArea.getText();
			}
		};
	}
}
