package com.kahomesl.allergenradar.data

import org.junit.Assert.assertTrue
import org.junit.Test

class ApiClientTest {
    @Test
    fun clientRetriesIdempotentRequestsAfterAStaleConnection() {
        assertTrue(ApiClient.createHttpClient(debug = false).retryOnConnectionFailure)
    }
}
