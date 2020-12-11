package com.appier.aidealapp.ui.item

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.appier.aidealapp.R
import com.appier.aidealsdk.AiDeal
import com.appier.aidealapp.util.ShoppingCart
import com.appier.aidealapp.ui.cart.CartActivity
import com.appier.aidealapp.ui.cart_form.CartFormActivity
import com.appier.aidealsdk.AiDeal.PageType.*
import kotlinx.android.synthetic.main.activity_item.*

class ItemActivity : Activity() {
    private val aiDeal = AiDeal.getInstance()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)

        val item = intent.getIntExtra("ITEM", 0)
        txt_title.text = "ITEM: $item"

        btn_addToCart.setOnClickListener {
            ShoppingCart.addToCart(item.toString())
            Toast.makeText(applicationContext, "Item $item is added to Cart", Toast.LENGTH_SHORT).show()
        }

        btn_cart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        btn_direct_buy.setOnClickListener {
            startActivity(Intent(this, CartFormActivity::class.java))
        }
    }

    override fun onStart() {
        aiDeal.startLogging(this, AiDeal.Attributes(pageType = ITEM, itemPrice = 100F))
        super.onStart()
    }

    override fun onStop() {
        aiDeal.stopLogging(this)
        super.onStop()
    }
}
