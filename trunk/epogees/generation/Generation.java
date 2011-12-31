
package epogees.generation;

import epogees.model.*;
import epogees.model.language.*;
import epogees.ui.*;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

public class Generation {

    final static String N_RANDOM = "N Random";
    final static String STOCHASTIC_BEAM_SEARCH = "Stochastic Beam Search";

    private PhonemeEvaluation pe;
    private LanguageModel lm;
    private RhymeModel rm;

    int nRandPopulationSize = 10;
    int stochasticBeamPopulationSize = 6;
    int stochasticBeamGenerations = 5;
    boolean showGenerationDetails = false;
    // used for length of line
    int numberOfAccentedVowelPhonemes = 7;
    boolean estimateMissingPhonemes = true;

    // number of lines to generate
    int linesToGenerate = 4;
    // search algorithm to use
    String searchAlgorithm = STOCHASTIC_BEAM_SEARCH;

    // rhymeschemes are: "none", "aabb", "abab", "abba"
    String rhymeScheme = "aabb";

    String enjambment = "";
    boolean useEnjambment = true;
    boolean printTrailingNewline = true;

    public Generation( PhonemeEvaluation p, LanguageModel c, RhymeModel r ) {
        pe = p;
        lm = c;
        rm = r;
    }


    // SETTERS AND GETTERS

    public void switchUseEnjambment() {
        if ( useEnjambment ) {
            useEnjambment = false;
            enjambment = "";
        } else {
            useEnjambment = true;
        }
    }

    public void switchTrailingNewline() {
        if ( printTrailingNewline ) {
            printTrailingNewline = false;
        } else {
            printTrailingNewline = true;
        }
    }


    public void setSearchAlgorithmStochasticBeam() {
        searchAlgorithm = STOCHASTIC_BEAM_SEARCH;
    }

    public void setSearchAlgorithmNRandom() {
        searchAlgorithm = N_RANDOM;
    }

    // sets the rhyme scheme string (something like: "a a b b")
    public void setRhymeScheme(String stringRhymeScheme) {
        rhymeScheme = stringRhymeScheme;
    }

    public int getLinesToGenerate() {
        return linesToGenerate;
    }

    public void setLinesToGenerate(int linesToGenerate) {
        this.linesToGenerate = linesToGenerate;
    }

    public void setNRandPopSize(int populationSize) {
        nRandPopulationSize = populationSize;
    }

    public void setStochBeamGenerations(int generations) {

        stochasticBeamGenerations = generations;
    }

    public void setStochBeamPopSize(int populationSize) {
        stochasticBeamPopulationSize = populationSize;
    }

    public void changeShowGenerationDetails() {
        if ( showGenerationDetails ) {
            showGenerationDetails = false;
        } else {
            showGenerationDetails = true;
        }
    }

    public void setNumberOfAccentedVowelPhonemes(int numberOfAccentedVowelPhonemes) {
        this.numberOfAccentedVowelPhonemes = numberOfAccentedVowelPhonemes;
    }

    public void setEstimateMissingPhonemes(boolean estimateMissingPhonemes) {
        this.estimateMissingPhonemes = estimateMissingPhonemes;
    }

    public boolean getEstimateMissingPhonemes() {
        return estimateMissingPhonemes;
    }



    // GENERATOR

