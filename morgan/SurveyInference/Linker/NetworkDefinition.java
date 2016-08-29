package morgan.SurveyInference.Linker;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.font.TextAttribute;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * The Network Definition creates both a GUI element and data required for creating
 * an creating a well-specified XML and CSV outputs
 * 
 * @author gmorgan
 *
 */
public class NetworkDefinition implements ActionListener, KeyListener {
	
	/**
	 * The unique identifier of the network should have a human-readable name
	 */
	private String name;
	/**
	 * The sourceType of the network is based on the identified nodes
	 */
	private String sourceType;
	/**
	 * The sinkType of the network is also based on the identified nodes
	 */
	private String sinkType;
	/**
	 * By selecting transpose, the person is flipping the default source/sink
	 * relationship between these nodes
	 */
	private boolean isTranspose;
	/**
	 * This controls whether the given network definition will be outputted to file
	 */
	private boolean isOutputted;
	
	/**
	 * GUI element responsible for controlling isOutputted
	 */
	JCheckBox outputCheckBox;
	/**
	 * GUI element responsible for controlling transpose
	 */
	JCheckBox transposeCheckBox;
	/**
	 * GUI element for identifying the network
	 */
	JTextField textLabel;
	
	/**
	 * The NetworkDefinition Constructor
	 * 
	 * @param theName, the network's human-readable name
	 * @param theSourceType, the Network's default source-type
	 * @param theSinkType, the Network's default sink-type
	 */
	NetworkDefinition(String theName, String theSourceType, String theSinkType) {
		name = theName;
		sourceType = theSourceType;
		sinkType = theSinkType;
		isOutputted = true;
		isTranspose = false;
		
		outputCheckBox = new JCheckBox("");
		outputCheckBox.setSelected(isOutputted);
		outputCheckBox.addActionListener(this);
		
		transposeCheckBox = new JCheckBox("");
		transposeCheckBox.setSelected(isTranspose);
		transposeCheckBox.addActionListener(this);
		
		textLabel = new JTextField(50);
		textLabel.setText(name);
		textLabel.addKeyListener(this);
	}
	
	/**
	 * Outputs useful debug information, information required for XML output, to String
	 */
	public String toString() {
		String label = (name + " {" + getSource() + " x " + getSink() + 
				"}. ToBeOutputted? " + isOutputted + " IsTranspose?" + isTranspose);
		return label;
	}
	
	/**
	 * Getter for the NetworkDefinition name
	 * @return the network's human-readable name
	 */
	String getName() {
		return name;
	}
	
	/**
	 * Returns the current source-type, this is dependent on whether the network
	 * has been transposed
	 * @return the current source node-type
	 */
	String getSource() {
		if(isTranspose) {
			return sinkType;
		}
		return sourceType;
	}
	
	/**
	 * Returns the current sink-type, this is dependent on whether the network has
	 * been transposed
	 * @return the current sink node-type
	 */
	String getSink() {
		if(isTranspose) {
			return sourceType;
		}
		return sinkType;
	}
	
	/**
	 * Getter for isOutputted, identifies whether this should be outputted at all
	 * @return true if network should be written to file, otherwise false
	 */
	boolean isOutputted() {
		return isOutputted;
	}
	
	/**
	 * Getter for isTranspose, identifies whether the network has been transposed
	 * @return true if the network has been transposed, otherwise false
	 */
	boolean isTranspose() {
		return isTranspose;
	}
	
	/**
	 * Creates a useful default top-level panel for the NetworkSelctionDialog
	 * based on the format of the NetworkDefinition Panel
	 * 
	 * Note this is static because the header panel should not need to be owned by
	 * any NetworkDefinition object
	 * @return a JPanel suitable for sitting above a set of NetworkDefinition panels
	 */
	static JPanel getHeaderPanel() {
		JPanel p = UsefulGUIMethods.getLineAxisJPanel();
		p.setPreferredSize(new Dimension(600, 50));
		JLabel meaning = UsefulGUIMethods.getLeftOrientedJLabel("Meaning");
		
		Font basicFont = meaning.getFont();
		Font f = new Font(basicFont.getName(), Font.BOLD, basicFont.getSize());
		Map attributes = f.getAttributes();
		attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		meaning.setFont(f.deriveFont(attributes));
		
		p.add(Box.createHorizontalStrut(10));
		p.add(meaning);
		p.add(Box.createHorizontalStrut(250));
		p.add(Box.createGlue());
		JLabel output = UsefulGUIMethods.getLeftOrientedJLabel("Ouput?");
		output.setFont(f.deriveFont(attributes));
		p.add(output);
		p.add(Box.createHorizontalStrut(10));
		JLabel transpose = UsefulGUIMethods.getLeftOrientedJLabel("Transpose?");
		transpose.setFont(f.deriveFont(attributes));
		p.add(transpose);
		p.add(Box.createHorizontalStrut(10));
		
		return p;
	}
	
	/**
	 * Creates a well-formatted JPanel that can be used to actively edit
	 * the Network Definition
	 * @return a JPanel
	 */
	JPanel formatNetworkDefinitionPanel() {
		JPanel p = UsefulGUIMethods.getLineAxisJPanel();
		p.setPreferredSize(new Dimension(600, 50));
		//p.add(outputCheckBox);
		//p.add(UsefulGUIMethods.getCenterOrientedJLabel(sourceType));
		//p.add(Box.createHorizontalStrut(10));
		//p.add(UsefulGUIMethods.getCenterOrientedJLabel(sinkType));
		p.add(Box.createHorizontalStrut(10));
		//p.add(Box.createGlue());
		//p.add(UsefulGUIMethods.getRightOrientedJLabel("Network Meaning:"));
		p.add(textLabel);
		p.add(Box.createHorizontalStrut(50));
		p.add(outputCheckBox);
		p.add(Box.createHorizontalStrut(40));
		p.add(transposeCheckBox);
		p.add(Box.createHorizontalStrut(10));
		return p;
	}

	/**
	 * Action Handlers for the NetworkDefinition JPanel
	 */
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(outputCheckBox)) {
			isOutputted = !isOutputted;
			outputCheckBox.setSelected(isOutputted);
			textLabel.setEnabled(isOutputted);
		}
		if(e.getSource().equals(transposeCheckBox)) {
			isTranspose = !isTranspose;
			transposeCheckBox.setSelected(isTranspose);
			if(isTranspose) {
				textLabel.setText(sinkType + " x " + sourceType);
			}
			else {
				textLabel.setText(sourceType + " x " + sinkType);
			}
			name = textLabel.getText();
		}
	}

	/**
	 * Text handler for the NetworkDefinition JPanel
	 */
	public void keyTyped(KeyEvent e) {
		name = textLabel.getText();
		
	}

	/**
	 * Text handler for the NetworkDefinition JPanel
	 */
	public void keyPressed(KeyEvent e) {
		name = textLabel.getText();
	}

	/**
	 * Text handler for the NetworkDefinition JPanel
	 */
	public void keyReleased(KeyEvent e) {
		name = textLabel.getText();
	}
	

}
