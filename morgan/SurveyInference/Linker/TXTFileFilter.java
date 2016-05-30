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

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * The TXTFileFilter ensures that we only try to load files with a txt extension
 * @author gmorgan, kgarbach
 *
 */
public class TXTFileFilter extends FileFilter {

	/**
	 * The accept function determine what is an acceptable file.
	 */
	@Override
	public boolean accept(File f) {
		
		String name = f.getName();
		//System.out.println(name);
		String[] nameParts = name.split("\\.");
		if(nameParts.length > 1) {
			String ext = nameParts[nameParts.length - 1];
			//System.out.println("\t" + ext);
			if(ext.equalsIgnoreCase("txt")) {
				return true;
			}
		}
		else {
			return true;
		}
		
		return false;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Tab-Separated Values File, with Headers";
	}

}
