/*
 * Anonymizer is a tool for anonymizing portions of a structured data-sheet.
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

package morgan.SurveyInference.Anonymizer;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import morgan.SurveyInference.Linker.TXTFileFilter;

/**
 * AnonymizerMain is the primary script that controls the Anonymizer.
 * 
 * @author gmorgan, kgarbach
 *
 */
public class AnonymizerMain {

	/**
	 * This is the list of the questions that indicate interaction partners.
	 */
	static String[] columnsToAnonymize = {"Quest14a", "Quest14b", "Quest14c", "Quest14d", "Quest14e"};

	/**
	 * The operator's name
	 */
	static String[] columnsIndicatingRespondent = {"respondent", "opername"};

	/**
	 * This indicates the data separation in the file, tab-delimited
	 */
	static String dataDelimiter = "\t";

	/**
	 * This indicates the separator between names and roles in partners
	 */
	static String entryDelimiter = ":";

	/**
	 * This is what the index card holding the person's raw name will use
	 */
	static String partnerNameSuffix = "_Name";

	/**
	 * This is what the index card holding the person's raw role will use
	 */
	static String partnerRoleSuffix = "_Role";

	/**
	 * If a partner's name wasn't provided, then we go with "Unknown"
	 */
	static String unknownActor = "Unknown";

	/**
	 * When we clean names and roles, we add this to the end of the index question
	 */
	static String cleanExtension = "_Cleaned";

	/**
	 * When we anonymize names, we add this to the end of the index question
	 */
	static String anonExtension = "_Anonymous";

	/**
	 * Respondents are, by default, growers
	 */
	static String implicitRole = "Grower";

	/**
	 * All headers found in the data
	 */
	static ArrayList<String> headers = new ArrayList<String>();

	/**
	 * All names so far found, used in Minimum Edit Distance
	 */
	static HashSet<String> uniqueNames = new HashSet<String>();

	/**
	 * All roles so far found, used in Minimum Edit Distance
	 */
	static HashSet<String> uniqueRoles = new HashSet<String>();

	/** 
	 * A ProgressBar that shows progress to the user
	 */
	static JProgressBar progress = new JProgressBar(0, 1);

	/**
	 * Gives reports to the user on what is going on.  Displays warnings
	 * and a control log to the user.
	 */
	static JTextArea textField = new JTextArea(10, 20);

	/**
	 * This array is used to control how much distance there needs to be between strings
	 * to consider they are similar 'enough'.  By default, we use exact matches at 3 or less.
	 * Distance 1 for strings of length 4 to 8; Distance 2 for strings length 9 to 12, and 
	 * Distance 3 for Strings longer than 12.
	 */
	static int[] cleaningThresholds = {3,8,12};

	/**
	 * This variable controls whether the fields identifed in the interactionPartners
	 * variable are expected to have two portions to the answer, separated by the
	 * entryDelimiter variable.
	 * 
	 * If true, then entries that do not have the entry delimiter are assumed to be less
	 * identifying information that does not require anonymization.
	 * 
	 * If false, then the entire entry is considered and needs to be anonymized.
	 */
	static boolean columnHasRole = true;


