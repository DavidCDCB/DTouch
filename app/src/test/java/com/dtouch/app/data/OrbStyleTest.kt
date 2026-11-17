package com.dtouch.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class OrbStyleTest {

    @Test
    fun fromIdReturnsMatchingStyle() {
        assertEquals(OrbStyle.CLASSIC, OrbStyle.fromId("classic"))
        assertEquals(OrbStyle.SOLID, OrbStyle.fromId("solid"))
        assertEquals(OrbStyle.RING, OrbStyle.fromId("ring"))
        assertEquals(OrbStyle.DOT, OrbStyle.fromId("dot"))
        assertEquals(OrbStyle.DUO, OrbStyle.fromId("duo"))
        assertEquals(OrbStyle.MIDNIGHT, OrbStyle.fromId("midnight"))
        assertEquals(OrbStyle.FROST, OrbStyle.fromId("frost"))
        assertEquals(OrbStyle.AQUA, OrbStyle.fromId("aqua"))
        assertEquals(OrbStyle.RAINBOW, OrbStyle.fromId("rainbow"))
        assertEquals(OrbStyle.AURORA, OrbStyle.fromId("aurora"))
        assertEquals(OrbStyle.PULSE, OrbStyle.fromId("pulse"))
        assertEquals(OrbStyle.COMET, OrbStyle.fromId("comet"))
    }

    @Test
    fun fromIdFallsBackToClassic() {
        assertEquals(OrbStyle.CLASSIC, OrbStyle.fromId(null))
        assertEquals(OrbStyle.CLASSIC, OrbStyle.fromId(""))
        assertEquals(OrbStyle.CLASSIC, OrbStyle.fromId("unknown"))
    }

    @Test
    fun styleIdsAreUnique() {
        val ids = OrbStyle.entries.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun hasTwelvePresetStyles() {
        assertEquals(12, OrbStyle.entries.size)
    }
}
