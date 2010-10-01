package org.amr.arabic;

/*
 *	Date : 25th of March 2008
 *	the class is Arabic string reshaper, this class is targeting Android platform
 *
 * 	By		: Ahmed Essam Naiem
 *  E-Mail 	: ahmed-essam@live.com
 *  Web		: www.ahmed-essam.com
 *  
 *  Updated Date : 20 of March 2009
 *  The class has been updated to include the Lam Alef Reshaping techniques
 *  
 *  By		: Amr Ismail gawish
 *  Email   : amr.gawish@gmail.com
 *  Web		: www.amr-gawish.com
 *  
 *  Updated : 8th of June 2009
 *  Adding comments and Announcing Open Source
 *  
 *  
 *  By		: Ahmed Essam Naiem
 *  E-Mail	: ahmed-essam@live.com
 *  Web		: www.ahmed-essam.com
 *  
 *  Update	: Sunday 7th of February 2010 
 *  Fixing the Harakat (Fatha, Dama, etc ...), still has bug in the 2nd character and last character correct character shape
 * */
import java.lang.String;
/**
 * This class is the main class that is responsible for Reshaping Arabic Word
 * The Reshaping Engine
 * It's role is to provide the right form of the word.
 * @author Ahmed Essam Naiem, Amr Ismail Gawish
 */
public class ArabicReshaper{
	/**
	 * The reshaped Word String
	 */
	private String _returnString;
 
 
	/**
	 * The Reshaped Word
	 * @return reshaped Word
	 */
	public String getReshapedWord(){
 
		return _returnString;
	}
 
	public static char DEFINED_CHARACTERS_ORGINAL_ALF_UPPER_MDD = 0x0622;
 
	public static char DEFINED_CHARACTERS_ORGINAL_ALF_UPPER_HAMAZA = 0x0623;
 
	public static char DEFINED_CHARACTERS_ORGINAL_ALF_LOWER_HAMAZA = 0x0625;
 
	public static char DEFINED_CHARACTERS_ORGINAL_ALF = 0x0627;
 
	public static char DEFINED_CHARACTERS_ORGINAL_LAM	=0x0644;
 
 
 
	/**
	 * The All Lam Forms in the [0] element
	 * With all its forms either at the end of the word, or in the middle of the word in [1][2] respectively
	 */
	public static char[][] LAM_ALEF_GLPHIES=
	{{15270,65270,65269},
	 {15271,65272,65271},
	 {1575, 65276,65275},
	 {1573, 65274,65273}
	};

	/**
	 * All Arabic Letters with It's different Forms
	 * [0] Element is the letter Alone
	 * [1][2][3][4] are all the forms of the Letter
	 * [5] is the number of Forms the Letter has
	 * and so on
	 */
	
	// The Array is sorted to be able to use the binary search, please don't miss with it.
	public static char[][] ARABIC_GLPHIES=
		   {{ 1569,65152,65163,65164,65152,3 } ,
			{ 1570,65153,65153,65154,65154,2 } ,
			{ 1571,65155,65155,65156,65156,2 } ,
			{ 1572,65157,65157,65158,65158,2 } ,
			{ 1573,65159,65159,65160,65160,2 } ,
			{ 1574,65161,65161,65162,65162,2 } ,
			{ 1575,65165,65165,65166,65166,2 } ,
			{ 1576,65167,65169,65170,65168,4 } ,
			{ 1577,65171,65171,65172,65172,2 } ,
			{ 1578,65173,65175,65176,65174,4 } ,
			{ 1579,65177,65179,65180,65178,4 } ,
			{ 1580,65181,65183,65184,65182,4 } ,
			{ 1581,65185,65187,65188,65186,4 } ,
			{ 1582,65189,65191,65192,65190,4 } ,
			{ 1583,65193,65193,65194,65194,2 } ,
			{ 1584,65195,65195,65196,65196,2 } ,
			{ 1585,65197,65197,65198,65198,2 } ,
			{ 1586,65199,65199,65200,65200,2 } ,
			{ 1587,65201,65203,65204,65202,4 } ,
			{ 1588,65205,65207,65208,65206,4 } ,
			{ 1589,65209,65211,65212,65210,4 } ,
			{ 1590,65213,65215,65216,65214,4 } ,
			{ 1591,65217,65219,65218,65220,4 } ,
			{ 1592,65221,65223,65222,65222,4 } ,
			{ 1593,65225,65227,65228,65226,4 } ,
			{ 1594,65229,65231,65232,65230,4 } ,
			{ 1601,65233,65235,65236,65234,4 } ,
			{ 1602,65237,65239,65240,65238,4 } ,
			{ 1603,65241,65243,65244,65242,4 } ,
			{ 1604,65245,65247,65248,65246,4 } ,
			{ 1605,65249,65251,65252,65250,4 } ,
			{ 1606,65253,65255,65256,65254,4 } ,
			{ 1607,65257,65259,65260,65258,4 } ,
			{ 1608,65261,65261,65262,65262,2 } ,
			{ 1609,65263,65263,65264,65264,2 } ,			
			{ 1610,65265,65267,65268,65266,4 } };
 
