
package epogees.model;

import epogees.ui.*;
import epogees.generation.*;
import epogees.model.language.*;
import epogees.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class PhonemeModel {

    // keys: plaintext words, content: Word objects
    Hashtable allKnownPronunciations = new Hashtable();

    // holds list of references to words in allKnownPronunciations hashtable, for easy access to random word and to track size
    List allWords = new ArrayList();

    // used for random word generation
    Random random = new Random(System.currentTimeMillis());
    
    
//    public static void main(String[] args) {
//
//        PhonemeModel pm = new PhonemeModel();
//        pm.readPhonemeModel( "./data/cmudict/cmudict.0.7a");
//        System.out.println( pm.getPhonemesForFile("./data/shakespeare/shakespeare-sonnet1.txt") );
//        //System.out.println( pm.findWordsWithoutPhonemes( "./data/shakespeare/shakespeare-sonnetsAll.txt" ) );
//    }

    public int getSize() {
        return allWords.size();
    }

    // see if the phoneme model contains an entry for the given word
    public boolean hasPhonemes( String query ) {

        if ( allKnownPronunciations.containsKey(query) ) {
            return true;
        }
        
        return false;
    }
    
    // given the relative location of an input file, read a phoneme model from that file, put it into a Hashtable
    // skips comments.  multiple pronounciations are keyed as eg "ABSTAINED(1)".
    public void readPhonemeModel( String inputFileLocation  ) {

        BufferedReader inputStream = null;
        String currentLine = new String();

        try {
            inputStream = new BufferedReader(new FileReader( inputFileLocation  ) );

            while ( (currentLine = inputStream.readLine()) != null ) {

                if ( ! currentLine.startsWith(";") ) {

                    String[] aA = currentLine.split("  ");

                    allKnownPronunciations.put( aA[0].toLowerCase(), new Word( aA[0].toLowerCase(), aA[1]) );
                    allWords.add( aA[0].toLowerCase() );
                    //phonemes.put(aA[0].toLowerCase(), aA[1]);
                }

            }
        } catch (Exception e) {
            System.out.println( "probable misformat in: " + currentLine);
            e.printStackTrace();
        }
    }

    public void readPhonemeList( List fileContents, LanguageModel lm  ) {

        String currentLine = new String();

        try {
            for ( int x=0; x< fileContents.size(); x++ ) {
            
                currentLine = fileContents.get(x).toString();

                if ( ! currentLine.startsWith(";") && ! currentLine.equals("") ) {

                    String[] aA = currentLine.split("  ");
                    
                    String word = aA[0].replaceAll("\\W", "").trim().toLowerCase();

                    if ( lm.isInLanguageModel(word) ) {
                        allKnownPronunciations.put(aA[0].toLowerCase(), new Word(aA[0].toLowerCase(), aA[1]));
                        allWords.add(aA[0].toLowerCase());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println( "probable misformat in: |" + currentLine + "|");
            e.printStackTrace();
        }
    }

    // get the text for all words in a file, in plain format
    public String getTextFromFile( String inputFileLocation  ) {

        BufferedReader inputStream = null;
        StringBuffer toReturn = new StringBuffer();

        try {
            inputStream = new BufferedReader(new FileReader( inputFileLocation  ) );
            String currentLine;

            while ( (currentLine = inputStream.readLine()) != null ) {
                //currentLine = currentLine.trim();
                toReturn.append( currentLine );
                toReturn.append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return toReturn.toString();
    }

    // get the text for all words in a file, in html format
    public String getHtmlTextFromFile( String inputFileLocation  ) {

        BufferedReader inputStream = null;
        StringBuffer toReturn = new StringBuffer();
        toReturn.append("<html>\n");
        
        try {
            inputStream = new BufferedReader(new FileReader( inputFileLocation  ) );
            String currentLine;

            while ( (currentLine = inputStream.readLine()) != null ) {
                //currentLine = currentLine.trim();
                if (!currentLine.startsWith("//") && !currentLine.equals("")) {
                    toReturn.append(currentLine);
                    toReturn.append("<br>\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        toReturn.append("</html>");

        return toReturn.toString();
    }

    // get the phonemes for all words in a file
    public String getPhonemesForFile( String inputFileLocation  ) {

        BufferedReader inputStream = null;
        StringBuffer toReturn = new StringBuffer();
        toReturn.append("<html>\n");

        try {
            inputStream = new BufferedReader(new FileReader( inputFileLocation  ) );
            String currentLine;

            while ( (currentLine = inputStream.readLine()) != null ) {

                currentLine = currentLine.trim();

                if ( ! currentLine.startsWith("//") && ! currentLine.equals("") ) {

                    currentLine = replacePunctuation(currentLine);

                    // get all the tokens
                    String[] aA = currentLine.split(" ");

                    // look up the phoneme string for each token, add it to return buffer
                    for (int x = 0; x < aA.length; x++) {

                        String toLookup = aA[x];
                        toLookup = toLookup.trim();
                        toLookup = toLookup.toLowerCase();


                        if (allKnownPronunciations.containsKey(toLookup)) {
                            if ( x!=0 ) {
                                //toReturn.append(" [ ] ");
                                toReturn.append(" _ ");
                            }
                            toReturn.append(phonemesForWord(toLookup));
                        } else {
                            if ( isPunctuation(toLookup) ) {
                                //toReturn.append(" [ ");
                            } else {
                                toReturn.append(" _ ");
                                //toReturn.append(" [ ] ");
                            }
                            toReturn.append(toLookup);
                            if ( isPunctuation(toLookup) ) {
                                //toReturn.append(" ] ");
                            }
                        }
                    }
                    toReturn.append("<br>\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        toReturn.append("</html>");

        return toReturn.toString();
    }

    // get the phonemes for vowels only, for all words in a file
    public String getPhonemesVowelsOnlyForFile( String inputFileLocation  ) {

        BufferedReader inputStream = null;
        StringBuffer toReturn = new StringBuffer();
        toReturn.append("<html>\n");

        try {
            inputStream = new BufferedReader(new FileReader( inputFileLocation  ) );
            String currentLine;

            while ( (currentLine = inputStream.readLine()) != null ) {

                currentLine = currentLine.trim();

                if ( ! currentLine.startsWith("//") && ! currentLine.equals("") ) {

                    currentLine = replacePunctuation(currentLine);

                    // get all the tokens
                    String[] aA = currentLine.split(" ");

                    // look up the phoneme string for each token, add it to return buffer
                    for (int x = 0; x < aA.length; x++) {

                        String toLookup = aA[x];
                        toLookup = toLookup.trim();
                        toLookup = toLookup.toLowerCase();


                        if (allKnownPronunciations.containsKey(toLookup)) {
                            if ( x!=0 ) {
                                //toReturn.append(" [ ] ");
                                toReturn.append(" _ ");
                            }
                            toReturn.append(phonemesForWordVowelsOnly(toLookup));

                        } else {
                            if ( isPunctuation(toLookup) ) {
                                //toReturn.append(" [ ");
                            } else {
                                toReturn.append(" _ ");
                                //toReturn.append(" [ ] ");
                            }
                            toReturn.append(toLookup);
                            if ( isPunctuation(toLookup) ) {
                                //toReturn.append(" ] ");
                            }
                        }
                    }
                    toReturn.append("<br>\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        toReturn.append("</html>");

        return toReturn.toString();
    }


    // get a random sequence of words of at least numVowelPhonemes syllabes
    public String getRandomWordSequence( int numVowelPhonemes ) {
        StringBuffer toReturn = new StringBuffer();
        StringBuffer vowelPhonemesFound = new StringBuffer();
        
        int numPhonemesInSequence = 0;
        String wordFound = new String();
        
        while ( numPhonemesInSequence < numVowelPhonemes ) {
            
            wordFound = getRandomWord();
            toReturn.append( wordFound + " " );
            numPhonemesInSequence += numPhonemesForWordAccentedVowelsOnly( wordFound );
            vowelPhonemesFound.append( phonemesForWord(wordFound) + " - ");
                    
//            System.out.print( wordFound + " " );
        }  

//        System.out.print( "\t" + vowelPhonemesFound );
//        System.out.print( numPhonemesInSequence );
//        System.out.println();
                
        return toReturn.toString();
    }
    
    // get a random word, non-weighed
    public String getRandomWord() {

        int n = allWords.size();
        int i = random.nextInt(n);
      
        return allWords.get(i).toString();
    }
    
    
    // for a given line and phoneme, find number of times phoneme is in that line
    public int phonemesOfType( String line, String phoneme ) {

        int toReturn = 0;
        
        try {

            String[] words = line.split(" ");

            for (int y = 0; y < words.length; y++) {
                
                if ( allKnownPronunciations.containsKey(words[y].toLowerCase())) {
                    
                    Word w = (Word) (allKnownPronunciations.get(words[y].toLowerCase()));

                    String s = w.getPhonemesPlain();

                    String[] aA = s.split(" ");
                    for (int x = 0; x < aA.length; x++) {
                        if (aA[x].toLowerCase().matches(".*" + phoneme + ".*")) {
                            toReturn++;
                        }
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println( "line and phoneme: " + line + " " + phoneme );
        }


        return toReturn;
    }
    
    // for a given line (of phonemes) and phoneme, find number of times phoneme is in that line
    public int phonemesInPhonemeLine( String phonemeLine, String testPhoneme    ) {

        int toReturn = 0;
        
        try {
            //System.out.println( "phoneme line: " + phonemeLine );
            // trim extra spaces, remove digits and commas
            phonemeLine = phonemeLine.trim();
            phonemeLine = phonemeLine.replaceAll(" +", " ");
            phonemeLine = phonemeLine.replaceAll("\\d", "");
            phonemeLine = phonemeLine.replaceAll(",", "");
            
            //System.out.println( "phoneme line: " + phonemeLine );
            
            // looking at a line of phonemes
            String[] phonemes = phonemeLine.split(" ");

            // looking at each individual phoneme
            for (int y = 0; y < phonemes.length; y++) {

                //System.out.println("looking at: " + phonemes[y].toLowerCase() + " and " + testPhoneme.toLowerCase() );
                
                if (phonemes[y].toLowerCase().matches(".*" + testPhoneme.toLowerCase() + ".*")) {
                    toReturn++;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println( "line and phoneme: " + phonemeLine + " " + testPhoneme );
        }

        return toReturn;
    }
    
    // for a given line (of phonemes) and phoneme, find number of times phoneme is in that line
    public int consonantsInPhonemeLineStartingWords( String phonemeLine, String testPhoneme    ) {

        int toReturn = 0;
        
        try {
            //System.out.println( "phoneme line: " + phonemeLine );
            // a line looks like:
            // dh AE1 t,  OW1 n,  hh IY1 r s EY2,  dh AE1 t,  r AE1 ng k,  d EH1 d,  IH1 ng k

            // trim extra spaces, remove digits and commas
            phonemeLine = phonemeLine.trim();
            phonemeLine = phonemeLine.replaceAll(" +", " ");
            phonemeLine = phonemeLine.replaceAll("\\d", "");
            //phonemeLine = phonemeLine.replaceAll(",", "");
            
            //System.out.println( "phoneme line: " + phonemeLine );
            
            // looking at a line of phonemes
            String[] phonemeWords = phonemeLine.split(",");

            // looking at each individual phonemeWord.  each phonemeWord looks like: dh AE1 t
            for (int y = 0; y < phonemeWords.length; y++) {
                
                String currentWord = phonemeWords[y].trim();
                //System.out.println( "          |" + currentWord + "|");
                String[] individualPhonemes = currentWord.split(" ");

                if ( individualPhonemes.length > 0 ) {
                    String firstPhoneme = individualPhonemes[0];
                    //System.out.println( "          first phoneme is: " + firstPhoneme );

                    if (firstPhoneme.toLowerCase().equals( testPhoneme.toLowerCase() ) && isConsonant(firstPhoneme)) {
                        toReturn++;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println( "line and phoneme: " + phonemeLine + " " + testPhoneme );
        }

        return toReturn;
    }
    
    // for a given word, get a list of phonemes in it
    public String phonemesForWord( String word ) {

        StringBuffer toReturn = new StringBuffer();
        Word w = (Word)(allKnownPronunciations.get( word.toLowerCase() ));

        if ( w != null ) {
            String s = w.getPhonemesPlain();
            //String s = w.getPhonemesFormatted();

            String[] aA = s.split(" ");
            for (int x = 0; x < aA.length; x++) {
                if (isConsonant(aA[x])) {
                    toReturn.append(" " + aA[x].toLowerCase());
                } else {
                    toReturn.append(" " + aA[x]);
                }
            }

        }
        
        return toReturn.toString();
        //return w.getPhonemesPlain();
        //return w.getPhonemesFormatted();
        //return phonemes.get( word.toLowerCase() ).toString();
    }

    // for a given word, find the number of phonemes in it
    public int numPhonemesForWord( String word ) {
        Word w = (Word)(allKnownPronunciations.get( word.toLowerCase() ));

        //String s = w.getPhonemesFormatted();
        String s = w.getPhonemesPlain();
        
        String[] aA = s.split(" ");
        
        return aA.length;
    }
    
    
    // for a given word, find the phonemes - vowels only
    public String phonemesForWordVowelsOnly( String word ) {
        Word w = (Word)(allKnownPronunciations.get( word.toLowerCase() ));
        //String s = w.getPhonemesFormatted();
        String s = w.getPhonemesPlain();
        StringBuffer toReturn = new StringBuffer();
        
        String[] aA = s.split(" ");
        for ( int x=0; x<aA.length; x++ ){
            if ( isConsonant(aA[x]) ) {
                toReturn.append(" -");
            } else {
                toReturn.append( " " + aA[x] );
            }
        }
        
        return toReturn.toString();
        //return w.getPhonemesPlain();
        //return w.getPhonemesFormatted();
        //return phonemes.get( word.toLowerCase() ).toString();
    }


    // for a given word, find the number of phonemes - vowels only
    public int numPhonemesForWordVowelsOnly( String word ) {
        Word w = (Word)(allKnownPronunciations.get( word.toLowerCase() ));
        //String s = w.getPhonemesFormatted();
        String s = w.getPhonemesPlain();
        int vowelPhonemesCount = 0;
        
        try {
            String[] aA = s.split(" ");
            for (int x = 0; x < aA.length; x++) {
                if (!isConsonant(aA[x])) {
                    vowelPhonemesCount++;
                }
            }
            
        } catch ( Exception e ) {
            e.printStackTrace();
            System.out.println( "word was: " + word);
        }
        
        return vowelPhonemesCount;
    }

    // for a given word, find the number of phonemes - accented vowels only
    public int numPhonemesForWordAccentedVowelsOnly( String word ) {

        int vowelPhonemesCount = -1;

        try {

            Word w = (Word) (allKnownPronunciations.get(word.toLowerCase()));
            if ( w != null ) {
                vowelPhonemesCount = 0;
                
                String s = w.getPhonemesPlain();

                String[] aA = s.split(" ");
                for (int x = 0; x < aA.length; x++) {
                    if (!isConsonant(aA[x]) && aA[x].matches(".*1.*")) {
                        vowelPhonemesCount++;
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("word was: " + word);
        }
        
        return vowelPhonemesCount;
    }
    
    // for a given word, find the phonemes - vowels only
    public int numPhonemesForLineAccentedVowelsOnly( String line ) {

        int vowelPhonemesCount = 0;

        String[] aA = line.split(" ");
        for (int x = 0; x < aA.length; x++) {

            vowelPhonemesCount += numPhonemesForWordAccentedVowelsOnly( aA[x] );
        }
        
        return vowelPhonemesCount;
    }

    // for a given line, find the phonemes 
    public String phonemesForLine( String line ) {

        StringBuffer toReturn = new StringBuffer();
        
        String[] aA = line.split(" ");
        for (int x = 0; x < aA.length; x++) {

            if ( x != 0 ) {
                toReturn.append( ", " );
            }
            toReturn.append( this.phonemesForWord(aA[x]) );
            //vowelPhonemesCount += numPhonemesForWordAccentedVowelsOnly( aA[x] );
        }
        
        return toReturn.toString();
    }
    
    
    public String findWordsFromPhonemes( String phonemes ) {
    //public String findWordsFromPhonemes( List phonemes ) {
        StringBuffer toReturn = new StringBuffer();

//        StringBuffer phonemesToFind = new StringBuffer();
//        for (int x = 0; x < phonemes.size(); x++) {
//            phonemesToFind.append( phonemes.get(x).toString() );
//        }
//        String phonemesToMatch = ".*" + phonemesToFind.toString() + ".*";
        String phonemesToMatch = ".*" + phonemes + ".*";
        
        Enumeration e = allKnownPronunciations.elements();
        while ( e.hasMoreElements() ) {
            Word currentWord = ((Word)e.nextElement());
            String ph = currentWord.getPhonemesPlain().toLowerCase().trim();
            ph = ph.replaceAll( "\\d", "");
            
            if ( ph.matches( phonemesToMatch ) ) {
                toReturn.append( currentWord.getText() + "\t" + ph + "\n" );
            }
            
//            String[] aPhonemes = s.split(" ");
//            boolean isMatch = true;            
//            try {
//                for (int x = 0; x < phonemes.size(); x++) {
//                    String a = phonemes.get(x).toString();
//                    String b = aPhonemes[x];
//                    if (! a.equalsIgnoreCase(b)) {
//                        isMatch = false;
//                    }
//                }
//
//            } catch (Exception exception) {
//                isMatch = false;
//            }
//            if ( isMatch ) {
//                toReturn.append( currentWord.getText() + "\t" + s + "\n" );
//            }
        }
        return toReturn.toString();
    }
    

    // prints phoneme model to applet
    public void printPhonemeModelToApplet( EpogeesApplet applet, PhonemeEvaluation pe, LanguageModel lm ) {
        // iterate through each vowel phoneme of single rhyme hashtable
        applet.printModel("\nPHONEME MODEL: \n\n");

        ArrayList alp = new ArrayList(allKnownPronunciations.keySet());
        Collections.sort( alp );
        //Iterator i = allKnownPronunciations.keySet().iterator();
        Iterator i = alp.iterator();
        while ( i.hasNext() ) {
            String phonemeHashtableItem = i.next().toString();
            String wordText = ((Word)allKnownPronunciations.get(phonemeHashtableItem)).getText();
            String wordPronunciation = ((Word)allKnownPronunciations.get(phonemeHashtableItem)).getPhonemesPlain();

            applet.printModel( "\n" + wordText + " - " + wordPronunciation + "\n");
            
        }

        applet.printModel( getMissingPhonemes( pe, lm ) );
    }    


    public String getMissingPhonemes( PhonemeEvaluation pe, LanguageModel lm ) {

        StringBuffer toReturn = new StringBuffer();
        toReturn.append("\n\n\nMISSING PHONEMES: \n\n\n");

        List allWordsInModel = lm.getListOfAllWords();

        Collections.sort( allWordsInModel);
        Iterator i = allWordsInModel.iterator();
        while ( i.hasNext() ) {
            String word = i.next().toString();
            String phonemes = pe.phonemesForLine(word);
            if ( phonemes.equals("") ) {
                //System.out.println( word + " |" + phonemes + "|");
                //System.out.println( word );
                toReturn.append( word + "\n\n" );
            }
        }

        return toReturn.toString();
    }


    
    // Punctuation-related

    // given a line, put a space before certain kinds of punctuation, to make punctuations their own tokens
    public String replacePunctuation( String currentLine ) {
        currentLine = currentLine.replaceAll(",", " ,");
        currentLine = currentLine.replaceAll(":", " :");
        currentLine = currentLine.replaceAll(";", " ;");
        currentLine = currentLine.replaceAll("\\?", " ?");
        currentLine = currentLine.replaceAll("!", " !");
        currentLine = currentLine.replaceAll("\\.", " .");
        return currentLine;
    }

    // see if a given string is only punctuation
    public boolean isPunctuation( String s ) {
        if ( s.equals(",") || s.equals(":") || s.equals(";") || s.equals("?") || s.equals("!") || s.equals(".") ) {
            return true;
        }
        return false;
    }

    // see if a given string is only a phoneme consonant
    public boolean isConsonant( String s1 ) {
        String s = s1.toUpperCase();
        if ( s.equals("B") || s.equals("CH") || s.equals("D") || s.equals("DH") || s.equals("F") || s.equals("G") 
                || s.equals("HH") || s.equals("JH") || s.equals("K") || s.equals("L") || s.equals("M") 
                || s.equals("N") || s.equals("NG") || s.equals("P") || s.equals("R") || s.equals("S") 
                || s.equals("SH") || s.equals("T") || s.equals("TH") || s.equals("V") || s.equals("W") 
                || s.equals("Y") || s.equals("Z") || s.equals("ZH") 
                ) {
            return true;
        }
        return false;
        
    }
    
// would no longer work - phoneme model only reads words with pronunciations when loaded
    
    // given a file location, find all the words in that file that do not have a phoneme model
//    public String findWordsWithoutPhonemes( String inputFileLocation  ) {
//        BufferedReader inputStream = null;
//        StringBuffer toReturn = new StringBuffer();
//        Hashtable uniqueWords = new Hashtable();
//
//        try {
//            inputStream = new BufferedReader(new FileReader( inputFileLocation  ) );
//            String currentLine;
//
//            // make a hashtable of all keys not in the phoneme model
//            while ( (currentLine = inputStream.readLine()) != null ) {
//                currentLine = currentLine.trim();
//
//                if ( ! currentLine.startsWith("//") && ! currentLine.equals("") ) {
//
//                    currentLine = replacePunctuation(currentLine);
//
//                    // get all the tokens
//                    String[] aA = currentLine.split(" ");
//
//                    for (int x = 0; x < aA.length; x++) {
//
//                        String toLookup = aA[x];
//                        toLookup = toLookup.trim();
//                        toLookup = toLookup.toLowerCase();
//
//                        if (!allKnownPronunciations.containsKey(toLookup)) {
//
//                            if ( ! uniqueWords.containsKey(toLookup) && ! isPunctuation( toLookup ) ) {
//                                uniqueWords.put(toLookup, "1");
//                            }
//                        }
//                    }
//                }
//            }
//
//            // make a newline-delimited string of all keys in hashtable
//            Enumeration allKeys = uniqueWords.keys();
//            while ( allKeys.hasMoreElements() ) {
//                String aKey = allKeys.nextElement().toString();
//                toReturn.append( aKey.toUpperCase() );
//                toReturn.append("\n");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return toReturn.toString();
//    }
    
}
