package com.appier.aidealsdk

import android.app.Activity
import com.google.common.truth.Truth.*
import com.nhaarman.mockitokotlin2.*
import com.appier.aidealsdk.AIDCampaignState.*
import com.appier.aidealsdk.AiDeal.PageType.*
import com.appier.aidealsdk.view.CampaignLayout
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import java.lang.ref.WeakReference

@RunWith(RobolectricTestRunner::class)
class CampaignPlayerTest {
    companion object {
        private const val CONTROL_RANDOM_NUMBER = 1
        private const val EXPERIMENTAL_RANDOM_NUMBER = 2
    }

    private val cacheStorage = mock<AIDCacheStorage>()
    private val socket = mock<AIDSocket>()
    private val campaignLayout = mock<CampaignLayout>()
    private val campaignLayouts = HashMap<Int, WeakReference<CampaignLayout?>>()
    private val activity = Robolectric.setupActivity(Activity::class.java)

    private var campaignPlayer: CampaignPlayer? = null

    @Before
    fun setUp() {
        campaignPlayer = CampaignPlayer(cacheStorage, socket, ATTRIBUTES, CONFIG, CAMPAIGN, campaignLayouts)
    }

    @Test
    fun hideAll_updateCreativeDisplay() {
        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.hideAll(activity)

        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = false, showWebView = false)
    }

    @Test
    fun hideAll_withoutCampaignLayout_doesNotUpdateCreativeDisplay() {
        campaignPlayer!!.hideAll(activity)

        verify(campaignLayout, never()).updateCreativeDisplay(showBadge = false, showWebView = false)
    }

    @Test
    fun hideAll_withEmptyReference_doesNotUpdateCreativeDisplay() {
        val weakReference = WeakReference(campaignLayout)
        campaignLayouts[activity.hashCode()] = weakReference
        weakReference.clear()
        campaignPlayer!!.hideAll(activity)

        verify(campaignLayout, never()).updateCreativeDisplay(showBadge = false, showWebView = false)
    }

    @Test
    fun cancelCountDownCoroutine_cancelsCountDownCoroutine() {
        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.cancelCountDownCoroutine(activity)

        verify(campaignLayout, times(1)).cancelCountDownCoroutine()
    }

    @Test
    fun cancelCountDownCoroutine_withoutCampaignLayout_doesNotCancelCountDownCoroutine() {
        campaignPlayer!!.hideAll(activity)

        verify(campaignLayout, never()).cancelCountDownCoroutine()
    }

    @Test
    fun cancelCountDownCoroutine_withEmptyReference_doesNotCancelCountDownCoroutine() {
        val weakReference = WeakReference(campaignLayout)
        campaignLayouts[activity.hashCode()] = weakReference
        weakReference.clear()
        campaignPlayer!!.hideAll(activity)

        verify(campaignLayout, never()).cancelCountDownCoroutine()
    }

    @Test
    fun start_givenDefaultStateOnItemPageTypeInControlGroup_showsNothing() {
        doReturn(CONTROL_RANDOM_NUMBER).whenever(cacheStorage).getInt(AID_RANDOM_NUMBER_KEY)
        doReturn(DEFAULT).whenever(cacheStorage).getState()

        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.start(activity, arrayListOf(ITEM))

        verify(cacheStorage, never()).setState(any())
        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = false, showWebView = false)
        verify(socket, never()).sendSaveCampaign(ATTRIBUTES)
    }

    @Test
    fun start_givenDefaultStateOnItemPageTypeInExperimentalGroup_showsNothing() {
        doReturn(EXPERIMENTAL_RANDOM_NUMBER).whenever(cacheStorage).getInt(AID_RANDOM_NUMBER_KEY)
        doReturn(DEFAULT).whenever(cacheStorage).getState()

        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.start(activity, arrayListOf(ITEM))

        verify(cacheStorage, never()).setState(any())
        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = false, showWebView = false)
        verify(socket, never()).sendSaveCampaign(ATTRIBUTES)
    }

    @Test
    fun start_givenDefaultStateOnCartPageTypeInExperimentalGroup_showsNothing() {
        doReturn(EXPERIMENTAL_RANDOM_NUMBER).whenever(cacheStorage).getInt(AID_RANDOM_NUMBER_KEY)
        doReturn(DEFAULT).whenever(cacheStorage).getState()

        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.start(activity, arrayListOf(CART))

        verify(cacheStorage, never()).setState(any())
        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = false, showWebView = false)
        verify(socket, never()).sendSaveCampaign(ATTRIBUTES)
    }

    @Test
    fun start_givenDefaultStateOnCartFormPageTypeOnExperimentalGroup_showsNothing() {
        doReturn(EXPERIMENTAL_RANDOM_NUMBER).whenever(cacheStorage).getInt(AID_RANDOM_NUMBER_KEY)
        doReturn(DEFAULT).whenever(cacheStorage).getState()

        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.start(activity, arrayListOf(CART_FORM))

        verify(cacheStorage, times(1)).setState(DISABLED)
        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = false, showWebView = false)
        verify(socket, never()).sendSaveCampaign(ATTRIBUTES)
    }

    @Test
    fun start_givenDefaultStateOnConversionPageTypeOnExperimentalGroup_showsNothing() {
        doReturn(EXPERIMENTAL_RANDOM_NUMBER).whenever(cacheStorage).getInt(AID_RANDOM_NUMBER_KEY)
        doReturn(DEFAULT).whenever(cacheStorage).getState()

        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.start(activity, arrayListOf(CONVERSION))

        verify(cacheStorage, times(1)).setState(DISABLED)
        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = false, showWebView = false)
        verify(socket, never()).sendSaveCampaign(ATTRIBUTES)
    }

    @Test
    fun start_givenReadyStateOnItemPageTypeInControlGroup_showsFirstOffer() {
        doReturn(CONTROL_RANDOM_NUMBER).whenever(cacheStorage).getInt(AID_RANDOM_NUMBER_KEY)
        doReturn(READY).whenever(cacheStorage).getState()

        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.start(activity, arrayListOf(ITEM))

        verify(cacheStorage, times(1)).setState(FIRST_OFFER)
        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = false, showWebView = false)
        val captor = argumentCaptor<JSONObject>()
        verify(socket, times(1)).sendSaveCampaign(captor.capture())
        assertThat(captor.firstValue.toString()).isEqualTo(JSONObject(ATTRIBUTES.toString()).apply {
            put("is_control", true)
        }.toString())
    }

    @Test
    fun start_givenReadyStateOnItemPageTypeInExperimentalGroup_showsFirstOffer() {
        doReturn(EXPERIMENTAL_RANDOM_NUMBER).whenever(cacheStorage).getInt(AID_RANDOM_NUMBER_KEY)
        doReturn(READY).whenever(cacheStorage).getState()

        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.start(activity, arrayListOf(ITEM))

        verify(cacheStorage, times(1)).setState(FIRST_OFFER)
        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = false, showWebView = true)
        val captor = argumentCaptor<JSONObject>()
        verify(socket, times(1)).sendSaveCampaign(captor.capture())
        assertThat(captor.firstValue.toString()).isEqualTo(JSONObject(ATTRIBUTES.toString()).apply {
            put("is_control", false)
        }.toString())
    }

    @Test
    fun start_givenReadyStateOnCartPageTypeInExperimentalGroup_showsNothing() {
        doReturn(EXPERIMENTAL_RANDOM_NUMBER).whenever(cacheStorage).getInt(AID_RANDOM_NUMBER_KEY)
        doReturn(READY).whenever(cacheStorage).getState()

        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.start(activity, arrayListOf(CART))

        verify(cacheStorage, never()).setState(any())
        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = false, showWebView = false)
        verify(socket, never()).sendSaveCampaign(ATTRIBUTES)
    }

    @Test
    fun start_givenReadyStateOnCartFormPageTypeOnExperimentalGroup_showsNothing() {
        doReturn(EXPERIMENTAL_RANDOM_NUMBER).whenever(cacheStorage).getInt(AID_RANDOM_NUMBER_KEY)
        doReturn(READY).whenever(cacheStorage).getState()

        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.start(activity, arrayListOf(CART_FORM))

        verify(cacheStorage, times(1)).setState(DISABLED)
        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = false, showWebView = false)
        verify(socket, never()).sendSaveCampaign(ATTRIBUTES)
    }

    @Test
    fun start_givenReadyStateOnConversionPageTypeOnExperimentalGroup_showsNothing() {
        doReturn(EXPERIMENTAL_RANDOM_NUMBER).whenever(cacheStorage).getInt(AID_RANDOM_NUMBER_KEY)
        doReturn(READY).whenever(cacheStorage).getState()

        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.start(activity, arrayListOf(CONVERSION))

        verify(cacheStorage, times(1)).setState(DISABLED)
        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = false, showWebView = false)
        verify(socket, never()).sendSaveCampaign(ATTRIBUTES)
    }

    @Test
    fun start_givenFirstOfferStateOnItemPageTypeInControlGroup_showsBadge() {
        doReturn(CONTROL_RANDOM_NUMBER).whenever(cacheStorage).getInt(AID_RANDOM_NUMBER_KEY)
        doReturn(FIRST_OFFER).whenever(cacheStorage).getState()

        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.start(activity, arrayListOf(ITEM))

        verify(cacheStorage, times(1)).setState(OFFER)
        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = false, showWebView = false)
        verify(socket, never()).sendSaveCampaign(ATTRIBUTES)
    }

    @Test
    fun start_givenFirstOfferStateOnItemPageTypeInExperimentalGroup_showsBadge() {
        doReturn(EXPERIMENTAL_RANDOM_NUMBER).whenever(cacheStorage).getInt(AID_RANDOM_NUMBER_KEY)
        doReturn(FIRST_OFFER).whenever(cacheStorage).getState()

        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.start(activity, arrayListOf(ITEM))

        verify(cacheStorage, times(1)).setState(OFFER)
        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = true, showWebView = false)
        verify(socket, never()).sendSaveCampaign(ATTRIBUTES)
    }

    @Test
    fun start_givenFirstOfferStateOnCartPageTypeInExperimentalGroup_showsFirstPresent() {
        doReturn(EXPERIMENTAL_RANDOM_NUMBER).whenever(cacheStorage).getInt(AID_RANDOM_NUMBER_KEY)
        doReturn(FIRST_OFFER).whenever(cacheStorage).getState()

        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.start(activity, arrayListOf(CART))

        verify(cacheStorage, times(1)).setState(FIRST_PRESENT)
        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = false, showWebView = true)
        verify(socket, never()).sendSaveCampaign(ATTRIBUTES)
    }

    @Test
    fun start_givenFirstOfferStateOnCartFormPageTypeOnExperimentalGroup_showsFirstPresent() {
        doReturn(EXPERIMENTAL_RANDOM_NUMBER).whenever(cacheStorage).getInt(AID_RANDOM_NUMBER_KEY)
        doReturn(FIRST_OFFER).whenever(cacheStorage).getState()

        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.start(activity, arrayListOf(CART_FORM))

        verify(cacheStorage, times(1)).setState(FIRST_PRESENT)
        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = false, showWebView = true)
        verify(socket, never()).sendSaveCampaign(ATTRIBUTES)
    }

    @Test
    fun start_givenFirstOfferStateOnConversionPageTypeOnExperimentalGroup_showsNothing() {
        doReturn(EXPERIMENTAL_RANDOM_NUMBER).whenever(cacheStorage).getInt(AID_RANDOM_NUMBER_KEY)
        doReturn(FIRST_OFFER).whenever(cacheStorage).getState()

        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.start(activity, arrayListOf(CONVERSION))

        verify(cacheStorage, times(1)).setState(DISABLED)
        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = false, showWebView = false)
        verify(socket, never()).sendSaveCampaign(ATTRIBUTES)
    }

    @Test
    fun start_givenOfferStateOnItemPageTypeInControlGroup_showsBadge() {
        doReturn(CONTROL_RANDOM_NUMBER).whenever(cacheStorage).getInt(AID_RANDOM_NUMBER_KEY)
        doReturn(OFFER).whenever(cacheStorage).getState()

        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.start(activity, arrayListOf(ITEM))

        verify(cacheStorage, never()).setState(any())
        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = false, showWebView = false)
        verify(socket, never()).sendSaveCampaign(ATTRIBUTES)
    }

    @Test
    fun start_givenOfferStateOnItemPageTypeInExperimentalGroup_showsBadge() {
        doReturn(EXPERIMENTAL_RANDOM_NUMBER).whenever(cacheStorage).getInt(AID_RANDOM_NUMBER_KEY)
        doReturn(OFFER).whenever(cacheStorage).getState()

        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.start(activity, arrayListOf(ITEM))

        verify(cacheStorage, never()).setState(any())
        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = true, showWebView = false)
        verify(socket, never()).sendSaveCampaign(ATTRIBUTES)
    }

    @Test
    fun start_givenOfferStateOnCartPageTypeInExperimentalGroup_showsFirstPresent() {
        doReturn(EXPERIMENTAL_RANDOM_NUMBER).whenever(cacheStorage).getInt(AID_RANDOM_NUMBER_KEY)
        doReturn(OFFER).whenever(cacheStorage).getState()

        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.start(activity, arrayListOf(CART))

        verify(cacheStorage, times(1)).setState(FIRST_PRESENT)
        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = false, showWebView = true)
        verify(socket, never()).sendSaveCampaign(ATTRIBUTES)
    }

    @Test
    fun start_givenOfferStateOnCartFormPageTypeOnExperimentalGroup_showsFirstPresent() {
        doReturn(EXPERIMENTAL_RANDOM_NUMBER).whenever(cacheStorage).getInt(AID_RANDOM_NUMBER_KEY)
        doReturn(OFFER).whenever(cacheStorage).getState()

        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.start(activity, arrayListOf(CART_FORM))

        verify(cacheStorage, times(1)).setState(FIRST_PRESENT)
        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = false, showWebView = true)
        verify(socket, never()).sendSaveCampaign(ATTRIBUTES)
    }

    @Test
    fun start_givenOfferStateOnConversionPageTypeOnExperimentalGroup_showsNothing() {
        doReturn(EXPERIMENTAL_RANDOM_NUMBER).whenever(cacheStorage).getInt(AID_RANDOM_NUMBER_KEY)
        doReturn(OFFER).whenever(cacheStorage).getState()

        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.start(activity, arrayListOf(CONVERSION))

        verify(cacheStorage, times(1)).setState(DISABLED)
        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = false, showWebView = false)
        verify(socket, never()).sendSaveCampaign(ATTRIBUTES)
    }

    @Test
    fun start_givenFirstPresentStateOnItemPageTypeInControlGroup_showsBadge() {
        doReturn(CONTROL_RANDOM_NUMBER).whenever(cacheStorage).getInt(AID_RANDOM_NUMBER_KEY)
        doReturn(FIRST_PRESENT).whenever(cacheStorage).getState()

        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.start(activity, arrayListOf(ITEM))

        verify(cacheStorage, times(1)).setState(PRESENT)
        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = false, showWebView = false)
        verify(socket, never()).sendSaveCampaign(ATTRIBUTES)
    }

    @Test
    fun start_givenFirstPresentStateOnItemPageTypeInExperimentalGroup_showsBadge() {
        doReturn(EXPERIMENTAL_RANDOM_NUMBER).whenever(cacheStorage).getInt(AID_RANDOM_NUMBER_KEY)
        doReturn(FIRST_PRESENT).whenever(cacheStorage).getState()

        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.start(activity, arrayListOf(ITEM))

        verify(cacheStorage, times(1)).setState(PRESENT)
        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = true, showWebView = false)
        verify(socket, never()).sendSaveCampaign(ATTRIBUTES)
    }

    @Test
    fun start_givenFirstPresentStateOnCartPageTypeInExperimentalGroup_showsBadge() {
        doReturn(EXPERIMENTAL_RANDOM_NUMBER).whenever(cacheStorage).getInt(AID_RANDOM_NUMBER_KEY)
        doReturn(FIRST_PRESENT).whenever(cacheStorage).getState()

        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.start(activity, arrayListOf(CART))

        verify(cacheStorage, times(1)).setState(PRESENT)
        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = true, showWebView = false)
        verify(socket, never()).sendSaveCampaign(ATTRIBUTES)
    }

    @Test
    fun start_givenFirstPresentStateOnCartFormPageTypeOnExperimentalGroup_showsBadge() {
        doReturn(EXPERIMENTAL_RANDOM_NUMBER).whenever(cacheStorage).getInt(AID_RANDOM_NUMBER_KEY)
        doReturn(FIRST_PRESENT).whenever(cacheStorage).getState()

        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.start(activity, arrayListOf(CART_FORM))

        verify(cacheStorage, times(1)).setState(PRESENT)
        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = true, showWebView = false)
        verify(socket, never()).sendSaveCampaign(ATTRIBUTES)
    }

    @Test
    fun start_givenFirstPresentStateOnConversionPageTypeOnExperimentalGroup_showsNothing() {
        doReturn(EXPERIMENTAL_RANDOM_NUMBER).whenever(cacheStorage).getInt(AID_RANDOM_NUMBER_KEY)
        doReturn(FIRST_PRESENT).whenever(cacheStorage).getState()

        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.start(activity, arrayListOf(CONVERSION))

        verify(cacheStorage, times(1)).setState(DISABLED)
        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = false, showWebView = false)
        verify(socket, never()).sendSaveCampaign(ATTRIBUTES)
    }

    @Test
    fun start_givenPresentStateOnItemPageTypeInControlGroup_showsBadge() {
        doReturn(CONTROL_RANDOM_NUMBER).whenever(cacheStorage).getInt(AID_RANDOM_NUMBER_KEY)
        doReturn(PRESENT).whenever(cacheStorage).getState()

        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.start(activity, arrayListOf(ITEM))

        verify(cacheStorage, never()).setState(any())
        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = false, showWebView = false)
        verify(socket, never()).sendSaveCampaign(ATTRIBUTES)
    }

    @Test
    fun start_givenPresentStateOnItemPageTypeInExperimentalGroup_showsBadge() {
        doReturn(EXPERIMENTAL_RANDOM_NUMBER).whenever(cacheStorage).getInt(AID_RANDOM_NUMBER_KEY)
        doReturn(PRESENT).whenever(cacheStorage).getState()

        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.start(activity, arrayListOf(ITEM))

        verify(cacheStorage, never()).setState(any())
        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = true, showWebView = false)
        verify(socket, never()).sendSaveCampaign(ATTRIBUTES)
    }

    @Test
    fun start_givenPresentStateOnCartPageTypeInExperimentalGroup_showsBadge() {
        doReturn(EXPERIMENTAL_RANDOM_NUMBER).whenever(cacheStorage).getInt(AID_RANDOM_NUMBER_KEY)
        doReturn(PRESENT).whenever(cacheStorage).getState()

        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.start(activity, arrayListOf(CART))

        verify(cacheStorage, never()).setState(any())
        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = true, showWebView = false)
        verify(socket, never()).sendSaveCampaign(ATTRIBUTES)
    }

    @Test
    fun start_givenPresentStateOnCartFormPageTypeOnExperimentalGroup_showsBadge() {
        doReturn(EXPERIMENTAL_RANDOM_NUMBER).whenever(cacheStorage).getInt(AID_RANDOM_NUMBER_KEY)
        doReturn(PRESENT).whenever(cacheStorage).getState()

        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.start(activity, arrayListOf(CART_FORM))

        verify(cacheStorage, never()).setState(any())
        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = true, showWebView = false)
        verify(socket, never()).sendSaveCampaign(ATTRIBUTES)
    }

    @Test
    fun start_givenPresentStateOnConversionPageTypeOnExperimentalGroup_showsNothing() {
        doReturn(EXPERIMENTAL_RANDOM_NUMBER).whenever(cacheStorage).getInt(AID_RANDOM_NUMBER_KEY)
        doReturn(PRESENT).whenever(cacheStorage).getState()

        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.start(activity, arrayListOf(CONVERSION))

        verify(cacheStorage, times(1)).setState(DISABLED)
        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = false, showWebView = false)
        verify(socket, never()).sendSaveCampaign(ATTRIBUTES)
    }

    @Test
    fun start_givenDisabledStateOnItemPageTypeInControlGroup_showsNothing() {
        doReturn(CONTROL_RANDOM_NUMBER).whenever(cacheStorage).getInt(AID_RANDOM_NUMBER_KEY)
        doReturn(DISABLED).whenever(cacheStorage).getState()

        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.start(activity, arrayListOf(ITEM))

        verify(cacheStorage, never()).setState(any())
        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = false, showWebView = false)
        verify(socket, never()).sendSaveCampaign(ATTRIBUTES)
    }

    @Test
    fun start_givenDisabledStateOnItemPageTypeInExperimentalGroup_showsNothing() {
        doReturn(EXPERIMENTAL_RANDOM_NUMBER).whenever(cacheStorage).getInt(AID_RANDOM_NUMBER_KEY)
        doReturn(DISABLED).whenever(cacheStorage).getState()

        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.start(activity, arrayListOf(ITEM))

        verify(cacheStorage, never()).setState(any())
        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = false, showWebView = false)
        verify(socket, never()).sendSaveCampaign(ATTRIBUTES)
    }

    @Test
    fun start_givenDisabledStateOnCartPageTypeInExperimentalGroup_showsNothing() {
        doReturn(EXPERIMENTAL_RANDOM_NUMBER).whenever(cacheStorage).getInt(AID_RANDOM_NUMBER_KEY)
        doReturn(DISABLED).whenever(cacheStorage).getState()

        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.start(activity, arrayListOf(CART))

        verify(cacheStorage, never()).setState(any())
        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = false, showWebView = false)
        verify(socket, never()).sendSaveCampaign(ATTRIBUTES)
    }

    @Test
    fun start_givenDisabledStateOnCartFormPageTypeOnExperimentalGroup_showsNothing() {
        doReturn(EXPERIMENTAL_RANDOM_NUMBER).whenever(cacheStorage).getInt(AID_RANDOM_NUMBER_KEY)
        doReturn(DISABLED).whenever(cacheStorage).getState()

        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.start(activity, arrayListOf(CART_FORM))

        verify(cacheStorage, never()).setState(any())
        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = false, showWebView = false)
        verify(socket, never()).sendSaveCampaign(ATTRIBUTES)
    }

    @Test
    fun start_givenDisabledStateOnConversionPageTypeOnExperimentalGroup_showsNothing() {
        doReturn(EXPERIMENTAL_RANDOM_NUMBER).whenever(cacheStorage).getInt(AID_RANDOM_NUMBER_KEY)
        doReturn(DISABLED).whenever(cacheStorage).getState()

        campaignLayouts[activity.hashCode()] = WeakReference(campaignLayout)
        campaignPlayer!!.start(activity, arrayListOf(CONVERSION))

        verify(cacheStorage, never()).setState(any())
        verify(campaignLayout, times(1)).updateCreativeDisplay(showBadge = false, showWebView = false)
        verify(socket, never()).sendSaveCampaign(ATTRIBUTES)
    }

    @Test
    fun isControl_withNIs1000AndControlPercentIs50_satisfiesExactBinomialTest() {
        assertThat(trialIsControl(1000, 50)).isGreaterThan(400)
        assertThat(trialIsControl(1000, 50)).isAtMost(600)
    }

    @Test
    fun isControl_withNIs1000AndControlPercentIs20_satisfiesExactBinomialTest() {
        assertThat(trialIsControl(1000, 20)).isGreaterThan(100)
        assertThat(trialIsControl(1000, 20)).isAtMost(300)
    }

    @Test
    fun isControl_withNIs1000AndControlPercentIs80_satisfiesExactBinomialTest() {
        assertThat(trialIsControl(1000, 80)).isGreaterThan(700)
        assertThat(trialIsControl(1000, 80)).isAtMost(900)
    }

    @Test
    fun isControl_withNIs100AndControlPercentIs50_satisfiesExactBinomialTest() {
        assertThat(trialIsControl(100, 50)).isGreaterThan(30)
        assertThat(trialIsControl(100, 50)).isAtMost(70)
    }

    @Test
    fun isControl_withNIs100AndControlPercentIs20_satisfiesExactBinomialTest() {
        assertThat(trialIsControl(100, 20)).isGreaterThan(0)
        assertThat(trialIsControl(100, 20)).isAtMost(40)
    }

    @Test
    fun isControl_withNIs100AndControlPercentIs80_satisfiesExactBinomialTest() {
        assertThat(trialIsControl(100, 80)).isGreaterThan(60)
        assertThat(trialIsControl(100, 80)).isAtMost(100)
    }

    private fun trialIsControl(N: Int, controlPercent: Int): Int {
        val campaignId = (0 until 1000).random()
        var success = 0
        repeat(N) {
            val randomNumber = (0 until 100).random()
            val isControl = campaignPlayer!!.calculateIsControl(campaignId, controlPercent, randomNumber)
            success += if (isControl) 1 else 0
        }
        return success
    }
}
