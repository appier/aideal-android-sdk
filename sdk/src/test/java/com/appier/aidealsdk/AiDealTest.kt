package com.appier.aidealsdk

import android.app.Activity
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.*
import com.appier.aidealsdk.AiDeal.PageType.*
import io.socket.client.Socket
import io.socket.emitter.Emitter
import junit.framework.Assert.fail
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import java.lang.IllegalArgumentException
import kotlin.collections.ArrayList

@RunWith(RobolectricTestRunner::class)
class AiDealTest {
    companion object {
        private val INIT_AT = System.currentTimeMillis()
        private const val TWO_MINUTES = 1000 * 60 * 2
        private const val FOUR_MINUTES = 1000 * 60 * 4
    }

    private val systemTimer = mock<SystemTimer>()
    private val cacheStorage = mock<AIDCacheStorage>()
    private val requestQueue = mock<AIDRequestQueue>()
    private val socketIO = mock<Socket>()
    private val socket = spy(AIDSocket(socketIO))
    private val campaignManager = mock<AIDCampaignManager>()
    private val manifestReader = mock<ManifestReader>()
    private val activity = Robolectric.setupActivity(Activity::class.java)

    private var aiDeal = AiDeal(systemTimer)

    @Before
    fun setUp() {
        doReturn(true).whenever(manifestReader).getDataCollectionEnabled()
        doReturn(true).whenever(cacheStorage).getBoolean(APR_DATA_COLLECTION_ENABLED_KEY)
        aiDeal.configure(cacheStorage, requestQueue, socket, campaignManager, manifestReader, APIKEY)
    }

    @Test
    fun getInstance_returnsSingleton() {
        val instance1 = AiDeal.getInstance()
        val instance2 = AiDeal.getInstance()
        Truth.assertThat(instance1).isEqualTo(instance2)
    }

    @Test
    fun onReceive_withoutLoggedIn_sendsInit() {
        doAnswer {
            val callback = it.arguments[1] as Emitter.Listener
            callback.call()
            socketIO
        }.whenever(socketIO).on(eq(Socket.EVENT_CONNECT), any())

        aiDeal.startLogging(activity, AiDeal.Attributes(pageType = ITEM, itemPrice = ITEM_PRICE))

        verify(socket, times(1)).sendInit(
            null,
            null,
            null,
            arrayListOf(ITEM),
            null,
            ITEM_PRICE,
            null,
            aiDeal
        )
    }

    @Test
    fun onReceive_within3Minutes_receiveCampaign() {
        doReturn(INIT_AT, INIT_AT + TWO_MINUTES).whenever(systemTimer).timeInMillis
        doAnswer {
            val callback = it.arguments[1] as Emitter.Listener
            callback.call(ATTRIBUTES)
            socketIO
        }.whenever(socketIO).on(eq(RECEIVE_CAMPAIGN_EVENT), any())

        aiDeal.startLogging(activity, AiDeal.Attributes(pageType = ITEM, loggedIn = IS_LOGIN, itemPrice = ITEM_PRICE))

        verify(campaignManager, times(1)).onCampaignReceived(any(), any(), any())
    }

    @Test
    fun onReceive_over3Minutes_receiveCampaign() {
        doReturn(INIT_AT, INIT_AT + FOUR_MINUTES).whenever(systemTimer).timeInMillis
        doAnswer {
            val callback = it.arguments[1] as Emitter.Listener
            callback.call(ATTRIBUTES)
            socketIO
        }.whenever(socketIO).on(eq(RECEIVE_CAMPAIGN_EVENT), any())

        aiDeal.startLogging(activity, AiDeal.Attributes(pageType = ITEM, loggedIn = IS_LOGIN, itemPrice = ITEM_PRICE))

        verify(campaignManager, never()).onCampaignReceived(any(), any(), any())
    }

