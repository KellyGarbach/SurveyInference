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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * The LinkerFrame is the root UI object to allow users to configure the Linker
 * @author gmorgan, kgarbach
 *
 */
public class LinkerFrame extends JFrame implements ActionListener {

	/**
	 * The generated auto-id used to extend a JFrame
	 */
	private static final long serialVersionUID = -2656988792943548847L;
	/**
	 * The headers of the chosen data-file
	 */
	static ArrayList<String> dataFileHeaders;
	
	/**
	 * A set of definitions, specified by their unique definition ID
	 */
	HashMap<String, NodeDefinition> definitionMap;
	/**
	 * Each definition panel is on its own tab
	 */
	HashMap<String, NodeDefinitionPanel> definitionPanels;

	/**
	 * A sortable arraylist of node types, kept consistent with the definitionMap
	 */
	ArrayList<NodeDefinition> nodeTypes;
	/**
	 * Tabbed Panes, with the root panel of control specified first
	 */
	JTabbedPane tabbedPane;

	JButton addNodeTypeButton, removeNodeTypeButton;
	JButton saveNodeTypeButton, loadNodeTypeButton;
	JButton processLinkagesButton;

	JPanel contentPanel;

	LinkerFrame(String title, ArrayList<NodeDefinition> theCollaborators, ArrayList<String> headers) {
		super(title);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		dataFileHeaders = new ArrayList<String>();
		for(int i = 0; i < headers.size(); ++i) {
			//System.out.println(headers.get(i));
			dataFileHeaders.add(headers.get(i));
		}
		nodeTypes = theCollaborators;
		initializeFromState();

	}