    // generate a number of lines, attending to the rhymeScheme which has been set
    public void generateVerse( EpogeesApplet applet, String wordToRhyme  ) {
        String[] generatedOutput;

        String futureRhymeA = "";
        String futureRhymeB = "";

        // if there is no rhyme scheme, just generate the lines
        if (rhymeScheme.equalsIgnoreCase("none")) {
            for (int x = 1; x <= linesToGenerate; x++) {

                if (searchAlgorithm.equals(STOCHASTIC_BEAM_SEARCH)) {

                    System.out.println("\n* * * * * * * * * * * * * * \n");
                    System.out.println("LINE: " + x);

                    generatedOutput = generateStochasticBeamSearch(wordToRhyme);
                } else {
                    generatedOutput = generateNRandom(wordToRhyme);
                }

                generatedOutput[1] = punctuationFinalReplacement(generatedOutput[1], x);
                applet.printDetails(generatedOutput[0]);
                applet.printOutput(generatedOutput[1]);
            }

        // otherwise, there is a rhyme scheme
        } else {

            for (int x = 1; x <= linesToGenerate; x++) {

                if (rhymeScheme.equalsIgnoreCase("abab")) {
                    if (x % 2 == 0) {
                        wordToRhyme = futureRhymeB;
                    } else {
                        wordToRhyme = futureRhymeA;
                    }
                }

                if (!wordToRhyme.equalsIgnoreCase("")) {
                    int rhymesAvailable = applet.rm.countRhymesAvailableForWord(wordToRhyme);
                    if (rhymesAvailable == 0) {
                        applet.printDetails( "Unable to find line that rhymes with: " + wordToRhyme + "\n\n");
                        //System.out.println("unable to find line that rhymes with: " + wordToRhyme);
                        wordToRhyme = "";
                    }
                }

                if (searchAlgorithm.equals(STOCHASTIC_BEAM_SEARCH)) {

                    System.out.println("\n* * * * * * * * * * * * * * \n");
                    System.out.println("LINE: " + x);

                    generatedOutput = generateStochasticBeamSearch(wordToRhyme);
                } else {
                    generatedOutput = generateNRandom(wordToRhyme);
                }

                generatedOutput[1] = punctuationFinalReplacement(generatedOutput[1], x);
                applet.printDetails(generatedOutput[0]);
                applet.printOutput(generatedOutput[1]);

                if (rhymeScheme.equalsIgnoreCase("abab")) {
                    // the next line need not rhyme
                    wordToRhyme = "";

                    // in 2 turns will rhyme this word, so:
                    // get word to rhyme from generatedOutput
                    String[] a = generatedOutput[1].split(" ");

                    // if its an even iteration, we have found a future rhyme B
                    if (x % 2 == 0) {
                        if ( futureRhymeB.equalsIgnoreCase("")) {
                            futureRhymeB = a[a.length - 1];
                            futureRhymeB = futureRhymeB.trim();
                            futureRhymeB = futureRhymeB.replaceAll("\\W","");
                        } else {
                            futureRhymeB = "";
                        }

                    // otherwise if it's an odd iteration (not div by 2), we have found a future rhyme A
                    } else {
                        if ( futureRhymeA.equalsIgnoreCase("")) {
                            futureRhymeA = a[a.length - 1];
                            futureRhymeA = futureRhymeA.trim();
                            futureRhymeA = futureRhymeA.replaceAll("\\W","");
                        } else {
                            futureRhymeA = "";
                        }
                    }

                } else if (rhymeScheme.equalsIgnoreCase("aabb")) {

                    // if it's an even iteration, then the next line need not rhyme
                    if (x % 2 == 0) {
                        wordToRhyme = "";

                    // otherwise if its an odd iteration, the next line should rhyme with this
                    } else {
                        // get word to rhyme from generatedOutput
                        String[] a = generatedOutput[1].split(" ");
                        wordToRhyme = a[a.length - 1];
                        wordToRhyme = wordToRhyme.trim();
                        wordToRhyme = wordToRhyme.replaceAll("\\W","");
                    }
                }
            }

            if ( printTrailingNewline ) {
                applet.printOutput("\n");
            }

            wordToRhyme = "";
            futureRhymeA = "";
            futureRhymeB = "";
            enjambment = "";
        }
    }

