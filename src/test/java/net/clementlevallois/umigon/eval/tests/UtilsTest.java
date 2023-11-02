/*
 * Copyright Clement Levallois 2021-2023. License Attribution 4.0 Intertnational (CC BY 4.0)
 */
package net.clementlevallois.umigon.eval.tests;

import java.util.List;
import net.clementlevallois.umigon.eval.utils.Utils;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;


/**
 *
 * @author LEVALLOIS
 */
public class UtilsTest {

    @Test
    public void getClosestCeilingNumber() {
        int P = 42; // Your integer parameter
        List<Integer> C = List.of(10, 20, 30, 40, 50, 60); // Your sorted collection

        try {
            int closestLowerElement = Utils.findClosestLowerElement(P, C);
            assertThat(closestLowerElement).isEqualTo(40);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }

    }

}
