/*
 * Cleaner is a tool for recoding elements of a structured data-sheet.
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

package morgan.SurveyInference.Cleaner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * CleanerMain is the script file that controls Cleaner operations.
 * 
 * @author gmorgan, kgarbach
 *
 */
public class CleanerMain {

	/**
	 * Set of elements that can be cleaned at once
	 */
	static String[] actors = {"opername", "Quest14a", "Quest14b", "Quest14c", "Quest14d", "Quest14e" };
	/**
	 * Consistent set of suffixes for each of the actor elements to look for
	 */
	static String[] elements = {"_Role_Cleaned"};
	/**
	 * The cleaning map is used to change elements from X to Y
	 */
	static HashMap<String, String> cleaningMap = new HashMap<String, String>();
	/**
	 * The category map is used to annotate elements with attribute X with Category Y
	 */
	static HashMap<String, String> categoryMap = new HashMap<String, String>();
	/**
	 * The cleaner expects tab-delimited files by default
	 */
	static String DELIMITER = "\t";
	/**
	 * The lastKnownPath indicates where File Selection directories should initialize.  It is
	 * updated every time a file chooser is used.
	 */
	static String lastKnownPath = ".";
	/**
	 * The cleaning mode is used to indicate whether we are in "role cleaning" (mode 0) or 
	 * single column cleaning (mode 1).
	 */
	static int cleanMode = 0;

	/**
	 * The full Cleaner script, this tool was designed to quickly and accurately replace elements that need
	 * to be cleaned.
	 * 
	 * It has two modes, Clean Roles, meant to handle many columns at once, and Clean Single Column, which
	 * cleans a single column at once.  It is feasible to replicate Clean Roles functionality with
	 * Clean Single Column.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO: Consider adding configuration file like Anonymizer
		
		// Load cleaning map from file
		JFileChooser chooser = new JFileChooser(lastKnownPath);
		Object[] options = {"Clean Roles (14a,b,c,d,e etc)",
				"Clean Single Column",
		"Cancel"};
		cleanMode = JOptionPane.showOptionDialog(null,
				"Select Cleaning Mode:",
				"Identify Cleaning Mode",
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[0]);


		if(cleanMode == 0) {
			//cleanMode = "Roles";
			chooser.setDialogTitle("Select Cleaning File (Has Recoded Values) to clean Roles");
			int returnVal = chooser.showOpenDialog(null);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				lastKnownPath = chooser.getSelectedFile().getParent();
				File cleanFile = chooser.getSelectedFile();
				loadCleaningMapFromFile(cleanFile);
				JOptionPane.showMessageDialog(null, "We found " + cleaningMap.size() + " re-code pairs and " + categoryMap.size() + " category pairs.");

				// Load tab-delimited text file to clean
				chooser = new JFileChooser(lastKnownPath);
				chooser.setDialogTitle("Select Tab-Delimited Text File to Clean (e.g. Survey Data)");
				returnVal = chooser.showOpenDialog(null);
				cleanRolesInFile(chooser.getSelectedFile());
				JOptionPane.showMessageDialog(null, "Cleaning Complete!");

			}
		}
		else if(cleanMode == 1) {
			//cleanMode = "Single";
			chooser = new JFileChooser(lastKnownPath);
			chooser.setDialogTitle("Select Cleaning File (Has Recoded Values) to clean ");
			int returnVal = chooser.showOpenDialog(null);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				File cleaningFile = chooser.getSelectedFile();
				lastKnownPath = cleaningFile.getParent();
				loadCleaningMapFromFile(cleaningFile);
				chooser = new JFileChooser(lastKnownPath);
				chooser.setDialogTitle("Select Tab-Delimited Text File to Clean (e.g. Survey Data)");
				returnVal = chooser.showOpenDialog(null);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					lastKnownPath = chooser.getSelectedFile().getParent();
					File fileToClean = chooser.getSelectedFile();
					String[] headers = getHeadersFromFile(fileToClean);
					String columnToClean = (String)JOptionPane.showInputDialog(
							null,
							"Choose Column to clean using '" + cleaningFile.getName() + "':",
							"Choose Single Column to Clean" ,
							JOptionPane.PLAIN_MESSAGE,
							null,
							headers,
							"Choose a header");
					
					cleanColumnInFile(columnToClean, fileToClean);
				}
			}
		}
		else {
			System.exit(0);
		}

	}

	/**
	 * This method is used to clean a set of columns at first using a consistent cleaning key.  This has been generally used
	 * to provide a new column "role category" that takes cleaned roles and allows us to clean many columns at once.
	 * 
	 * The main script runs either this method, or cleanColumnInFile.
	 * 
	 * @param f - the input file to clean
	 */
	static void cleanRolesInFile(File f) {
		try {

			BufferedReader reader = new BufferedReader(new FileReader(f));
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(f.getCanonicalPath() + "_CleanedRoles.txt")));
			String headerLine = reader.readLine();

