package io.retit.spring.carbon;

import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Example service that does some very complex processing.
 */
@Service
public class TestService {

    public void veryComplexBusinessFunction() throws InterruptedException {
        naiveSortingWithONSquareComplexity(generateRandomInputArray(30000));
    }

    private static int[] generateRandomInputArray(int size) {
        int array[] = new int[size];

        for (int i = 0; i < size; i++) {
            array[i] = new Random().nextInt(1000000);
        }

        return array;
    }

    // O(n²)
    private static int[] naiveSortingWithONSquareComplexity(int[] inputArray) {
        // Outer loop
        for (int i = 0; i < inputArray.length; i++) {

            // Inner nested loop pointing 1 index ahead
            for (int j = i + 1; j < inputArray.length; j++) {

                // Checking elements
                int temp = 0;
                if (inputArray[j] < inputArray[i]) {

                    // Swapping
                    temp = inputArray[i];
                    inputArray[i] = inputArray[j];
                    inputArray[j] = temp;
                }
            }

        }
        return inputArray;
    }
}
