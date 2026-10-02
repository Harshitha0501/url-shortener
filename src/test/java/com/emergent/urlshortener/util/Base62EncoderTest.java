package com.emergent.urlshortener.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Base62EncoderTest {

    @Test
    void encodesZero() {
        assertThat(Base62Encoder.encode(0)).isEqualTo("0");
    }

    @Test
    void encodesKnownValues() {
        assertThat(Base62Encoder.encode(61)).isEqualTo("z");
        assertThat(Base62Encoder.encode(62)).isEqualTo("10");
        assertThat(Base62Encoder.encode(125)).isEqualTo("21");
    }

    @Test
    void randomHasRequestedLength() {
        for (int len : new int[]{4, 7, 12, 32}) {
            String s = Base62Encoder.random(len);
            assertThat(s).hasSize(len);
            assertThat(s).matches("[0-9A-Za-z]+");
        }
    }
}
