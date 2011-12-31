
package epogees.model;

import epogees.ui.*;
import epogees.generation.*;
import epogees.model.language.*;
import epogees.util.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;


public class RhymeModel {

    // The Single Rhyme model is made up of a Hashtable of the final stressed vowels of the relevant word.
    //      key: vowel phoneme (String), 
    //      value: Hashtable of following consonants
    //                  key: consonant phoneme sequence (string)
    //                  value: List of all matching words (each of which is a Word)
    Hashtable singleRhyme = new Hashtable();

    Hashtable doubleRhyme = new Hashtable();

    // used for random word generation
    Random random = new Random(System.currentTimeMillis());

    // reference to phoneme evaluation model
    PhonemeEvaluation pe;

    public RhymeModel(PhonemeEvaluation p) {
        pe = p;
    }

    public RhymeModel() {
    }

    /**
     * use a List of pronunciations, which have been taken from a pronunciation file
     *
     * @param linesFromFile The lines of
     * @param lm
     */
    public void readRhymeList( List linesFromFile, LanguageModel lm ) {
        String currentLine = new String();
        // what words have already been added.  key: pronunciation string, value: word
        Hashtable addedWords = new Hashtable();

        try {
            for ( int x=0; x< linesFromFile.size(); x++ ) {
            
                // line looks like:
                // BENEATH  B IH0 N IY1 TH
                currentLine = linesFromFile.get(x).toString();

                String[] aA = currentLine.split("  ");
                // aA[0] looks like: BENEATH
                // aA[1] looks like: B IH0 N IY1 TH
                String word = aA[0].replaceAll("\\W", "").trim().toLowerCase();
                
                // only process it if the line is not commented, if the word is in the language model, 
                // and if the line is not empty
                if ( ! currentLine.startsWith(";") && lm.isInLanguageModel(word) && ! currentLine.equals("") ) {

                    String phonemeString = aA[1].trim();

                    // create new word
                    Word listedWord = new Word( word, phonemeString );

                    String[] bB = phonemeString.split(" ");
                    // bB[4] looks like: TH

                    // start looking through the line backwards
                    // to add to the rhyme data structure
                    int vowelsFound = 0;
                    String trailingPhonemesFound = "";
                    for ( int y=bB.length-1; y>=0; y-- ) {
                        String phonemeOfInterest = bB[y];
                        String toCompare = phonemeOfInterest.replaceAll("\\d", "");
                        
                        if ( isVowel( toCompare ) ){
                            
                            vowelsFound++;
                            // see if it's the accented vowel
                            if ( phonemeOfInterest.matches(".*1.*") ){
                                // see if you've only found one vowel so far
                                if (vowelsFound == 1) {
//                                System.out.println( "single rhyme: " + currentLine );
//                                System.out.println( "  vowel is: " + bB[y] );
//                                System.out.println( "  consonants found: " + trailingPhonemesFound );

                                    // add the word to the single rhyme model
                                    if (singleRhyme.containsKey(phonemeOfInterest)) {
                                        // get a reference to the consonants that trail the single rhymes accented vowel
                                        Hashtable trailingPhonemes = (Hashtable) singleRhyme.get(phonemeOfInterest);
                                        // get a reference to the list of rhyming word made up by this accented vowel and trailing consonants
                                        List rhymingWords = (ArrayList) trailingPhonemes.get(trailingPhonemesFound);
                                        // if no rhyming words have been found yet, create the List
                                        if ( rhymingWords == null ) {
                                            rhymingWords = new ArrayList();
                                        }
                                        // add Word to that list if it hasn't been already added (or if its been added but the pronunciation is different)
                                        //if ( ! addedWords.containsKey( word ) || ! addedWords.get(word).toString().equals(phonemeString) ) {
                                        if ( ! addedWords.containsKey( phonemeString ) ) {
                                            rhymingWords.add(listedWord);
                                            addedWords.put( phonemeString, word );
                                        }
                                        // add the rhyming words list to the trailing phonemes hashtable
                                        trailingPhonemes.put(trailingPhonemesFound, rhymingWords);
                                    } else {
                                        // build list of rhyming words and add the listed word
                                        List rhymingWords = new ArrayList();
                                        rhymingWords.add(listedWord);
                                        addedWords.put( phonemeString, word );
                                        // build hashtable of trailing phonemes and put the list there
                                        Hashtable trailingPhonemes = new Hashtable();
                                        trailingPhonemes.put(trailingPhonemesFound, rhymingWords);
                                        // put the hashtable of trailing phonemes in the hashtable of accented vowels
                                        singleRhyme.put(phonemeOfInterest, trailingPhonemes);
                                        //System.out.print("putting for accented vowel " + phonemeOfInterest );
                                        //System.out.print( " and for trailing phonemes " + trailingPhonemes);
                                        //System.out.println();
                                    }
                                    break;
                                    
                                } else {
                                    // it's a vowel, but not the accented vowel
                                    // so the word cannot be a single rhyme
//                                    System.out.println("possible multi-syllabic rhyme found: " + word + " " + phonemeString);
                                    if (vowelsFound == 2) {
                                        if (doubleRhyme.containsKey(phonemeOfInterest)) {

                                            // get a reference to the consonants that trail the double rhymes accented vowel
                                            Hashtable trailingPhonemes = (Hashtable) doubleRhyme.get(phonemeOfInterest);
                                            // get a reference to the list of rhyming word made up by this accented vowel and trailing consonants
                                            List rhymingWords = (ArrayList) trailingPhonemes.get(trailingPhonemesFound);
                                            // if no rhyming words have been found yet, create the List
                                            if (rhymingWords == null) {
                                                rhymingWords = new ArrayList();
                                            }
                                            // add Word to that list if it hasn't been already added (or if its been added but the pronunciation is different)
                                            if (!addedWords.containsKey(phonemeString)) {
                                                rhymingWords.add(listedWord);
                                                addedWords.put(phonemeString, word);
                                            }
                                            // add the rhyming words list to the trailing phonemes hashtable
                                            trailingPhonemes.put(trailingPhonemesFound, rhymingWords);
                                            
                                        } else {
                                            // build list of rhyming words and add the listed word
                                            List rhymingWords = new ArrayList();
                                            rhymingWords.add(listedWord);
                                            addedWords.put(phonemeString, word);
                                            // build hashtable of trailing phonemes and put the list there
                                            Hashtable trailingPhonemes = new Hashtable();
                                            trailingPhonemes.put(trailingPhonemesFound, rhymingWords);
                                            // put the hashtable of trailing phonemes in the hashtable of accented vowels
                                            doubleRhyme.put(phonemeOfInterest, trailingPhonemes);                                            
                                        }
                                        
                                    }
                                    break;
                                }
                            } else {
                                // its a vowel, but not accented.  add it to trailing Phonemes
                                trailingPhonemesFound = bB[y] + " " + trailingPhonemesFound;
                            }
                        } else {
                            // you are not looking at a vowel.  add it to trailing Phonemes
                            trailingPhonemesFound = bB[y] + " " + trailingPhonemesFound;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println( "probable misformat in: " + currentLine);
            e.printStackTrace();
        }

    }


    // print out the rhyme structures
    public String printAll() {
        StringBuffer toReturn = new StringBuffer();

        // iterate through each vowel phoneme of single rhyme hashtable
        Iterator i = singleRhyme.keySet().iterator();
        while ( i.hasNext() ) {
            String vowelPhonemes = i.next().toString();

            System.out.println("\nfor accented vowel: " + vowelPhonemes );

            // get reference to hashtable of trailing phonemes and iterate through it
            Hashtable trailingPhonemes = (Hashtable)singleRhyme.get(vowelPhonemes);
            Iterator j = trailingPhonemes.keySet().iterator();
            while ( j.hasNext() ) {
                // trailing phonemes
                String tp = j.next().toString();
                System.out.println("for trailing phonemes: " + tp );

                // iterate through list of all rhymes
                List allRhymes = (ArrayList)trailingPhonemes.get( tp );
                Iterator k = allRhymes.iterator();
                while ( k.hasNext() ) {
                    Word w = (Word)k.next();
                    System.out.print( "  word is: " + w.getText() );
                    System.out.print( " phonemes are: " + w.getPhonemesPlain() );
                    System.out.println();
                }
            }
            
        }

        return toReturn.toString();
    }

    public void printRhymeModelToApplet( EpogeesApplet applet ) {
        // iterate through each vowel phoneme of single rhyme hashtable
        applet.printModel("\nRHYME MODEL: \n\n");
        applet.printModel("Single rhyme: \n\n");

        Iterator i = singleRhyme.keySet().iterator();
        while ( i.hasNext() ) {
            String vowelPhonemes = i.next().toString();

            // get reference to hashtable of trailing phonemes and iterate through it
            Hashtable trailingPhonemes = (Hashtable)singleRhyme.get(vowelPhonemes);
            Iterator j = trailingPhonemes.keySet().iterator();
            while ( j.hasNext() ) {
                // trailing phonemes
                String tp = j.next().toString();
                applet.printModel( "\n" + vowelPhonemes + " + " + tp + "\n");

                // iterate through list of all rhymes
                List allRhymes = (ArrayList)trailingPhonemes.get( tp );
                Iterator k = allRhymes.iterator();
                while ( k.hasNext() ) {
                    Word w = (Word)k.next();
                    applet.printModel( "  " + w.getText() + " (" + w.getPhonemesPlain() + ")\n");
                }
            }            
        }
        
        applet.printModel("\nDouble rhyme: \n\n");
        i = doubleRhyme.keySet().iterator();
        while ( i.hasNext() ) {
            String vowelPhonemes = i.next().toString();

            // get reference to hashtable of trailing phonemes and iterate through it
            Hashtable trailingPhonemes = (Hashtable)doubleRhyme.get(vowelPhonemes);
            Iterator j = trailingPhonemes.keySet().iterator();
            while ( j.hasNext() ) {
                // trailing phonemes
                String tp = j.next().toString();
                applet.printModel( "\n" + vowelPhonemes + " + " + tp + "\n");

                // iterate through list of all rhymes
                List allRhymes = (ArrayList)trailingPhonemes.get( tp );
                Iterator k = allRhymes.iterator();
                while ( k.hasNext() ) {
                    Word w = (Word)k.next();
                    applet.printModel( "  " + w.getText() + " (" + w.getPhonemesPlain() + ")\n");
                }
            }            
        }
        
        
    }    
    
    // see if a given String is only a phoneme vowel
    public boolean isVowel (String s) {
        if ( s.equals("AA") || s.equals("AE") || s.equals("AH") || s.equals("AO") || s.equals("AW") || s.equals("AY") 
                || s.equals("EH") || s.equals("ER") || s.equals("EY") || s.equals("IH") || s.equals("IY") 
                || s.equals("OW") || s.equals("OY") || s.equals("UH") || s.equals("UW")
                ) {
            return true;
        }
        return false;
    }
    

    public String findRhymeFromWord ( String toRhyme ) {
        String[] aA = toRhyme.split(" ");
        String wordToRhyme = aA[aA.length-1];
        String wordToRhymePhonemes = pe.phonemesForLine(wordToRhyme);

        return findRhymeFromPhonemes(wordToRhymePhonemes);
    }
    
    public String findRhymeFromPhonemes( String phonemesToRhyme ) {
        String toReturn = new String();
        String phonemeOfInterest = new String();
        String trailingPhonemes = "";
        
        phonemesToRhyme = phonemesToRhyme.trim();

        // first, get the relevant accented vowel and trailing phonemes
        String[] bB = phonemesToRhyme.split(" ");

        int vowelsFound = 0;
        for (int y = bB.length - 1; y >= 0; y--) {
            phonemeOfInterest = bB[y];
            String toCompare = phonemeOfInterest.replaceAll("\\d", "");
            if (isVowel(toCompare)) {
                vowelsFound++;
                // see if it's the accented vowel
                if (phonemeOfInterest.matches(".*1.*")) {
                    // see if you've only found one vowel so far
                    if (vowelsFound == 1) {
                        break;
                    } else if (vowelsFound == 2) {
                        break;
                    }
                } else {
                    // this is an unaccented vowel.  add it to trailing Phonemes
                    trailingPhonemes = bB[y] + " " + trailingPhonemes;
                }
            } else {
                // you are not looking at a vowel.  add it to trailing Phonemes
                trailingPhonemes = bB[y] + " " + trailingPhonemes;
            }
        }
        trailingPhonemes = trailingPhonemes.toUpperCase();
        
        // at this point have found the accented vowel and trailing phonemes
//        System.out.println("phonemes to rhyme: " + phonemesToRhyme);
//        System.out.println("  vowel: " + phonemeOfInterest);
//        System.out.println("  trailing phonemes: " + trailingPhonemes);

        // get the list of rhyming phonemes
        // get a reference to the hashtable of trailing phonemes 
        Hashtable hTrailingPhonemes = new Hashtable();
        if (vowelsFound == 1) {
            hTrailingPhonemes = (Hashtable)singleRhyme.get(phonemeOfInterest);
        } else if (vowelsFound == 2) {
            hTrailingPhonemes = (Hashtable)doubleRhyme.get(phonemeOfInterest);
        }
//        Hashtable hTrailingPhonemes = (Hashtable)singleRhyme.get(phonemeOfInterest);
//        if ( hTrailingPhonemes == null ) {
//            System.out.println( " hashtable of trailing phonemes is null" );
//        }
        if ( hTrailingPhonemes != null ) {
            // get a reference to the List of rhymes
            List lAllRhymes = (ArrayList) hTrailingPhonemes.get(trailingPhonemes);
            if (lAllRhymes == null) {
                System.out.println(" list of all rhymes is null");
            }

            if ( lAllRhymes != null ) {
                // randomly choose one of the elements of the list
                int n = lAllRhymes.size();
                //System.out.println( "size of rhyme list: " + lAllRhymes.size() );
                int i = random.nextInt(n);
                Word foundWord = (Word)lAllRhymes.get(i);
                // if the rhymed word is the same as the word requested, just pick the next one
                //System.out.println( "|" + foundWord.getPhonemesPlain() + "| and |" + phonemesToRhyme.toUpperCase() + "|");
                if (foundWord.getPhonemesPlain().equals(phonemesToRhyme.toUpperCase())) {
                    if ( lAllRhymes.size() == 1 )  {
                        // if there's only one item in the rhyme list, return empty (fail)
                        return "";
                    } else if ( i == lAllRhymes.size()-1 ) {
                        i = 0;
                    } else {
                        i++;
                    }
                    foundWord = (Word)lAllRhymes.get(i);
                }
                toReturn = foundWord.getText();
            }
        }

        toReturn = toReturn.toLowerCase();
        toReturn = toReturn.replaceAll("\\d", "");
        return toReturn;
    }


    public int countRhymesAvailableForWord ( String toRhyme ) {
        toRhyme = toRhyme.trim();

        String[] aA = toRhyme.split(" ");
        String wordToRhyme = aA[aA.length-1];
        String wordToRhymePhonemes = pe.phonemesForLine(wordToRhyme);

        String phonemeOfInterest = new String();
        String trailingPhonemes = "";
        
        wordToRhymePhonemes = wordToRhymePhonemes.trim();

        // first, get the relevant accented vowel and trailing phonemes
        String[] bB = wordToRhymePhonemes.split(" ");

        int vowelsFound = 0;
        for (int y = bB.length - 1; y >= 0; y--) {
            phonemeOfInterest = bB[y];
            String toCompare = phonemeOfInterest.replaceAll("\\d", "");
            if (isVowel(toCompare)) {
                vowelsFound++;
                // see if it's the accented vowel
                if (phonemeOfInterest.matches(".*1.*")) {
                    // see if you've only found one vowel so far
                    if (vowelsFound == 1) {
                        break;
                    } else if (vowelsFound == 2) {
                        break;
                    }
                } else {
                    // this is an unaccented vowel.  add it to trailing Phonemes
                    trailingPhonemes = bB[y] + " " + trailingPhonemes;
                }
            } else {
                // you are not looking at a vowel.  add it to trailing Phonemes
                trailingPhonemes = bB[y] + " " + trailingPhonemes;
            }
        }
        trailingPhonemes = trailingPhonemes.toUpperCase();
        
        // at this point have found the accented vowel and trailing phonemes
        System.out.println("string to rhyme: " + toRhyme);
        System.out.println("  vowel: " + phonemeOfInterest);
        System.out.println("  trailing phonemes: " + trailingPhonemes);

        // get the list of rhyming phonemes
        // get a reference to the hashtable of trailing phonemes 
        Hashtable hTrailingPhonemes = new Hashtable();
        if (vowelsFound == 1) {
            hTrailingPhonemes = (Hashtable)singleRhyme.get(phonemeOfInterest);
        } else if (vowelsFound == 2) {
            hTrailingPhonemes = (Hashtable)doubleRhyme.get(phonemeOfInterest);
        }        
//        Hashtable hTrailingPhonemes = (Hashtable)singleRhyme.get(phonemeOfInterest);
//        if ( hTrailingPhonemes == null ) {
//            System.out.println( " hashtable of trailing phonemes is null" );
//        }
        if ( hTrailingPhonemes != null ) {
            // get a reference to the List of rhymes
            List lAllRhymes = (ArrayList) hTrailingPhonemes.get(trailingPhonemes);
            if (lAllRhymes == null) {
                System.out.println(" list of all the rhymes is null");
            }

            if ( lAllRhymes != null ) {
                return lAllRhymes.size()-1;
            }
        }

        // this will only be reached if something was null above
        return 0;
    }


    
//    public boolean checkRhyme (String a, String b ) {
//        String phonemesA = pe.phonemesForLine(a);
//        String phonemesB = pe.phonemesForLine(b);
//        return false;
//    }
}
