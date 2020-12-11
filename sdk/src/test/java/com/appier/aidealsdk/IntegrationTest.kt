package com.appier.aidealsdk

import android.app.Activity
import com.appier.aidealsdk.AiDeal.PageType.*
import com.appier.aidealsdk.view.CampaignLayout
import com.nhaarman.mockitokotlin2.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.lang.ref.WeakReference
import java.util.*

@RunWith(RobolectricTestRunner::class)
class IntegrationTest {
    private val application = RuntimeEnvironment.application
    private val firstActivity = Robolectric.setupActivity(Activity::class.java)
    private val secondActivity = Robolectric.setupActivity(Activity::class.java)

    private val cacheStorage = AIDCacheStorage.getInstance(application)
    private val requestQueue = mock<AIDRequestQueue>()
    private val socket = mock<AIDSocket>()
    private val campaignLayout = mock<CampaignLayout>()
    private val campaignLayouts = HashMap<Int, WeakReference<CampaignLayout?>>()
    private val campaignPlayer = CampaignPlayer(cacheStorage, socket, ATTRIBUTES, CONFIG, CAMPAIGN, campaignLayouts)
    private val campaignPlayers = HashMap<Int, CampaignPlayer>()
    private val campaignManager = AIDCampaignManager(cacheStorage, socket, campaignPlayers)
    private val manifestReader = mock<ManifestReader>()
    private var aiDeal = AiDeal.getInstance()

    @Before
    fun setUp() {
        doAnswer {
            aiDeal.onConnect()
        }.whenever(socket).connect(aiDeal)

        campaignLayouts[firstActivity.hashCode()] = WeakReference(campaignLayout)
        campaignLayouts[secondActivity.hashCode()] = WeakReference(campaignLayout)

        campaignPlayers[ELEMENT_ID] = campaignPlayer
    }

    @After
    fun tearDown() {
        cacheStorage.clear()
    }

    @Test
    fun startLogging_givenDataCollectionDisabledByDefault_doesNotConnect() {
        // Setup
        doReturn(false).whenever(manifestReader).getDataCollectionEnabled()
        aiDeal.configure(cacheStorage, requestQueue, socket, campaignManager, manifestReader, APIKEY)

        aiDeal.startLogging(firstActivity, AiDeal.Attributes(pageType = ITEM, loggedIn = IS_LOGIN, itemPrice = ITEM_PRICE))

        verify(socket, never()).connect(aiDeal)
        verify(socket, never()).listenEvents(aiDeal)
    }

    @Test
    fun startLogging_givenDataCollectionDisabledByDefaultButEnabled_connects() {
        // Setup
        doReturn(false).whenever(manifestReader).getDataCollectionEnabled()
        aiDeal.configure(cacheStorage, requestQueue, socket, campaignManager, manifestReader, APIKEY)
        aiDeal.setDataCollection(true)

        aiDeal.startLogging(firstActivity, AiDeal.Attributes(pageType = ITEM, loggedIn = IS_LOGIN, itemPrice = ITEM_PRICE))

        verify(socket, times(1)).connect(aiDeal)
        verify(socket, times(1)).listenEvents(aiDeal)
    }

    @Test
    fun startLogging_givenDataCollectionDisabled_doesNotConnects() {
        // Setup
        doReturn(true).whenever(manifestReader).getDataCollectionEnabled()
        aiDeal.configure(cacheStorage, requestQueue, socket, campaignManager, manifestReader, APIKEY)
        aiDeal.setDataCollection(false)

        aiDeal.startLogging(firstActivity, AiDeal.Attributes(pageType = ITEM, loggedIn = IS_LOGIN, itemPrice = ITEM_PRICE))

        verify(socket, never()).connect(aiDeal)
        verify(socket, never()).listenEvents(aiDeal)
    }

