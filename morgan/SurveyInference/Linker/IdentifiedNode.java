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

import java.util.ArrayList;
import java.util.HashMap;

/**
 * An found node in the data is represented by this object
 * @author gmorgan, kgarbach
 *
 */
public class IdentifiedNode {

	/**
	 * The unique-id of this node
	 */
	String id;
	/**
	 * The node-type of this node
	 */
	String type;
	/**
	 * A set of characteristics for this node
	 */
	HashMap<String, String> characteristics;
	/**
	 * A set of ties between nodes
	 */
	ArrayList<String> ties;

	/**
	 * The identified node constructor
	 * @param theID, the unique ID
	 * @param theType, the node's type
	 */
	IdentifiedNode(String theID, String theType) {
		id = theID;
		type = theType;
		characteristics = new HashMap<String, String>();
		characteristics.put("ID", id);
		characteristics.put("Type", type);

		ties = new ArrayList<String>();
	}

	/**
	 * Add a characteristic to this instantiated node.
	 * @param key, the characteristic
	 * @param element, the value of this characteristic for this node
	 */
	void addCharacteristic(String key, String element) {
		if(!element.equals("")) {
			key = simplifyKey(key);
			String theElement = "";
			if(characteristics.containsKey(key)) {
				theElement = characteristics.get(key);
				characteristics.remove(key);
			}

			if(theElement.equals("")) {
				theElement = element;
			}
			else {
				if(!theElement.contains(element)) {
					theElement += ";" + element;
				}
			}
			
			characteristics.put(key, theElement);
		}
	}

	/**
	 * This is to streamline the attributes so that Name_Anonymous and opername_anonymous
	 * are identical.
	 * @param originalKey, the key to be simplified
	 * @return the simplified key
	 */
	static String simplifyKey(String originalKey) {
		if(originalKey.contains("Name_Anonymous")) {
			return "Name_Anonymous";
		}
		else if(originalKey.contains("opername_Anonymous")) {
			return "Name_Anonymous";
		}
		else if(originalKey.contains("Role_Cleaned")) {
			return "Role_Cleaned";
		}
		else if(originalKey.contains("RoleCategory")) {
			return "RoleCategory";
		}
		else if(originalKey.contains("Role")) {
			return "Role";
		}

		return originalKey;
	}

}
