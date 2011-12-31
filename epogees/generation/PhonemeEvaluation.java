
package epogees.generation;


import epogees.model.*;
import epogees.model.language.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class PhonemeEvaluation {

    // phoneme model
    PhonemeModel pm = new PhonemeModel();
    // lists of all phonemes, vowels, and consonants.  each element is a string such as "AE"
    List allPhonemes = new ArrayList();
    List allVowels = new ArrayList();
    List allConsonants = new ArrayList();
    // list of back, central, and front vowels
    Hashtable backVowels = new Hashtable();
    Hashtable centralVowels = new Hashtable();
    Hashtable frontVowels = new Hashtable();
    
    // evaluation values
    // weight of each phoneme: keys are phonemes, values are integers
    Hashtable phonemesAndWeights = new Hashtable();
    int targetNumberOfSyllables = 5;
    int numberOfSyllablesWeight = 10;
    int alliterationWeight = 10;
    int assonanceWeight = 9;

    // used for random word generation
    Random random = new Random(System.currentTimeMillis());
    // phoneme model file locations
    String phonemeFile1 = "";
    String phonemeFile2 = "";

    // whether any phoneme should be used for alliteration/assonace,
    // or only those that are non-zero
    public boolean useAny = false;


    public String toString(){
        StringBuffer toReturn = new StringBuffer();
        
        Enumeration e = phonemesAndWeights.keys();
        while (e.hasMoreElements()) {
            String sPhoneme = e.nextElement().toString();

            toReturn.append( sPhoneme + " " + phonemesAndWeights.get(sPhoneme) + ", ");
            
            //phonemesAndWeights.put(sPhoneme, new Integer(i));            
        }    

        toReturn.append( "assonance " + assonanceWeight + ", ");
        toReturn.append( "alliteration " + alliterationWeight );
        
        return toReturn.toString();
    }
    
    
    // evaluation values getters and setters
    public void setAlliterationWeight(int alliterationWeight) {
        this.alliterationWeight = alliterationWeight;
        //System.out.println("setting alliteartion weight to " + alliterationWeight );
    }

    public void setAssonanceWeight(int assonanceWeight) {
        this.assonanceWeight = assonanceWeight;
        //System.out.println("setting assonance weight to " + assonanceWeight );
    }

    public void setNumberOfSyllablesWeight(int numberOfSyllablesWeight) {
        this.numberOfSyllablesWeight = numberOfSyllablesWeight;
    }

    public void setTargetNumberOfSyllables(int targetNumberOfSyllables) {
        this.targetNumberOfSyllables = targetNumberOfSyllables;
    }

    public void setPhonemesAndWeights(Hashtable phonemesAndWeights) {
        this.phonemesAndWeights = phonemesAndWeights;
    }

    // set all weights to a given value
    public void setPhonemesAndWeightsAll(int i ) {
        //this.phonemesAndWeights = phonemesAndWeights;

        Enumeration e = phonemesAndWeights.keys();
        while (e.hasMoreElements()) {
            String sPhoneme = e.nextElement().toString();

            phonemesAndWeights.put(sPhoneme, new Integer(i));            
        }    
    }
    
    // set a given phoneme to a given value
    public void setPhonemesAndWeightsPairs( String key, int value) {
        phonemesAndWeights.put(key, new Integer(value));
        //System.out.println( key + " set to " + value );
    }

    // get useAny
    public boolean isUseAny() {
        return useAny;
    }

    // set useAny
    public void setUseAny(boolean useAny) {
        this.useAny = useAny;
    }


    public void readPhonemeModel( List modelList, LanguageModel lm ) {
        pm.readPhonemeList(modelList, lm);
    }
    
    // constructor
    public PhonemeEvaluation() {
        
//        createAllPhonemesLists();
        allPhonemes.add( AA );
        allPhonemes.add( AE );
        allPhonemes.add( AH );
        allPhonemes.add( AO );
        allPhonemes.add( AW );
        allPhonemes.add( AY );
        allPhonemes.add( B );
        allPhonemes.add( CH );
        allPhonemes.add( D );
        allPhonemes.add( DH );
        allPhonemes.add( EH );
        allPhonemes.add( ER );
        allPhonemes.add( EY );
        allPhonemes.add( F );
        allPhonemes.add( G );
        allPhonemes.add( HH );
        allPhonemes.add( IH );
        allPhonemes.add( IY );
        allPhonemes.add( JH );
        allPhonemes.add( K );
        allPhonemes.add( L );
        allPhonemes.add( M );
        allPhonemes.add( N );
        allPhonemes.add( NG );
        allPhonemes.add( OW );
        allPhonemes.add( OY );
        allPhonemes.add( P );
        allPhonemes.add( R );
        allPhonemes.add( S );
        allPhonemes.add( SH );
        allPhonemes.add( T );
        allPhonemes.add( TH );
        allPhonemes.add( UH );
        allPhonemes.add( UW );
        allPhonemes.add( V );
        allPhonemes.add( W );
        allPhonemes.add( Y );
        allPhonemes.add( Z );

        allVowels.add( AA );
        allVowels.add( AE );
        allVowels.add( AH );
        allVowels.add( AO );
        allVowels.add( AW );
        allVowels.add( AY );
        allVowels.add( EH );
        allVowels.add( ER );
        allVowels.add( EY );
        allVowels.add( IH );
        allVowels.add( IY );
        allVowels.add( OW );
        allVowels.add( OY );
        allVowels.add( UH );
        allVowels.add( UW );


        allConsonants.add( B );
        allConsonants.add( CH );
        allConsonants.add( D );
        allConsonants.add( DH );
        allConsonants.add( F );
        allConsonants.add( G );
        allConsonants.add( HH );
        allConsonants.add( JH );
        allConsonants.add( K );
        allConsonants.add( L );
        allConsonants.add( M );
        allConsonants.add( N );
        allConsonants.add( NG );
        allConsonants.add( P );
        allConsonants.add( R );
        allConsonants.add( S );
        allConsonants.add( SH );
        allConsonants.add( T );
        allConsonants.add( TH );
        allConsonants.add( V );
        allConsonants.add( W );
        allConsonants.add( Y );
        allConsonants.add( Z );
        allConsonants.add( ZH );

        backVowels.put(UH, "1");
        backVowels.put(AO, "1");
        backVowels.put(AW, "1");
        backVowels.put(ER, "1");
        backVowels.put(AY, "1");
        backVowels.put(UW, "1");
        centralVowels.put(EH, "1");
        centralVowels.put(AH, "1");
        centralVowels.put(OY, "1");
        centralVowels.put(OW, "1");
        frontVowels.put(IY, "1");
        frontVowels.put(AE, "1");
        frontVowels.put(EY, "1");
        frontVowels.put(IH, "1");
        frontVowels.put(AA, "1");
        
    }

    // evaluate a given line, and return a data structure with evaluation number, the line, and phoneme line
    public PhonemeLine evaluate( String candidateLine ) {

        PhonemeLine toReturn = new PhonemeLine();
        
        //toReturn.setPunctuatedLine(candidateLine);
        //String plainLine = candidateLine.replaceAll("[^\\w ]", "");
        String plainLine = candidateLine;
        String phonemeLine = pm.phonemesForLine(plainLine);
        toReturn.setPlainLine(plainLine);
        toReturn.setPhonemeString( phonemeLine );

        PhonemeEvaluationResults candidateValue = evaluateSequence(plainLine, phonemeLine);        
        toReturn.setEvaluation(candidateValue);

        return toReturn;
    }
        
    // evaluate a line
    public PhonemeEvaluationResults evaluateSequence(String candidateLine, String phonemeLine) {

        int phonemeScore = 0;
        int syllableCountScore = 0;
        int assonanceScore = 0;
        int alliterationScore = 0;
        int totalScore = 0;
        PhonemeEvaluationResults toReturn = new PhonemeEvaluationResults();
        StringBuffer evaluationDetails = new StringBuffer();

        // TO DO: this is inefficient.  re-write.  loop over each type of phoneme?

        // find phonemeScore
        // for each syllable in phonemesAndWeights, find number of times it is in candidateLine and score it accordingly
        Enumeration e = phonemesAndWeights.keys();
        while (e.hasMoreElements()) {
            String evaluationPhoneme = e.nextElement().toString();

            int weight = Integer.parseInt(phonemesAndWeights.get(evaluationPhoneme).toString());
            //int phonemeCount = pm.phonemesOfType(candidateLine, evaluationPhoneme);
            int phonemeCount = pm.phonemesInPhonemeLine(phonemeLine,evaluationPhoneme);
            
            int p = phonemeCount * weight;
            phonemeScore += p;

            if ( p != 0 ) {
                evaluationDetails.append( p + " (" + phonemeCount + "*" + weight + " for " + evaluationPhoneme + ") + " );                
            }            
        }

        // find assonanceScore
        // for each possible vowel phoneme, find number of times it is in candidateLine 
        Hashtable vowelsInLine = new Hashtable();
        for (int x = 0; x < allVowels.size(); x++) {
            //int vowelCount = pm.phonemesOfType(candidateLine, allVowels.get(x).toString());
            int vowelCount = pm.phonemesInPhonemeLine(phonemeLine, allVowels.get(x).toString());
            vowelsInLine.put(allVowels.get(x), new Integer(vowelCount));
        }            
        
        // look through the list of vowels that were in the line
        Enumeration evk = vowelsInLine.keys();
        while (evk.hasMoreElements()) {
            // for a given vowel
            String vowel = evk.nextElement().toString();
            // find how many times it was in the line
            int vowelCount = Integer.parseInt(vowelsInLine.get(vowel).toString());
            // if it was there more than once, add it to the assonanceScore
//            if (vowelCount > 1) {
//                assonanceScore += vowelCount;
//            }

            // if it was there more than once, add it to the assonanceScore
            if (vowelCount > 1) {
                if ( useAny ) {
                assonanceScore += vowelCount;
                } else {
                    // want to see if the weight is 0 or not
                    int x = Integer.parseInt(phonemesAndWeights.get(vowel).toString());
                    if ( x != 0 ) {
                        assonanceScore += vowelCount;
                    }
                }
            }
        }

        // find the weighed assonanceScore
        assonanceScore *= assonanceWeight;
        if ( assonanceScore != 0 ) {                    
            evaluationDetails.append( assonanceScore + " (for assonance, weight " + assonanceWeight + ") + " );
        }


        // find alliterationScore
        // for each possible consonant phoneme, find number of times it is in candidateLine 
        Hashtable consonantsInLine = new Hashtable();
        for (int x = 0; x < allConsonants.size(); x++) {
            //int consonantCount = pm.phonemesOfType(candidateLine, allConsonants.get(x).toString());
            //int consonantCount = pm.phonemesInPhonemeLine(phonemeLine, allConsonants.get(x).toString());
            int consonantCount = pm.consonantsInPhonemeLineStartingWords(phonemeLine, allConsonants.get(x).toString());
            consonantsInLine.put(allConsonants.get(x), new Integer(consonantCount));
//            if ( consonantCount != 0 ) {
//                System.out.println("     found " + consonantCount + " examples of " + allConsonants.get(x) );
//            }
        }
        // look through the list of consonants that were in the line
        Enumeration eck = consonantsInLine.keys();
        while (eck.hasMoreElements()) {
            // for a given consonant
            String consonant = eck.nextElement().toString();
            // find how many times it was in the line
            int consonantCount = Integer.parseInt(consonantsInLine.get(consonant).toString());
            
            //System.out.println("     now there are " + consonantCount + " of " + consonant );

            // if it was there more than once, add it to the alliterationScore
            if (consonantCount > 1) {
                if ( useAny ) {
                    alliterationScore += consonantCount;
                } else {
                    // want to see if the weight is 0 or not
                    int w = Integer.parseInt(phonemesAndWeights.get(consonant).toString());
                    if ( w != 0 ) {
                        alliterationScore += consonantCount;
                    }
                }
            }
        }
        // find the weighed alliterationScore
        alliterationScore *= alliterationWeight;
        if ( alliterationScore != 0 ) {
            evaluationDetails.append( alliterationScore + " (for alliteration, weight " + alliterationWeight + ")" );
        }

        // find syllableCountScore
        // evaluate based on how well it matches the expected number of syllables
//        syllableCountScore = Math.abs(pm.numPhonemesForLineAccentedVowelsOnly(candidateLine) - targetNumberOfSyllables);
//        syllableCountScore *= numberOfSyllablesWeight;
//
//        if ( syllableCountScore != 0 ) {
//            evaluationDetails.append( " - " + syllableCountScore + " (syllable count adjustment)" );
//        }

        // RHYME: evaluate how well it rhymes?

        totalScore = phonemeScore - syllableCountScore + alliterationScore + assonanceScore;
        if (totalScore <= 0) {
            totalScore = 0;
        }

        if ( totalScore != 0 ) {
            evaluationDetails.insert(0, totalScore + " = ");
        } else {
            evaluationDetails.insert(0, "score = 0");            
        }
        // replace possible final "+ "
        String ed = evaluationDetails.toString().replaceAll("\\+ $", " ");
        
        toReturn.setSyllableCountScore(syllableCountScore);
        toReturn.setPhonemicScore(phonemeScore);
        toReturn.setAlliterationScore(alliterationScore);
        toReturn.setAssonanceScore(assonanceScore);
        toReturn.setTotalScore(totalScore);
        toReturn.setEvaluationDetails( ed );
        return toReturn;
    }

    // get information about phonemes in a string
    public String phoneCheck(String phonemeLine) {

        StringBuffer toReturn = new StringBuffer();
        Map phonesInLine = new TreeMap();
        int backVowelCount = 0;
        int centralVowelCount = 0;
        int frontVowelCount = 0;
        int allVowelCount = 0;
        double backVowelPercent = 0.0;
        double centralVowelPercent = 0.0;
        double frontVowelPercent = 0.0;

        // phonemeLine looks like: hh AW1,  m AY1,  w ER1 th,  dh AY1,  b AY1,  w EY1 t IH0 ng,  dh AE1 t,  t UW1,  d U
        phonemeLine = phonemeLine.replaceAll(",", "");
        phonemeLine = phonemeLine.replaceAll("2", "");
        phonemeLine = phonemeLine.replaceAll("1", "");
        phonemeLine = phonemeLine.replaceAll("0", "");
        phonemeLine = phonemeLine.trim();
        phonemeLine = phonemeLine.replaceAll(" +", " ");

        String[] phonemeArray = phonemeLine.split(" ");

        // phonemeLine[x] should look like: "AW"
        for (int x=0; x<phonemeArray.length; x++ ) {
            if ( phonesInLine.containsKey(phonemeArray[x])) {
                int tk = ((Integer)phonesInLine.get(phonemeArray[x])).intValue();
                tk++;
                phonesInLine.put(phonemeArray[x], new Integer(tk));
            } else {
                phonesInLine.put(phonemeArray[x], new Integer(1));
            }

            if ( backVowels.containsKey(phonemeArray[x])) {
                backVowelCount++;
                allVowelCount++;
            } else if ( centralVowels.containsKey(phonemeArray[x])) {
                centralVowelCount++;
                allVowelCount++;
            } else if ( frontVowels.containsKey(phonemeArray[x])) {
                frontVowelCount++;
                allVowelCount++;
            }
        }
        DecimalFormat df = new DecimalFormat("###.0");
        backVowelPercent = (double)backVowelCount / (double)allVowelCount;
        backVowelPercent *= 100;
        centralVowelPercent = (double)centralVowelCount / (double)allVowelCount;
        centralVowelPercent *= 100;
        frontVowelPercent = (double)frontVowelCount / (double)allVowelCount;
        frontVowelPercent *= 100;

        Iterator i = phonesInLine.keySet().iterator();
        while ( i.hasNext() ) {
            String phone = i.next().toString();
            String count = phonesInLine.get(phone).toString();
            toReturn.append( phone + " " + count + ", ");
        }

        toReturn.append("\n  back vowels: " + backVowelCount + " (" + df.format( backVowelPercent ) + "%), central vowels: " + centralVowelCount + " (" + df.format(centralVowelPercent )  + "%), front vowels: " + frontVowelCount + " (" + df.format( frontVowelPercent )  + "%)");

        return toReturn.toString();
    }

    
    // TO DO: this is strange... need to stop making PhonemeEvaluation a middleman
    public int countStressedVowels( String toEvaluate ) {

        //return pm.numPhonemesForWordAccentedVowelsOnly(toEvaluate);
        return pm.numPhonemesForLineAccentedVowelsOnly(toEvaluate.trim());
    }
    
    public String phonemesForLine( String line ) {
        return pm.phonemesForLine(line);
    }
    
    // returns a reference to the phoneme model
    public PhonemeModel getPhonemeModel() {
        return pm;
    }
    
// remove?   moved up
//    // create lists of all consonants, all vowels, and all phonemes
//    private void createAllPhonemesLists(){
//
//    }
    
    
    // define phoneme constants
    static final String AA = "AA";
    static final String AE = "AE";
    static final String AH = "AH";
    static final String AO = "AO";
    static final String AW = "AW";
    static final String AY = "AY";
    static final String B = "B";
    static final String CH = "CH";
    static final String D = "D";
    static final String DH = "DH";
    static final String EH = "EH";
    static final String ER = "ER";
    static final String EY = "EY";
    static final String F = "F";
    static final String G = "G";
    static final String HH = "HH";
    static final String IH = "IH";
    static final String IY = "IY";
    static final String JH = "JH";
    static final String K = "K";
    static final String L = "L";
    static final String M = "M";
    static final String N = "N";
    static final String NG = "NG";
    static final String OW = "OW";
    static final String OY = "OY";
    static final String P = "P";
    static final String R = "R";
    static final String S = "S";
    static final String SH = "SH";
    static final String T = "T";
    static final String TH = "TH";
    static final String UH = "UH";
    static final String UW = "UW";
    static final String V = "V";
    static final String W = "W";
    static final String Y = "Y";
    static final String Z = "Z";
    static final String ZH = "ZH";
    
}
