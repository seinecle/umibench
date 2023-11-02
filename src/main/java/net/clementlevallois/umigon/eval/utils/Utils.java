/*
 * Copyright Clement Levallois 2021-2023. License Attribution 4.0 Intertnational (CC BY 4.0)
 */
package net.clementlevallois.umigon.eval.utils;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author LEVALLOIS
 */
public class Utils {
    public static int findClosestLowerElement(int P, List<Integer> C) {
        int closestLowerElementIndex = Collections.binarySearch(C, P);

        if (closestLowerElementIndex >= 0) {
            // Exact match found, the element at closestLowerElementIndex is equal to P
            return C.get(closestLowerElementIndex);
        } else {
            // No exact match found, binarySearch returns the negative insertion point
            int insertionPoint = -(closestLowerElementIndex + 1);

            // Check if insertionPoint is greater than 0 to avoid index out of bounds
            if (insertionPoint > 0) {
                // Element at insertionPoint - 1 is the closest lower element
                return C.get(insertionPoint - 1);
            } else {
                // There is no lower element, P is smaller than the first element in C
                throw new IllegalArgumentException("No lower element found");
            }
        }
    }
}