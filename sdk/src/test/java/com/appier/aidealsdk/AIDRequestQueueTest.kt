package com.appier.aidealsdk

import com.android.volley.ExecutorDelivery
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.*
import com.google.common.truth.Truth.assertThat
import com.appier.aidealsdk.mock.MockHttpStack
import junit.framework.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.concurrent.Executors

@RunWith(RobolectricTestRunner::class)
class AIDRequestQueueTest {
    companion object {
        private const val THREAD_POOL_SIZE = 1
    }

    @Test
    fun getInstance_returnsSingleton() {
        val application = RuntimeEnvironment.application
        val instance1 = AIDRequestQueue.getInstance(application)
        val instance2 = AIDRequestQueue.getInstance(application)
        assertThat(instance1).isEqualTo(instance2)
    }

    @Test
    fun add_givenError_throwsError() {
        val mockHttpStack = MockHttpStack()
        val requestQueue = mockRequestQueue(mockHttpStack)

        val futureError = RequestFuture.newFuture<String>()
        mockHttpStack.setException("https://example.com/500", IOException("No network"))
        requestQueue.add(StringRequest(Request.Method.GET, "https://example.com/500", futureError, futureError))

        requestQueue.start()
        try {
            futureError.get()
            fail()
        } catch (e: Exception) {
            assertThat(e).hasMessageThat().contains("No network")
        }
    }

    @Test
    fun add_givenSuccess_returnsText() {
        val mockHttpStack = MockHttpStack()
        val requestQueue = mockRequestQueue(mockHttpStack)

        val textSuccess = "{\"success\":true}"
        val futureSuccess = RequestFuture.newFuture<String>()
        val streamSuccess = ByteArrayInputStream(textSuccess.toByteArray())
        val responseSuccess = HttpResponse(200, listOf(), textSuccess.length, streamSuccess)
        mockHttpStack.setResponse("https://example.com/200", responseSuccess)
        requestQueue.add(StringRequest(Request.Method.GET, "https://example.com/200", futureSuccess, futureSuccess))
        requestQueue.start()
        assertThat(futureSuccess.get()).isEqualTo(textSuccess)
    }

    @Test
    fun add_givenMultiple_completesAll() {
        val mockHttpStack = MockHttpStack()
        val requestQueue = mockRequestQueue(mockHttpStack)

        val futureNotFound = RequestFuture.newFuture<String>()
        requestQueue.add(StringRequest(Request.Method.GET, "https://example.com/404", futureNotFound, futureNotFound))

        val futureError = RequestFuture.newFuture<String>()
        mockHttpStack.setException("https://example.com/500", IOException("No network"))
        requestQueue.add(StringRequest(Request.Method.GET, "https://example.com/500", futureError, futureError))

        val textSuccess = "{\"success\":true}"
        val futureSuccess = RequestFuture.newFuture<String>()
        val streamSuccess = ByteArrayInputStream(textSuccess.toByteArray())
        val responseSuccess = HttpResponse(200, listOf(), textSuccess.length, streamSuccess)
        mockHttpStack.setResponse("https://example.com/200", responseSuccess)
        requestQueue.add(StringRequest(Request.Method.GET, "https://example.com/200", futureSuccess, futureSuccess))

        requestQueue.start()
        try {
            futureNotFound.get()
            fail()
        } catch (e: Exception) {
            assertThat(e).hasMessageThat().contains("ClientError")
        }
        try {
            futureError.get()
            fail()
        } catch (e: Exception) {
            assertThat(e).hasMessageThat().contains("No network")
        }
        assertThat(futureSuccess.get()).isEqualTo(textSuccess)
    }

    private fun mockRequestQueue(mockHttpStack: MockHttpStack): AIDRequestQueue {
        val cache = NoCache()
        val network = BasicNetwork(mockHttpStack)
        val responseDelivery = ExecutorDelivery(Executors.newSingleThreadExecutor())
        val mockRequestQueue = RequestQueue(cache, network, THREAD_POOL_SIZE, responseDelivery)
        return AIDRequestQueue(mockRequestQueue)
    }
}