	/**
	 * Binary search for the character, it returns the index of the character in the array. 
	 * @param start, 	the start point for the binary search
	 * @param end, 		the end point for the binary search
	 * @param target, 	the character that is function looking for
	 * @return The index of the found character.
	 */
	private int findGlphy(int start,int end,char target){
		if(start<0)
			return -1;
		if(end>35)
			return -1;
		
		int middle = start + ((end - start)/2);
		if(middle<end)
			if(ARABIC_GLPHIES[middle+1][0]==target)
				return middle+1;
		if (ARABIC_GLPHIES[middle][0]==target)
			return middle;
		if(end-middle==1)
			return -1;
		if(ARABIC_GLPHIES[middle][0]<target)
			return findGlphy(middle, end, target);
		if(ARABIC_GLPHIES[middle][0]>target)
			return findGlphy(start, middle, target);
		return -1;
	}
	
 
	/**
	 * Searching for the letter and Get the right shape for the character depends on the location specified 
	 * @param target The character that needs to get its form
	 * @param location The location of the Form letter
	 * @return The letter with its right shape
	 */
	private char getReshapedGlphy(char target,int location){
		int found = findGlphy(0, 35, target);
		if(found==-1)
			return target;
		return ARABIC_GLPHIES[found][location];
	}
 
	/**
	 * The function should return the correct Glphy type for the caller, the function Escape any character like "7arakat"
	 * @param target 	The character, that needed 
	 * @param location 	The location of the character to be return its type
	 * @return the integer number indicated the Number of forms the Character has, return 2 otherwise
	 */
	private int getGlphyTypeBefore(String wholeTarget,int location){
		if(location==0)
			return 2;
		char target = wholeTarget.charAt(location);
		int foundGlphy = findGlphy(0,35,target);
		if(foundGlphy==-1){
			location--;
			return getGlphyTypeBefore(wholeTarget, location);
		}
		return ARABIC_GLPHIES[foundGlphy][5];		
	}
	
	/**
	 * Define which Character Type is This, that has 2,3 or 4 Forms variation?
	 * @param target The character, that needed 
	 * @return the integer number indicated the Number of forms the Character has, return 2 otherwise
	 */
	private int getGlphyType(String wholeTarget,int location){
		char target = wholeTarget.charAt(location);
		
		// Something Magical should happen here
		int n = findGlphy(0, 35, target);
		if(n==-1)
			return 2;
		return ARABIC_GLPHIES[n][5];
	}
 
 
	/**
	 * Get LamAlef right Character Presentation of the character
	 * @param candidateAlef The letter that is supposed to Alef
	 * @param candidateLam The letter that is supposed to Lam
	 * @param isEndOfWord Is those characters at the end of the Word, to get its right form 
	 * @return Reshaped character of the LamAlef
	 */
	private char getLamAlef(char candidateAlef,char candidateLam,boolean isEndOfWord){
		//The shift rate, depends if the the end of the word or not!
		int shiftRate = 1;
 
		//The reshaped Lam Alef
		char reshapedLamAlef=0;
 
		//Check if at the end of the word
		if(isEndOfWord)
			shiftRate++;
 
		//check if the Lam is matching the candidate Lam
		if((int)DEFINED_CHARACTERS_ORGINAL_LAM ==(int)candidateLam){
 
			//Check which Alef is matching after the Lam and get Its form
			if((int)candidateAlef ==(int)DEFINED_CHARACTERS_ORGINAL_ALF_UPPER_MDD){
				reshapedLamAlef = LAM_ALEF_GLPHIES[0][shiftRate];
			}
 
			if((int)candidateAlef ==(int)DEFINED_CHARACTERS_ORGINAL_ALF_UPPER_HAMAZA){
				reshapedLamAlef = LAM_ALEF_GLPHIES[1][shiftRate];
			}
 
			if((int)candidateAlef ==(int)DEFINED_CHARACTERS_ORGINAL_ALF_LOWER_HAMAZA){
				reshapedLamAlef = LAM_ALEF_GLPHIES[3][shiftRate];
			}
 
			if((int)candidateAlef ==(int)DEFINED_CHARACTERS_ORGINAL_ALF){
				reshapedLamAlef = LAM_ALEF_GLPHIES[2][shiftRate];
			}
		}
		//return the ReshapedLamAlef
		return reshapedLamAlef;
	}
 
 
	/**
	 * Constructor of the Class
	 * It doesn't support Alef Lam by Default
	 * @param unshapedWord The unShaped Word
	 */
	public ArabicReshaper(String unshapedWord){
		_returnString=reshapeIt(unshapedWord);
	}
 
 
	/**
	 * The Enhanced Arabic Reshaper Constructor with Lam Alef Support
	 * @param unshapedWord The unShaped Word
	 * @param supportAlefLam To check If to support AlefLam or Not
	 */
	public ArabicReshaper(String unshapedWord,boolean supportAlefLam){
		if(!supportAlefLam) {
			_returnString=reshapeIt(unshapedWord);
		}else {
			_returnString=reshapeItWithLamAlef(unshapedWord);
		}
	}
 
