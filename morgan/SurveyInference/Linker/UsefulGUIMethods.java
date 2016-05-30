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

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A set of useful methods that make other GUI-centric classes more succinct.
 * @author gmorgan
 *
 */
public abstract class UsefulGUIMethods {

	/**
	 * How much space should we place between elements
	 */
	static int DEFAULT_SPACING = 10;

	/**
	 * Return a JPanel that uses a Box-Layout and a LineAxis panel
	 * @return a JPanel that lays components along a line
	 */
	static JPanel getLineAxisJPanel() {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
		
		return p;
	}
	
	/**
	 * Return a JPanel that uses a Box-Layout and a PageAxis panel
	 * @return a JPanel that lays components along the page
	 */
	static JPanel getPageAxisJPanel() {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
		
		return p;
	}
	
	/**
	 * Return a JLabel where the text will be right-aligned
	 * @param text, the text to display
	 * @return a right-aligned label displaying the text
	 */
	static JLabel getRightOrientedJLabel(String text) {
		JLabel l = new JLabel(text, JLabel.RIGHT);
		return l;
	}
	
	/**
	 * Return a JLabel where the text will be left-aligned
	 * @param text, the text to display
	 * @return a left-aligned label displaying the text
	 */
	static JLabel getLeftOrientedJLabel(String text) {
		JLabel l = new JLabel(text, JLabel.LEFT);
		return l;
	}
	
	/**
	 * Return a JLabel where the text will be center-aligned
	 * @param text, the text to display
	 * @return a center-aligned label displaying the text
	 */
	static JLabel getCenterOrientedJLabel(String text) {
		JLabel l = new JLabel(text, JLabel.CENTER);
		return l;
	}
}
	