    @Test
    fun receiveCampaign_showsCampaign() {
        // Setup
        doReturn(true).whenever(manifestReader).getDataCollectionEnabled()
        aiDeal.configure(cacheStorage, requestQueue, socket, campaignManager, manifestReader, APIKEY)

        // First activity
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

        aiDeal.startLogging(firstActivity, AiDeal.Attributes(pageType = ITEM, loggedIn = IS_LOGIN, itemPrice = ITEM_PRICE))

        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = false, showWebView = true)
    }

    @Test
    fun receiveCampaignOnFirstActivity_inSameSession_showsCampaignOnSecondActivity() {
        // Setup
        doReturn(true).whenever(manifestReader).getDataCollectionEnabled()
        aiDeal.configure(cacheStorage, requestQueue, socket, campaignManager, manifestReader, APIKEY)

        // First activity
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

        aiDeal.startLogging(firstActivity, AiDeal.Attributes(pageType = ITEM, loggedIn = IS_LOGIN, itemPrice = ITEM_PRICE))

        // Second activity
        doAnswer {
            aiDeal.onReply(REPLY)
        }.whenever(socket).sendInit(
                UUID,
                USID,
                SID,
                arrayListOf(ITEM),
                IS_LOGIN,
                ITEM_PRICE,
                null,
                aiDeal
        )
        doNothing().whenever(socket).listenEvents(aiDeal)

        aiDeal.stopLogging(secondActivity)
        aiDeal.startLogging(secondActivity, AiDeal.Attributes(pageType = ITEM, loggedIn = IS_LOGIN, itemPrice = ITEM_PRICE))

        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = true, showWebView = false)
    }

    @Test
    fun receiveCampaignOnFirstActivity_restartedAppInSameSession_showsCampaignOnSecondActivity() {
        // Setup
        doReturn(true).whenever(manifestReader).getDataCollectionEnabled()
        aiDeal.configure(cacheStorage, requestQueue, socket, campaignManager, manifestReader, APIKEY)

        // First activity
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

        aiDeal.startLogging(firstActivity, AiDeal.Attributes(pageType = ITEM, loggedIn = IS_LOGIN, itemPrice = ITEM_PRICE))

        // Restart app
        application.onTerminate()
        application.onCreate()

        // Second activity
        doAnswer {
            aiDeal.onReply(REPLY)
        }.whenever(socket).sendInit(
                UUID,
                USID,
                SID,
                arrayListOf(ITEM),
                IS_LOGIN,
                ITEM_PRICE,
                null,
                aiDeal
        )
        doNothing().whenever(socket).listenEvents(aiDeal)

        aiDeal.startLogging(secondActivity, AiDeal.Attributes(pageType = ITEM, loggedIn = IS_LOGIN, itemPrice = ITEM_PRICE))

        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = true, showWebView = false)
    }

    @Test
    fun receiveCampaignOnFirstActivity_inNextSession_doesNotShowsCampaignOnSecondActivity() {
        // Setup
        doReturn(true).whenever(manifestReader).getDataCollectionEnabled()
        aiDeal.configure(cacheStorage, requestQueue, socket, campaignManager, manifestReader, APIKEY)

        // First activity
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

        aiDeal.startLogging(firstActivity, AiDeal.Attributes(pageType = ITEM, loggedIn = IS_LOGIN, itemPrice = ITEM_PRICE))

        // Session expire
        cacheStorage.resetSession()

        // Second activity
        doAnswer {
            aiDeal.onReply(REPLY)
        }.whenever(socket).sendInit(
                UUID,
                USID,
                SID,
                arrayListOf(ITEM),
                IS_LOGIN,
                ITEM_PRICE,
                null,
                aiDeal
        )
        doNothing().whenever(socket).listenEvents(aiDeal)

        aiDeal.stopLogging(secondActivity)
        aiDeal.startLogging(secondActivity, AiDeal.Attributes(pageType = ITEM, loggedIn = IS_LOGIN, itemPrice = ITEM_PRICE))

        verify(campaignLayout, never()).updateCreativeDisplay(showBadge = true, showWebView = false)
    }

    @Test
    fun receiveCampaignOnFirstActivity_restartedAppInNextSession_doesNotShowsCampaignOnSecondActivity() {
        // Setup
        doReturn(true).whenever(manifestReader).getDataCollectionEnabled()
        aiDeal.configure(cacheStorage, requestQueue, socket, campaignManager, manifestReader, APIKEY)

        // First activity
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

        aiDeal.startLogging(firstActivity, AiDeal.Attributes(pageType = ITEM, loggedIn = IS_LOGIN, itemPrice = ITEM_PRICE))

        // Session expire
        cacheStorage.resetSession()

        // Restart app
        application.onTerminate()
        application.onCreate()

        // Second activity
        doAnswer {
            aiDeal.onReply(REPLY)
        }.whenever(socket).sendInit(
                UUID,
                USID,
                SID,
                arrayListOf(ITEM),
                IS_LOGIN,
                ITEM_PRICE,
                null,
                aiDeal
        )
        doNothing().whenever(socket).listenEvents(aiDeal)

        aiDeal.stopLogging(secondActivity)
        aiDeal.startLogging(secondActivity, AiDeal.Attributes(pageType = ITEM, loggedIn = IS_LOGIN, itemPrice = ITEM_PRICE))

        verify(campaignLayout, never()).updateCreativeDisplay(showBadge = true, showWebView = false)
    }
}
