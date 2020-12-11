package com.appier.aidealapp.ui.category

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.appier.aidealapp.R
import com.appier.aidealsdk.AiDeal
import com.appier.aidealapp.ui.cart.CartActivity
import com.appier.aidealsdk.AiDeal.PageType.*
import kotlinx.android.synthetic.main.activity_category.*
import org.json.JSONObject

class CategoryActivity : Activity() {
    private val aiDeal = AiDeal.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        recyclerView.setHasFixedSize(true)

        // use a linear layout manager
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        // specify an adapter (see also next example)
        val mAdapter = CategoryAdapter(myDataSet)
        recyclerView.adapter = mAdapter

        btn_cart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        reduceCD.setOnClickListener {
            val attributes = JSONObject().put(
                    "element_id", 18205
            ).put("campaign_id", 17525
            ).put("coupon_code", "TEST CODE")
            aiDeal.onCampaignReceived(attributes)
        }
    }

    override fun onStart() {
        aiDeal.startLogging(this, AiDeal.Attributes(pageType = CATEGORY))
        super.onStart()
    }

    override fun onStop() {
        aiDeal.stopLogging(this)
        super.onStop()
    }

    private val myDataSet = Array(70) { i -> "Item $i" }
}
