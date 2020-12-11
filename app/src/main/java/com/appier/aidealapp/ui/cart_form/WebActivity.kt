package com.appier.aidealapp.ui.cart_form

import android.app.Activity
import android.os.Bundle
import com.appier.aidealapp.R
import com.appier.aidealsdk.AiDeal
import com.appier.aidealsdk.AiDeal.PageType.*
import kotlinx.android.synthetic.main.activity_web.*
import org.json.JSONObject

class WebActivity : Activity() {
    private val aiDeal = AiDeal.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)

        webview.loadUrl("file:///android_res/raw/example.html")

        reduceCD.setOnClickListener {
            val attributes = JSONObject().put("element_id", 17835).put("campaign_id", 17167)
            aiDeal.onCampaignReceived(attributes)
        }
    }

    override fun onStart() {
        aiDeal.startLogging(this, webview, AiDeal.Attributes(pageType = CART_FORM, cartPrice = 1000F))
        super.onStart()
    }

    override fun onStop() {
        aiDeal.stopLogging(this)
        super.onStop()
    }
}
