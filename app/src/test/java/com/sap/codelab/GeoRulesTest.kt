package com.sap.codelab

import com.sap.codelab.location.GeoRules
import org.junit.Assert.*
import org.junit.Test

class GeoRulesTest {
    @Test fun radiusIsExactly200Meters() = assertEquals(200f, GeoRules.RADIUS_METERS, 0f)

    @Test fun acceptsPrecisionAndEquator() {
        assertTrue(GeoRules.validPoint(47.16252145, 19.50334779))
        assertTrue(GeoRules.validPoint(0.0, 0.0))
        assertTrue(GeoRules.validPoint(-90.0, 180.0))
    }

    @Test fun rejectsMissingAndInvalidCoordinates() {
        assertFalse(GeoRules.validPoint(null, 1.0))
        assertFalse(GeoRules.validPoint(20.0, null))
        assertFalse(GeoRules.validPoint(91.0, 0.0))
        assertFalse(GeoRules.validPoint(1.0, -181.0))
        assertFalse(GeoRules.validPoint(Double.NaN, 0.0))
        assertFalse(GeoRules.validPoint(0.0, Double.POSITIVE_INFINITY))
    }

    @Test fun requestIdsAreStableAndRejectGarbage() {
        assertEquals("memo:42", GeoRules.requestId(42))
        assertEquals(42L, GeoRules.memoId("memo:42"))
        assertNull(GeoRules.memoId("unexpected"))
        assertNull(GeoRules.memoId("memo:-1"))
        assertNull(GeoRules.memoId("memo:wat"))
    }
}
