package com.appier.aidealapp.ui.conversion

import android.app.Activity
import android.os.Bundle
import com.appier.aidealapp.R
import com.appier.aidealsdk.AiDeal
import com.appier.aidealapp.util.ShoppingCart
import com.appier.aidealsdk.AiDeal.PageType.*

class ConversionActivity : Activity() {
    private val aiDeal = AiDeal.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversion)

    }

    override fun onStart() {
        aiDeal.startLogging(this, AiDeal.Attributes(pageType = CONVERSION))
        super.onStart()

        ShoppingCart.clearCart()

        sendConversion()
    }
    
    private fun sendConversion() {
        // send conversion info
        val items = ArrayList<AiDeal.ConversionItem>()
        val item = AiDeal.ConversionItem(
                "9817493",
                "Adidas Shoes",
                "https://www.google.com?product=9817493",
                199.99,
                1
        )
        items.add(item)
        val coupons = ArrayList<String>()
        coupons.add("20OFF")

        val conversion = AiDeal.Conversion(
                "1234",
                "first_time_buy",
                coupons,
                items.size,
                199.99,
                items
        )

        aiDeal.log(conversion)
    }

    override fun onStop() {
        aiDeal.stopLogging(this)
        super.onStop()
    }
}