	/**
	 * This is the main execution thread of the program.  It does the following:
	 * 	1) Identify a tab-delimited data file to process
	 *  2) Instantiate the ProgressBar
	 *  3) Read in the DataFile
	 *  4) Elaborate (separate participant name and roles) each participant
	 *  5) Clean (check unique sets via minimum edit distance) each participant
	 *  6) Anonymize each participant
	 *  7) Write out the data
	 *  
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			if(args.length > 0) {
				readConfigurationFile(new File(args[0]));
			}
			else {
				readConfigurationFile(new File("anonymizerConfig.txt"));
			}
		} catch (Exception e) {
			System.out.println("Error reading configuration file, using defaults!");
		}
		JFileChooser dataFileChooser = new JFileChooser(".");
		dataFileChooser.setDialogTitle("Select data file to anonymize.");
		dataFileChooser.setFileFilter(new TXTFileFilter());
		int returnVal = dataFileChooser.showOpenDialog(null);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			File dataFile = dataFileChooser.getSelectedFile();
			try {
				JFrame progressFrame = new JFrame();
				JPanel content = new JPanel();
				content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
				progressFrame.setContentPane(content);
				progressFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				progressFrame.setTitle("Anonymizer!");
				progressFrame.setPreferredSize(new Dimension(600,400));
				content.add(progress);
				JScrollPane scrollPanel = new JScrollPane(textField);
				scrollPanel.setPreferredSize(new Dimension(600, 350));
				content.add(scrollPanel);
				textField.setText("Anonymizer Run Log:");
				progressFrame.pack();
				progressFrame.setVisible(true);
				// 1. Read file, convert to HashMap "index card" representation
				ArrayList<HashMap<String, String>> pData = readDataFile(dataFile);
				// 2. Convert Quest14a, Quest14b, Quest14c, Quest14d, Quest14e to name and role
				//    2a. If the cell isn't empty, then
				//    2b. Delimiter between name and role is ":"
				//    2c. If no delimiter, then the name is unknown and the value given is role
				progress.setMaximum(pData.size());
				progress.setStringPainted(true);
				elaborateParticipants(pData);
				// 3. Create "cleaned names" based on variable Levenshtein distance for:
				//    Quest14a_Name, Quest14b_Name, Quest14c_Name, Quest14d_Name, Quest14e_Name,
				//    opername
				// 4. Create "cleaned roles" based on variable Levenshtein distance for:
				//    Quest14a_Role, Quest14b_Role, Quest14c_Role, Quest14d_Role, Quest14e_Role,
				Thread.sleep(1000);
				cleanParticipants(pData);
				// 5. Create "anonymous names"
				Thread.sleep(1000);
				anonymizeParticipants(pData);
				// 6. Output new file
				File fileToWrite = new File(dataFile.getParentFile().getCanonicalPath() + "//Cleaned_" + dataFile.getName());
				writeDataFile(fileToWrite, pData);
				Thread.sleep(1000);
				progress.setString("Process Complete!");
				textField.setText(textField.getText() + "\nDone!\n\nClose the Window when ready.");
				//Thread.sleep(1000);
				//progressFrame.setVisible(false);
				//System.exit(0);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	/**
	 * This method reads the configuration file and uses those to inform static variables
	 * that control the Anonymizer's behavior.
	 * 
	 * @param configFile
	 * @throws Exception
	 */
	static void readConfigurationFile(File configFile) throws Exception {
		/**
		 * 	columnsToAnonymize: Quest14a,Quest14b,Quest14c,Quest14d,Quest14e
			columnsIndicatingRespondent: respondent,opername
			dataDelimiter: \t
			partnerDelimiter: :
			implicitRole: Grower
		 */
		BufferedReader reader = new BufferedReader(new FileReader(configFile));
		HashMap<String,String> dataMap = new HashMap<String,String>();
		while(reader.ready()) {
			String dataLine = reader.readLine();
			String flag = dataLine.substring(0, dataLine.indexOf(":"));
			String value = dataLine.substring(dataLine.indexOf(":")+1, dataLine.length());
			flag = flag.trim();
			value = value.trim();
			//System.out.println(flag + " --- " + value);
			dataMap.put(flag, value);
		}
		reader.close();

		for(String flag : dataMap.keySet()) {
			if(flag.equalsIgnoreCase("columnsToAnonymize")) {
				columnsToAnonymize = dataMap.get(flag).split(",");
			}
			else if(flag.equalsIgnoreCase("columnsIndicatingRespondent")) {
				columnsIndicatingRespondent = dataMap.get(flag).split(",");
			}
			else if(flag.equalsIgnoreCase("dataDelimiter")) {
				dataDelimiter = dataMap.get(flag);
			}
			else if(flag.equalsIgnoreCase("intraColumnDelimiter")) {
				entryDelimiter = dataMap.get(flag);
			}
			else if(flag.equalsIgnoreCase("implicitRole")) {
				implicitRole = dataMap.get(flag);
			}
			else if(flag.equalsIgnoreCase("columnHasRole")) {
				columnHasRole = Boolean.parseBoolean(dataMap.get(flag));
			}
			else if(flag.equalsIgnoreCase("cleaningThresholds")) {
				if(!dataMap.get(flag).equals("")) {
					String[] values = dataMap.get(flag).split(",");
					int[] intValues = new int[values.length];
					for(int i = 0; i < values.length; ++i) {
						intValues[i] = Integer.parseInt(values[i]);
					}
					cleaningThresholds = intValues;
				}
				else {
					cleaningThresholds = null;
				}
			}
			else {
				JOptionPane.showMessageDialog(null,  "Configuration flag " + flag + " is not recognized.  Please check.", "Configuration File Error: " + configFile.getName(), JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Reads in a given data file and generates a collection of hashmaps, each hashmap represents
	 * a participant.
	 * 
	 * Note that we trim data as we get it, which removes white-spaces from in front and behind each
	 * element of the data.
	 * 
	 * @param dataFile The tab-delimited data-file to read
	 * @return a set of participant data 
	 * @throws Exception If someone goes wrong in reading the data file, we cancel execution
	 */
	static ArrayList<HashMap<String, String>> readDataFile(File dataFile) throws Exception {
		ArrayList<HashMap<String, String>> participantData = new ArrayList<HashMap<String, String>>();

		BufferedReader reader = new BufferedReader(new FileReader(dataFile));
		String headerLine = reader.readLine();
		String delimiterToUse = dataDelimiter.replaceAll("\"", "");
		String[] headerElements = headerLine.split(delimiterToUse);
		for(int i = 0; i < headerElements.length; ++i) {
			headerElements[i] = headerElements[i].trim();
			//System.out.println(headerElements[i]);
		}
		Collections.addAll(headers, headerElements);
		
		textField.setText(textField.getText() + "\nConfiguring...");
		for(String partner : columnsToAnonymize) {	
			if(!headers.contains(partner)) {
				textField.setText(textField.getText() + "\n\tAnonymization Column, " + partner + ", not found!");
			}
		}
		for(String operator : columnsIndicatingRespondent) {	
			if(!headers.contains(operator)) {
				textField.setText(textField.getText() + "\n\tAnonymization Column, " + operator + ", not found!");
			}
		}
		
		

		while(reader.ready()) {
			HashMap<String, String> participant = new HashMap<String, String>();
			String dataLine = reader.readLine();
			String[] dataElements = dataLine.split(delimiterToUse);
			for(int i = 0; i < dataElements.length; ++i) {
				if(headerElements.length > i) {
					if(!headerElements[i].equals("")) {
						String d = dataElements[i].trim();
						d = d.replaceAll("\"", "");
						//System.out.println(headerElements[i] + " : " + d);
						participant.put(headerElements[i], d);
					}
				}
			}
			participantData.add(participant);
		}
		reader.close();

		return participantData;
	}

	/**
	 * Go through and elaborate each participant
	 * 
	 * @param pData
	 */
	static void elaborateParticipants(ArrayList<HashMap<String, String>> pData) {
		int counter = 1;
		progress.setString("Elaborating...");
		textField.setText(textField.getText() + "\nElaborating...");
		for(HashMap<String, String> participant : pData) {
			progress.setValue(++counter);
			elaborateParticipant(counter, participant);
		}
	}

	/**
	 * For each participant, we do two things:
	 * 
	 * 1) We add an implicit role for each participant
	 * 2) For each valid Interaction Partner, we split them in half based on the given
	 *     delimiter, if there is no delimiter, but it's not empty, then we assume it
	 *     is a role, and that the name is "Unknown".
	 * 
	 * We also capitalize all three letter names or roles (and only three-letter names and roles).
	 *     
	 * @param participant A collection of data representing a participant
	 */
	static void elaborateParticipant(int row, HashMap<String, String> participant) {
		for(String iPartner : columnsToAnonymize) {
			if(participant.containsKey(iPartner)) {
				String partnerData = participant.get(iPartner).trim();
				//System.out.println("Data Check (" + iPartner + ") :" + partnerData);
				if(!partnerData.equals("")) {
					try{
						// We have data
						if(columnHasRole) {
							System.out.println("hasRole:" + partnerData);
							if(partnerData.contains(entryDelimiter)) {
								// We have a name and a role
								String[] partner = partnerData.split(entryDelimiter);
								String pName = partner[0].trim();
								String pRole = partner[1].trim();
								if(pName.length() <= 3) {
									pName = pName.toUpperCase();
								}
								if(pRole.length() <= 3) {
									pRole = pRole.toUpperCase();
								}
								participant.put(iPartner + partnerNameSuffix, pName);
								participant.put(iPartner + partnerRoleSuffix, pRole);
							}
							else {
								if(partnerData.length() <= 3) {
									partnerData = partnerData.toUpperCase();
								}
								participant.put(iPartner + partnerNameSuffix, unknownActor);
								participant.put(iPartner + partnerRoleSuffix, partnerData);
							}
						}
						else {
							System.out.println("noRole:" + partnerData);
							if(partnerData.length() <= 3) {
								partnerData = partnerData.toUpperCase();
							}
							participant.put(iPartner + partnerNameSuffix, partnerData);
							//participant.put(iPartner + partnerRoleSuffix, implicitRole);
						}
					} catch (Exception e) {
						String message = "\tError Parsing Line " + row + ": " + iPartner
								+ " - " + partnerData;
						textField.setText(textField.getText() + "\n" + message);
					}
				}
				participant.remove(iPartner);

			}
		}
		for(String n : columnsIndicatingRespondent) {
			participant.put(n + "_Role", implicitRole);
		}
	}

	/**
	 * Go through each participant and clean them. By clean, we mean check if the names
	 * and roles are so similar they should really be the same name or role.
	 * 
	 * @param pData - the Collection of participant data
	 */
	static void cleanParticipants(ArrayList<HashMap<String, String>> pData) {
		int counter = 0;
		progress.setString("Cleaning...");
		textField.setText(textField.getText() + "\nCleaning...");
		for(HashMap<String, String> participant : pData) {
			progress.setValue(++counter);
			cleanParticipant(participant);
		}
	}

	/**
	 * Helper function per participant to clean each participant, we check against
	 * the existing set of names and roles and see if something close enough already exists.
	 * 
	 * @param participant
	 */
	static void cleanParticipant(HashMap<String, String> participant) {
		for(String iPartner : columnsToAnonymize) {
			String iPartnerName = iPartner + partnerNameSuffix;
			String iPartnerRole = iPartner + partnerRoleSuffix;
			cleanParticipantInfo(participant, iPartnerName, uniqueNames);
			cleanParticipantInfo(participant, iPartnerRole, uniqueRoles);
		}
		for(String respondent : columnsIndicatingRespondent) {
			cleanParticipantInfo(participant, respondent, uniqueNames);
			cleanParticipantInfo(participant, respondent + partnerRoleSuffix, uniqueRoles);
		}
	}

	/**
	 * We take a given participant, a data element, and a set of existing data entries that the
	 * data element should be compared against.
	 * 
	 * @param participant - data for each participant
	 * @param key - the key to data that should be checked
	 * @param uniqueSet - unique values that should be compared against
	 */
	static void cleanParticipantInfo(HashMap<String, String> participant, String key, HashSet<String> uniqueSet) {
		if(participant.containsKey(key)) {
			String info = participant.get(key);
			String cleanedInfo = info;
			if(!info.equals(unknownActor)) {
				// Ignore the unknown actors
				int infoLength = info.length();
				int closestThreshold;
				if(cleaningThresholds == null) {
					closestThreshold = 0;
				}
				else {
					closestThreshold = cleaningThresholds.length;
					for(int i = 0; i < cleaningThresholds.length; ++i) {
						if(cleaningThresholds[i] >= infoLength) {
							closestThreshold = i;
							break;
						}
					}
				}
				System.out.println("Cleaning Check : " + info + " - " + closestThreshold);

				cleanedInfo = getClosestName(closestThreshold, info, uniqueSet);

			}
			//textField.setText(textField.getText() + "\n\t" + key + " - " + info + ":" + cleanedInfo);
			participant.put(key + cleanExtension, cleanedInfo);
		}
	}

	/**
	 * Go through each unique name found in the set and create an anonymous version
	 * Then go through each participant and create anonymous versions.
	 * 
	 * @param pData
	 */
	static void anonymizeParticipants(ArrayList<HashMap<String, String>> pData) {
		int counter = 0;
		progress.setString("Anonymizing...");
		textField.setText(textField.getText() + "\nAnonymizing...");

		HashMap<String, String> anonymousNames = new HashMap<String,String>();
		int nameCounter = 0;
		for(String name : uniqueNames) {
			String anonName = "Name" + ++nameCounter;
			textField.setText(textField.getText() + "\n\t" + name + ":" + anonName);
			anonymousNames.put(name, anonName);
		}

		for(HashMap<String, String> participant : pData) {
			progress.setValue(++counter);
			anonymizeParticipant(anonymousNames, participant);
		}

	}

	/**
	 * Go through all the potential partners and create anonymous versions by using the anonymous
	 * hashmap lookup via the cleaned name.
	 * 
	 * We don't remove the cleaned name, we just add the anonymous one at this point.
	 * 
	 * @param anonymousNames
	 * @param participant
	 */
	static void anonymizeParticipant(HashMap<String, String> anonymousNames, HashMap<String, String> participant) {

		for(String iPartner : columnsToAnonymize) {
			String iPartnerName = iPartner + partnerNameSuffix;
			String iPartnerCleaned = iPartnerName + cleanExtension;
			String iPartnerAnonymous = iPartnerName + anonExtension;
			if(participant.containsKey(iPartnerCleaned)) {
				if(anonymousNames.containsKey(participant.get(iPartnerCleaned))) {
					String anonName = anonymousNames.get(participant.get(iPartnerCleaned));
					participant.put(iPartnerAnonymous, anonName);
				}
				else {
					participant.put(iPartnerAnonymous, unknownActor);
				}
			}
		}

		for(String n : columnsIndicatingRespondent) {
			String pNameCleaned = n  + cleanExtension;
			String pNameAnon = n + anonExtension;
			if(participant.containsKey(pNameCleaned)) {
				//System.out.println("Participant did include: " + pNameCleaned);
				String pAnon = anonymousNames.get(participant.get(pNameCleaned));
				//System.out.println(participant.get(pNameCleaned) + ":" + pAnon);
				participant.put(pNameAnon, pAnon);
			}
			else {
				System.out.println("Participant did not include: " + pNameCleaned);
			}
		}
	}

	/**
	 * We keep the data in each dictionary, but we remove our ability to access it, while adding
	 * the anonymous version to the header.
	 */
	static void removeHeaderElements() {
		for(String iPartner : columnsToAnonymize) {
			String iPartnerName = iPartner + partnerNameSuffix;
			String iPartnerNameCleaned = iPartnerName + cleanExtension;
			String iPartnerNameAnon = iPartnerName + anonExtension;
			if(headers.contains(iPartner)) {
				headers.remove(iPartner);
			}
			if(headers.contains(iPartnerName)) {
				headers.remove(iPartnerName);
			}
			if(headers.contains(iPartnerNameCleaned)) {
				headers.remove(iPartnerNameCleaned);
			}

			String iPartnerRole = iPartner + partnerRoleSuffix;
			String iPartnerRoleCleaned = iPartnerRole + cleanExtension;

			headers.add(iPartnerNameAnon);
			headers.add(iPartnerRole);
			headers.add(iPartnerRoleCleaned);
		}

		for(String n : columnsIndicatingRespondent) {
			headers.remove(n);
			headers.remove(n  + cleanExtension);
			headers.add(n + anonExtension);
			headers.add(n + partnerRoleSuffix);
			headers.add(n + partnerRoleSuffix + cleanExtension);
		}
	}

	/**
	 * We sort the final header here and write out the data.
	 * 
	 * @param f - the output file
	 * @param pData - the set of participant data
	 * @throws IOException - in case there is an error with writing the file
	 */
	static void writeDataFile(File f, ArrayList<HashMap<String, String>> pData) throws IOException {

		removeHeaderElements();
		Collections.sort(headers);

		int counter = 0;
		progress.setString("Writing...");
		textField.setText(textField.getText() + "\nWriting...");

		BufferedWriter writer = new BufferedWriter(new FileWriter(f));
		String headerLine = "";
		for(String h : headers) {
			headerLine += h + dataDelimiter;
		}
		// Remove the closing comma
		headerLine = headerLine.substring(0, headerLine.length() - 1);
		writer.write(headerLine);
		writer.newLine();

		for(HashMap<String, String> participant : pData) {
			progress.setValue(++counter);
			String dataLine = "";
			for(String h : headers) {
				if(participant.containsKey(h)) {
					dataLine += participant.get(h);
				}
				dataLine += dataDelimiter;
			}
			// Remove the closing comma
			dataLine = dataLine.substring(0, dataLine.length() - 1);
			//System.out.println(dataLine);
			writer.write(dataLine);
			writer.newLine();
		}
		writer.flush();
		writer.close();
	}

	/**
	 * Of all names already seen in the data-set, examine them and, if any are close enough,
	 * identify this name as the same as the other name.
	 * 
	 * This uses the Levenshtein, also known as Minimum Edit, Distance to compare the strings
	 * 
	 * The first-found most similar name is returned.
	 * 
	 * @param maxDistance - the amount of difference between names that are actually the same name
	 * @param target - the string to be checked against prior-found names
	 * @return the closest string, if any based on the max-distance, null if no suitable string is found.
	 */
	static String getClosestName(int maxDistance, String target, HashSet<String> uniqueSet) {
		String currentBest = null;
		int currentLowestDistance = maxDistance + 1;

		for(String currentName : uniqueSet) {
			int distance = LevenshteinDistance(target, currentName);
			if(distance < currentLowestDistance) {
				currentBest = currentName;
				currentLowestDistance = distance;
			}
		}

		if(currentBest == null) {
			//System.out.println("Adding " + target + " to the name-list.");
			if(!uniqueSet.contains(target)) {
				uniqueSet.add(target);
			}
			return target;
		}

		return currentBest;
	}

	/**
	 * This implementation is taken entirely from 
	 * http://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance
	 * 
	 * Note, we can edit the insert, delete, and modify distances to suit our needs
	 * 
	 * Accessed July 7th, 2014
	 * 
	 * @param s0 - string to compare, e.g. "kitten"
	 * @param s1 - other string to compare, e.g., "sitting"
	 * @return the Levenshtein Distance, or Minimum Edit Distance, which would be 3 between "kitten" and "sitting"
	 */
	static public int LevenshteinDistance (String s0, String s1) {
		int len0 = s0.length()+1;
		int len1 = s1.length()+1;

		// the array of distances
		int[] cost = new int[len0];
		int[] newcost = new int[len0];

		// initial cost of skipping prefix in String s0
		for(int i=0;i<len0;i++) cost[i]=i;

		// dynamically computing the array of distances

		// transformation cost for each letter in s1
		for(int j=1;j<len1;j++) {

			// initial cost of skipping prefix in String s1
			newcost[0]=j-1;

			// transformation cost for each letter in s0
			for(int i=1;i<len0;i++) {

				// matching current letters in both strings
				int match = (s0.charAt(i-1)==s1.charAt(j-1))?0:1;

				// computing cost for each transformation
				int cost_replace = cost[i-1]+match;
				int cost_insert  = cost[i]+1;
				int cost_delete  = newcost[i-1]+1;

				// keep minimum cost
				newcost[i] = Math.min(Math.min(cost_insert, cost_delete),cost_replace );
			}

			// swap cost/newcost arrays
			int[] swap=cost; cost=newcost; newcost=swap;
		}

		// the distance is the cost for transforming all letters in both strings
		return cost[len0-1];
	}

}
