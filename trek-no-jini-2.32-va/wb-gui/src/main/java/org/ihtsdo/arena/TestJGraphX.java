package org.ihtsdo.arena;

import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.UIManager;

import com.mxgraph.examples.swing.ClickHandler;
import com.mxgraph.examples.swing.CustomCanvas;
import com.mxgraph.examples.swing.FixedPoints;
import com.mxgraph.examples.swing.GraphEditor;
import com.mxgraph.examples.swing.HelloWorld;
import com.mxgraph.examples.swing.Port;
import com.mxgraph.examples.swing.SchemaEditor;
import com.mxgraph.examples.swing.UserObject;
import com.mxgraph.examples.swing.Validation;
import com.mxgraph.examples.swing.editor.EditorMenuBar;
import com.mxgraph.examples.swing.editor.SchemaEditorMenuBar;
import com.mxgraph.util.mxConstants;

public class TestJGraphX {
    /**
     * 
     * @param args
     */
    public static void main(String[] args)
    {
        graphEditor();
 
        helloWorld();

        clickHandler();

        customCanvas();
    
        fixedPoints();

        port();

        schemaEditor();

        userObject();
    
        validation();

    }

    private static void validation() {
        Validation frame = new Validation();
        frame.setTitle("validation");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 320);
        frame.setVisible(true);
    }

    private static void userObject() {
        UserObject frame = new UserObject();
        frame.setTitle("UserObject");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 320);
        frame.setVisible(true);
    }

    private static void schemaEditor() {
        SchemaEditor editor = new SchemaEditor();
        editor.createFrame(new SchemaEditorMenuBar(editor)).setVisible(true);
    }

    private static void port() {
        Port frame = new Port();
        frame.setTitle("Port");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 320);
        frame.setVisible(true);
    }

    private static void fixedPoints() {
        FixedPoints frame = new FixedPoints();
        frame.setTitle("FixedPoints");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 320);
        frame.setVisible(true);
    }

    private static void customCanvas() {
        CustomCanvas frame = new CustomCanvas();
        frame.setTitle("CustomCanvas");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 320);
        frame.setVisible(true);
    }

    private static void clickHandler() {
        ClickHandler frame = new ClickHandler();
        frame.setTitle("ClickHandler");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 320);
        frame.setVisible(true);
    }

    private static void graphEditor() {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }

        mxConstants.SHADOW_COLOR = Color.LIGHT_GRAY;
        GraphEditor editor = new GraphEditor();
        editor.createFrame(new EditorMenuBar(editor)).setVisible(true);
    }

    private static void helloWorld() {
        HelloWorld frame = new HelloWorld();
        frame.setTitle("HelloWorld");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 320);
        frame.setVisible(true);
    }

}
