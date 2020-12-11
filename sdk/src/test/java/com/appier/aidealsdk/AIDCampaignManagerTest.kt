package com.appier.aidealsdk

import android.app.Activity
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import com.appier.aidealsdk.AIDCampaignState.*
import com.appier.aidealsdk.AiDeal.PageType.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import java.lang.ref.WeakReference

@RunWith(RobolectricTestRunner::class)
class AIDCampaignManagerTest {
    private val cacheStorage = mock<AIDCacheStorage>()
    private val socket = mock<AIDSocket>()
    private val useConstructor = UseConstructor.withArguments(cacheStorage, socket, ATTRIBUTES, CONFIG, CAMPAIGN)
    private val campaignPlayer = mock<CampaignPlayer>(useConstructor = useConstructor)
    private val campaignPlayers = HashMap<Int, CampaignPlayer>()
    private val activity = Robolectric.setupActivity(Activity::class.java)

    private var campaignManager: AIDCampaignManager? = null

    @Before
    fun setUp() {
        campaignManager = AIDCampaignManager(cacheStorage, socket, campaignPlayers)
    }

    @After
    fun tearDown() {
        reset(cacheStorage)
        reset(socket)
        reset(campaignPlayer)
        campaignPlayers.clear()
    }

    @Test
    fun getInstance_returnsSingleton() {
        val instance1 = AIDCampaignManager.getInstance(cacheStorage, socket)
        val instance2 = AIDCampaignManager.getInstance(cacheStorage, socket)
        assertThat(instance1).isEqualTo(instance2)
    }

    @Test
    fun onCampaignReceived_startCampaign() {
        doReturn(DEFAULT).whenever(cacheStorage).getState()
        doReturn(ATTRIBUTES.toString()).whenever(cacheStorage).getString(AID_CAMPAIGN_ATTRIBUTES)

        campaignPlayers[ELEMENT_ID] = campaignPlayer
        campaignManager!!.onCampaignReceived(WeakReference(activity), arrayListOf(ITEM), ATTRIBUTES)

        verify(cacheStorage, times(1)).put(AID_CAMPAIGN_ATTRIBUTES, ATTRIBUTES.toString())
        verify(cacheStorage, times(1)).setState(READY)
        verify(campaignPlayer, times(1)).start(activity, arrayListOf(ITEM))
    }

    @Test
    fun onCampaignReceived_onCart_doesNotStartCampaign() {
        doReturn(DEFAULT).whenever(cacheStorage).getState()
        doReturn(ATTRIBUTES.toString()).whenever(cacheStorage).getString(AID_CAMPAIGN_ATTRIBUTES)

        campaignPlayers[ELEMENT_ID] = campaignPlayer
        campaignManager!!.onCampaignReceived(WeakReference(activity), arrayListOf(CART), ATTRIBUTES)

        verify(cacheStorage, times(1)).put(AID_CAMPAIGN_ATTRIBUTES, ATTRIBUTES.toString())
        verify(cacheStorage, times(1)).setState(READY)
        verify(campaignPlayer, never()).start(activity, arrayListOf(CART))
    }

    @Test
    fun onCampaignReceived_onCartForm_doesNotStartCampaign() {
        doReturn(DEFAULT).whenever(cacheStorage).getState()
        doReturn(ATTRIBUTES.toString()).whenever(cacheStorage).getString(AID_CAMPAIGN_ATTRIBUTES)

        campaignPlayers[ELEMENT_ID] = campaignPlayer
        campaignManager!!.onCampaignReceived(WeakReference(activity), arrayListOf(CART_FORM), ATTRIBUTES)

        verify(cacheStorage, never()).put(AID_CAMPAIGN_ATTRIBUTES, ATTRIBUTES.toString())
        verify(cacheStorage, never()).setState(READY)
        verify(campaignPlayer, never()).start(activity, arrayListOf(CART_FORM))
    }

    @Test
    fun onCampaignReceived_onConversion_doesNotStartCampaign() {
        doReturn(DEFAULT).whenever(cacheStorage).getState()
        doReturn(ATTRIBUTES.toString()).whenever(cacheStorage).getString(AID_CAMPAIGN_ATTRIBUTES)

        campaignPlayers[ELEMENT_ID] = campaignPlayer
        campaignManager!!.onCampaignReceived(WeakReference(activity), arrayListOf(CONVERSION), ATTRIBUTES)

        verify(cacheStorage, never()).put(AID_CAMPAIGN_ATTRIBUTES, ATTRIBUTES.toString())
        verify(cacheStorage, never()).setState(READY)
        verify(campaignPlayer, never()).start(activity, arrayListOf(CONVERSION))
    }

    @Test
    fun onCampaignReceived_withDisabledState_doesNotStartCampaign() {
        doReturn(DISABLED).whenever(cacheStorage).getState()
        doReturn(ATTRIBUTES.toString()).whenever(cacheStorage).getString(AID_CAMPAIGN_ATTRIBUTES)

        campaignPlayers[ELEMENT_ID] = campaignPlayer
        campaignManager!!.onCampaignReceived(WeakReference(activity), arrayListOf(ITEM), ATTRIBUTES)

        verify(cacheStorage, never()).put(AID_CAMPAIGN_ATTRIBUTES, ATTRIBUTES.toString())
        verify(cacheStorage, never()).setState(READY)
        verify(campaignPlayer, never()).start(activity, arrayListOf(ITEM))
    }