			String[] header = headerLine.split(DELIMITER);
			String[] updatedHeader = new String[header.length + actors.length];
			for(int j = 0; j < header.length; ++j) {
				header[j] = header[j].trim();
				updatedHeader[j] = header[j];
			}
			for(int a = 0; a < actors.length; ++a) {
				int indexToAdd = header.length + a;
				updatedHeader[indexToAdd] = actors[a] + "_RoleCategory";
			}
			// Now sort the header
			Arrays.sort(updatedHeader);
			//header = updatedHeader;
			// Update the headerLine so that it includes new stuff
			headerLine = ""; 
			for(String h : updatedHeader) {
				headerLine += h + DELIMITER;
			}
			headerLine = headerLine.substring(0, headerLine.length() - DELIMITER.length());
			writer.write(headerLine);
			writer.newLine();


			while(reader.ready()) {
				HashMap<String, String> dataMap = new HashMap<String, String>();
				String dataLine = reader.readLine();
				String[] data = dataLine.split(DELIMITER);
				int minLength = Math.min(header.length, data.length);
				for(int i = 0; i < minLength; ++i) {
					dataMap.put(header[i].trim(), data[i].trim());
				}


				// DataMap is now ready
				for(String actor : actors) {
					String categoryKey = actor + "_RoleCategory";
					for(String element : elements) {
						String key = actor + element;
						if(dataMap.containsKey(key)) {
							String currentValue = dataMap.get(key);
							String categoryKeyValue = dataMap.get(key);
							if(!currentValue.equals("")) {
								//System.out.println("Attempting to clean " + key + ", value: " + dataMap.get(key));
							}
							dataMap.remove(key);
							for(String cleanKey : cleaningMap.keySet()) {

								if(currentValue.equals(cleanKey)) {
									System.out.println("Replacing " + currentValue + " with " + cleaningMap.get(cleanKey));
									currentValue = cleaningMap.get(cleanKey);
									break;
								}
							}
							dataMap.put(key, currentValue);

							if(categoryMap.containsKey(categoryKeyValue)) {
								//System.out.println("")
								dataMap.put(categoryKey, categoryMap.get(categoryKeyValue));
							}
						}
					}
				}

				String newDataLine = "";
				for(String h : updatedHeader) {
					String value = "";
					if(dataMap.containsKey(h)) {
						value = dataMap.get(h);
					}
					else {
						System.out.println("Value-Pair not found. " + h);
					}
					newDataLine += value + DELIMITER;
				}
				newDataLine = newDataLine.trim();
				writer.write(newDataLine);
				writer.newLine();
			}

			reader.close();
			writer.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * This method is used to clean a single given column based on an input file, it automatically writes out 
	 * a new file with a suffix "_Cleaned<column>.txt
	 * 
	 * @param column, the specified column from the header that needs to be cleaned
	 * @param f, the input file used and then cleaned
	 */
	static void cleanColumnInFile(String column, File f) {
		try {

			BufferedReader reader = new BufferedReader(new FileReader(f));
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(f.getCanonicalPath() + "_Cleaned" + column + ".txt")));
			String headerLine = reader.readLine();
			writer.write(headerLine);
			writer.newLine();
			String[] header = headerLine.split(DELIMITER);
			for(int j = 0; j < header.length; ++j) {
				header[j] = header[j].trim();
			}

