package com.appier.aidealsdk.mock

import com.android.volley.Request
import com.android.volley.toolbox.BaseHttpStack
import com.android.volley.toolbox.HttpResponse
import java.io.ByteArrayInputStream
import java.io.IOException

class MockHttpStack : BaseHttpStack() {
    private var responses = mutableMapOf<String, HttpResponse>()
    private var exceptions = mutableMapOf<String, Throwable>()

    fun setResponse(url: String, response: HttpResponse) {
        responses[url] = response
    }

    fun setException(url: String, exception: IOException) {
        exceptions[url] = exception
    }

    override fun executeRequest(request: Request<*>, additionalHeaders: Map<String, String>): HttpResponse {
        if (exceptions[request.url] != null) throw exceptions[request.url]!!
        if (responses[request.url] != null) return responses[request.url]!!
        val text = "Not found"
        val responseStream = ByteArrayInputStream(text.toByteArray())
        return HttpResponse(404, listOf(), text.length, responseStream)
    }
}


