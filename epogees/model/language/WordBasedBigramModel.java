
package epogees.model.language;

import epogees.ui.*;
import epogees.generation.*;
import epogees.model.*;
import epogees.util.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;


public class WordBasedBigramModel extends LanguageModel {

    // the tag used for the start state of a line
    static final String STARTWORD = "*START*";
    // the hasthable of possible first words
    Hashtable startWords = new Hashtable();    
    // probability distribution of possible starting tags
    ProbabilityDistribution startWordsDistribution;
    
    // hashtable of the bigrams.  For "when I", keys is Strings (when), values is BigramData ("I" and count for "when I")
    Hashtable firstTokens = new Hashtable();

    
    // hashtable of all words in language model
    // keys: words (Strings), values: Hashtable of previous words (value: word (String), key: "1")
    Hashtable allWordsInModel = new Hashtable();


    // whether it's OK to break strict ngram model when looking for rhyme
    boolean okToBreakNgramForRhyme = true;
    
    // whether it's OK to break strict ngram model when searching
    boolean okToBreakNgramForSearch = true;

    // used for random word generation
    Random random = new Random(System.currentTimeMillis());

    // limit to number of times to iterate do-loops while searching for a line 
    // (usually only needed when searching for lines without phoneme models)
    int limitSearch = 50;
    
    // whether the model should use periods, exclamation marks, question marks, commas, semicolons and colons
    boolean usePunctuation = true;


    public void changeUsePunctuation() {
        if ( usePunctuation ) {
            usePunctuation = false;
        } else {
            usePunctuation = true;
        }
    }

    public boolean getUsePunctuation() {
        return usePunctuation;
    }

    public void setUsePunctuation( boolean b ) {
        usePunctuation = b;
    }

    class BigramData {
        // key: token (String), value: count (Integer)
        Hashtable secondTokens = new Hashtable();
        ProbabilityDistribution secondTokensDistribution;

        public void addSecondToken( String toAdd ) {
            if ( secondTokens.containsKey(toAdd) ) {
                incrementCount(toAdd);
            } else {
                secondTokens.put(toAdd, new Integer(1));                
            }
        }

        public void incrementCount( String secondTokenToIncrement ) {
            int count = ((Integer) secondTokens.get(secondTokenToIncrement)).intValue();
            count++;        
            secondTokens.put( secondTokenToIncrement, new Integer(count));
        }
        
        public void makeSecondTokenDistribution() {
            secondTokensDistribution = new ProbabilityDistribution(secondTokens);
        }

        public String getRandomSecondToken() {
             return secondTokensDistribution.getItemFromWeighedDistribution();
        }
        
        public String secondTokensAndCounts() {
            StringBuffer toReturn = new StringBuffer();
            
            ArrayList alm = new ArrayList(secondTokens.keySet());
            Collections.sort(alm);
            Iterator allKeys = alm.iterator();
            while (allKeys.hasNext()) {
                String aKey = allKeys.next().toString();
            
                toReturn.append( " " + aKey );
                toReturn.append( " " + ((Integer) secondTokens.get(aKey)).intValue() + ", " );
            }
            
            return toReturn.toString();
        }

        public int getTotalBigramCount() {
            int toReturn = 0;

            ArrayList alm = new ArrayList(secondTokens.keySet());
            Collections.sort(alm);
            Iterator allKeys = alm.iterator();
            while (allKeys.hasNext()) {
                String aKey = allKeys.next().toString();

                toReturn += ((Integer)secondTokens.get(aKey)).intValue();
            }

            return toReturn;

        }
    }