	/**
	 * Main Reshaping function, Doesn't Support LamAlef
	 * @param unshapedWord The unReshaped Word to Reshape
	 * @return The Reshaped Word without the LamAlef Support
	 */
	public String reshapeIt(String unshapedWord){
 
		//The reshaped Word to Return
		StringBuffer reshapedWord=new StringBuffer("");
 
		//The Word length
		int wordLength = unshapedWord.length();
 
		//The Word Letters
		char [] wordLetters = new char[wordLength];
 
		//Copy the unreshapedWord to the WordLetters Character Array
		unshapedWord.getChars(0, wordLength, wordLetters,0 );
 
 
		//for the first letter
		reshapedWord.append(getReshapedGlphy(wordLetters[0], 2));//2 is the Form when the Letter is at the start of the word
 
 
		//iteration from the second till the second to last
		for(int i=1;i<wordLength-1;i++){
			int beforeLast=i-1;
				//Check if the Letter Before Last has only 2 Forms, for the current Letter to be as a start for a new Word!
				if(getGlphyTypeBefore(unshapedWord,beforeLast)==2){ //checking if it's only has 2 shapes
					//If the letter has only 2 shapes, then it doesnt matter which position it is, It'll be always the second form
					reshapedWord.append(getReshapedGlphy(wordLetters[i], 2));
				}else {
					//Then it should be in the middle which should be placed in its right form [3]
					reshapedWord.append(getReshapedGlphy(wordLetters[i], 3));
				}
		}
 
		//check for the last letter Before last has 2 forms, that means that the last Letter will be alone.
		if(getGlphyTypeBefore(unshapedWord,wordLength-2)==2){
			//If the letter has only 2 shapes, then it doesnt matter which position it is, It'll be always the second form
			reshapedWord.append(getReshapedGlphy(wordLetters[wordLength-1], 1));
		}else {
			//Put the right form of the character, 4 for the last letter in the word
			reshapedWord.append(getReshapedGlphy(wordLetters[wordLength-1], 4));
		}
 
		//Return the ReshapedWord
		return reshapedWord.toString();
	}
 
 
	/**
	 * Main Reshaping Function, With LamAlef Support
	 * @param unshapedWord The UnReshaped Word
	 * @return The Shaped Word with Lam Alef Support
	 */
	public String reshapeItWithLamAlef(String unshapedWord){
 
		//The reshaped Word to Return
		StringBuffer reshapedWord=new StringBuffer("");
 
		//The Word length
		int wordLength = unshapedWord.length();
 
		//The Word Letters
		char [] wordLetters = new char[wordLength];
 
		//The reshaped Letters
		char [] reshapedLetters=new char[wordLength];
 
		//Indicator Character, to Tell that lam is exist
		char lamIndicator=43;//The '+' 
 
		//Copy the unreshapedWord to the WordLetters Character Array
		unshapedWord.getChars(0, wordLength, wordLetters,0 );
 
		//Check if the Word Length is 0, then return empty String
		if(wordLength==0){
			return "";
		}
 
		//Check if the Word length is 1, then return the Reshaped One letter, which is the same character of input
		if(wordLength==1){
			return getReshapedGlphy(wordLetters[0],1)+"";
		}
 
		//Check if the word length is 2, Check if the Word is LamAlef 
		if(wordLength==2){
			//Assign Candidate Lam
			char lam=wordLetters[0];
 
			//Assign Candidate Alef
			char alef=wordLetters[1];
 
			//Check if The word is Lam Alef.
			if(getLamAlef(alef, lam, true)>0){
				return (char)getLamAlef(alef,lam,true)+" ";
			}
 
		}
 
		//For the First Letter
		reshapedLetters[0]=getReshapedGlphy(wordLetters[0], 2);
 
		//The current Letter
		char currentLetter=wordLetters[0];
 
		/**
		 * The Main Iterator
		 */
 
		//Iterate over the word from the second character till the second to the last
		for(int i=1;i<wordLength-1;i++){
 
			//Check if the Letters are Lam Alef
			if(getLamAlef(wordLetters[i], currentLetter, true)>0){
				//Check if the Letter before the Lam is 2 Forms Letter, to Make the Lam Alef as its the end of the Word
				if(getGlphyTypeBefore(unshapedWord, i-2)==2){
 
					//Mark the letter of Lam as Lam Indicator
					reshapedLetters[i-1]=lamIndicator;
 
					//Assign Lam Alef to the Letter of Alef
					reshapedLetters[i]=(char)getLamAlef(wordLetters[i], currentLetter, true);
 
				}else{ //The Letter before the Lam is more than 2 Forms Letter
 
					//Mark the letter of Lam as Lam Indicator
					reshapedLetters[i-1]=lamIndicator;
 
					//Assign Lam Alef to the Letter of Alef
					reshapedLetters[i]=(char)getLamAlef(wordLetters[i], currentLetter, false);
				}
			}else{ //The Word doesn't have LamAlef
 
				int beforeLast=i-1;
 
				//Check if the Letter Before Last has only 2 Forms, for the current Letter to be as a start for a new Word!
				if(getGlphyTypeBefore(unshapedWord, beforeLast)==2){
 
					//If the letter has only 2 shapes, then it doesnt matter which position it is, It'll be always the second form
					reshapedLetters[i]=getReshapedGlphy(wordLetters[i], 2);
				}else{
 
					//Then it should be in the middle which should be placed in its right form [3]
					reshapedLetters[i]=getReshapedGlphy(wordLetters[i], 3);
				}
			}
			//Assign the CurrentLetter as the Word Letter
			currentLetter=wordLetters[i];
		}
 
 
		/**
		 * The Last Letters Check
		 */
 
		//Check if the Letters are Lam Alef
		if(getLamAlef(wordLetters[wordLength-1], wordLetters[wordLength-2], true)>0){
 
			//Check if the Letter before the Lam is 2 Forms Letter, to Make the Lam Alef as its the end of the Word
			if(getGlphyType(unshapedWord,wordLength-3)==2){ //check for the last letter
 
				//Mark the letter of Lam as Lam Indicator
				reshapedLetters[wordLength-2]=lamIndicator;
 
				//Assign Lam Alef to the Letter of Alef
				reshapedLetters[wordLength-1]=(char)getLamAlef(wordLetters[wordLength-1], wordLetters[wordLength-2], true);
			}else {
 
				//Mark the letter of Lam as Lam Indicator
				reshapedLetters[wordLength-2]=lamIndicator;
 
				//Assign Lam Alef to the Letter of Alef
				reshapedLetters[wordLength-1]=(char)getLamAlef(wordLetters[wordLength-1], wordLetters[wordLength-2], false);
			}
 
		}else { 
			//check for the last letter Before last has 2 forms, that means that the last Letter will be alone.
			if(getGlphyType(unshapedWord,wordLength-2)==2){
				//If the letter has only 2 shapes, then it doesn't matter which position it is, It'll be always the second form
				reshapedLetters[wordLength-1]=getReshapedGlphy(wordLetters[wordLength-1], 1);
			}else {
				//Put the right form of the character, 4 for the last letter in the word
				reshapedLetters[wordLength-1]=getReshapedGlphy(wordLetters[wordLength-1], 4);
			}
		}
 
		/**
		 * Assign the Final Results of Shaped Word
		 */
 
		//Iterate over the Reshaped Letters and remove the Lam Indicators
		for(int i=0;i<reshapedLetters.length;i++){
 
			//Check if the Letter is Lam Indicator
			if(reshapedLetters[i]!=lamIndicator)
				reshapedWord.append(reshapedLetters[i]);
		}
 
		//Return the Reshaped Word
		return reshapedWord.toString();
	}	
}
