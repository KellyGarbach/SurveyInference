/*
 * Linker is a tool for generating network files from a structured data-sheet.
 * It generates network data in a format directly compatible with ORA (Netanomics)
 * It is different from the other tools (including ORA) because it allows you to use 
 * multiple attributes of a data-sheet to differentiate nodes.
 * Copyright 2016, Geoffrey P Morgan and Kelly Garbach
 * 
 * This tool is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License version 2 as published by the Free Software Foundation.
 * 
 * This tool is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this tool; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA 02110-1301, USA.
 * 
 * You can also read the license directly at: http://www.gnu.org/licenses/gpl-2.0.html
 *
 * You can contact the authors by e-mailing kgarbach@luc.edu
*/

package morgan.SurveyInference.Linker;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * The Node Definition Panel holds the data used to define an entity to look for in the data
 * @author gmorgan, kgarbach
 *
 */
public class NodeDefinitionPanel extends JPanel implements ActionListener, KeyListener {

	/**
	 * An auto-generated id because we are extending JPanel
	 */
	private static final long serialVersionUID = 6161656802155260046L;
	
	/**
	 * The NodeDefinition which we are updating via this panel
	 */
	NodeDefinition theDefinition;
	
	/**
	 * The parent frame
	 */
	LinkerFrame parent;
	
	/**
	 * Panel which contains the ID and Type
	 */
	JPanel idPanel;
	/**
	 * Two editable text fields
	 */
	JTextField idField, typeField;
	/**
	 * A set of combo-boxes that identify id characteristics
	 */
	ArrayList<JComboBox<String>> idCharacteristics;
	/**
	 * A set of combo-boxes that identify additional data characteristics
	 */
	ArrayList<JComboBox<String>> dataCharacteristics;
	
	/**
	 * Whether the node entity has a delimiter value
	 */
	JCheckBox delimiterBox;
	/**
	 * If the node entity has a delimiter, what is the delimiter?
	 */
	JTextField delimiterField;
	/**
	 * A set of buttons used to add ID and Data characteristics
	 */
	JButton addIDChar, addDataChar;
	
	/**
	 * Constructor used to generate a NodeDefinitionPanel
	 * @param c, the NodeDefinition being represented
	 * @param p, the parent panel
	 */
	NodeDefinitionPanel(NodeDefinition c, LinkerFrame p) {
		super();
		//this.setPreferredSize(new Dimension(400,400));
		idCharacteristics = new ArrayList<JComboBox<String>>();
		dataCharacteristics = new ArrayList<JComboBox<String>>();
		
		parent = p;
		theDefinition = c;
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		initializeFromState();
		
	}
	
