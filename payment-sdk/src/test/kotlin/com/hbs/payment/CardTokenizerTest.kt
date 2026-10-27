package com.hbs.payment

import org.junit.Assert.assertEquals
import org.junit.Test

class CardTokenizerTest {
    @Test fun `maps test cards ignoring spaces`() {
        assertEquals(CardTokenizer.DECLINED, CardTokenizer.tokenize("4000 0000 0000 0002"))
        assertEquals(CardTokenizer.THREE_DS, CardTokenizer.tokenize("4000000000003220"))
    }

    @Test fun `other numbers succeed`() = assertEquals(CardTokenizer.OK, CardTokenizer.tokenize("4242424242424242"))
}
