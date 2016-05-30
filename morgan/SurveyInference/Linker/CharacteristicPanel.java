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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

/**
 * The Characteristic Panel is used to help add features that are collected for each node.
 * 
 * @author gmorgan, kgarbach
 *
 */
public class CharacteristicPanel extends JPanel implements ActionListener {

	/**
	 * The unique ID used for compilation
	 */
	private static final long serialVersionUID = -8001131948305580583L;
	
	/**
	 * A NodeDefinitionPanel has multiple characteristic panels
	 */
	NodeDefinitionPanel parent;
	/**
	 * Used to change the current feature being used
	 */
	JComboBox<String> combo;
	/**
	 * The currently selected feature is indicated.
	 */
	int currentSelectedIndex;
	/**
	 * Is this feature used for identification, or merely should be included in the node attribute
	 */
	boolean IDCharacteristic;
	/**
	 * The remove button removes this entire panel
	 */
	JButton removeButton;
	
	/**
	 * This is used to instantiate the characteristic panel
	 * @param p, the parent NodeDefinitionPanel
	 * @param selectedIndex, the initial index used to identify the characteristic
	 * @param isID, whether the attribute is used to identify the node
	 */
	CharacteristicPanel(NodeDefinitionPanel p, int selectedIndex, boolean isID) {
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.add(Box.createHorizontalStrut(6 * UsefulGUIMethods.DEFAULT_SPACING));
		parent = p;
		currentSelectedIndex = selectedIndex;
		IDCharacteristic = isID;
		
		combo = new JComboBox<String>(LinkerFrame.dataFileHeaders);
		combo.setSelectedIndex(selectedIndex);
		combo.addActionListener(this);
		this.add(combo);
		this.add(Box.createHorizontalStrut(UsefulGUIMethods.DEFAULT_SPACING));
		removeButton = new JButton("-");
		removeButton.addActionListener(this);
		this.add(removeButton);
		this.add(Box.createHorizontalGlue());
		
	}

	/**
	 * This method handles interface actions that occur on the CharacteristicPanel
	 */
	public void actionPerformed(ActionEvent ae) {
		if(ae.getSource().equals(combo)) {
			// Don't allow the change in selected Index if it's already happened
			// First, get the change:
			String oldValue = combo.getItemAt(currentSelectedIndex);
			String newValue = combo.getItemAt(combo.getSelectedIndex());
			// Now, check if the new value is a legit one
			boolean isLegit = true;
			if(IDCharacteristic) {
				for(String characteristic : parent.theDefinition.identifyingCharacteristics) {
					if(!oldValue.equals(characteristic)) {
						if(newValue.equals(characteristic)) {
							isLegit = false;
						}
					}
				}
				if(isLegit) {
					parent.theDefinition.identifyingCharacteristics.remove(oldValue);
					parent.theDefinition.identifyingCharacteristics.add(newValue);
					currentSelectedIndex = this.combo.getSelectedIndex();
				}
				else {
					this.combo.setSelectedIndex(currentSelectedIndex);
				}
			}
			else {
				for(String characteristic : parent.theDefinition.otherCharacteristics) {
					if(!oldValue.equals(characteristic)) {
						if(newValue.equals(characteristic)) {
							isLegit = false;
						}
					}
				}
				
				if(isLegit) {
					parent.theDefinition.otherCharacteristics.remove(oldValue);
					parent.theDefinition.otherCharacteristics.add(newValue);
					currentSelectedIndex = this.combo.getSelectedIndex();
				}
				else {
					this.combo.setSelectedIndex(currentSelectedIndex);
				}
			}
		}
		else if(ae.getSource().equals(removeButton)) {
			String oldValue = combo.getItemAt(currentSelectedIndex);
			if(IDCharacteristic) {
				parent.theDefinition.identifyingCharacteristics.remove(oldValue);
				parent.remove(this);
			}
			else {
				parent.theDefinition.otherCharacteristics.remove(oldValue);
				parent.remove(this);
			}
			parent.invalidate();
			parent.parent.pack();
			parent.repaint();
		}
	}

}
