package com.example.bookeeper.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class TopLevelDestinationTest {
    @Test
    fun routesAreUnique() {
        val routes = TopLevelDestination.entries.map(TopLevelDestination::route)

        assertEquals(routes.size, routes.toSet().size)
    }

    @Test
    fun homeIsTheFirstTopLevelDestination() {
        assertEquals(TopLevelDestination.HOME, TopLevelDestination.entries.first())
        assertEquals("home", TopLevelDestination.HOME.route)
    }
}
