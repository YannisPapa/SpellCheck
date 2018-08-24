import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {

	public static Map<String, Integer> words;
	public static final String[] alphabet = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o",
			"p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z" };

	public static String cleanLine(String line) {
		//This function is used to clean one line at a time
		
		//these are some of the symbols and numbers that show up in text and need to be cleaned off
		line = line.replace("--", " ");
		line = line.replace("''", " ");
		line = line.replace("...", " ");
		line = line.replaceAll("[/!@#$%^&*(){}=;:,<>+?_|~0-9]", ""); // removed .
		line = line.replaceAll("\\[", "");
		line = line.replaceAll("\\]", "");
		line = line.replaceAll("\"", "");
		return line;
	}

	public static String cleanWord(String word) {
		//This function is used to clean a word
		
		//If the word is just ' or - return a blank
		if (word.equals("'") || word.equals("-")) {
			return "";
		}
		//If the word is too small to have extra stuff return the word
		if (word.length() < 2) {
			return word;
		}

		//Remove junk from start of words as words always starts with letter
		while (word.substring(0, 1).equals("'") || word.substring(0, 1).equals("-") || word.substring(0, 1).equals(".")) {
			word = word.substring(1, word.length());
			if(word.length() == 0) {
				return "";
			}
		}

		//Find where the actual word ends in the mess of characters
		int wordEnd = word.length();
		for (int i = 0; i < word.length(); i++) {
			if (Character.isLetter(word.charAt(i))) {
				wordEnd = i;
			}
		}
		wordEnd++;

		// remove all - from the end of the word as it can only occur inside of the word
		if (word.substring(wordEnd, word.length()).contains("-")) {
			for (int i = wordEnd; i < word.length(); i++) {
				if (word.charAt(i) == '-') {
					word = word.substring(0, i) + word.substring(i + 1, word.length());
				}
			}
		}

		// if the words has a period inside of it it can have a period at the end ex. "W.H."
		if (!word.substring(0, wordEnd).contains(".")) {
			word = word.replaceAll("\\.", "");
		} else {
			if (word.substring(wordEnd, word.length()).contains(".")) {
				word = word.substring(0, wordEnd) + ".";
			}
		}

		// words are allowed to end with '
		if (word.substring(wordEnd, word.length()).contains("'")) {
			word = word.substring(0, wordEnd) + "'";
		}

		return word;
	}

	public static void createDictionary(String fileName) {
		//This function is used to build a dictionary from the text given
		
		//This program uses a HashMap as it has O(1) time to search
		words = new HashMap<String, Integer>();
		Scanner scan = null;

		try {
			scan = new Scanner(new File(fileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		//Read one line at a time from the file
		while (scan.hasNextLine()) {
			String line = scan.nextLine();
			line = cleanLine(line);
			Scanner scan2 = new Scanner(line);
			//Read one word after having cleaned the line a bit and add it to the dictionary
			while (scan2.hasNext()) {
				String s = scan2.next();

				s = cleanWord(s);
				s = s.toLowerCase();
				if (s.equals("")) {
					continue;
				}

				if (words.containsKey(s)) {
					words.put(s, words.get(s) + 1);
				} else {
					words.put(s, 1);
				}
			}
			scan2.close();
		}
		scan.close();
	}

	public static Map.Entry<String, Integer> chkDict(String str) {
		//This function finds if a word is in the dictionary and if it is it return the entry(String and int)
		if(words.containsKey(str)) {
			Map.Entry<String, Integer> entry = new AbstractMap.SimpleEntry<String, Integer>(str, words.get(str));
			return entry;
		}
		return null;
	}

	public static void missingChar(Map<String, Integer> corrections, String str) {
		//This function checks if the word is missing a character by adding a letter from the alphabet one at a time in each
		//position and seeing if it makes a valid word. If it does it adds the words to a list of possible correct words.
		String chngStr = "";
		for (int i = 0; i < str.length() + 1; i++) {
			for (int j = 0; j < alphabet.length; j++) {
				chngStr = str.substring(0, i) + alphabet[j] + str.substring(i, str.length());
				Map.Entry<String, Integer> entry = chkDict(chngStr);
				if (entry != null) {
					corrections.put(entry.getKey(), entry.getValue());
				}
			}
		}
	}

	public static void wrongChar(Map<String, Integer> corrections, String str) {
		//This function checks if the word has a wrong character by replacing a letter with one from the alphabet one at a time in each
		//position and seeing if it makes a valid word. If it does it adds the words to a list of possible correct words.
		String chngStr = "";
		for (int i = 1; i < str.length() + 1; i++) {
			for (int j = 0; j < alphabet.length; j++) {
				chngStr = str.substring(0, i - 1) + alphabet[j] + str.substring(i, str.length());
				Map.Entry<String, Integer> entry = chkDict(chngStr);
				if (entry != null) {
					corrections.put(entry.getKey(), entry.getValue());
				}
			}
		}
	}

	public static void swapChar(Map<String, Integer> corrections, String str) {
		//This function checks if the word has two characters next to each other in the wrong place by swapping each letter
		//with its neighbors one at a time and seeing if it makes a valid word. 
		//If it does it adds the words to a list of possible correct words.
		String chngStr = "";
		for (int i = 0; i < str.length() - 1; i++) {
			StringBuilder temp = new StringBuilder(str);
			temp.setCharAt(i, str.charAt(i + 1));
			temp.setCharAt(i + 1, str.charAt(i));
			chngStr = temp.toString();
			Map.Entry<String, Integer> entry = chkDict(chngStr);
			if (entry != null) {
				corrections.put(entry.getKey(), entry.getValue());
			}
		}
	}

	public static void extraChar(Map<String, Integer> corrections, String str) {
		//This function checks if the word has an extra character by removing a letter from the word one at a time
		//and seeing if it makes a valid word. If it does it adds the words to a list of possible correct words.
		String chngStr = "";
		for (int i = 1; i < str.length() + 1; i++) {
			chngStr = str.substring(0, i - 1) + str.substring(i, str.length());
			Map.Entry<String, Integer> entry = chkDict(chngStr);
			if (entry != null) {
				corrections.put(entry.getKey(), entry.getValue());
			}
		}
	}

	public static String spellChk(String str) {
		//This is the spell check function that will find the correction for a word if one exists
		Map.Entry<String, Integer> correct = null;
		Map<String, Integer> corrections = new HashMap<String, Integer>();

		//Check if the word exists in the dictionary
		correct = chkDict(str);

		//if it doesn't then just return the word
		if (correct == null) {
			//call each error checking function
			missingChar(corrections, str);
			wrongChar(corrections, str);
			swapChar(corrections, str);
			extraChar(corrections, str);

			Integer mostOccure = 0;

			//if there was no better word return the word
			if (corrections.isEmpty()) {
				return str;
			}

			//return one of the recommended words with the highest occurrence
			for (Map.Entry<String, Integer> entry : corrections.entrySet()) {
				if (entry.getValue() > mostOccure) {
					str = entry.getKey();
					mostOccure = entry.getValue();
				}
				if(entry.getValue() == mostOccure) {
					if(str.compareTo(entry.getKey()) >= 1) {
						str = entry.getKey();
						mostOccure = entry.getValue();
					}
				}
			}
		}
		return str;
	}

	public static void main(String[] args) throws IOException {
		String path = Paths.get("").toAbsolutePath().toString();
		String fileName = path + "\\corpus-challenge4.txt";

		System.out.println("***Spell Checker Starting***");
		System.out.print("Building Dictionary . . .");

		createDictionary(fileName);
		
		System.out.println(" Dictionary done");
		System.out.println("Enter -1 to end the program");
		System.out.println("----------------------------");

		Scanner scan = new Scanner(System.in);

		int numLines = scan.nextInt();

		while (numLines != -1) {
			List<String> entryWrds = new ArrayList<String>();

			for (int i = 0; i < numLines; i++) {
				entryWrds.add(scan.next());
			}

			for (int i = 0; i < numLines; i++) {
				entryWrds.set(i, spellChk(entryWrds.get(i).toLowerCase()));
			}

			for (String fnl : entryWrds) {
				System.out.println(fnl);
			}

			System.out.println("----------------------------");

			numLines = scan.nextInt();
		}

		System.out.println("----------------------------");
		System.out.println("*****Spell Checker done*****");
		scan.close();

	}
}
