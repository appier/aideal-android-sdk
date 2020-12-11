@file:Suppress("unused")

package com.appier.aidealsdk

import android.annotation.SuppressLint
import org.json.JSONArray
import org.json.JSONObject

// Init values
const val APIKEY = "testtest"
const val UUID = "5e4e1657165b3c10e721711b"
const val USID = "5e4e1657165b3c10e721711b"
const val SID = "5f5dda3a8aeb791193da4f12"
const val IS_LOGIN = false
const val ITEM_PRICE = 3000f
const val CONVERSION_ID = "1234"
const val CONVERSION_NAME = "member"
const val CONVERSION_ITEM_COUNT = 1
const val CONVERSION_PRICE = 199.99
const val CONVERSION_ITEM1_ID = "9817493"
const val CONVERSION_ITEM1_NAME = "Adidas Shoes"
const val CONVERSION_ITEM1_URL = "https://www.google.com?product=9817493"
const val CONVERSION_ITEM1_PRICE = 199.99
const val CONVERSION_ITEM1_COUNT = 1

// Behavior values
const val HEIGHT = 2112
const val WIDTH = 1440
const val SCROLL_Y = 423

// JSON values
const val ELEMENT_ID = 18205
const val CAMPAIGN_ID = 17525
const val COUPON_CODE = "TEST CODE"
const val CAMPAIGN_CONTROL_PERCENT = 10
const val LIMIT = 61

// JSONs
val ATTRIBUTES = JSONObject().apply {
    put("element_id", ELEMENT_ID)
    put("campaign_id", CAMPAIGN_ID)
    put("coupon_code", COUPON_CODE)
}

val CONFIG = JSONObject().apply {
    put("element_id", ELEMENT_ID)
    put("campaign_id", CAMPAIGN_ID)
    put("campaign_control_percent", CAMPAIGN_CONTROL_PERCENT)
    put("variables", JSONObject().apply {
        put("limit", LIMIT)
    })

}

@SuppressLint("UnusedResources")
val CONFIGS = JSONObject().apply {
    put("apikey", APIKEY)
    put("campaignObjectsByElementId", JSONObject().apply {
        put(ELEMENT_ID.toString(), CONFIG)
    })
}

val CAMPAIGN = JSONObject()

@SuppressLint("UnusedResources")
val CAMPAIGNS = JSONObject().apply {
    put(ELEMENT_ID.toString(), CAMPAIGN)
}

val REPLY = JSONObject().apply {
    put("uuid", UUID)
    put("usid", USID)
    put("sid", SID)
}

val CONVERSION_INFO = JSONObject().apply {
    put("id", CONVERSION_ID)
    put("name", CONVERSION_NAME)
    put("coupon_codes", JSONArray())
    put("item_count", CONVERSION_ITEM_COUNT)
    put("price", CONVERSION_PRICE)
    put("items", JSONArray())
}

val CONVERSION_ITEM1 = JSONObject().apply {
    put("id", CONVERSION_ITEM1_ID)
    put("name", CONVERSION_ITEM1_NAME)
    put("url", CONVERSION_ITEM1_URL)
    put("price", CONVERSION_ITEM1_PRICE)
    put("count", CONVERSION_ITEM1_COUNT)
}
