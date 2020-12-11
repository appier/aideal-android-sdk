package com.appier.aidealsdk

import com.google.common.truth.Truth.assertThat
import com.appier.aidealsdk.AIDCampaignState.*
import junit.framework.Assert.fail
import org.json.JSONException
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class AIDCacheStorageTest {
    companion object {
        private const val KEY = "key"
        private const val STRING_VALUE = "string"
        private const val INT_VALUE = 1
        private const val LONG_VALUE = 2L
        private const val BOOLEAN_VALUE = false
        private const val MAX_AGE = 60 * 5
    }

    @Suppress("DEPRECATION")
    private val application = RuntimeEnvironment.application
    private val cacheStorage = AIDCacheStorage.getInstance(application)

    @After
    fun tearDown() {
        cacheStorage.clear()
    }

    @Test
    fun getInstance_returnsSingleton() {
        val instance1 = AIDCacheStorage.getInstance(application)
        val instance2 = AIDCacheStorage.getInstance(application)
        assertThat(instance1).isEqualTo(instance2)
    }

    @Test
    fun getString_returnsNull() {
        val value = cacheStorage.getString(KEY)
        assertThat(value).isNull()
    }

    @Test
    fun getString_givenPutString_returnsString() {
        cacheStorage.put(KEY, STRING_VALUE)

        val value = cacheStorage.getString(KEY)
        assertThat(value).isEqualTo(STRING_VALUE)
    }

    @Test
    fun getString_givenPutInt_returnsString() {
        cacheStorage.put(KEY, INT_VALUE)

        val value = cacheStorage.getString(KEY)
        assertThat(value).isEqualTo(INT_VALUE.toString())
    }

    @Test
    fun getString_givenPutLong_returnsString() {
        cacheStorage.put(KEY, LONG_VALUE)

        val value = cacheStorage.getString(KEY)
        assertThat(value).isEqualTo(LONG_VALUE.toString())
    }

    @Test
    fun getString_givenPutBoolean_returnsString() {
        cacheStorage.put(KEY, BOOLEAN_VALUE)

        val value = cacheStorage.getString(KEY)
        assertThat(value).isEqualTo(BOOLEAN_VALUE.toString())
    }

    @Test
    fun getInt_returnsNull() {
        val value = cacheStorage.getInt(KEY)
        assertThat(value).isNull()
    }

    @Test
    fun getInt_givenPutInt_returnsInt() {
        cacheStorage.put(KEY, INT_VALUE)

        val value = cacheStorage.getInt(KEY)
        assertThat(value).isEqualTo(INT_VALUE)
    }

    @Test
    fun getInt_givenPutString_throwsError() {
        cacheStorage.put(KEY, STRING_VALUE)

        try {
            cacheStorage.getInt(KEY)
            fail()
        } catch (e: Exception) {
            assertThat(e).isInstanceOf(JSONException::class.java)
        }
    }

    @Test
    fun getInt_givenPutIntString_returnsInt() {
        cacheStorage.put(KEY, INT_VALUE.toString())

        val value = cacheStorage.getInt(KEY)
        assertThat(value).isEqualTo(INT_VALUE)
    }

    @Test
    fun getInt_givenPutLong_returnsInt() {
        cacheStorage.put(KEY, LONG_VALUE)

        val value = cacheStorage.getInt(KEY)
        assertThat(value).isEqualTo(LONG_VALUE.toInt())
    }

    @Test
    fun getInt_givenPutBoolean_throwsError() {
        cacheStorage.put(KEY, BOOLEAN_VALUE)

        try {
            cacheStorage.getInt(KEY)
            fail()
        } catch (e: Exception) {
            assertThat(e).isInstanceOf(JSONException::class.java)
        }
    }

    @Test
    fun getLong_givenPutNull_returnsNull() {
        val value = cacheStorage.getLong(KEY)
        assertThat(value).isNull()
    }

    @Test
    fun getLong_givenPutLong_returnsLong() {
        cacheStorage.put(KEY, LONG_VALUE)

        val value = cacheStorage.getLong(KEY)
        assertThat(value).isEqualTo(LONG_VALUE)
    }

    @Test
    fun getLong_givenPutString_throwsError() {
        cacheStorage.put(KEY, STRING_VALUE)

        try {
            cacheStorage.getLong(KEY)
            fail()
        } catch (e: Exception) {
            assertThat(e).isInstanceOf(JSONException::class.java)
        }
    }

    @Test
    fun getLong_givenPutLongString_returnsLong() {
        cacheStorage.put(KEY, LONG_VALUE.toString())

        val value = cacheStorage.getInt(KEY)
        assertThat(value).isEqualTo(LONG_VALUE)
    }

    @Test
    fun getLong_givenPutInt_returnsLong() {
        cacheStorage.put(KEY, INT_VALUE)

        val value = cacheStorage.getInt(KEY)
        assertThat(value).isEqualTo(INT_VALUE.toLong())
    }

    @Test
    fun getLong_givenPutBoolean_throwsError() {
        cacheStorage.put(KEY, BOOLEAN_VALUE)

        try {
            cacheStorage.getLong(KEY)
            fail()
        } catch (e: Exception) {
            assertThat(e).isInstanceOf(JSONException::class.java)
        }
    }

    @Test
    fun getBoolean_returnsNull() {
        val value = cacheStorage.getBoolean(KEY)
        assertThat(value).isNull()
    }

    @Test
    fun getBoolean_givenPutInt_throwsError() {
        cacheStorage.put(KEY, INT_VALUE)

        try {
            cacheStorage.getBoolean(KEY)
            fail()
        } catch (e: Exception) {
            assertThat(e).isInstanceOf(JSONException::class.java)
        }
    }

    @Test
    fun getBoolean_givenPutString_throwsError() {
        cacheStorage.put(KEY, STRING_VALUE)

        try {
            cacheStorage.getBoolean(KEY)
            fail()
        } catch (e: Exception) {
            assertThat(e).isInstanceOf(JSONException::class.java)
        }
    }

    @Test
    fun getBoolean_givenPutBooleanString_returnsBoolean() {
        cacheStorage.put(KEY, BOOLEAN_VALUE.toString())

        val value = cacheStorage.getBoolean(KEY)
        assertThat(value).isEqualTo(BOOLEAN_VALUE)
    }

    @Test
    fun getBoolean_givenPutLong_throwsError() {
        cacheStorage.put(KEY, LONG_VALUE)

        try {
            cacheStorage.getBoolean(KEY)
            fail()
        } catch (e: Exception) {
            assertThat(e).isInstanceOf(JSONException::class.java)
        }
    }

    @Test
    fun getBoolean_givenPutBoolean_returnsBoolean() {
        cacheStorage.put(KEY, BOOLEAN_VALUE)

        val value = cacheStorage.getBoolean(KEY)
        assertThat(value).isEqualTo(BOOLEAN_VALUE)
    }

    @Test
    fun putString_getsString() {
        cacheStorage.put(KEY, STRING_VALUE)

        val value = cacheStorage.getString(KEY)
        assertThat(value).isEqualTo(STRING_VALUE)
    }

    @Test
    fun putString_giveRemove_getsNull() {
        cacheStorage.put(KEY, STRING_VALUE)
        cacheStorage.remove(KEY)

        val value = cacheStorage.getString(KEY)
        assertThat(value).isNull()
    }

    @Test
    fun putString_givePositiveMaxAge_getsString() {
        cacheStorage.put(KEY, STRING_VALUE, MAX_AGE)

        val value = cacheStorage.getString(KEY)
        assertThat(value).isEqualTo(STRING_VALUE)
    }

    @Test
    fun putString_giveNegativeMaxAge_getsNull() {
        cacheStorage.put(KEY, STRING_VALUE, -MAX_AGE)

        val value = cacheStorage.getString(KEY)
        assertThat(value).isNull()
    }

    @Test
    fun setSessionExpires_givenPositiveMaxAge_continuesSession() {
        cacheStorage.put(AID_USID_KEY, USID)
        cacheStorage.put(AID_SID_KEY, SID)
        cacheStorage.put(AID_CAMPAIGN_STATE_KEY, DISABLED.name)
        cacheStorage.setSessionExpires(MAX_AGE)

        val usid = cacheStorage.getString(AID_USID_KEY)
        val sid = cacheStorage.getString(AID_SID_KEY)
        val campaignState = cacheStorage.getString(AID_CAMPAIGN_STATE_KEY)
        assertThat(usid).isEqualTo(USID)
        assertThat(sid).isEqualTo(SID)
        assertThat(campaignState).isEqualTo(DISABLED.name)
    }

    @Test
    fun setSessionExpires_givenNegativeMaxAge_resetsSession() {
        cacheStorage.put(AID_USID_KEY, USID)
        cacheStorage.put(AID_SID_KEY, SID)
        cacheStorage.put(AID_CAMPAIGN_STATE_KEY, DISABLED.name)
        cacheStorage.setSessionExpires(-MAX_AGE)

        val usid = cacheStorage.getString(AID_USID_KEY)
        val sid = cacheStorage.getString(AID_SID_KEY)
        val campaignState = cacheStorage.getString(AID_CAMPAIGN_STATE_KEY)
        assertThat(usid).isNull()
        assertThat(sid).isNull()
        assertThat(campaignState).isNull()
    }

    @Test
    fun setSessionExpires_resetSession_remainSession() {
        cacheStorage.put(AID_USID_KEY, USID)
        cacheStorage.put(AID_SID_KEY, SID)
        cacheStorage.put(AID_CAMPAIGN_STATE_KEY, DISABLED.name)
        cacheStorage.resetSession()

        val usid = cacheStorage.getString(AID_USID_KEY)
        val sid = cacheStorage.getString(AID_SID_KEY)
        val campaignState = cacheStorage.getString(AID_CAMPAIGN_STATE_KEY)
        assertThat(usid).isNull()
        assertThat(sid).isNull()
        assertThat(campaignState).isNull()
    }

    @Test
    fun getState_returnsDefault() {
        val state = cacheStorage.getState()
        assertThat(state).isEqualTo(DEFAULT)
    }

    @Test
    fun getState_givenDisabled_returnsDisable() {
        cacheStorage.setState(DISABLED)

        val state = cacheStorage.getState()
        assertThat(state).isEqualTo(DISABLED)
    }
}
