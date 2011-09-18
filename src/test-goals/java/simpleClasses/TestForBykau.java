package simpleClasses;

/**
 * @version 1.0 19-Oct-2010 16:10:53
 * @author: Hut
 */
public class TestForBykau {


    public void printAllCombinations(int[] lengths){

        int[] divisors = new int[lengths.length + 1];
        divisors[divisors.length - 1] = 1;
        for(int i = lengths.length - 1;i >= 0; i--){
            divisors[i] = lengths[i] * divisors[i+1];
        }

        int totalNumberOfCombinations = divisors[0];
        System.out.println(String.format("Total number of combinations is %d", totalNumberOfCombinations));

        for(int i = 0; i < totalNumberOfCombinations; i++){
            int[] indexArray = getIndexArray(lengths, divisors, i);
            for(int j = 0; j < indexArray.length; j++){
                System.out.print(String.format("%d ", indexArray[j]));
            }
            System.out.println("");
        }


    }

    protected int[] getIndexArray(int[] lengths, int[] divisors, int index){
        int[] output = new int[lengths.length];
        int rest = index;
        for(int i= 0; i < output.length; i++){
            output[i] = rest / divisors[i+1];
            rest = rest % divisors[i+1];
        }
        return output;
    }


    public static void main(String[] args) {
        int[] inData = new int[]{4, 3, 5};
        new TestForBykau().printAllCombinations(inData);
     }



}
