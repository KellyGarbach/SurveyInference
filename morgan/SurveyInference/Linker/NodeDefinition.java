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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

/**
 * The NodeDefinition identifies a set of characteristics used to identify a unique node
 * 
 * @author gmorgan, kgarbach
 *
 */
public class NodeDefinition {
	
	/**
	 * A list of unique node-definitions by their types
	 */
	static ArrayList<String> uniqueTypes = new ArrayList<String>();
	/**
	 * Characteristics used to distinguish different nodes
	 */
	ArrayList<String> identifyingCharacteristics;
	/**
	 * Characteristics that are added to node but are not used to identify nodes
	 */
	ArrayList<String> otherCharacteristics;
	/**
	 * The id of this definition
	 */
	String id;
	/**
	 * The type of node produced by this definition
	 */
	String type;
	/**
	 * The Delimiter may not be enabled
	 */
	boolean hasDelimiter = false;
	/**
	 * The current delimiter
	 */
	String delimiter = "";
	/**
	 * Tab-Delimited file write-out is the default
	 */
	static String FILE_WRITE_DELIMITER = "\t";
	
	/**
	 * The default NodeDefinition constructor
	 * @param theID, the unique ID
	 * @param theType, the type of this node
	 */
	NodeDefinition(String theID, String theType) {
		id = theID;
		type = theType;
		identifyingCharacteristics = new ArrayList<String>();
		otherCharacteristics = new ArrayList<String>();
		
		if(!uniqueTypes.contains(type)) {
			uniqueTypes.add(type);
		}
	}
	
	/**
	 * The NodeDefinition constructor which has a delimiter
	 * @param theID, the unique ID
	 * @param theType, the type of this node
	 * @param theDelimiter, the delimiter used when using questions
	 */
	NodeDefinition(String theID, String theType, String theDelimiter) {
		id = theID;
		type = theType;
		identifyingCharacteristics = new ArrayList<String>();
		otherCharacteristics = new ArrayList<String>();
		if(!theDelimiter.trim().equals("")) {
			hasDelimiter = true;
			delimiter = theDelimiter.trim(); 
		}
		
		if(!uniqueTypes.contains(type)) {
			uniqueTypes.add(type);
		}
	}
	
	/**
	 * Add an ID characteristic to this NodeDefinition
	 * @param c, the characteristic to add
	 */
	void addIDCharacteristic(String c) {
		if(!identifyingCharacteristics.contains(c)) {
			identifyingCharacteristics.add(c);
		}
	}
	
	/**
	 * Remove a given ID characteristic from this NodeDefinition
	 * @param c, the characteristic to remove
	 */
	void removeIDCharacteristic(String c) {
		if(identifyingCharacteristics.contains(c)) {
			identifyingCharacteristics.remove(c);
		}
	}
	
	/**
	 * Add a data characteristic to this NodeDefinition
	 * @param c, the data characteristic to add
	 */
	void addDataCharacteristic(String c) {
		if(!otherCharacteristics.contains(c)) {
			otherCharacteristics.add(c);
		}
	}
	
	/**
	 * Remove a given data characteristic from this NodeDefinition
	 * @param c, the characteristic to remove
	 */
	void removeDataCharacteristic(String c) {
		if(otherCharacteristics.contains(c)) {
			otherCharacteristics.remove(c);
		}
	}
	
	/**
	 * Generate a string that represents the NodeDefinition
	 * @return a string that summarizes the entire NodeDefinition
	 */
	String printDefinitionToString() {
		String printString = "";
		
		printString = id + FILE_WRITE_DELIMITER + type + FILE_WRITE_DELIMITER;
		printString += delimiter + FILE_WRITE_DELIMITER;
		
		for(String s : identifyingCharacteristics) {
			printString += s + "___";
		}
		if(identifyingCharacteristics.size() > 0) {
			printString = printString.substring(0, printString.length()-3);
		}
		printString += FILE_WRITE_DELIMITER;
		
		for(String s : otherCharacteristics) {
			printString += s + "___";
		}
		if(otherCharacteristics.size() > 0) {
			printString = printString.substring(0, printString.length()-3);
		}
		
		return printString;
	}
	
	/**
	 * Read a string (like those generated from printDefinitionToString) to instantiate
	 * a NodeDefinition
	 * @param line, a line to read to get a NodeDefinition
	 * @return a instantiated NodeDefinition
	 */
	static NodeDefinition readDefinitionFromString(String line) {
		String[] dataElements = line.split(FILE_WRITE_DELIMITER);
		String id = dataElements[0];
		String type = dataElements[1];
		String delimiter = dataElements[2];
		String idCharsRaw = dataElements[3];
		String dataCharsRaw = "";
		if(dataElements.length > 4) {
			dataCharsRaw = dataElements[4];
		}
		NodeDefinition temp = new NodeDefinition(id, type, delimiter);
		String[] idCharArray = idCharsRaw.split("___");
		String[] dataCharArray = dataCharsRaw.split("___");
		for(String s : idCharArray) {
			temp.addIDCharacteristic(s);
		}
		for(String s : dataCharArray) {
			temp.addDataCharacteristic(s);
		}
		
		return temp;
	}
	
	/**
	 * Write out a configuration file for all current NodeDefinitions
	 * @param configFile, the file to write out
	 * @param definitions, the definitions to write to file
	 */
	static void writeConfiguration(File configFile, ArrayList<NodeDefinition> definitions) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(configFile));
			String header = "ID" + FILE_WRITE_DELIMITER + "TYPE" + FILE_WRITE_DELIMITER
					+ "Delimiter" + FILE_WRITE_DELIMITER + "ID-Chars" + FILE_WRITE_DELIMITER
					+ "Data-Chars";
			writer.write(header);
			writer.newLine();
			for(NodeDefinition c : definitions) {
				writer.write(c.printDefinitionToString());
				writer.newLine();
			}
			writer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Read a configuration file (like those generated by writeConfigurationFile) to 
	 * get a set of NodeDefinitions
	 * @param configFile, the file to read
	 * @return a list of node definitions
	 */
	static ArrayList<NodeDefinition> readConfigurationFile(File configFile) {
		uniqueTypes = new ArrayList<String>();
		ArrayList<NodeDefinition> tempCollabs = new ArrayList<NodeDefinition>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(configFile));
			reader.readLine();
			while(reader.ready()) {
				NodeDefinition temp = NodeDefinition.readDefinitionFromString(reader.readLine());
				tempCollabs.add(temp);
			}
			reader.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tempCollabs;
	}
	

}