			while(reader.ready()) {
				HashMap<String, String> dataMap = new HashMap<String, String>();
				String dataLine = reader.readLine();
				String[] data = dataLine.split(DELIMITER);
				int minLength = Math.min(header.length, data.length);
				for(int i = 0; i < minLength; ++i) {
					dataMap.put(header[i].trim(), data[i].trim());
				}


				// DataMap is now ready

				String key = column;
				if(dataMap.containsKey(key)) {
					String currentValue = dataMap.get(key);
					if(!currentValue.equals("")) {
						//System.out.println("Attempting to clean " + key + ", value: " + dataMap.get(key));
					}
					System.out.println("\tOriginal:" + currentValue);
					currentValue = currentValue.replaceAll("\"", "");
					dataMap.remove(key);
					String[] commaSeparated = currentValue.split(",");
					String tokenValue = "";
					for(String t : commaSeparated) {
						t = t.trim();
						for(String cleanKey : cleaningMap.keySet()) {
							//System.out.println("\t\tTrying " + cleanKey);
							if(t.equals(cleanKey)) {
								System.out.println("\t\t\tUpdating " + cleanKey + " with " + cleaningMap.get(cleanKey));
								t = cleaningMap.get(cleanKey);
								
							}
						}
						tokenValue += t + ","; 
					}
					currentValue = tokenValue.substring(0, tokenValue.length()-1);
					/*String[] tokens = currentValue.split("\\s+");
					String newValue = "";
					for(String token : tokens) {

						String[] commaSeparated = token.split(",");
						String tokenValue = "";
						for(String t : commaSeparated) {
							
							for(String cleanKey : cleaningMap.keySet()) {
								//System.out.println("\t\tTrying " + cleanKey);
								if(t.equals(cleanKey)) {
									System.out.println("\t\t\tUpdating " + cleanKey + " with " + cleaningMap.get(cleanKey));
									t = cleaningMap.get(cleanKey);
									
								}
							}
							tokenValue += t + ","; 
						}
						token = tokenValue; //.substring(0, tokenValue.length()-1);
						newValue += token + " ";

					}
					currentValue = newValue.substring(0, newValue.length()-2);
					*/
					System.out.println("\tCurrent:" + currentValue);
					dataMap.put(key, currentValue);

				}


				String newDataLine = "";
				for(String h : header) {
					newDataLine += dataMap.get(h) + DELIMITER;
				}
				newDataLine = newDataLine.trim();
				writer.write(newDataLine);
				writer.newLine();
			}

			reader.close();
			writer.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Read the top line of a file to get its headers
	 * @param f - the file to read headers from
	 * @return a list of columns
	 */
	static String[] getHeadersFromFile(File f) {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(f));
			String headerLine = reader.readLine();
			String[] header = headerLine.split(DELIMITER);
			reader.close();
			return header;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Read a file to generate a cleaning map, by which we mean
	 * "If you see X, replace Y", with X and Y specified by the
	 * key-set and value-set of the data map, respectively
	 * 
	 * @param f - a file that contains the cleaning map, it may have 
	 *            other columns that are ignored.
	 */
	static void loadCleaningMapFromFile(File f) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(f));
			String headerLine = reader.readLine();
			String[] header = headerLine.split(DELIMITER);
			int categoryIndex = -1;
			int sourceIndex = JOptionPane.showOptionDialog(null,
					"Select uncleaned data column.",
					"Cleaner: Select Source",
					JOptionPane.DEFAULT_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,
					header,
					header[0]);
			int targetIndex = JOptionPane.showOptionDialog(null,
					"Select mapping column for cleaning.",
					"Cleaner: Select Mapping Column",
					JOptionPane.DEFAULT_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,
					header,
					header[0]);

			if(cleanMode == 0) {
				categoryIndex = JOptionPane.showOptionDialog(null,
						"Select category column",
						"Cleaner: Select Mapping Column",
						JOptionPane.DEFAULT_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						header,
						header[0]);
			}

			while(reader.ready()) {
				String dataLine = reader.readLine().trim();
				if(!dataLine.equals("")) {
					String[] data = dataLine.split("\t");
					if(data.length > Math.max(sourceIndex, targetIndex)) {
						//System.out.println("Raw: " + data[sourceIndex] + " Cleaned: " + data[targetIndex]);
						cleaningMap.put(data[sourceIndex].trim(), data[targetIndex].trim());
						if(categoryIndex != -1) {
							categoryMap.put(data[sourceIndex].trim(), data[categoryIndex].trim());
							System.out.println("Raw: " + data[sourceIndex] + " Cleaned: " + data[targetIndex] + " Category: " + data[categoryIndex]);

						}
					}
				}
			}

			reader.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
