package com.appier.aidealapp.ui.cart_form

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.appier.aidealapp.R
import com.appier.aidealsdk.AiDeal
import com.appier.aidealapp.util.ShoppingCart
import com.appier.aidealapp.ui.conversion.ConversionActivity
import com.appier.aidealsdk.AiDeal.PageType.*
import kotlinx.android.synthetic.main.activity_cart_form.*

class CartFormActivity : Activity() {
    private val aiDeal = AiDeal.getInstance()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart_form)

        total_price.text = "Total Price: " + ShoppingCart.getCart().size * 100F

        checkout.setOnClickListener {
            startActivity(Intent(this, ConversionActivity::class.java))
        }
    }

    override fun onStart() {
        aiDeal.startLogging(this, AiDeal.Attributes(pageType = CART_FORM, cartPrice = ShoppingCart.getCart().size * 100F))
        super.onStart()
    }

    override fun onStop() {
        aiDeal.stopLogging(this)
        super.onStop()
    }
}
