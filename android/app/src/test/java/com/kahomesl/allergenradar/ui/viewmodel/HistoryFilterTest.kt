package com.kahomesl.allergenradar.ui.viewmodel

import org.junit.Assert.assertNull
import org.junit.Test

class HistoryFilterTest {
    @Test
    fun `all taxon filter leaves the API taxon parameter unset`() {
        assertNull(HistoryTaxonFilter.ALL.taxon)
    }
}
