
package epogees.model.language;


import epogees.util.*;
import epogees.ui.*;
import epogees.model.*;
import epogees.generation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;

public class ClassBasedBigramModel extends LanguageModel {

    // hashtable of the word classes: keys are pos tags, values are TagData objects
    Hashtable posTags = new Hashtable();

    // hashtable of pos tags of each word: keys are words (Strings), values are tags (Strings)
    Hashtable tagsPerWord = new Hashtable();

    
    Hashtable tagsToIgnore = new Hashtable();

    // probability distribution of possible starting tags
    ProbabilityDistribution startingTags;
    // hashtable of possible start tags: keys are tags, values are counts, like: NNP 355
    Hashtable startTags = new Hashtable();
    

    // the tag used for the start state of a line
    static final String STARTTAG = "*START*";

    // used for random word generation
    Random random = new Random(System.currentTimeMillis());
    

    // a class that tracks data for a given tag
    // for example, how often the tag was seen in a corpus, what the possible next tags are,
    // and what words were seen that had that tag.
    // This will be used in a hashtable, whose keys are tags and values are TagData objects
    class TagData {

        // how many times the tag was seen in the corpus.  used to determine probability
        long tagCount = 0;

        // hashtable: Keys are possible next tags, Values are tag counts in corpus
        Hashtable nextTags = new Hashtable();

        // hashtable: Keys are possible words in this word class, Values are counts in corpus
        Hashtable possibleWords = new Hashtable();

        // distribution of next possible words
        ProbabilityDistribution possibleWordDistribution;

        // distribution of next possible tag
        ProbabilityDistribution possibleNextTagDistribution;
        
        public void makePossibleWordDistribution() {
            possibleWordDistribution = new ProbabilityDistribution(possibleWords);
        }

        public void makePossibleNextTagDistribution() {
            possibleNextTagDistribution = new ProbabilityDistribution(nextTags);
        }
        
        public long getTagCount() {
            return tagCount;
        }

        public void setTagCount(long tagCount) {
            this.tagCount = tagCount;
        }

        // adds one to tagCount
        public void incrementTagCount(){
            tagCount++;
        }

        // add possible word
        public void addPossibleWord( String word ) {
            if ( possibleWords.containsKey(word)) {
                int tc = ((Integer) possibleWords.get(word)).intValue();
                tc++;
                possibleWords.put( word, new Integer(tc) );

            } else {
                possibleWords.put( word, new Integer(1) );
            }
        }


        // get count of a given possible word
        public int getPossibleWordCount( String word ) {
            if ( possibleWords.containsKey(word)) {
                return ((Integer) possibleWords.get(word)).intValue();
            }
            return 0;
        }

        // return string representation of possible word 
        public String getPossibleWordString() {

            StringBuffer toReturn = new StringBuffer();

            Enumeration allKeys = possibleWords.keys();
            while ( allKeys.hasMoreElements() ) {
                String aKey = allKeys.nextElement().toString();
                toReturn.append( " " + aKey + ", " + getPossibleWordCount(aKey) + ";" );
            }

            return toReturn.toString();
        }

        // add next tag
        public void addNextTag( String tag ) {

            if ( nextTags.containsKey(tag)) {
                int tc = ((Integer) nextTags.get(tag)).intValue();
                tc++;
                nextTags.put( tag, new Integer(tc) );

            } else {
                nextTags.put( tag, new Integer(1) );
            }
        }

        // get count of next tag
        public int getNextTagCount( String tag ) {
            if ( nextTags.containsKey(tag)) {
                return ((Integer) nextTags.get(tag)).intValue();
            }
            return 0;
        }

        public String getNextTagString() {

            StringBuffer toReturn = new StringBuffer();

            Enumeration allKeys = nextTags.keys();
            while ( allKeys.hasMoreElements() ) {
                String aKey = allKeys.nextElement().toString();
                toReturn.append( " " + aKey + ", " + getNextTagCount(aKey) + ";" );
            }

            return toReturn.toString();
        }
        
