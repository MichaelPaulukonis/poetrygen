
package epogees.generation;

public class PhonemeLine implements Comparable {

    String plainLine = new String();

    String phonemeString = new String();
    
    PhonemeEvaluationResults evaluation = new PhonemeEvaluationResults();

    
    public String toString() {

        return plainLine + 
                "\n  " + phonemeString + 
                "\n  " + evaluation.toString() ;
//        return evaluation.toString() + 
//                "   |  " + plainLine + 
//                "   |  " + phonemeString ;
    }
    
    public String toPrint() {
        return plainLine;
    }


    public String getLastWord() {

        String toReturn = "";

        String[] s = plainLine.split(" ");

        toReturn = s[s.length-1];

        return toReturn;
    }

    public int compareTo(Object o) {

        PhonemeEvaluationResults otherResults = ((PhonemeLine) o).getEvaluation();

        if (otherResults.getTotalScore() > (this.getEvaluation()).getTotalScore()) {
            return -1;
        } else if (otherResults.getTotalScore() < (this.getEvaluation()).getTotalScore()) {
            return 1;
        }
        //return 0;
        // if they are equal, arbitrarily pick this one as greater
        return 1;

    //throw new UnsupportedOperationException("Not supported yet.");
    }

    // getters and setters
    
    public PhonemeEvaluationResults getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(PhonemeEvaluationResults evaluation) {
        this.evaluation = evaluation;
    }

    public String getPhonemeString() {
        return phonemeString;
    }

    public void setPhonemeString(String phonemeString) {
        this.phonemeString = phonemeString;
    }

    public String getPlainLine() {
        return plainLine;
    }

    public void setPlainLine(String plainLine) {
        this.plainLine = plainLine;
    }

    
}