    @Test
    fun initPage_withSinglePageType_receiveCampaign() {
        doAnswer {
            aiDeal.onConnect()
        }.whenever(socket).connect(aiDeal)
        doAnswer {
            aiDeal.onReply(REPLY)
        }.whenever(socket).sendInit(
            null,
            null,
            null,
            arrayListOf(ITEM),
            IS_LOGIN,
            ITEM_PRICE,
            null,
            aiDeal
        )
        doAnswer {
            aiDeal.onCampaignReceived(ATTRIBUTES)
        }.whenever(socket).listenEvents(aiDeal)

        aiDeal.startLogging(activity, AiDeal.Attributes(pageType = ITEM, loggedIn = IS_LOGIN, itemPrice = ITEM_PRICE))

        verify(socket, times(1)).disconnect()
        verify(campaignManager, times(1)).startCampaign(activity, arrayListOf(ITEM))
        verify(socket, times(1)).connect(aiDeal)
        verify(socket, times(1)).listenEvents(aiDeal)
        verify(socket, times(1)).sendInit(
            null,
            null,
            null,
            arrayListOf(ITEM),
            IS_LOGIN,
            ITEM_PRICE,
            null,
            aiDeal
        )
        verify(cacheStorage, times(1)).put(AID_UUID_KEY, UUID, AID_USER_MAX_AGE)
        verify(cacheStorage, times(1)).put(AID_USID_KEY, USID)
        verify(cacheStorage, times(1)).put(AID_SID_KEY, SID)
        verify(campaignManager, times(1)).onCampaignReceived(any(), eq(arrayListOf(ITEM)), eq(ATTRIBUTES))
    }

    @Test
    fun initPage_withMultiplePageTypes_receiveCampaign() {
        doAnswer {
            aiDeal.onConnect()
        }.whenever(socket).connect(aiDeal)
        doAnswer {
            aiDeal.onReply(REPLY)
        }.whenever(socket).sendInit(
            null,
            null,
            null,
            arrayListOf(CATEGORY, SEARCH),
            IS_LOGIN,
            ITEM_PRICE,
            null,
            aiDeal
        )
        doAnswer {
            aiDeal.onCampaignReceived(ATTRIBUTES)
        }.whenever(socket).listenEvents(aiDeal)

        aiDeal.startLogging(activity, AiDeal.Attributes(pageTypes = arrayListOf(CATEGORY, SEARCH), loggedIn = IS_LOGIN, itemPrice = ITEM_PRICE))

        verify(socket, times(1)).disconnect()
        verify(campaignManager, times(1)).startCampaign(activity, arrayListOf(CATEGORY, SEARCH))
        verify(socket, times(1)).connect(aiDeal)
        verify(socket, times(1)).listenEvents(aiDeal)
        verify(socket, times(1)).sendInit(
            null,
            null,
            null,
            arrayListOf(CATEGORY, SEARCH),
            IS_LOGIN,
            ITEM_PRICE,
            null,
            aiDeal
        )
        verify(cacheStorage, times(1)).put(AID_UUID_KEY, UUID, AID_USER_MAX_AGE)
        verify(cacheStorage, times(1)).put(AID_USID_KEY, USID)
        verify(cacheStorage, times(1)).put(AID_SID_KEY, SID)
        verify(campaignManager, times(1)).onCampaignReceived(any(), eq(arrayListOf(CATEGORY, SEARCH)), eq(ATTRIBUTES))
    }

    @Test
    fun initPage_withPageTypeAndPageTypes_throwsError() {
        try {
            aiDeal.startLogging(activity, AiDeal.Attributes(pageTypes = arrayListOf(CATEGORY, SEARCH), pageType = ITEM))
            fail()
        } catch (e: Exception) {
            Truth.assertThat(e).isInstanceOf(IllegalArgumentException::class.java)
        }
    }

    @Test
    fun initPage_givenNoContext_doesNotConnect() {
        aiDeal.startLogging(null, AiDeal.Attributes(pageType = ITEM, loggedIn = IS_LOGIN, itemPrice = ITEM_PRICE))

        verify(socket, never()).disconnect()
        verify(campaignManager, never()).startCampaign(activity, arrayListOf(ITEM))
        verify(socket, never()).connect(aiDeal)
        verify(socket, never()).listenEvents(aiDeal)
    }

    @Test
    fun initPage_withoutConnection_doesNotSendInit() {
        aiDeal.startLogging(activity, AiDeal.Attributes(pageType = ITEM, loggedIn = IS_LOGIN, itemPrice = ITEM_PRICE))

        verify(socket, times(1)).disconnect()
        verify(campaignManager, times(1)).startCampaign(activity, arrayListOf(ITEM))
        verify(socket, times(1)).connect(aiDeal)
        verify(socket, times(1)).listenEvents(aiDeal)
        verify(socket, never()).sendInit(any(), any(), any(), any(), any(), any(), any(), any())
    }

