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

public class NetworkDefinition implements ActionListener, KeyListener {
	
	private String name;
	private String sourceType;
	private String sinkType;
	private boolean isTranspose;
	private boolean isOutputted;
	
	JCheckBox outputCheckBox;
	JCheckBox transposeCheckBox;
	JTextField textLabel;
	
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
	
	public String toString() {
		String label = (name + " {" + getSource() + " x " + getSink() + 
				"}. ToBeOutputted? " + isOutputted + " IsTranspose?" + isTranspose);
		return label;
	}
	
	String getName() {
		return name;
	}
	
	String getSource() {
		if(isTranspose) {
			return sinkType;
		}
		return sourceType;
	}
	
	String getSink() {
		if(isTranspose) {
			return sourceType;
		}
		return sinkType;
	}
	
	boolean isOutputted() {
		return isOutputted;
	}
	
	boolean isTranspose() {
		return isTranspose;
	}
	
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

	public void keyTyped(KeyEvent e) {
		name = textLabel.getText();
		
	}

	public void keyPressed(KeyEvent e) {
		name = textLabel.getText();
	}

	public void keyReleased(KeyEvent e) {
		name = textLabel.getText();
	}
	

}
