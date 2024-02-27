/*
 * Copyright Clement Levallois 2021-2023. License Attribution 4.0 Intertnational (CC BY 4.0)
 */
package net.clementlevallois.umigon.eval.tests;

import net.clementlevallois.umigon.eval.datasets.Apple;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

/**
 *
 * @author LEVALLOIS
 */
public class AppleTest {

    @Test
    public void conductTests() {
        Apple apple = new Apple();
        apple.read();
        assertThat(apple.getGoldenLabels().size()).isEqualTo(apple.getNumberOfEntries());
    }

}
