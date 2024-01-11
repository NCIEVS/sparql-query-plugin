package org.protege.editor.owl.rdf;

import org.protege.editor.core.ui.preferences.PreferencesLayoutPanel;
import org.protege.editor.core.ui.util.JOptionPaneEx;
import org.protege.editor.owl.ui.preferences.OWLPreferencesPanel;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class SPARQLPreferencesPanel extends OWLPreferencesPanel {

    private static final long serialVersionUID = -818021477356581474L;

    private JTextField txtIndexLocation = new JTextField(40);
    
    private JCheckBox updOnCommit = new JCheckBox("Update Triple Store on Commits");

   

    @Override
    public void initialise() throws Exception {
        setLayout(new BorderLayout());

        PreferencesLayoutPanel panel = new PreferencesLayoutPanel();
        add(panel, BorderLayout.NORTH);

        panel.addGroup("Virtuoso Server");
        panel.addGroupComponent(txtIndexLocation);
        txtIndexLocation.setText(SPARQLPreferences.getServerLocation());
        
        

        panel.addGroup("Protege Server");
        panel.addGroupComponent(updOnCommit);
        updOnCommit.setSelected(SPARQLPreferences.getUpdateOnCommit());
        
        
    }

    @Override
    public void dispose() throws Exception {
        // NO-OP
    }

    @Override
    public void applyChanges() {
        SPARQLPreferences.setServerLocation(txtIndexLocation.getText());
        SPARQLPreferences.setUpdateOnCommit(updOnCommit.isSelected());
    }
}