    @Test
    fun onCampaignReceived_withOfferState_doesNotStartCampaign() {
        doReturn(OFFER).whenever(cacheStorage).getState()
        doReturn(ATTRIBUTES.toString()).whenever(cacheStorage).getString(AID_CAMPAIGN_ATTRIBUTES)

        campaignPlayers[ELEMENT_ID] = campaignPlayer
        campaignManager!!.onCampaignReceived(WeakReference(activity), arrayListOf(ITEM), ATTRIBUTES)

        verify(cacheStorage, never()).put(AID_CAMPAIGN_ATTRIBUTES, ATTRIBUTES.toString())
        verify(cacheStorage, never()).setState(READY)
        verify(campaignPlayer, never()).start(activity, arrayListOf(ITEM))
    }

    @Test
    fun onCampaignReceived_withPresentState_doesNotStartCampaign() {
        doReturn(PRESENT).whenever(cacheStorage).getState()
        doReturn(ATTRIBUTES.toString()).whenever(cacheStorage).getString(AID_CAMPAIGN_ATTRIBUTES)

        campaignPlayers[ELEMENT_ID] = campaignPlayer
        campaignManager!!.onCampaignReceived(WeakReference(activity), arrayListOf(ITEM), ATTRIBUTES)

        verify(cacheStorage, never()).put(AID_CAMPAIGN_ATTRIBUTES, ATTRIBUTES.toString())
        verify(cacheStorage, never()).setState(READY)
        verify(campaignPlayer, never()).start(activity, arrayListOf(ITEM))
    }

    @Test
    fun startCampaign_startsCampaign() {
        doReturn(ATTRIBUTES.toString()).whenever(cacheStorage).getString(AID_CAMPAIGN_ATTRIBUTES)

        campaignPlayers[ELEMENT_ID] = campaignPlayer
        campaignManager!!.startCampaign(activity, arrayListOf(ITEM))

        verify(campaignPlayer, times(1)).start(activity, arrayListOf(ITEM))
    }

    @Test
    fun startCampaign_withoutAttributesCache_doesNotStartsCampaign() {
        campaignPlayers[ELEMENT_ID] = campaignPlayer
        campaignManager!!.startCampaign(activity, arrayListOf(ITEM))

        verify(campaignPlayer, never()).start(activity, arrayListOf(ITEM))
    }

    @Test
    fun startCampaign_withoutCampaignPlayer_doesNotStartsCampaign() {
        doReturn(ATTRIBUTES.toString()).whenever(cacheStorage).getString(AID_CAMPAIGN_ATTRIBUTES)

        campaignManager!!.startCampaign(activity, arrayListOf(ITEM))

        verify(campaignPlayer, never()).start(activity, arrayListOf(ITEM))
    }

    @Test
    fun cancelCountDownCoroutine_cancelsCountDownCoroutine() {
        doReturn(ATTRIBUTES.toString()).whenever(cacheStorage).getString(AID_CAMPAIGN_ATTRIBUTES)

        campaignPlayers[ELEMENT_ID] = campaignPlayer
        campaignManager!!.cancelCountDownCoroutine(activity)

        verify(campaignPlayer, times(1)).cancelCountDownCoroutine(activity)
    }

    @Test
    fun cancelCountDownCoroutine_withoutAttributesCache_doesNotCancelCountDownCoroutine() {
        campaignPlayers[ELEMENT_ID] = campaignPlayer
        campaignManager!!.cancelCountDownCoroutine(activity)

        verify(campaignPlayer, never()).cancelCountDownCoroutine(activity)
    }

    @Test
    fun cancelCountDownCoroutine_withoutCampaignPlayer_doesNotCancelCountDownCoroutine() {
        doReturn(ATTRIBUTES.toString()).whenever(cacheStorage).getString(AID_CAMPAIGN_ATTRIBUTES)

        campaignManager!!.cancelCountDownCoroutine(activity)

        verify(campaignPlayer, never()).cancelCountDownCoroutine(activity)
    }

    @Test
    fun setOfferCountDownTime_setsOfferCountDownTime() {
        doReturn(ATTRIBUTES.toString()).whenever(cacheStorage).getString(AID_CAMPAIGN_ATTRIBUTES)

        campaignPlayers[ELEMENT_ID] = campaignPlayer
        campaignManager!!.setOfferCountDownTime(121)

        verify(campaignPlayer, times(1)).offerCountDownTime = 121000L
    }

    @Test
    fun setOfferCountDownTime_withoutAttributesCache_doesNotSetOfferCountDownTime() {
        campaignPlayers[ELEMENT_ID] = campaignPlayer
        campaignManager!!.setOfferCountDownTime(121)

        verify(campaignPlayer, never()).offerCountDownTime = 121000L
    }

    @Test
    fun setOfferCountDownTime_withoutCampaignPlayer_doesNotSetOfferCountDownTime() {
        doReturn(ATTRIBUTES.toString()).whenever(cacheStorage).getString(AID_CAMPAIGN_ATTRIBUTES)

        campaignManager!!.setOfferCountDownTime(121)

        verify(campaignPlayer, never()).offerCountDownTime = 121000L
    }

    @Test
    fun hideAll_hidesAll() {
        doReturn(ATTRIBUTES.toString()).whenever(cacheStorage).getString(AID_CAMPAIGN_ATTRIBUTES)

        campaignPlayers[ELEMENT_ID] = campaignPlayer
        campaignManager!!.hideAll(activity)

        verify(campaignPlayer, times(1)).hideAll(activity)
    }

    @Test
    fun hideAll_withoutAttributesCache_doesNotHideAll() {
        campaignPlayers[ELEMENT_ID] = campaignPlayer
        campaignManager!!.hideAll(activity)

        verify(campaignPlayer, never()).hideAll(activity)
    }

    @Test
    fun hideAll_withoutCampaignPlayer_doesNotHideAll() {
        doReturn(ATTRIBUTES.toString()).whenever(cacheStorage).getString(AID_CAMPAIGN_ATTRIBUTES)

        campaignManager!!.hideAll(activity)

        verify(campaignPlayer, never()).hideAll(activity)
    }
}