    // turning punctuation tokens into punctuation, and capitalizing appropriately
    public String punctuationFinalReplacement( String input, int lineNumber ) {
        String toReturn = input;

        if ( lm.getUsePunctuation() ) {

            toReturn = toReturn.replaceAll(" PERIODPUNCTUATION", ".");
            toReturn = toReturn.replaceAll(" QUESTIONMARKPUNCTUATION", "?");
            toReturn = toReturn.replaceAll(" EXCLAMATIONMARKPUNCTUATION", "!");

            // with enjambment, they may start a line
            toReturn = toReturn.replaceAll("PERIODPUNCTUATION", ".");
            toReturn = toReturn.replaceAll("QUESTIONMARKPUNCTUATION", "?");
            toReturn = toReturn.replaceAll("EXCLAMATIONMARKPUNCTUATION", "!");

            toReturn = toReturn.replaceAll(" COMMAPUNCTUATION", ",");
            toReturn = toReturn.replaceAll(" SEMICOLONPUNCTUATION", ";");
            toReturn = toReturn.replaceAll(" COLONPUNCTUATION", ":");

            // with enjambment, they may start a line
            toReturn = toReturn.replaceAll("COMMAPUNCTUATION", ",");
            toReturn = toReturn.replaceAll("SEMICOLONPUNCTUATION", ";");
            toReturn = toReturn.replaceAll("COLONPUNCTUATION", ":");

            // make sure that the first word is not punctuation
            if (toReturn.charAt(0) == ',' || toReturn.charAt(0) == ';' || toReturn.charAt(0) == ':' ||
                toReturn.charAt(0) == '.' || toReturn.charAt(0) == '?' || toReturn.charAt(0) == '!') {

                toReturn = toReturn.substring(2, toReturn.length());
            }


            // look through the line
            for (int x=0; x < toReturn.length(); x++) {

                // capitalize the character that is 2 spaces after punctuation
                // see if the character is punctuation
                if (toReturn.charAt(x) == '.' || toReturn.charAt(x) == '?' || toReturn.charAt(x) == '!') {

                    int nextSentIndex = x + 2;
                    if (nextSentIndex < toReturn.length()) {
                        String s = toReturn.substring(nextSentIndex, nextSentIndex + 1);
                        String su = s.toUpperCase();
                        String replacer = "\\. " + s;
                        String replacee = ". " + su;
                        toReturn = toReturn.replaceAll(replacer, replacee);
                        replacer = "\\? " + s;
                        replacee = "? " + su;
                        toReturn = toReturn.replaceAll(replacer, replacee);
                        replacer = "\\! " + s;
                        replacee = "! " + su;
                        toReturn = toReturn.replaceAll(replacer, replacee);
                    }
                }
            }

            // capitalize the first character of the line
            //if ( enjambment.equals("") || lineNumber == 1 ) {
            if ( ! useEnjambment || lineNumber == 1 ) {
                String s = toReturn.substring(0, 1);
                String su = s.toUpperCase();
                toReturn = toReturn.replaceFirst(s, su);
            }


            if (useEnjambment) {
                String[] s = toReturn.split(" ");
                enjambment = s[s.length - 1];
            }

            // handle end-of-line punctuation
            //if ( enjambment.equals("") ) {
            if ( ! useEnjambment || lineNumber == linesToGenerate ) {
                String lastChar = toReturn.substring(toReturn.length() - 1, toReturn.length());
                if (lastChar.equals(",") || lastChar.equals(":") || lastChar.equals(";")) {
                    toReturn = toReturn.substring(0, toReturn.length() - 2);
                    toReturn = toReturn + ".";
                } else if (lastChar.equals(".") || lastChar.equals("!") || lastChar.equals("?")) {
                    // do nothing
                } else {
                    toReturn = toReturn + ".";
                }
            }
        }

        toReturn = toReturn + "\n";

        return toReturn;
    }