    // reading file
    public void readModelList(List fileContents) {

        String currentLine;
    
        try {            
            for ( int x=0; x< fileContents.size(); x++ ) {
                
                currentLine = fileContents.get(x).toString();
                currentLine = currentLine.replaceAll("--", " ");
                currentLine = currentLine.trim();
                
                if ( ! currentLine.startsWith("//") && ! currentLine.equals("") ) {

                    //System.out.println( "|" + currentLine + "|");
                    // current line should look like: That thereby beauty's rose might never die,
                    String previousWord = STARTWORD;

                    // handle punctuation, keeping some
                    currentLine = handlePunctuationInLine(currentLine);
                    
                    String[] tokens = currentLine.split(" ");
                    
                    for ( int i=0; i<tokens.length; i++ ) {
                        String currentWord = tokens[i];
                        currentWord = handlePunctuationInWord( currentWord );

                        // if we didn't end up removing all punctuation completely, proceed
                        if ( ! currentWord.equals("") ) {

                            // add to 'all words in model' hashtable (with previous token)
                            // if already there, then add to, not replace
                            if ( allWordsInModel.containsKey(currentWord) ) {
                                Hashtable t = (Hashtable) allWordsInModel.get(currentWord);

                                if (t.containsKey(previousWord) ) {
                                    // if the token is already there, increment it
                                    int sc = ((Integer) t.get(previousWord)).intValue();
                                    sc++;
                                    t.put(previousWord, new Integer(sc));
                                } else {
                                    // the token is not already there, add it
                                    t.put(previousWord, new Integer(1));
                                }
                                allWordsInModel.put(currentWord, t);                                 
                                
                            } else {
                                Hashtable t = new Hashtable();
                                t.put(previousWord, new Integer(1));
                                allWordsInModel.put(currentWord, t); 
                            }

                            if (previousWord.equalsIgnoreCase(STARTWORD)) {
                                // its a start tag
                                if (startWords.containsKey(currentWord)) {
                                    // if the token is already there, increment it
                                    int sc = ((Integer) startWords.get(currentWord)).intValue();
                                    sc++;
                                    startWords.put(currentWord, new Integer(sc));
                                } else {
                                    // the token is not already there, add it
                                    startWords.put(currentWord, new Integer(1));
                                }

                            } else if (firstTokens.containsKey(previousWord)) {
                                // the previous token has already been seen, and its not a start tag
                                // add or increment the second token
                                BigramData bg = (BigramData) firstTokens.get(previousWord);
                                bg.addSecondToken(currentWord);

                            } else {
                                // the first token has not yet been seen, and it is not a start tag
                                // create the first and second hashtables
                                //System.out.println("putting: " + previousWord + " " + currentWord);
                                BigramData bg = new BigramData();
                                bg.addSecondToken(currentWord);
                                firstTokens.put(previousWord, bg);
                            }
                            previousWord = currentWord;


                        }

                        
                    }
                    
                    
                }
            } 
            // create probability distribution for starting tags 
            startWordsDistribution = new ProbabilityDistribution(startWords);
            // create probability distribution for each secondToken
            Enumeration firstTokensKeys = firstTokens.keys();
            while ( firstTokensKeys.hasMoreElements() ) {
                String aKey = firstTokensKeys.nextElement().toString();
                BigramData bg = (BigramData)firstTokens.get(aKey);
                bg.makeSecondTokenDistribution();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    // used for reading file
    public String handlePunctuationInLine ( String currentLine ) {

        currentLine = currentLine.replaceAll(" +", " ");
        currentLine = currentLine.toLowerCase();
        // some punctuation you want to keep, some you don't.

        if ( usePunctuation ) {
            currentLine = currentLine.replaceAll(",", " COMMAPUNCTUATION");
            currentLine = currentLine.replaceAll(";", " SEMICOLONPUNCTUATION");
            currentLine = currentLine.replaceAll(":", " COLONPUNCTUATION");
            currentLine = currentLine.replaceAll("\\.", " PERIODPUNCTUATION");
            currentLine = currentLine.replaceAll("\\?", " QUESTIONMARKPUNCTUATION");
            currentLine = currentLine.replaceAll("!", " EXCLAMATIONMARKPUNCTUATION");

        } else {
            currentLine = currentLine.replaceAll(",", "");
            currentLine = currentLine.replaceAll(";", "");
            currentLine = currentLine.replaceAll(":", "");
            currentLine = currentLine.replaceAll("\\.", "");
            currentLine = currentLine.replaceAll("\\?", "");
            currentLine = currentLine.replaceAll("!", "");
        }
        currentLine = currentLine.replaceAll("\\(", "");
        currentLine = currentLine.replaceAll("\\)", "");
        currentLine = currentLine.replaceAll("\\\"", "");
        currentLine = currentLine.replaceAll("''", "");

        return currentLine;
    }

    // used for reading file
    public String handlePunctuationInWord ( String currentWord ) {

        // may lose some |th'| and |'fore|
        currentWord = currentWord.replaceAll("^'", "");
        currentWord = currentWord.replaceAll("'$", "");

        // try to replace weird punctuation you haven't seen before
        // note: we may lose info here
        // if it's a non-word character, but not a character we want...
        if (currentWord.matches(".*\\W.*") && 
                 ! currentWord.matches(".*'.*") && ! currentWord.matches(".*-.*") && ! currentWord.matches(".*\\..*") &&
                 ! currentWord.matches(".*\\!.*") && ! currentWord.matches(".*\\?.*") ) {
            System.out.println(" replacing punctuation in: |" + currentWord + "|");
            currentWord = currentWord.replaceAll("\\W", "");
            System.out.println(" now: |" + currentWord + "|");
        }
        // I hate poems with lowercase "i"s
        if (currentWord.equals("i")) {
            currentWord = "I";
        } else if (currentWord.equals("i'm")) {
            currentWord = "I'm";
        } else if (currentWord.equals("i'd")) {
            currentWord = "I'd";
        } else if (currentWord.equals("i'll")) {
            currentWord = "I'll";
        } else if (currentWord.equals("i've")) {
            currentWord = "I've";
        }

        return currentWord;
    }
    
    // given a token, find a next token in the n-gram
    public String getSecondTokenGivenFirstToken ( String firstToken ) {

        if ( firstTokens.containsKey(firstToken) ) {
            BigramData bg = (BigramData) firstTokens.get(firstToken);

            //System.out.println(" ECCE: " + bg.getRandomSecondToken());

            return bg.getRandomSecondToken();            
        }
        // if there is no existing bigram, start over
        String s = startWordsDistribution.getItemFromWeighedDistribution();
        System.out.println(" no token following " + firstToken + " so returning " + s + " instead");
        return s;
    }

    // check whether a given word is anywhere in the language model
    public boolean isInLanguageModel(String word) {

        return this.allWordsInModel.containsKey(word);
    }

    
    // generate a line beginning from start tags
    public String generateLine(PhonemeEvaluation pe, int numberOfAccentedVowelPhonemes,
            boolean estimateMissing, RhymeModel rm, String wordToRhyme, boolean isFirstInPair, String enjambment) {

        StringBuffer toReturn = new StringBuffer();
        // number of phonemes currently in line
        int runningPhonemes = 0;
        // last word in line
        String lastWord = new String();
        // used when replacing the last word
        String newLastWord = "";
        // latest word found
        String latestWord = new String();
        // phoneme count for first word
        int phonemeCount = -1;
        int firstWordTried = 0;
        int subsequentWordTried = 0;

        // get starting word, make sure it's OK
        do {
            // get starting word
            if ( enjambment.equals("")) {
                latestWord = startWordsDistribution.getItemFromWeighedDistribution();
            } else {

                latestWord = getSecondTokenGivenFirstToken(enjambment);
            }
            phonemeCount = pe.countStressedVowels(latestWord);

            if (phonemeCount == -1 && estimateMissing) {
                // guess at number of stressed vowels in word
                if ( latestWord.matches(".*PUNCTUATION.*") ) {
                    phonemeCount = 0;
                } else {
                    phonemeCount = 1;
                }
            } else if (phonemeCount == -1 && !estimateMissing) {
                // word has no phoneme value and we don't want to guess
                // we'll try 10 times (in case there are no words with phoneme values in startWord)
                System.out.println( "no phoneme model found for proposed first word ");
            }
            firstWordTried++;

        } while ( phonemeCount == -1 && firstWordTried < limitSearch );
        toReturn.append(latestWord);
        runningPhonemes = phonemeCount;
//        System.out.println("starting line: " + toReturn.toString() + pe.phonemesForLine( toReturn.toString() ) );

        do {
            // get a word
            String candidateWord = getSecondTokenGivenFirstToken(latestWord);
            int candidatePhonemes = pe.countStressedVowels(candidateWord);
            subsequentWordTried++;

//            System.out.println( "first: " + latestWord );
//            System.out.println( "candidate: " + candidateWord );
            
            if ( candidatePhonemes == -1 && estimateMissing ) {
                // guess at number of stressed vowels in word
                //candidatePhonemes = 1;
                // guess at number of stressed vowels in word
                if ( candidateWord.matches(".*PUNCTUATION.*") ) {
                    candidatePhonemes = 0;
                } else {
                    candidatePhonemes = 1;
                }
            } else if ( candidatePhonemes == -1 && ! estimateMissing ) {
                // word has no phoneme value and we don't want to guess, so:
                // give it a very large value so it fails the checks below
                candidatePhonemes = 999;                
            }
                    
//            System.out.println("candidatePhonemes: " + candidatePhonemes );
//            System.out.println("runningPhonemes: " + runningPhonemes );
//            System.out.println("numberOfAccentedVowelPhonemes: " + numberOfAccentedVowelPhonemes );
            
            if ( candidatePhonemes + runningPhonemes <= numberOfAccentedVowelPhonemes ) {
                // it's OK to append this word; it won't run over
                toReturn.append(" ");
                toReturn.append(candidateWord);
                runningPhonemes += candidatePhonemes;
            
//                System.out.println("line is now: " + toReturn.toString() + pe.phonemesForLine( toReturn.toString() ) );

                // save last word, in case need to find rhyme
                lastWord = candidateWord;
                // in case the cycle doesn't end, get a next word
                latestWord = candidateWord;
            }
//            System.out.println( toReturn.toString() );

        } while ( runningPhonemes < numberOfAccentedVowelPhonemes && subsequentWordTried < limitSearch);

        if ( subsequentWordTried >= limitSearch ) {                
//            System.out.println("ECCE " + toReturn.toString() );
            return "";
        }
        
        // RHYME: handle rhymed pair
        //  - if this is the first line, make sure it has other rhymes
        //  - if this is the second line, make sure it rhymes
        //  - in either case, if not, re-generate
        if ( ! wordToRhyme.equals("")) {
            if (isFirstInPair) {
                // currently only generating second word in rhymed pair

            } else {
                // is second line.  try to make sure it rhymes with first
                int seekTimes = 0;
                boolean foundRhyme = false;

                // look until you've found an appropriate rhyme, or have tried to 10 times
                while (! foundRhyme && seekTimes < 10) {
                    seekTimes++;
                    // find a candidate rhyming word
                    //System.out.println("looking for rhyme for: " + wordToRhyme );
                    newLastWord = rm.findRhymeFromWord(wordToRhyme);
                    // make sure you've found a rhyme, it's in the right class and it's not the same word
                    // the newLastWord is a true successor of the previous word
                    if ( ! newLastWord.equals("")
                            //&& tagsPerWord.get(newLastWord).equals(tagsPerWord.get(wordToRhyme))
                            && ( haveCommonAntecedent( newLastWord, wordToRhyme) || this.okToBreakNgramForRhyme )
                            && ! newLastWord.equals(wordToRhyme) ) {
                        foundRhyme = true;
                    }
                }
                // either you have found a new last word, or given up
                if ( newLastWord.equals("") ) {
                    System.out.println("unable to find rhyme in line: " + toReturn.toString() );
                } else {
                    // replace the last word with the new last word
                    int lastIndex = toReturn.lastIndexOf(lastWord);
                    toReturn.delete(lastIndex, toReturn.length());
                    toReturn.append( newLastWord );
                }
            }
        }


//        for (int y = 1; y <= 9; y++) {
//            latestWord = getSecondTokenGivenFirstToken(latestWord);
//            toReturn.append(" " + latestWord);
//        }

        return toReturn.toString();
    }

    
    // generate a word
    // TO DO: what if 'include words missing phoneme models' is false and word is missing?
    public String generateWord( String previousWord, String followingWord ) {

        String proposedWord = "";
        int iterations = 0;
        boolean stillLooking = true;
        
        do {
            if ( previousWord.equals( "" ) ) {
                proposedWord = startWordsDistribution.getItemFromWeighedDistribution();
            } else {
                proposedWord = getSecondTokenGivenFirstToken(previousWord);                            
            }
            iterations++;

            // if this is the end of the sentence, we're done
            if ( followingWord.equals("") ) {
                stillLooking = false;
                
            // if the combination is in the model, or we can break ngrams, or we're in the number of iterations,
            // then we're done
            } else if ( isInModel( proposedWord, followingWord ) || okToBreakNgramForSearch || iterations >= limitSearch ) {
                stillLooking = false;
            }
            
        } while ( stillLooking );
        
        if ( iterations >= limitSearch ) {
            proposedWord = "";
        }
        
        return proposedWord;
    }
    
    // TODO error?  should be .get(b)?
    // used when finding rhyme in generateLine
    public boolean haveCommonAntecedent( String a, String b ) {

        Hashtable hA = (Hashtable)allWordsInModel.get(a);
        Hashtable hB = (Hashtable)allWordsInModel.get(a);
        if ( hA.containsKey(b) || hB.containsKey(a) ) {
            return true;
        }
        return false;
    }

    
    // check if a given word pair is in the language model
    public boolean isInModel( String a, String b ) {
        if ( allWordsInModel.contains(b) ) {
            Hashtable previousTokens = (Hashtable)allWordsInModel.get(a);
            if ( previousTokens.contains(a) ) {
                return true;
            }
        }
        return false;
    }
    
    public List getListOfAllWords () {
        List toReturn = new ArrayList();
        
        ArrayList alm = new ArrayList(allWordsInModel.keySet());
        Collections.sort( alm );
        Iterator allItems = alm.iterator();
        
        while ( allItems.hasNext() ) {
            String aKey = allItems.next().toString();

            toReturn.add(aKey);
        }
        return toReturn;
    }
    
    // given a line, pick a word in the line, and replace it with a word from this model
    // note: this will always work (worst case, it will pick the same line)
    public String generateLineReplacingWord( String oldLine, PhonemeEvaluation pe, 
            boolean estimateMissing, boolean isRhymed ) {
        
        StringBuffer toReturn = new StringBuffer();
        
        String newWord;
        String nextWord;
        String[] newLine = oldLine.split(" ");
        boolean stillLooking = true;
        int ri =0 ;
        
        try {
            do {
                // determine which word to replace
                // if it's a rhymed line, don't change the last word
                if ( isRhymed ) {
                    ri = random.nextInt(newLine.length-1);                    
                } else {
                    ri = random.nextInt(newLine.length);                    
                }

                //System.out.print(" replacing index: " + ri + ", ");

                // look for a new word, being constrained by previous word
                if (ri == 0) {
                    // since the first token is being picked, find one from the startWords Hashtable
                    newWord = startWordsDistribution.getItemFromWeighedDistribution();
                } else {
                    newWord = getSecondTokenGivenFirstToken(newLine[ri-1]);
//                    // I'm not sure what I was thinking here.  this is crazy                    
//                    // given the word chosen, look at the possible previous words 
//                    // pick one of those: previousWordChosen
//                    // then pick one of previousWordChosen's next words
//                    Hashtable possiblePreviousWords = (Hashtable)allWordsInModel.get(newLine[ri]);
//                    possiblePreviousWords.remove(STARTWORD);
//                    Object[] sppw = possiblePreviousWords.keySet().toArray();
//                    int ppwS = random.nextInt(sppw.length);
//                    newWord = sppw[ppwS].toString();
                }
                //System.out.print( " b " );

                //System.out.print("which is now: " + newWord + " ");

                // now need to see if the next word (that has already been generated) doesn't
                // pose any objections to the current word
                
                if (ri == newLine.length - 1) {
                    // ok... it's the last one, no next-word check needed
                    //System.out.print("and there is no next word" );
                    stillLooking = false;
                } else {
                    // look at next word
                    // if next-class is there, we're done looking.  if not, we'll look again.
                    nextWord = newLine[ri + 1].toString();
                    //System.out.print("next word: " + newLine[ri + 1] );
                    //System.out.print(" (" + nextTag + ")");
                    //System.out.print( " c2b " );
                    if ( firstTokens.containsKey(newWord) ) {
                        BigramData td = (BigramData) firstTokens.get(newWord);
                        Hashtable ht = (Hashtable) td.secondTokens;
                        //System.out.print(" c2c ");
                        if (ht.containsKey(nextWord)) {
                            stillLooking = false;
                        }
                    }
                }

            } while (stillLooking);

            // replace the word
            newLine[ri] = newWord;

            for (int i = 0; i < newLine.length; i++) {
                toReturn.append(newLine[i] + " ");
            }

        // if there's an exception, it's probably because a word like "thou's" was created which has no PosTags entry
        // just return the original line, and make a note of it
        } catch (Exception exception) {
            System.out.print( " (returning original: " + oldLine + ", chose: " + ri + " ) " );
            toReturn = new StringBuffer( oldLine );
        }
            
        //System.out.println("");

                
        // clean up punctuation
        String finalCandidate = toReturn.toString();
        finalCandidate = finalCandidate.trim();
        //System.out.println( pe.countStressedVowels(finalCandidate));
        
        return finalCandidate;
    }
    
    public void printNextWordsToApplet ( EpogeesApplet applet, String previousToken ) {
//    public String getNextWordsLineWrapped ( String previousToken, int lineLength ) {

        int lineLength = 90;
        
        StringBuffer nextWordsBuffer = new StringBuffer();
        
        if ( previousToken.equals("") ) {
            ArrayList alm = new ArrayList(startWords.keySet());
            Collections.sort(alm);
            Iterator allItems = alm.iterator();
//            applet.printDetails(" ");
            nextWordsBuffer.append(" ");
            while (allItems.hasNext()) {
                String aKey = allItems.next().toString();
//                applet.printDetails(aKey + " " + ((Integer) startWords.get(aKey)).intValue() + ", ");
                nextWordsBuffer.append(aKey + " " + ((Integer) startWords.get(aKey)).intValue() + ", ");
            }
            nextWordsBuffer.append("\n");
//            applet.printDetails("\n");
        } else {
            BigramData bg = (BigramData)firstTokens.get(previousToken);
            if ( bg != null ) {
//                applet.printDetails( " " + bg.secondTokensAndCounts() + "\n" );            
                nextWordsBuffer.append( " " + bg.secondTokensAndCounts() + "\n" );            
            } else {
//                applet.printDetails( "\n" );                            
                nextWordsBuffer.append( "\n" );                            
            }
        }

        String temp = nextWordsBuffer.toString();
        StringBuffer toReturn = new StringBuffer();
        int count = 0;
        boolean readyToInsert = false;
        for ( int x=0; x<temp.length(); x++ ) {
            if ( x<temp.length()-1 ) {
                toReturn.append(temp.substring(x,x+1) );                
            } else {
                toReturn.append(temp.substring(x) );                                
            }
            count++;
            if ( count == lineLength ) {
                readyToInsert = true;
            }
            if ( readyToInsert && x<temp.length()-1 && temp.substring(x,x+1).equals(",") ) {
                toReturn.append( "\n" );
                readyToInsert = false;
                count = 0;
            }
        }
        
        applet.printDetails( toReturn.toString() );                            
//        return toReturn.toString();
    }
    
    public void printPreviousWordsToApplet(EpogeesApplet applet, String nextToken) {

        int lineLength = 90;
        StringBuffer nextWordsBuffer = new StringBuffer();
        Hashtable thePrevWords = (Hashtable) allWordsInModel.get(nextToken);
        
        ArrayList alm = new ArrayList(thePrevWords.keySet());
        Collections.sort( alm );
        Iterator allItems = alm.iterator();
        
        while ( allItems.hasNext() ) {
            String aKey = allItems.next().toString();
            String count = thePrevWords.get(aKey).toString();

            nextWordsBuffer.append(aKey + " " + count + ", ");
        }
        
        String temp = nextWordsBuffer.toString();
        StringBuffer toReturn = new StringBuffer();
        int count = 0;
        boolean readyToInsert = false;
        for ( int x=0; x<temp.length(); x++ ) {
            if ( x<temp.length()-1 ) {
                toReturn.append(temp.substring(x,x+1) );                
            } else {
                toReturn.append(temp.substring(x) );                                
            }
            count++;
            if ( count == lineLength ) {
                readyToInsert = true;
            }
            if ( readyToInsert && x<temp.length()-1 && temp.substring(x,x+1).equals(",") ) {
                toReturn.append( "\n" );
                readyToInsert = false;
                count = 0;
            }
        }
        
        applet.printDetails( toReturn.toString() + "\n" );                            
//        applet.printDetails( nextWordsBuffer.toString() );                            
        
    }
    
    public void printOverviewToApplet( EpogeesApplet applet ) {
        applet.printModel("\nUNIQUE TOKEN TYPES IN MODEL: " + allWordsInModel.size() + "\n");

    }
    
    // assumes a ClassNPhoneApplet applet with a "printModel" method
    public void printLanguageModelToApplet( EpogeesApplet applet ) {
        //StringBuffer toReturn = new StringBuffer();
        
        Enumeration allKeys;
        Iterator allItems;
        
//        int i = 0;
        ArrayList alm;

        int totalBigrams = 0;
        
        applet.printModel("\nSTARTING TOKENS, WITH COUNTS: \n\n");
        
        // look through hashtable of starting words
        alm = new ArrayList(startWords.keySet());
        Collections.sort( alm );
        allItems = alm.iterator();
        
//        allKeys = startWords.keys();
//        while (allKeys.hasMoreElements()) {
        while ( allItems.hasNext() ) {
//            String aKey = allKeys.nextElement().toString();
            String aKey = allItems.next().toString();
            //int tagCount = ((Integer) startWords.get(aKey)).intValue();
            applet.printModel(aKey + " \t ");
            //System.out.println(aKey);
            applet.printModel( ((Integer) startWords.get(aKey)).intValue() + "\n" );

            totalBigrams += ((Integer) startWords.get(aKey)).intValue();
        }

        applet.printModel("\n\nALL SEEN BIGRAMS, WITH COUNTS: \n");
        //applet.printModel("\n\n* * * * * \nall seen bigrams, with counts: \n* * * * * \n");
        //allKeys = firstTokens.keys();
        alm = new ArrayList(firstTokens.keySet());
        Collections.sort( alm );
        allItems = alm.iterator();
        
        // look through hashtable of first words in bigram
        //while (allKeys.hasMoreElements()) {
        while (allItems.hasNext()) {
            //String aKey = allKeys.nextElement().toString();
            String aKey = allItems.next().toString();
            
            //applet.printModel("\nfirst token: ");
            applet.printModel("\n");
            applet.printModel( aKey + "\n" );
//            i++;
//            System.out.println( i + " first token: " + aKey);
            BigramData bg = (BigramData)firstTokens.get(aKey);
//            System.out.println( "second tokens: " + bg.secondTokensAndCounts() );
//            applet.printModel("second tokens: ");
            applet.printModel( " " + bg.secondTokensAndCounts() + "\n" );

            totalBigrams += bg.getTotalBigramCount();
        }

        applet.printModel("\n\ntotal bigrams seen: " + totalBigrams);
                
    }

   
}
