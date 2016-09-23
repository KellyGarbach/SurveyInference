package morgan.SurveyInference.Anonymizer;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTextField;

public class CandidateIdentifier implements Comparable<CandidateIdentifier> {

	String id;
	String cleanedID;
	String anonID;

	CandidateIdentifier(String theName) {
		id = theName;
		cleanedID = null;
		anonID = null;
	}

	public int compareTo(CandidateIdentifier o) {
		Integer thisLength = this.id.length();
		Integer oLength = o.id.length();

		return thisLength.compareTo(oLength);
	}
	
	static HashSet<String> cleanCandidateIDs(List<CandidateIdentifier> theRawIDs, HashSet<String> uniqueIDs) {
		// The shortest elements are now first
		Collections.sort(theRawIDs);
		// For each candidate
		// Identify their threshold and the candidate's threshold
		// Calculate the LevenshteinDistance
		// Check that both unique identifiers would be willing to replace each other based
		// on current config
		for(CandidateIdentifier candidate : theRawIDs) {
			int threshold = CandidateIdentifier.getDistanceThreshold(candidate.id);
			int maxDistance = threshold + 1;
			CandidateIdentifier bestCandidate = null;
			// Skip the cleaning process if you're unknown
			if(!candidate.equals(AnonymizerMain.unknownActor)) {
				for(CandidateIdentifier alter : theRawIDs) {
					// Don't match to myself or to unknown actors
					if(!candidate.equals(alter) && !candidate.id.equals(AnonymizerMain.unknownActor)) {
						
						int alterThreshold = CandidateIdentifier.getDistanceThreshold(alter.id) + 1;
						int distance = LevenshteinDistance(candidate.id, alter.id);
						// Check for symmetry (Jan should replace Jane only if Jane and Jan would both match)
						if(distance < maxDistance && distance < alterThreshold) {				
							bestCandidate = alter;
							maxDistance = distance;
							System.out.println("Original: " + candidate.id + " Best Candidate:" + bestCandidate.id + ", Distance: " + distance);
						}
					}
				}
			}

			if(bestCandidate != null) {
				candidate.cleanedID = bestCandidate.id;
			}
			else {
				candidate.cleanedID = candidate.id;
			}
			
			if(!uniqueIDs.contains(candidate.cleanedID)) {
				uniqueIDs.add(candidate.cleanedID);
			}
		}

		return uniqueIDs;
	}

	static int getDistanceThreshold(String info) {
		int infoLength = info.length();
		int closestThreshold;
		if(AnonymizerMain.cleaningThresholds == null) {
			closestThreshold = 0;
		}
		else {
			closestThreshold = AnonymizerMain.cleaningThresholds.length;
			for(int i = 0; i < AnonymizerMain.cleaningThresholds.length; ++i) {
				if(AnonymizerMain.cleaningThresholds[i] >= infoLength) {
					closestThreshold = i;
					break;
				}
			}
		}

		return closestThreshold;
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

	void addVisualElementsToPanel(JPanel component) {
		if(!id.equals(cleanedID)) {
			JTextField idField, cleanedField;
			idField = new JTextField(id);
			idField.setEditable(false);
			component.add(idField);
		
			cleanedField = new JTextField(cleanedID);
			cleanedField.setEditable(false);
			component.add(cleanedField);
		}
	}

}
