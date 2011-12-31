
package epogees.util;


public class Word {

    private String text;
    private String phonemesPlain;
    //private String phonemesFormatted;

    // constructor
    
    public Word(String t, String p) {
        text = t.trim();
        phonemesPlain = p;

//        // make formatted phonemes
//        StringBuffer phB = new StringBuffer();
//        String[] aA = p.split( " " );
//        for ( int x=0; x<aA.length ; x++ ) {
//            String ph = aA[x];
//            // no stress
//            if ( ph.endsWith("0")) {
//                ph = ph.replaceAll( "0$", "" );
//                phB.append( ph );
//            // primary stress
//            } else if ( ph.endsWith("1")) {
//                ph = ph.replaceAll( "1$", "" );
//                phB.append( "<b>" + ph + "</b>");
//            // secondary stress
//            } else if ( ph.endsWith("2")) {
//                ph = ph.replaceAll( "2$", "" );                
//                phB.append( "<i>" + ph + "</i>");
//            } else {
//                phB.append( ph );
//            }
//
//            phB.append( " " );
//        }
//        
//        phonemesFormatted = phB.toString();
    }

    
    // getters and setters
    
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPhonemesPlain() {
        return phonemesPlain;
    }

    public void setPhonemesPlain(String phonemesPlain) {
        this.phonemesPlain = phonemesPlain;
    }

//    public String getPhonemesFormatted() {
//        return phonemesFormatted;
//    }
//
//    public void setPhonemesFormatted(String phonemesFormatted) {
//        this.phonemesFormatted = phonemesFormatted;
//    }
    
}