	/**
	 * Helper function that sets up the contents of the NodeDefinitionPanel so we can
	 * call this stuff independent of the Constructor
	 */
	void initializeFromState() {
		
		this.removeAll();
		
		this.add(Box.createVerticalStrut(UsefulGUIMethods.DEFAULT_SPACING));
		idPanel = UsefulGUIMethods.getLineAxisJPanel();
		idPanel.add(Box.createHorizontalStrut(UsefulGUIMethods.DEFAULT_SPACING));
		idPanel.add(UsefulGUIMethods.getRightOrientedJLabel("ID:"));
		idPanel.add(Box.createHorizontalStrut(UsefulGUIMethods.DEFAULT_SPACING));
		idField = new JTextField(20);
		idField.setText(theDefinition.id);
		idField.addActionListener(this);
		idPanel.add(idField);
		idPanel.add(Box.createHorizontalStrut(UsefulGUIMethods.DEFAULT_SPACING));
		idPanel.add(UsefulGUIMethods.getRightOrientedJLabel("Type:"));
		idPanel.add(Box.createHorizontalStrut(UsefulGUIMethods.DEFAULT_SPACING));
		typeField = new JTextField(20);
		typeField.setText(theDefinition.type);
		typeField.addActionListener(this);
		idPanel.add(typeField);
		idPanel.add(Box.createHorizontalStrut(UsefulGUIMethods.DEFAULT_SPACING));
		idPanel.add(Box.createHorizontalGlue());
		this.add(idPanel);
		this.add(Box.createVerticalStrut(UsefulGUIMethods.DEFAULT_SPACING));
		
		JPanel delimiterPanel = UsefulGUIMethods.getLineAxisJPanel();
		delimiterPanel.add(Box.createHorizontalStrut(UsefulGUIMethods.DEFAULT_SPACING));
		delimiterBox = new JCheckBox("Uses Delimiter");
		delimiterBox.addActionListener(this);
		delimiterField = new JTextField(10);
		delimiterField.addActionListener(this);
		delimiterField.addKeyListener(this);
		if(theDefinition.delimiter.equals("")) {
			delimiterBox.setSelected(false);
			delimiterField.setEnabled(false);
		}
		else {
			delimiterBox.setSelected(true);
			delimiterField.setEnabled(true);
			delimiterField.setText(theDefinition.delimiter);
		}
		delimiterPanel.add(delimiterBox);
		delimiterPanel.add(Box.createHorizontalStrut(UsefulGUIMethods.DEFAULT_SPACING));
		delimiterPanel.add(delimiterField);
		delimiterPanel.add(Box.createHorizontalGlue());
		delimiterPanel.add(Box.createHorizontalStrut(UsefulGUIMethods.DEFAULT_SPACING));
		this.add(delimiterPanel);
		
		this.add(Box.createVerticalGlue());
		
		JPanel idCharacteristics = UsefulGUIMethods.getLineAxisJPanel();
		idCharacteristics.add(Box.createHorizontalStrut(3 * UsefulGUIMethods.DEFAULT_SPACING));
		idCharacteristics.add(UsefulGUIMethods.getLeftOrientedJLabel("Identifying Characteristics:"));
		idCharacteristics.add(Box.createHorizontalGlue());
		this.add(idCharacteristics);
		
		for(String idChar : theDefinition.identifyingCharacteristics) {
			JComboBox<String> combo = new JComboBox<String>(LinkerFrame.dataFileHeaders);
			int selectedIndex = -1;
			for(int i = 0; i < combo.getItemCount(); ++i) {
				String s = combo.getItemAt(i).trim();
				if(s.equals(idChar)) {
					selectedIndex = i;
					combo.setSelectedIndex(i);
					break;
				}
			}
			
			if(selectedIndex != -1) {
				JPanel theCharPanel = new CharacteristicPanel(this, selectedIndex, true);
				this.add(theCharPanel);
			}
						
		}
		JPanel addIDCharPanel = UsefulGUIMethods.getLineAxisJPanel();
		addIDCharPanel.add(Box.createHorizontalStrut(6 * UsefulGUIMethods.DEFAULT_SPACING));
		addIDChar = new JButton("Add ID Characteristic");
		addIDChar.addActionListener(this);
		addIDCharPanel.add(addIDChar);
		addIDCharPanel.add(Box.createHorizontalGlue());
		this.add(addIDCharPanel);
		
		this.add(Box.createVerticalStrut(UsefulGUIMethods.DEFAULT_SPACING));
		
		JPanel dataCharacteristics = UsefulGUIMethods.getLineAxisJPanel();
		dataCharacteristics.add(Box.createHorizontalStrut(3 * UsefulGUIMethods.DEFAULT_SPACING));
		dataCharacteristics.add(UsefulGUIMethods.getLeftOrientedJLabel("Data Characteristics:"));
		dataCharacteristics.add(Box.createHorizontalGlue());
		this.add(dataCharacteristics);
		
		for(String dataChar : theDefinition.otherCharacteristics) {
			JComboBox<String> combo = new JComboBox<String>(LinkerFrame.dataFileHeaders);
			int selectedIndex = -1;
			for(int i = 0; i < combo.getItemCount(); ++i) {
				String s = combo.getItemAt(i).trim();
				if(s.equals(dataChar)) {
					selectedIndex = i;
					combo.setSelectedIndex(i);
					break;
				}
			}
			
			if(selectedIndex != -1) {
				JPanel theCharPanel = new CharacteristicPanel(this, selectedIndex, false);
				this.add(theCharPanel);
			}
						
		}
		JPanel addDataCharPanel = UsefulGUIMethods.getLineAxisJPanel();
		addDataCharPanel.add(Box.createHorizontalStrut(6 * UsefulGUIMethods.DEFAULT_SPACING));
		addDataChar = new JButton("Add Data Characteristic");
		addDataChar.addActionListener(this);
		addDataCharPanel.add(addDataChar);
		addDataCharPanel.add(Box.createHorizontalGlue());
		this.add(addDataCharPanel);
		
		this.add(Box.createVerticalStrut(UsefulGUIMethods.DEFAULT_SPACING));
		this.add(Box.createVerticalGlue());
		
		parent.pack();
	}


