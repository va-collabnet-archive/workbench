package org.ihtsdo.arena.editor;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JToolBar;

import org.ihtsdo.arena.ArenaComponentSettings;
import org.ihtsdo.arena.conceptview.ConceptViewSettings;
import org.ihtsdo.arena.taxonomyview.TaxonomyViewSettings;
import org.w3c.dom.Document;

import com.mxgraph.io.mxCodec;
import com.mxgraph.io.mxCodecRegistry;
import com.mxgraph.io.mxObjectCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;
import org.dwfa.ace.api.I_ConfigAceFrame;

public class ArenaEditor extends BasicGraphEditor {

    public enum CELL_ATTRIBUTES {

        LINKED_TAB, COMPONENT_KIND
    };

    public enum COMPONENT_KIND {

        CONCEPT, TAXONOMY
    };
    /**
     * 
     */
    private static final long serialVersionUID = -7007225006753337933L;
    private List<? extends ArenaComponentSettings> arenaList;

    /**
     * @throws IOException 
     * 
     */
    public ArenaEditor(I_ConfigAceFrame config) throws IOException {
        super("mxGraph for JFC/Swing", new ArenaGraphComponent(new mxGraph() {

            /**
             * Allows expanding tables
             */
            @Override
            public boolean isCellFoldable(Object cell, boolean collapse) {
                return model.isVertex(cell);
            }
        }, config) {

            /**
             * 
             */
            private static final long serialVersionUID = -1194463455177427496L;

            /**
             * Disables folding icons.
             */
            @Override
            public ImageIcon getFoldingIcon(mxCellState state) {
                return null;
            }
        });

        arenaList = (List<? extends ArenaComponentSettings>) config.getProperty(this.getClass().getCanonicalName());
        if (arenaList == null) {
            // Creates a single shapes palette
            graphOutline.setVisible(true);
            addPaletteTemplate(editorPalette, "tab 1", "view", 1);
            addPaletteTemplate(editorPalette, "tab 2", "view", 2);
            addPaletteTemplate(editorPalette, "tab 3", "view", 3);
            addPaletteTemplate(editorPalette, "tab 4", "view", 4);
            addPaletteTemplate(editorPalette, "taxonomy", "text_tree", 1, new TaxonomyViewSettings());

            getGraphComponent().getGraph().setCellsResizable(false);
            getGraphComponent().setConnectable(false);
            getGraphComponent().getGraphHandler().setCloneEnabled(false);
            getGraphComponent().getGraphHandler().setImagePreview(false);

        } else {
            mxGraph graph = getGraphComponent().getGraph();
            Object parent = graph.getDefaultParent();
            for (ArenaComponentSettings settings : arenaList) {
                mxRectangle bounds = settings.getBounds();
                mxCell v1 = (mxCell) graph.insertVertex(parent, null, settings.getTitle(),
                        bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
                v1.getGeometry().setAlternateBounds(settings.getAlternateBounds());
                v1.setValue(settings);
            }
        }
        mxCodecRegistry.addPackage("org.ihtsdo.arena.conceptview");
        mxCodecRegistry.register(new mxObjectCodec(new ConceptViewSettings()));
        File defaultArenaConfig = new File("arena/default.mxe");
        if (defaultArenaConfig.exists()) {
            Document document = mxUtils.parse(mxUtils.readFile(defaultArenaConfig.getAbsolutePath()));
            mxCodec codec = new mxCodec(document);
            codec.decode(document.getDocumentElement(), getGraphComponent().getGraph().getModel());
        }

        setModified(false);
        setCurrentFile(null);


    }

    private void addPaletteTemplate(EditorPalette palette, String label, String imageName, int link) {
        addPaletteTemplate(palette, label, imageName, link, new ConceptViewSettings(link));
    }

    private void addPaletteTemplate(EditorPalette palette, String label, String imageName, int link, ArenaComponentSettings settings) {

        mxGeometry geometry = new mxGeometry(20, 20, 300, 200);
        geometry.setAlternateBounds(new mxRectangle(0, 0, 300, 20));
        settings.setBounds(geometry);
        settings.setAlternateBounds(geometry.getAlternateBounds());
        mxCell tableTemplate = new mxCell(label, geometry, null);
        tableTemplate.setValue(settings);
        tableTemplate.setVertex(true);
        ImageIcon icon = new ImageIcon(ArenaEditor.class.getResource("/24x24/plain/" + imageName + ".png"));
        palette.addTemplate(label, icon, tableTemplate);
    }

    /**
     * 
     */
    @Override
    protected void installToolBar() {
        add(new ArenaEditorToolBar(this, JToolBar.HORIZONTAL),
                BorderLayout.NORTH);
    }
}
