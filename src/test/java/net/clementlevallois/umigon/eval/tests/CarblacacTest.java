/*
 * Copyright Clement Levallois 2021-2023. License Attribution 4.0 Intertnational (CC BY 4.0)
 */
package net.clementlevallois.umigon.eval.tests;

import net.clementlevallois.umigon.eval.datasets.Carblacac;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

/**
 *
 * @author LEVALLOIS
 */
public class CarblacacTest {

    @Test
    public void conductTests() {
        Carblacac carblacac = new Carblacac();
        carblacac.read();
        assertThat(carblacac.getGoldenLabels().size()).isEqualTo(carblacac.getNumberOfEntries());
    }

}