	/**
	 * Action-Handling for GUI objects on the NodeDefinitionPanel
	 */
	public void actionPerformed(ActionEvent ae) {
		if(ae.getSource().equals(idField)) {
			theDefinition.id = idField.getText();
		}
		else if(ae.getSource().equals(typeField)) {
			theDefinition.type = typeField.getText();
		}
		else if(ae.getSource().equals(addIDChar)) {
			String[] options = new String[LinkerFrame.dataFileHeaders.length - theDefinition.identifyingCharacteristics.size()];
			int i = 0;
			for(String h : LinkerFrame.dataFileHeaders) {
				if(!theDefinition.identifyingCharacteristics.contains(h)) {
					options[i++] = h;
				}
			}
			
			String answer = (String)JOptionPane.showInputDialog(this.parent, "Select new ID Characteristic:", "Add ID Characteristic", JOptionPane.QUESTION_MESSAGE, null, options, "Select choice");
			//System.out.println(answer);
			if(answer != null && (answer.length() > 0)) {
				theDefinition.identifyingCharacteristics.add(answer);
				initializeFromState();
			}
			
			
		}
		else if(ae.getSource().equals(addDataChar)) {
			String[] options = new String[LinkerFrame.dataFileHeaders.length - theDefinition.otherCharacteristics.size()];
			int i = 0;
			for(String h : LinkerFrame.dataFileHeaders) {
				if(!theDefinition.otherCharacteristics.contains(h)) {
					options[i++] = h;
				}
			}
			
			String answer = (String)JOptionPane.showInputDialog(this.parent, "Select new ID Characteristic:", "Add ID Characteristic", JOptionPane.QUESTION_MESSAGE, null, options, "Select choice");
			//System.out.println(answer);
			if(answer != null && (answer.length() > 0)) {
				theDefinition.otherCharacteristics.add(answer);
				initializeFromState();
			}
		}
		else if(ae.getSource().equals(delimiterBox)) {
			if(delimiterBox.isSelected()) {
				delimiterField.setEnabled(true);
				delimiterField.setText(theDefinition.delimiter);
			}
			else {
				delimiterField.setEnabled(false);
				delimiterField.setText("");
				updateDelimiter(delimiterField.getText());
			}
		}
		else if(ae.getSource().equals(delimiterField)) {
			updateDelimiter(delimiterField.getText());
		}
	}
	
	/**
	 * Helper function to make sure we handle both data elements of delimiter
	 * on a NodeDefinition
	 * @param theDelimiter, the new value of the Delimiter
	 */
	private void updateDelimiter(String theDelimiter) {
		theDefinition.delimiter = theDelimiter;
		if(theDelimiter.equals("")) {
			theDefinition.hasDelimiter = false;
		}
		else {
			theDefinition.hasDelimiter = true;
		}
	}

	/**
	 * Key-Handling function for the delimiter
	 */
	public void keyPressed(KeyEvent arg0) {
		updateDelimiter(delimiterField.getText());
	}

	/**
	 * Key-Handling function for the delimiter
	 */
	public void keyReleased(KeyEvent arg0) {
		updateDelimiter(delimiterField.getText());
	}

	/**
	 * Key-Handling function for the delimiter
	 */
	public void keyTyped(KeyEvent arg0) {
		updateDelimiter(delimiterField.getText());
	}

}
