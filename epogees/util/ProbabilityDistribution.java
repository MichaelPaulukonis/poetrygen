
package epogees.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

public class ProbabilityDistribution {

    List itemsAndBounds = new ArrayList();
    int highestBound = 0;

    // used for random word generation
    Random random = new Random(System.currentTimeMillis());

    class ItemsAndBounds {
        String item = new String();

        // upper bound (inclusive) of distribution region
        int upperBound = 0;

        public String getItem() {
            return item;
        }

        public void setItem(String item) {
            this.item = item;
        }

        public int getUpperBound() {
            return upperBound;
        }

        public void setUpperBound(int upperBound) {
            this.upperBound = upperBound;
        }
        //int lowerBound = 0;
        
    }
    
    
    public ProbabilityDistribution( Hashtable iAndB ) {
        // create list of items and bounds
        Enumeration allKeys = iAndB.keys();

        int ub = 0;
        
        while (allKeys.hasMoreElements()) {
            String aKey = allKeys.nextElement().toString();
            ItemsAndBounds i = new ItemsAndBounds();
            i.setItem( aKey );
            ub += ((Integer)iAndB.get(aKey)).intValue();
            i.setUpperBound( ub );
            itemsAndBounds.add( i );
        }
        
        highestBound = ub;
    }
    
    public String getItemFromWeighedDistribution() {
        
        int ri = random.nextInt(highestBound);

        // can cut runtime in half by doing divide-and-conquer if need be
        for (int x=0; x<highestBound; x++ ) {
            int localHighestBound = ((ItemsAndBounds)(itemsAndBounds.get(x))).getUpperBound();
            if ( ri <= localHighestBound ) {
                return ((ItemsAndBounds)(itemsAndBounds.get(x))).getItem();
            }
        }
        return "";
    }
    
//    @Override
//    public String toString() {
//        StringBuffer toReturn = new StringBuffer();
//        return "";
//    }
}
