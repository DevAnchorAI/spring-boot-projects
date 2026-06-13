package com.example.urlshortener.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Base62Test {

    @Test
    void encodeDecode() {
        long[] vals = {0L, 1L, 10L, 61L, 62L, 12345L, Long.MAX_VALUE/1000000};
        for (long v : vals) {
            String enc = Base62.encode(v);
            long dec = Base62.decode(enc);
            assertEquals(v, dec);
        }
    }
}

