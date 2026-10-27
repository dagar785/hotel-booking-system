package com.hbs.payment

/** Test card numbers -> gateway tokens (like Stripe's test cards). Any other valid number succeeds. */
object CardTokenizer {
    const val OK = "tok_ok"
    const val DECLINED = "tok_declined"
    const val THREE_DS = "tok_3ds"
    /** First attempt throws IOException, a retry with the SAME idempotency key succeeds. */
    const val FLAKY = "tok_flaky"
    /** Charge succeeds server-side but the client sees a timeout. Recover with status(). */
    const val TIMEOUT = "tok_timeout"

    fun tokenize(cardNumber: String): String = when (cardNumber.filter(Char::isDigit)) {
        "4000000000000002" -> DECLINED
        "4000000000003220" -> THREE_DS
        "4000000000000341" -> FLAKY
        "4000000000000259" -> TIMEOUT
        else -> OK
    }
}