        public Hashtable getNextTagHash() {
            return nextTags;
        }

        public boolean hasNextTag( String nextTag ) {
            if ( nextTags.containsKey(nextTag) ) {
                return true;
            }
            return false;
        }
    }

    
    public boolean isInLanguageModel( String word ) {
        if ( tagsPerWord.containsKey(word)) {
            return true;
        }
        
        return false;
    }


    public void changeUsePunctuation() {
        System.out.println("punctuation not supported with class-based models");
    }

    public void setUsePunctuation( boolean b ) {
    }

    public boolean getUsePunctuation() {
        return false;
    }
    // read a list of strings from a tagged file, count the word classes, create model of words per class type
    public void readModelList( List fileContents ) {

            String currentLine;

        // for now, ignore punctuation tags
        tagsToIgnore.put("-RRB-", "1");
        tagsToIgnore.put("-LRB-", "1");
        tagsToIgnore.put("''", "1");
        tagsToIgnore.put("``", "1");
        tagsToIgnore.put(":", "1");
        tagsToIgnore.put(".", "1");
        tagsToIgnore.put(",", "1");
        
        try {            
            // a line should look like:
            // Thou/NNP that/IN art/NN now/RB the/DT world/NN 's/POS fresh/JJ ornament/NN ,/,

            for ( int x=0; x< fileContents.size(); x++ ) {
                
                currentLine = fileContents.get(x).toString();
                if ( ! currentLine.startsWith("//") && ! currentLine.equals("") ) {

                    String previousTag = STARTTAG;
                    // each wordAndTag element should look like: Thou/NNP
                    String[] wordAndTag = currentLine.split(" ");

                    for ( int i=0; i<wordAndTag.length; i++ ) {
                        // wordOrTag[0] looks like: "Thou"
                        // wordOrTag[1] looks like: "NNP"
                        String[] wordOrTag = wordAndTag[i].split( "/" );
                        String word = wordOrTag[0].toLowerCase();
                        String tag = wordOrTag[1];

                        // make sure it's not a punctuation tag being ignored
                        if ( ! tagsToIgnore.containsKey(tag) ) {
                            
                            //System.out.println("looking at tag: " + tag);
                            
                            // first, add to hashtable that lets you look up a tag given a word
                            tagsPerWord.put(word, tag);

                            // next, work on hashtable that lets you look up information given a tag

                            // increment tag count
                            if (posTags.containsKey(tag)) {
                                ((TagData) posTags.get(tag)).incrementTagCount();
                            } else {
                                posTags.put(tag, new TagData());
                                ((TagData) posTags.get(tag)).incrementTagCount();
                            }

                            // add possible word to tag
                            ((TagData) posTags.get(tag)).addPossibleWord(word);

                            // add 'next possible tag' to previous tag seen
                            if (previousTag.equalsIgnoreCase(STARTTAG)) {

                                if (startTags.containsKey(tag)) {
                                    int tc = ((Integer) startTags.get(tag)).intValue();
                                    tc++;
                                    startTags.put(tag, new Integer(tc));
                                } else {
                                    startTags.put(tag, new Integer(1));
                                }

                            } else {
                                ((TagData) posTags.get(previousTag)).addNextTag(tag);
                            }

                            previousTag = tag;                            
                        }
                    }
                }
            }

            // look through hashtable of pos tags
            Enumeration allKeys = posTags.keys();
            while ( allKeys.hasMoreElements() ) {
                String aKey = allKeys.nextElement().toString();
                TagData td = (TagData)posTags.get(aKey);

                // create probability distribution for possible words and next words for each tag
                td.makePossibleWordDistribution();
                td.makePossibleNextTagDistribution();
            }
            
            // create probability distribution for starting tags
            startingTags = new ProbabilityDistribution( startTags );
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    

    public void printOverviewToApplet( EpogeesApplet applet ) {
        applet.printModel("\nUNIQUE TOKEN TYPES IN MODEL: " + tagsPerWord.size() + "\n");
    }    

    public void printLanguageModelToApplet( EpogeesApplet applet ) {

        Enumeration allKeys;
                
        applet.printModel("\n* * * * * \nstarting word classes: \n* * * * * \n\n");
        
        allKeys = startTags.keys();
        while (allKeys.hasMoreElements()) {
            String aKey = allKeys.nextElement().toString();
            int tagCount = ((Integer) startTags.get(aKey)).intValue();
            applet.printModel(aKey + " \t" + tagCount + "\n");
        }

        applet.printModel("\n\n* * * * * \nall seen word classes, with counts: \n* * * * * \n");
        // look through hashtable of pos tags
        allKeys = posTags.keys();
        //allKeys = posTags.elements();
        while (allKeys.hasMoreElements()) {
            String aKey = allKeys.nextElement().toString();
            
            TagData td = (TagData) posTags.get(aKey);

            applet.printModel("\n\nword class: ");
            applet.printModel( aKey + " " + td.getTagCount());

            // a string representation of possible word distribution
            applet.printModel("\nseen words in word class: ");
            applet.printModel( td.getPossibleWordString() );

            // a string representation of next tag distribution
            applet.printModel("\nseen next-class items: ");
            applet.printModel( td.getNextTagString() );            
        }
        
        applet.printModel("\n\n* * * * * \nall seen word classes, with counts: \n* * * * * \n\n");
        // look through hashtable of pos tags
        allKeys = posTags.keys();
        //allKeys = posTags.elements();
        while (allKeys.hasMoreElements()) {
            String aKey = allKeys.nextElement().toString();
            
            TagData td = (TagData) posTags.get(aKey);

            applet.printModel( aKey + " \t" + td.getTagCount() + "\n" );
        }
        
    }

    public void printNextWordsToApplet ( EpogeesApplet applet, String previousToken ) {
        if ( previousToken.equals("") ) {
            Enumeration allKeys = startTags.keys();
            while (allKeys.hasMoreElements()) {
                String aKey = allKeys.nextElement().toString();
                TagData td = (TagData) posTags.get(aKey);
                
                Hashtable nextTags = td.getNextTagHash();

                Enumeration allKeys2 = nextTags.keys();
                while (allKeys2.hasMoreElements()) {
                    String aKey2 = allKeys2.nextElement().toString();
                    TagData td2 = (TagData) posTags.get(aKey2);
                    applet.printDetails(td2.getPossibleWordString());
                }
            
                int tagCount = ((Integer) startTags.get(aKey)).intValue();
                applet.printDetails(aKey + " " + tagCount + ", ");
            }
        } else {
            String tagPrevious = tagsPerWord.get( previousToken ).toString();
            TagData td = (TagData) posTags.get(tagPrevious);

            Hashtable nextTags = td.getNextTagHash();

            Enumeration allKeys = nextTags.keys();
            while (allKeys.hasMoreElements()) {
                String aKey = allKeys.nextElement().toString();
                TagData td2 = (TagData) posTags.get(aKey);
                applet.printDetails( td2.getPossibleWordString() );
            }                        
        }        
        applet.printDetails("\n");
    }    
    
    // given a line, pick a word in the line, and replace it with a word from this model
    // note: this will always work (worst case, it will pick the same line)
    public String generateLineReplacingWord( String oldLine, PhonemeEvaluation pe, 
            boolean estimateMissing, boolean isRhymed ) {
        
        StringBuffer toReturn = new StringBuffer();
        
        String newWord;
        String[] newLine = oldLine.split(" ");
        boolean stillLooking = true;
        int ri;
        
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

                // look at the class of the word before the word to replace (or start if it is 0)
                // pick a 'next word' in that word class
                int lengthOldWord = pe.countStressedVowels( newLine[ri] );
                if (lengthOldWord == -1 && estimateMissing) {
                    lengthOldWord = 1;
                }
                int lengthNewWord;
                if (ri == 0) {
                    do {
                        String startingTag = startingTags.getItemFromWeighedDistribution();
                        TagData td = (TagData) posTags.get(startingTag);
                        newWord = td.possibleWordDistribution.getItemFromWeighedDistribution();
                        lengthNewWord = pe.countStressedVowels( newWord );
                        //System.out.println( lengthNewWord + " " + lengthOldWord );
                        if ( lengthNewWord == -1 && estimateMissing ) {
                            lengthNewWord = 1;
                        }
                    } while ( lengthOldWord != lengthNewWord );
                } else {
                    do {
                        String previousTag = tagsPerWord.get(newLine[ri]).toString();
                        TagData td = (TagData) posTags.get(previousTag);
                        newWord = td.possibleWordDistribution.getItemFromWeighedDistribution();
                        lengthNewWord = pe.countStressedVowels( newWord );
                        //System.out.println( lengthNewWord + " " + lengthOldWord );
                        if ( lengthNewWord == -1 && estimateMissing ) {
                            lengthNewWord = 1;
                        }
                    } while ( lengthOldWord != lengthNewWord );
                }

                //System.out.print("which is now: " + newWord + " ");

                String nextTag = new String();
                String thisTag = new String();
                // find class and tagdata of newWord
                thisTag = tagsPerWord.get(newWord).toString();
                //System.out.print("(" + thisTag + "), ");
                TagData td = (TagData) posTags.get(thisTag);
                
                if (ri == newLine.length - 1) {
                    // ok... it's the last one, no next-class check needed
                    //System.out.print("and there is no next word" );
                    stillLooking = false;
                } else {
                    // look at next-class of newWord's tagData. 
                    // if next-class is there, we're done looking.  if not, we'll look again.
                    nextTag = tagsPerWord.get(newLine[ri + 1]).toString();
                    //System.out.print("next word: " + newLine[ri + 1] );
                    //System.out.print(" (" + nextTag + ")");
                    if (td.hasNextTag(nextTag)) {
                        stillLooking = false;
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
            System.out.print( " (returning original) " );
            toReturn = new StringBuffer( oldLine );
        }
            
        //System.out.println("");

                
        // clean up punctuation
        String finalCandidate = toReturn.toString();
        finalCandidate = finalCandidate.trim();
        //System.out.println( pe.countStressedVowels(finalCandidate));
        
        return finalCandidate;
    }
    


    // generate a line beginning with start tags
    public String generateLine( PhonemeEvaluation pe, int numberOfAccentedVowelPhonemes, 
            boolean estimateMissing, RhymeModel rm, String wordToRhyme, boolean isFirstInPair, String enjambment ){

        StringBuffer toReturn = new StringBuffer();
        String candidateWord;
        String nextTag;
        int candidatePhonemes = 0;
        int runningPhonemes = 0;
        TagData lastTag;
        String lastWord;
        String newLastWord = "";
        
        // get a starting tag
        String startingTag = startingTags.getItemFromWeighedDistribution();        
        TagData td = (TagData)posTags.get(startingTag);

        do {
            // get an item (word)
            candidateWord = td.possibleWordDistribution.getItemFromWeighedDistribution();
            // find how many stressed vowels are in that word
            candidatePhonemes = pe.countStressedVowels(candidateWord);

            if ( candidatePhonemes == -1 && estimateMissing ) {
                //System.out.println("word without phoneme model found: " + candidateWord );
                candidatePhonemes = 1;
            } else if ( candidatePhonemes == -1 && ! estimateMissing ) {
                // give it a very large value so it fails the checks below
                candidatePhonemes = 999;
            }
            
                
            //System.out.println( runningPhonemes + " " + candidatePhonemes );
            // if it doesn't knock it over the limit, append it and update counts
            // otherwise it will be ignored and chosen again next turn
            if ( candidatePhonemes + runningPhonemes <= numberOfAccentedVowelPhonemes ) {
                toReturn.append(candidateWord);
                toReturn.append(" ");
                runningPhonemes += candidatePhonemes;
            }
            // save last tag, in case need to find rhyme
            lastTag = td;
            lastWord = candidateWord;
            // in case the cycle doesn't end,
            // get a next tag and the tag data for that next tag
            nextTag = td.possibleNextTagDistribution.getItemFromWeighedDistribution();
            td = (TagData) posTags.get(nextTag);

        } while ( runningPhonemes < numberOfAccentedVowelPhonemes);
        // test if this cycle should end

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
                //while (!lastWord.equals(wordToRhyme) && seekTimes < 10) {
                while (! foundRhyme && seekTimes < 10) {
                    seekTimes++;
                    // find a candidate rhyming word
                    //System.out.println("seeking rhyme for: " + wordToRhyme + " whose tag is: " + tagsPerWord.get(wordToRhyme));
                    newLastWord = rm.findRhymeFromWord(wordToRhyme);
                    //System.out.println("found potential: " + newLastWord + " whose tag is: " + tagsPerWord.get(newLastWord));
                    // make sure you've found a rhyme, it's in the right class and it's not the same word
                    if ( ! newLastWord.equals("") 
                            && tagsPerWord.get(newLastWord).equals(tagsPerWord.get(wordToRhyme)) 
                            && ! newLastWord.equals(wordToRhyme) ) {
                        foundRhyme = true;
                    }
                }
                // either you have found a new last word, or given up
                if ( newLastWord.equals("") ) {
                    System.out.println("unable to find rhyme for line: " + toReturn.toString() );
                } else {
                    // replace the last word with the new last word
                    //System.out.println("found word: " + newLastWord );
                    int lastIndex = toReturn.lastIndexOf(lastWord);
                    toReturn.delete(lastIndex-1, toReturn.length()-1);
                    toReturn.append( newLastWord );
                }
                //System.out.println("- - -");
            }
        }

        //System.out.println("line and accented vowel count: " + toReturn.toString() + " " + numberOfAccentedVowelPhonemes);
        
        // clean up punctuation
        String finalCandidate = toReturn.toString();
        finalCandidate = finalCandidate.replaceAll(" '", "'");
        return finalCandidate;
    }

    
    public String generateWord( String previousWord, String followingWord ) {

        String toReturn = "";
        TagData td = new TagData();        
        if (previousWord.equals("") || ! tagsPerWord.containsKey(previousWord) ) {
            String startingTag = startingTags.getItemFromWeighedDistribution();
            td = (TagData) posTags.get(startingTag);
            toReturn = td.possibleWordDistribution.getItemFromWeighedDistribution();
        } else {
            // TO DO : make sure you don't break ngram model
            
            // get tag for previousWord
            String tagPrevious = tagsPerWord.get( previousWord ).toString();
            td = (TagData) posTags.get(tagPrevious);
            // then get tag for this one
            String thisTag = td.possibleNextTagDistribution.getItemFromWeighedDistribution();
            td = (TagData) posTags.get(thisTag);
            // then get word from tag
            toReturn = td.possibleWordDistribution.getItemFromWeighedDistribution();
        }
        
        return toReturn;
    }
    

    // just used for development
    public List getListOfAllWords() {
        List toReturn = new ArrayList();
        
        // gotta be a better way of doing this
        Iterator i = tagsPerWord.keySet().iterator();
        while ( i.hasNext() ) {
            String word = i.next().toString();
            toReturn.add(word);
        }
        
        return toReturn;
    }

    public void printPreviousWordsToApplet(EpogeesApplet applet, String nextToken) {
        applet.printDetails("(Not implemented for class-based language models.)\n");
    }
    
}



