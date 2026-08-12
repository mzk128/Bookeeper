package com.example.bookeeper.domain.model

/**
 * A monetary amount stored in the smallest currency unit.
 *
 * Bookeeper currently uses CNY, so one unit is one fen. This type is signed so
 * it can also represent balances and calculated differences. Individual
 * transactions enforce a strictly positive amount separately.
 */
@JvmInline
value class Money(val cents: Long) {
    operator fun plus(other: Money): Money = Money(Math.addExact(cents, other.cents))

    operator fun minus(other: Money): Money = Money(Math.subtractExact(cents, other.cents))

    companion object {
        val Zero = Money(0L)
    }
}
