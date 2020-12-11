package com.appier.aidealsdk

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import com.appier.aidealsdk.AiDeal.PageType.*
import io.socket.client.Ack
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AIDSocketTest {

    private val socketIO = mock<Socket>()
    private val listener = object : AIDSocket.Listener {
        override fun onConnect() {
            connected = true
        }
        override fun onReply(attributes: JSONObject) {
            replied = true
        }
        override fun onReceive(attributes: JSONObject) {
            received = true
        }
    }

    private var socket: AIDSocket? = null
    private var connected = false
    private var replied = false
    private var received = false

    @Before
    fun setUp() {
        socket = AIDSocket(socketIO)
    }

    @After
    fun tearDown() {
        reset(socketIO)
        connected = false
        replied = false
        received = false
    }

    @Test
    fun getInstance_returnsSingleton() {
        val instance1 = AIDSocket.getInstance(APIKEY)
        val instance2 = AIDSocket.getInstance(APIKEY)
        assertThat(instance1).isEqualTo(instance2)
    }

    @Test
    fun connect_givenConnect_connected() {
        doAnswer {
            val callback = it.arguments[1] as Emitter.Listener
            callback.call()
            socketIO
        }.whenever(socketIO).on(eq(Socket.EVENT_CONNECT), any())

        socket!!.connect(listener)

        assertThat(connected).isTrue()
        assertThat(replied).isFalse()
        assertThat(received).isFalse()
        verify(socketIO, times(3)).on(any(), any())
        verify(socketIO, times(1)).connect()
    }

    @Test
    fun connect_givenConnectError_notConnected() {
        doAnswer {
            val callback = it.arguments[1] as Emitter.Listener
            callback.call()
            socketIO
        }.whenever(socketIO).on(eq(Socket.EVENT_CONNECT_ERROR), any())

        socket!!.connect(listener)

        assertThat(connected).isFalse()
        assertThat(replied).isFalse()
        assertThat(received).isFalse()
        verify(socketIO, times(3)).on(any(), any())
        verify(socketIO, times(1)).connect()
    }

    @Test
    fun connect_givenReconnect_connected() {
        doAnswer {
            val callback = it.arguments[1] as Emitter.Listener
            callback.call()
            socketIO
        }.whenever(socketIO).on(eq(Socket.EVENT_RECONNECT), any())

        socket!!.connect(listener)

        assertThat(connected).isTrue()
        assertThat(replied).isFalse()
        assertThat(received).isFalse()
        verify(socketIO, times(3)).on(any(), any())
        verify(socketIO, times(1)).connect()
    }

    @Test
    fun sendInit_callsEmit() {
        val pageTypes = JSONArray(arrayListOf(ITEM).map { it.toString() })
        val params = JSONObject().apply {
            put("uuid", UUID)
            put("usid", USID)
            put("sid", SID)
            put("device", DEVICE)
            put("page_types", pageTypes)
            put("is_login", IS_LOGIN)
            put("item_price", ITEM_PRICE)
        }

        doAnswer {
            val ack = it.arguments[2] as Ack
            ack.call(params)
            socketIO
        }.whenever(socketIO).emit(eq(INIT_EVENT), any())

        socket!!.sendInit(UUID, USID, SID, arrayListOf(ITEM), IS_LOGIN, ITEM_PRICE, null, listener)

        assertThat(connected).isFalse()
        assertThat(replied).isTrue()
        assertThat(received).isFalse()
        val captor = argumentCaptor<JSONObject>()
        verify(socketIO, times(1)).emit(eq(INIT_EVENT), captor.capture(), any())
        assertThat(captor.firstValue.toString()).isEqualTo(params.toString())
    }

    @Test
    fun listenEvents_callsEmit() {
        doAnswer {
            val callback = it.arguments[1] as Emitter.Listener
            callback.call(ATTRIBUTES)
            socketIO
        }.whenever(socketIO).on(eq(RECEIVE_CAMPAIGN_EVENT), any())

        socket!!.listenEvents(listener)

        assertThat(connected).isFalse()
        assertThat(replied).isFalse()
        assertThat(received).isTrue()
        val captor = argumentCaptor<JSONObject>()
        verify(socketIO, times(1)).emit(eq(CAMPAIGN_RECEIVE_EVENT), captor.capture())
        assertThat(captor.firstValue.toString()).isEqualTo(ATTRIBUTES.toString())
    }

    @Test
    fun sendSaveCampaign_callsEmit() {
        socket!!.sendSaveCampaign(ATTRIBUTES)

        assertThat(connected).isFalse()
        assertThat(replied).isFalse()
        assertThat(received).isFalse()
        val captor = argumentCaptor<JSONObject>()
        verify(socketIO, times(1)).emit(eq(SAVE_CAMPAIGN_EVENT), captor.capture())
        assertThat(captor.firstValue.toString()).isEqualTo(ATTRIBUTES.toString())
    }

    @Test
    fun sendConversionData_callsEmit() {
        val item = JSONObject().apply {
            put("id", CONVERSION_ITEM1_ID)
            put("name", CONVERSION_ITEM1_NAME)
            put("url", CONVERSION_ITEM1_URL)
            put("price", CONVERSION_ITEM1_PRICE)
            put("count", CONVERSION_ITEM1_COUNT)
        }
        val conversion = JSONObject().apply {
            put("id", CONVERSION_ID)
            put("name", CONVERSION_NAME)
            put("item_count", CONVERSION_ITEM_COUNT)
            put("price", CONVERSION_PRICE)
            put("items", JSONArray(listOf(item)))
        }

        socket!!.sendConversionData(conversion)

        assertThat(connected).isFalse()
        assertThat(replied).isFalse()
        assertThat(received).isFalse()
        val captor = argumentCaptor<JSONObject>()
        verify(socketIO, times(1)).emit(eq(CONVERSION_EVENT), captor.capture())
        assertThat(captor.firstValue.toString()).isEqualTo(conversion.toString())
    }

    @Test
    fun logUserBehavior_givenOnce_doesNotSendUserBehaviors() {
        val timestamp = System.currentTimeMillis()

        socket!!.logUserBehavior(timestamp, HEIGHT, WIDTH, SCROLL_Y)

        verify(socketIO, never()).emit(eq(USER_BEHAVIOR_EVENT), any())
    }

    @Test
    fun logUserBehavior_givenTwiceWithinLogInterval_doesNotSendUserBehaviors() {
        val timestamp = System.currentTimeMillis()

        socket!!.logUserBehavior(timestamp, HEIGHT, WIDTH, SCROLL_Y)
        socket!!.logUserBehavior(timestamp + LOG_USER_BEHAVIOR_INTERVAL, HEIGHT, WIDTH, SCROLL_Y)

        verify(socketIO, never()).emit(eq(USER_BEHAVIOR_EVENT), any())
    }

    @Test
    fun logUserBehavior_givenTwiceWithinSendInterval_doesNotSendUserBehaviors() {
        val timestamp = System.currentTimeMillis()

        socket!!.logUserBehavior(timestamp, HEIGHT, WIDTH, SCROLL_Y)
        socket!!.logUserBehavior(timestamp + SEND_USER_BEHAVIOR_INTERVAL, HEIGHT, WIDTH, SCROLL_Y)

        verify(socketIO, times(1)).emit(eq(USER_BEHAVIOR_EVENT), any())
    }
}