    @Test
    fun stopPage_doesNotDisconnect() {
        aiDeal.stopLogging(activity)

        verify(campaignManager, never()).cancelCountDownCoroutine(activity)
        verify(socket, never()).disconnect()
        verify(cacheStorage, never()).put(any(), any())
    }

    @Test
    fun stopPage_givenSameHash_doesNotDisconnect() {
        doReturn(activity.hashCode()).whenever(cacheStorage).getInt(AID_CURRENT_PAGE_KEY)

        aiDeal.stopLogging(activity)

        verify(campaignManager, times(1)).cancelCountDownCoroutine(activity)
        verify(socket, times(1)).disconnect()
        verify(cacheStorage, times(1)).put(AID_CURRENT_PAGE_KEY, DEFAULT_CONTEXT_HASH)
    }

    @Test
    fun sendConversionInfo_sendsConversion() {
        val item = AiDeal.ConversionItem(
            CONVERSION_ITEM1_ID,
            CONVERSION_ITEM1_NAME,
            CONVERSION_ITEM1_URL,
            CONVERSION_ITEM1_PRICE,
            CONVERSION_ITEM1_COUNT
        )
        val conversion = AiDeal.Conversion(
            CONVERSION_ID,
            CONVERSION_NAME,
            ArrayList(listOf(COUPON_CODE)),
            CONVERSION_ITEM_COUNT,
            CONVERSION_PRICE,
            ArrayList(listOf(item))
        )

        aiDeal.log(conversion)

        val captor = argumentCaptor<JSONObject>()
        verify(socket).sendConversionData(captor.capture())
        Truth.assertThat(captor.firstValue.toString()).isEqualTo(JSONObject(CONVERSION_INFO.toString()).apply {
            put("coupon_codes", JSONArray().put(COUPON_CODE))
            put("items", JSONArray().put(CONVERSION_ITEM1))
        }.toString())
    }

    @Test
    fun sendConversionInfo_withMinimumFields_sendsConversion() {
        val conversion = AiDeal.Conversion(
            CONVERSION_ID,
            null,
            null,
            null,
            null,
            null
        )

        aiDeal.log(conversion)

        val captor = argumentCaptor<JSONObject>()
        verify(socket).sendConversionData(captor.capture())
        Truth.assertThat(captor.firstValue.toString()).isEqualTo(JSONObject().apply {
            put("id", CONVERSION_ID)
            put("name", null)
            put("coupon_codes", JSONArray())
            put("item_count", null)
            put("price", null)
            put("items", JSONArray())
        }.toString())
    }

    @Test
    fun sendConversionInfo_withEmptyItems_sendsConversion() {
        val conversion = AiDeal.Conversion(
            CONVERSION_ID,
            CONVERSION_NAME,
            ArrayList(listOf(COUPON_CODE)),
            CONVERSION_ITEM_COUNT,
            CONVERSION_PRICE,
            ArrayList()
        )

        aiDeal.log(conversion)

        val captor = argumentCaptor<JSONObject>()
        verify(socket).sendConversionData(captor.capture())
        Truth.assertThat(captor.firstValue.toString()).isEqualTo(JSONObject(CONVERSION_INFO.toString()).apply {
            put("coupon_codes", JSONArray().put(COUPON_CODE))
        }.toString())
    }

    @Test
    fun sendConversionInfo_withEmptyCouponCodes_sendsConversion() {
        val item = AiDeal.ConversionItem(
            CONVERSION_ITEM1_ID,
            CONVERSION_ITEM1_NAME,
            CONVERSION_ITEM1_URL,
            CONVERSION_ITEM1_PRICE,
            CONVERSION_ITEM1_COUNT
        )
        val conversion = AiDeal.Conversion(
            CONVERSION_ID,
            CONVERSION_NAME,
            ArrayList(),
            CONVERSION_ITEM_COUNT,
            CONVERSION_PRICE,
            ArrayList(listOf(item))
        )

        aiDeal.log(conversion)

        val captor = argumentCaptor<JSONObject>()
        verify(socket).sendConversionData(captor.capture())
        Truth.assertThat(captor.firstValue.toString()).isEqualTo(JSONObject(CONVERSION_INFO.toString()).apply {
            put("items", JSONArray().put(CONVERSION_ITEM1))
        }.toString())
    }
}
