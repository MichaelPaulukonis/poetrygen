

package epogees.generation;

public class PhonemeEvaluationResults {

    // score after adding/subtracting adjustments
    int totalScore = 0;
    // score for matching phoneme sounds
    int phonemicScore = 0;
    // adjustment for matching number of syllables
    int syllableCountScore = 0;
    // bonus for any alliteration (consonant repetitions at beginning of words)
    int alliterationScore = 0;
    // bonus for assonance (vowel repetitions)
    int assonanceScore = 0;
    
    // details about evaluation
    String evaluationDetails = new String();

    public String getEvaluationDetails() {
        return evaluationDetails;
    }

    public void setEvaluationDetails(String evaluationDetails) {
        this.evaluationDetails = evaluationDetails;
    }

    public String toString() {
//       return  totalScore + " = " + phonemicScore + " + " + assonanceScore + " + " + alliterationScore +
//                " - " + syllableCountScore; 

        return evaluationDetails;
    }
    
    
    // getters and setters
    
    public int getAlliterationScore() {
        return alliterationScore;
    }

    public void setAlliterationScore(int alliterationScore) {
        this.alliterationScore = alliterationScore;
    }

    public int getAssonanceScore() {
        return assonanceScore;
    }

    public void setAssonanceScore(int assonanceScore) {
        this.assonanceScore = assonanceScore;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public int getSyllableCountScore() {
        return syllableCountScore;
    }

    public void setSyllableCountScore(int lengthAdjustment) {
        this.syllableCountScore = lengthAdjustment;
    }

    public int getPhonemicScore() {
        return phonemicScore;
    }

    public void setPhonemicScore(int phonemicScore) {
        this.phonemicScore = phonemicScore;
    }

}
