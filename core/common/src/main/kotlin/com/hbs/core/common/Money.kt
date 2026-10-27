package com.hbs.core.common

import java.util.Locale

/** Money in minor units (cents). Never use Double for money. */
data class Money(val minor: Long, val currency: String = "USD") : Comparable<Money> {
    operator fun plus(other: Money): Money {
        require(currency == other.currency) { "Currency mismatch: $currency vs ${other.currency}" }
        return copy(minor = minor + other.minor)
    }

    operator fun times(n: Int): Money = copy(minor = minor * n)

    override fun compareTo(other: Money): Int {
        require(currency == other.currency) { "Currency mismatch: $currency vs ${other.currency}" }
        return minor.compareTo(other.minor)
    }

    fun format(): String = String.format(Locale.ROOT, "%s %d.%02d", currency, minor / 100, minor % 100)

    companion object {
        fun usd(minor: Long) = Money(minor, "USD")
    }
}
