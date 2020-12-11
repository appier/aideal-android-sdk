package com.appier.aidealapp.ui.cart

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.appier.aidealapp.R
import com.appier.aidealsdk.AiDeal
import com.appier.aidealapp.ui.cart_form.CartFormActivity
import com.appier.aidealsdk.AiDeal.PageType.*
import kotlinx.android.synthetic.main.activity_cart.*

class CartActivity : Activity() {
    private val aiDeal = AiDeal.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        btn_to_buy.setOnClickListener {
            startActivity(Intent(this, CartFormActivity::class.java))
        }

        recyclerView.setHasFixedSize(true)

        // use a linear layout manager
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        // specify an adapter (see also next example)
        val mAdapter = CartAdapter()
        recyclerView.adapter = mAdapter
    }

    override fun onStart() {
        aiDeal.startLogging(this, AiDeal.Attributes(pageType = CART))
        super.onStart()
    }

    override fun onStop() {
        aiDeal.stopLogging(this)
        super.onStop()
    }
}
