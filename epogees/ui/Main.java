package epogees.ui;


import epogees.generation.*;
import epogees.model.*;
import epogees.model.language.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Main {

    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        Main test = new Main();
        //test.readAndPrintRhymes();
        //test.findWordsWithoutPhonemeModels();
        test.tryWordBasedBigramModel();
    }

    String dataFileLocation = "./build/classes/epogees/data/";

    private void tryWordBasedBigramModel() {
        LanguageModel wbbm = new WordBasedBigramModel();

        // read language model
        String fileLocation = dataFileLocation + "shakespeare-sonnetsAll.txt";
        List languageModelList = readFileMakeList( fileLocation );
        wbbm.readModelList(languageModelList);

        // read phoneme evaluation model
        PhonemeEvaluation pe = new PhonemeEvaluation();
        String phonemeFileLocation = dataFileLocation + "cmudict.0.7a";
        List phonemeModelLines = readFileMakeList( phonemeFileLocation );
        pe.readPhonemeModel(phonemeModelLines, wbbm);
        setInitialPhonemeValues(pe);

        // read rhyme model
        RhymeModel rhymeModel = new RhymeModel(pe);
        String rhymeFileLocation = dataFileLocation + "cmudict.0.7a";
        List rhymeFileLines = readFileMakeList( rhymeFileLocation );
        rhymeModel.readRhymeList( rhymeFileLines, wbbm );

        // generation model
        Generation gen = new Generation( pe, wbbm, rhymeModel );

        boolean estimateMissingPhonemes = true;
        int numberOfAccentedVowelPhonemes = 7;
        String[] generatedOutput;

        StringBuffer toReturn = new StringBuffer();
        for ( int x=1; x<=5; x++ ) {
            String wordToRhyme = "";
            String proposedLine = wbbm.generateLine(pe, numberOfAccentedVowelPhonemes,
                    estimateMissingPhonemes, rhymeModel, wordToRhyme, false, "");
            //System.out.println(proposedLine);
            toReturn.append( proposedLine + "\n");
            // find last word
            String[] aS = proposedLine.split(" ");
            wordToRhyme = aS[ aS.length-1 ];
            // find line that rhymes
            String wordToRhymePhonemes = pe.phonemesForLine(wordToRhyme);
            String rhymeFound = rhymeModel.findRhymeFromPhonemes(wordToRhymePhonemes);
            if (rhymeFound.equals("")) {
                System.out.println("no rhyme available for " + wordToRhyme);
                generatedOutput = gen.generateStochasticBeamSearch("");
            } else {
                generatedOutput = gen.generateStochasticBeamSearch(wordToRhyme);
            }
            toReturn.append(generatedOutput[1] + "\n");
        }
        System.out.println("\n" + toReturn.toString());
    }

    // finds the words in the language model that don't have phoneme models
    public void findWordsWithoutPhonemeModels() {

//        ClassBasedBigramModel cbbm = new ClassBasedBigramModel();        
//        String fileLocation = "./build/classes/classnphone/data/shakespeare-sonnetsAll-tagged.txt";
        WordBasedBigramModel cbbm = new WordBasedBigramModel();
//        String fileLocation = "./build/classes/classnphone/data/shakespeare-sonnetsAll.txt";
        String fileLocation = "./build/classes/epogees/data/verse.txt";
        List languageModel = readFileMakeList( fileLocation );
        cbbm.readModelList( languageModel );
        
        PhonemeEvaluation pe = new PhonemeEvaluation();
        String phonemeFileLocation = "./build/classes/epogees/data/cmudict.0.7a";
        List phonemeModelLines = readFileMakeList( phonemeFileLocation );
        pe.readPhonemeModel(phonemeModelLines, cbbm);
        phonemeModelLines = readFileMakeList( "./build/classes/epogees/data/additional-pronunciations.txt" );
        pe.readPhonemeModel(phonemeModelLines, cbbm);
        
        List allWordsInModel = cbbm.getListOfAllWords();

        int hasPronounciation = 0;
        int hasNoPronounciation = 0;
        Collections.sort( allWordsInModel);
        Iterator i = allWordsInModel.iterator();
        while ( i.hasNext() ) {
            String word = i.next().toString();
            String phonemes = pe.phonemesForLine(word);
            if ( phonemes.equals("") ) {
                //System.out.println( word + " |" + phonemes + "|");
                System.out.println( "No pronunciation:" + word );
                hasNoPronounciation++;
            } else {
                System.out.println( "Has pronunciation:" + word );
                hasPronounciation++;
            }
        }

        System.out.println( hasPronounciation + " words have pronunciation, " + hasNoPronounciation + " words have No pronunciation.");
        
    }
    
    public void readAndPrintRhymes() {
        RhymeModel rhymeModel = new RhymeModel();

        ClassBasedBigramModel cbbm = new ClassBasedBigramModel();        
        String fileLocation = "./build/classes/classnphone/data/shakespeare-sonnetsAll-tagged.txt";
        List languageModel = readFileMakeList( fileLocation );
        //System.out.println( "lines in language model file: " + languageModel.size() );
        cbbm.readModelList( languageModel );
        //System.out.println( cbbm.toString() );

        String inputFileLocation = "./build/classes/classnphone/data/cmudict.0.7a";
        List allLines = new ArrayList();
        
        BufferedReader inputStream = null;
        String currentLine = new String();

        try {
            inputStream = new BufferedReader(new FileReader( inputFileLocation  ) );

            while ( (currentLine = inputStream.readLine()) != null ) {
                if ( ! currentLine.startsWith(";") ) {
                    allLines.add(currentLine);
                    //System.out.println( currentLine );
                }
            }
        } catch (Exception e) {
            System.out.println( "probable misformat in: " + currentLine);
            e.printStackTrace();
        }

        rhymeModel.readRhymeList( allLines, cbbm );

        rhymeModel.printAll();
        
    }
    
    public List readFileMakeList( String inputFileLocation  ) {

        BufferedReader inputStream = null;
        String currentLine = new String();
        List toReturn = new ArrayList();

        try {
            inputStream = new BufferedReader(new FileReader( inputFileLocation  ) );

            while ( (currentLine = inputStream.readLine()) != null ) {
                if ( ! currentLine.startsWith(";") ) {
                    toReturn.add(currentLine);
                }
            }
        } catch (Exception e) {
            System.out.println( "probable misformat in: " + currentLine);
            e.printStackTrace();
        }
        
        return toReturn;
    }

    public void setInitialPhonemeValues( PhonemeEvaluation pe ) {
        // internal rhyme
        pe.setAlliterationWeight(10);
        pe.setAssonanceWeight(9);
        // back vowels
        pe.setPhonemesAndWeightsPairs( UH, 10 );
        pe.setPhonemesAndWeightsPairs( AO, 10 );
        pe.setPhonemesAndWeightsPairs( ER, 10 );
        pe.setPhonemesAndWeightsPairs( UW, 10 );
        pe.setPhonemesAndWeightsPairs( AW, 10 );
        pe.setPhonemesAndWeightsPairs( AY, 10 );
        // central vowels
        pe.setPhonemesAndWeightsPairs( AH, 5 );
        pe.setPhonemesAndWeightsPairs( EH, 5 );
        pe.setPhonemesAndWeightsPairs( OW, 5 );
        pe.setPhonemesAndWeightsPairs( OY, 5 );
        // front vowels
        pe.setPhonemesAndWeightsPairs( AA, 1 );
        pe.setPhonemesAndWeightsPairs( AE, 1 );
        pe.setPhonemesAndWeightsPairs( EY, 1 );
        pe.setPhonemesAndWeightsPairs( IH, 1 );
        pe.setPhonemesAndWeightsPairs( IY, 1 );

        // affricatives
        pe.setPhonemesAndWeightsPairs( CH, 0 );
        pe.setPhonemesAndWeightsPairs( JH, 0 );
        // nasals
        pe.setPhonemesAndWeightsPairs( M, 0 );
        pe.setPhonemesAndWeightsPairs( N, 0 );
        pe.setPhonemesAndWeightsPairs( NG, 0 );
        // approximants
        pe.setPhonemesAndWeightsPairs( L, 5 );
        pe.setPhonemesAndWeightsPairs( R, 5 );
        pe.setPhonemesAndWeightsPairs( W, 5 );
        pe.setPhonemesAndWeightsPairs( Y, 5 );
        // plosives - voiced
        pe.setPhonemesAndWeightsPairs( B, 3 );
        pe.setPhonemesAndWeightsPairs( D, 3 );
        pe.setPhonemesAndWeightsPairs( G, 3 );
        // plosives - voiceless
        pe.setPhonemesAndWeightsPairs( P, 7 );
        pe.setPhonemesAndWeightsPairs( T, 7 );
        pe.setPhonemesAndWeightsPairs( K, 7 );
        // fricatives - sibilant
        pe.setPhonemesAndWeightsPairs( S, 1 );
        pe.setPhonemesAndWeightsPairs( SH, 1 );
        pe.setPhonemesAndWeightsPairs( Z, 1 );
        pe.setPhonemesAndWeightsPairs( ZH, 1 );
        // fricatives - non-sibilant
        pe.setPhonemesAndWeightsPairs( F, 3 );
        pe.setPhonemesAndWeightsPairs( TH, 3 );
        pe.setPhonemesAndWeightsPairs( DH, 3 );
        pe.setPhonemesAndWeightsPairs( V, 3 );
        pe.setPhonemesAndWeightsPairs( HH, 3 );
    }
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
