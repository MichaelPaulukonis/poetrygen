
package epogees.model.language;

import epogees.generation.*;
import epogees.ui.*;
import epogees.model.*;
import java.util.List;


public abstract class LanguageModel {

    abstract public void changeUsePunctuation();
    abstract public boolean getUsePunctuation();
    abstract public void setUsePunctuation( boolean b );
    abstract public List getListOfAllWords();

    abstract public void readModelList( List fileContents );

    abstract public boolean isInLanguageModel( String word );

    abstract public String generateLine( PhonemeEvaluation pe, int numberOfAccentedVowelPhonemes, 
            boolean estimateMissing, RhymeModel rm, String wordToRhyme, boolean isFirstInPair, String enjambment );

    abstract public String generateLineReplacingWord( String oldLine, PhonemeEvaluation pe, 
            boolean estimateMissing, boolean isRhymed );

    abstract public String generateWord( String previousWord, String followingWord ); 
    
//    abstract public String toStringStarting();
//    abstract public String toStringAllSeen();

    abstract public void printOverviewToApplet( EpogeesApplet applet );

    abstract public void printLanguageModelToApplet( EpogeesApplet applet );
        
    abstract public void printNextWordsToApplet ( EpogeesApplet applet, String previousToken );

    abstract public void printPreviousWordsToApplet ( EpogeesApplet applet, String nextToken );
    
}