    // generate a line using stochastic beam search
    // returns array: element 0 is line, element 1 is details
    public String[] generateStochasticBeamSearch( String wordToRhyme ) {

        String[] toReturn = new String[2];
        StringBuffer detailsOutput = new StringBuffer();
        StringBuffer generationOutput = new StringBuffer();
        boolean isRhymed = true;
        if ( wordToRhyme.equals("") ) {
            isRhymed = false;
        }

        detailsOutput.append("Generating line:\n");
        detailsOutput.append("  Stochastic Beam Search.  Population Size: " + stochasticBeamPopulationSize + ", Iterations: " + stochasticBeamGenerations + "\n");
        detailsOutput.append("  Evaluation weights: " + pe.toString() + "\n");

        SortedSet evaluatedLineSet = new TreeSet();
        SortedSet topN = new TreeSet();

        // generate inital set of lines
        if (showGenerationDetails) {
            System.out.println("Generating initial set of lines: ");
        }
        generateAndEvaluate( evaluatedLineSet, stochasticBeamPopulationSize, wordToRhyme  );


        for (int i = 1; i <= stochasticBeamGenerations; i++) {

            int popCount = 0;
            int topNCounter = 0;

            System.out.println();
            System.out.println("ITERATION: " + i );
            //System.out.println( "" );
//            if ( showGenerationDetails ) {
//                detailsOutput.append( "Iteration: " + i + "\n");
//            }

            // look through sorted set to add details to details buffer and to top n examples
            Iterator it = evaluatedLineSet.iterator();
            while (it.hasNext()) {
                topNCounter++;
                PhonemeLine tpl2 = (PhonemeLine) it.next();

                if (topNCounter > stochasticBeamPopulationSize / 2) {
                    topN.add(tpl2);
                    topN.add(tpl2);
                }
            }

            // take top n examples, generate new word for each of them

            evaluatedLineSet = new TreeSet();
            //System.out.println("\nold lines:\n");
            Iterator iTop = topN.iterator();
            while (iTop.hasNext()) {
                popCount++;
                PhonemeLine itpl = (PhonemeLine) iTop.next();

                if ( showGenerationDetails ) {
                    System.out.println("\n Individual: " + popCount);
//                    System.out.println("");
                    System.out.println(" old line: " + itpl.toString());
                } else {
                    System.out.print( popCount + " ");
                }

                String newLine = lm.generateLineReplacingWord(itpl.getPlainLine(), pe, estimateMissingPhonemes, isRhymed );

//                newLine = newLine.replaceAll("PERIODPUNCTUATION", "\\.");
//                newLine = newLine.replaceAll("QUESTIONMARKPUNCTUATION", "\\?");
//                newLine = newLine.replaceAll("EXCLAMATIONMARKPUNCTUATION", "\\!");

                PhonemeLine pl = pe.evaluate(newLine);

                evaluatedLineSet.add(pl);
                if ( showGenerationDetails ) {
                    System.out.println( " new line: " + pl.toString() );
                }

            }

            topN = new TreeSet();
        }
        System.out.println();

        // find the last element of the set (i.e. the highest evaluated) and add it to output buffer
        PhonemeLine tpl = (PhonemeLine)evaluatedLineSet.last();
        generationOutput.append( tpl.toPrint() );

        if ( useEnjambment ) {
            enjambment = tpl.getLastWord();
        }

//        if ( ! showGenerationDetails ) {
            detailsOutput.append( tpl.toString() + "\n\n" );
//        }

        toReturn[0] = detailsOutput.toString();
        toReturn[1] = generationOutput.toString();

        return toReturn;
    }

    // generate a line by random sampling
    public String[] generateNRandom( String wordToRhyme ) {

        //int nRandPopulationSize = 10;

        String[] toReturn = new String[2];

        StringBuffer detailsOutput = new StringBuffer();
        StringBuffer generationOutput = new StringBuffer();

        detailsOutput.append("Generating line:\n");
        detailsOutput.append("  Random Sampling.  Number of Samples:" + nRandPopulationSize + "\n");
        detailsOutput.append("  Evaluation weights: " + pe.toString() + "\n");

        SortedSet evaluatedLineSet = new TreeSet();

        generateAndEvaluate( evaluatedLineSet, nRandPopulationSize, wordToRhyme  );

        PhonemeLine tpl = (PhonemeLine)evaluatedLineSet.last();

        generationOutput.append( tpl.toPrint() );

        if ( showGenerationDetails) {
            Iterator it = evaluatedLineSet.iterator();
            while (it.hasNext()) {
                PhonemeLine tpl2 = (PhonemeLine) it.next();
//                detailsOutput.append(tpl2.toString() + "\n");
                System.out.println( tpl2.toString() );
            }
//        } else {
//            detailsOutput.append( tpl.toString() + "\n");
        }
        detailsOutput.append( tpl.toString() + "\n\n");

        toReturn[0] = detailsOutput.toString();
        toReturn[1] = generationOutput.toString();

        return toReturn;
    }

    // generate a set of evaluated lines
    public void generateAndEvaluate( SortedSet evaluatedLineSet, int populationSize, String wordToRhyme ) {


        for ( int i=1; i<=populationSize; i++ ) {

            String proposedLine = lm.generateLine( pe, numberOfAccentedVowelPhonemes,
                    estimateMissingPhonemes, rm, wordToRhyme, false, enjambment );

            PhonemeLine pl = pe.evaluate(proposedLine);

            evaluatedLineSet.add( pl );
        }

        if (this.showGenerationDetails) {
            //System.out.println("  " + proposedLine + " "+ pe.phonemesForLine(proposedLine));
            Iterator i = evaluatedLineSet.iterator();
            while (i.hasNext()) {
                PhonemeLine tPl = (PhonemeLine) i.next();
                System.out.println(" " + tPl.toString());
            }
        }


    }


}