	public void initializeFromState() {
		contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));
			
		JPanel titlePanel = UsefulGUIMethods.getLineAxisJPanel();
		titlePanel.add(Box.createHorizontalGlue());
		titlePanel.add(UsefulGUIMethods.getCenterOrientedJLabel("Linker: Identifying Network Ties"));
		titlePanel.add(Box.createHorizontalGlue());
		contentPanel.add(titlePanel);

		JPanel authorPanel = UsefulGUIMethods.getLineAxisJPanel();
		authorPanel.add(Box.createHorizontalGlue());
		authorPanel.add(UsefulGUIMethods.getCenterOrientedJLabel("Geoffrey P. Morgan and Kelly Garbach"));
		authorPanel.add(Box.createHorizontalGlue());
		contentPanel.add(authorPanel);
		contentPanel.add(Box.createVerticalStrut(3 * UsefulGUIMethods.DEFAULT_SPACING));

		addNodeTypeButton = new JButton("Add NodeDefinition");
		addNodeTypeButton.addActionListener(this);
		removeNodeTypeButton = new JButton("Remove NodeDefinition");
		removeNodeTypeButton.addActionListener(this);
		processLinkagesButton = new JButton("Process Linkages!");
		processLinkagesButton.addActionListener(this);
		saveNodeTypeButton = new JButton("Save Definition Config");
		saveNodeTypeButton.addActionListener(this);
		loadNodeTypeButton = new JButton("Load Definition Config");
		loadNodeTypeButton.addActionListener(this);

		JPanel buttonPanel = UsefulGUIMethods.getLineAxisJPanel();
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(addNodeTypeButton);
		buttonPanel.add(Box.createHorizontalStrut(UsefulGUIMethods.DEFAULT_SPACING));
		buttonPanel.add(removeNodeTypeButton);
		buttonPanel.add(Box.createHorizontalGlue());
		contentPanel.add(buttonPanel);
		
		JPanel buttonPanel2 = UsefulGUIMethods.getLineAxisJPanel();
		buttonPanel2.add(Box.createHorizontalGlue());
		buttonPanel2.add(saveNodeTypeButton);
		buttonPanel2.add(Box.createHorizontalStrut(UsefulGUIMethods.DEFAULT_SPACING));
		buttonPanel2.add(loadNodeTypeButton);
		buttonPanel2.add(Box.createHorizontalGlue());
		contentPanel.add(buttonPanel2);

		JPanel linkagePanel = UsefulGUIMethods.getLineAxisJPanel();
		linkagePanel.add(Box.createHorizontalGlue());
		linkagePanel.add(processLinkagesButton);
		linkagePanel.add(Box.createHorizontalGlue());
		contentPanel.add(linkagePanel);
		contentPanel.add(Box.createVerticalStrut(10));

		tabbedPane = new JTabbedPane();
		tabbedPane.setPreferredSize(new Dimension(900,400));

		this.setContentPane(tabbedPane);
		tabbedPane.addTab("Linker Main", null, contentPanel, "Process Links");
		definitionMap = new HashMap<String, NodeDefinition>();
		definitionPanels = new HashMap<String, NodeDefinitionPanel>();

		//int tabCount = 0;
		for(NodeDefinition c : nodeTypes) {
			definitionMap.put(c.id, c);
			NodeDefinitionPanel p = new NodeDefinitionPanel(c, this);
			definitionPanels.put(c.id, p);
			tabbedPane.addTab(c.id, null, p, "Specify characteristics of " + c.id);
			//tabbedPane.setMnemonicAt(tabCount, (KeyEvent.VK_1 + tabCount));
			//++tabCount;
			//contentPanel.add(p);

		}

		this.pack();
		this.setVisible(true);

	}
	public void actionPerformed(ActionEvent ae) {
		// TODO Auto-generated method stub
		if(ae.getSource().equals(processLinkagesButton)) {
			HashMap<String, IdentifiedNode> nodes = LinkerMain.identifyUniqueNodes(LinkerMain.nodeDefinitions, LinkerMain.pData);
			JOptionPane.showMessageDialog(this, "Number of Nodes Found: " + nodes.size());
			
			NetworkSelectionDialog netD = new NetworkSelectionDialog(null, nodes);
			netD.setVisible(true);
			//System.out.println("Number of Nodes: " + nodes.size());
			try {
				String metanetworkName = (String)JOptionPane.showInputDialog(
	                    this,
	                    "Give a name to the produced collection of nodes and edges:",
	                    "Meta-Network Name",
	                    JOptionPane.PLAIN_MESSAGE,
	                    null,
	                    null,
	                    "LinkerOuput");
				
				
				String[] outputOptions = {"Tab-Delimited Text", "ORA Dynetml"};
				String outputChoice = (String)JOptionPane.showInputDialog(
	                    this,
	                    "Output to Tab-Delimited Text or ORA Dynetml?",
	                    "Output Options",
	                    JOptionPane.PLAIN_MESSAGE,
	                    null,
	                    outputOptions,
	                    "ORA Dynetml");
				
				if(outputChoice != null && outputChoice.equals("ORA Dynetml")) {
					LinkerMain.writeDynetML(nodes, netD.definitions, metanetworkName, new File(LinkerMain.dataFile.getParentFile().getCanonicalPath() + "//" + metanetworkName + ".xml"));
				}
				else if (outputChoice != null) {
					File outputDir = new File(LinkerMain.dataFile.getParentFile().getCanonicalPath() + "//" + metanetworkName);
					outputDir.mkdir();
					LinkerMain.writeTabDelimitedTextFiles(nodes, netD.definitions, outputDir);
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if(ae.getSource().equals(addNodeTypeButton)) {
			String answer = (String)JOptionPane.showInputDialog(this, "Give a unique name for the new node definition:", "Name new node definition", JOptionPane.QUESTION_MESSAGE, null, null, "Actor");
			if(answer != null && answer.length() > 0) {
				NodeDefinition c = new NodeDefinition(answer, answer);
				if(!definitionMap.containsKey(answer)) {
					nodeTypes.add(c);
					definitionMap.put(c.id, c);
					NodeDefinitionPanel p = new NodeDefinitionPanel(c, this);
					definitionPanels.put(c.id, p);
					tabbedPane.addTab(c.id, null, p, "Specify characteristics of " + c.id);

				}
				else {
					JOptionPane.showMessageDialog(this, "For new definitions, make sure to use a unique identifier.");
				}
			}
		}
		else if(ae.getSource().equals(removeNodeTypeButton)) {
			String[] options = new String[definitionMap.keySet().size()];
			int i = 0;
			for(String s : definitionMap.keySet()) {
				options[i++] = s;
			}
			String answer = (String)JOptionPane.showInputDialog(this, "Identify definition to remove:", "Remove Collaborator", JOptionPane.QUESTION_MESSAGE, null, options, "Select choice");
			if(answer != null && answer.length() > 0) {
				NodeDefinition c = definitionMap.get(answer);

				nodeTypes.remove(c);
				
				initializeFromState();

			}
		}
		else if(ae.getSource().equals(saveNodeTypeButton)) {
			JFileChooser chooser = new JFileChooser(LinkerMain.lastFileLocation);
			chooser.setDialogTitle("Save current configuration!");
			chooser.setFileFilter(new TXTFileFilter());
			int returnVal = chooser.showSaveDialog(this);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				LinkerMain.lastFileLocation = chooser.getSelectedFile().getParent();
				NodeDefinition.writeConfiguration(chooser.getSelectedFile(), nodeTypes);
				
			}
		}
		else if(ae.getSource().equals(loadNodeTypeButton)) {
			JFileChooser chooser = new JFileChooser(LinkerMain.lastFileLocation);
			chooser.setDialogTitle("Load configuration");
			chooser.setFileFilter(new TXTFileFilter());
			int returnVal = chooser.showOpenDialog(this);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				LinkerMain.lastFileLocation = chooser.getSelectedFile().getParent();
				LinkerMain.nodeDefinitions = NodeDefinition.readConfigurationFile(chooser.getSelectedFile());
				this.nodeTypes = LinkerMain.nodeDefinitions;
				initializeFromState();
			}
		}
		
	}


}