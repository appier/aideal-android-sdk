package com.appier.aidealapp.ui.top

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.appier.aidealapp.R
import com.appier.aidealsdk.AiDeal
import com.appier.aidealapp.ui.cart.CartActivity
import com.appier.aidealapp.ui.category.CategoryActivity
import com.appier.aidealapp.ui.my_page.MyPageActivity
import com.appier.aidealsdk.AiDeal.PageType.*
import kotlinx.android.synthetic.main.activity_top.*
import kotlinx.android.synthetic.main.activity_top.reduceCD

class TopActivity : Activity() {
    private val aiDeal = AiDeal.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top)
        btn_category.setOnClickListener {
            val intent = Intent(this, CategoryActivity::class.java)
            startActivity(intent)
        }

        btn_mypage.setOnClickListener {
            val intent = Intent(this, MyPageActivity::class.java)
            startActivity(intent)
        }

        clearState.setOnClickListener {
            aiDeal.resetSessionState()
        }

        enableDataCollection.setOnClickListener {
            aiDeal.setDataCollection(true)
        }

        disableDataCollection.setOnClickListener {
            aiDeal.setDataCollection(false)
        }

        reduceCD.setOnClickListener {
            aiDeal.setOfferCountDownTime(20)
        }

        resetCD.setOnClickListener {
            aiDeal.setOfferCountDownTime(60 * 60)
        }

        reduceSessionTime.setOnClickListener {
            aiDeal.setSessionExtendTime(120)
        }

        btn_cart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
    }

    override fun onStart() {
        aiDeal.startLogging(this, AiDeal.Attributes(pageType = TOP))
        super.onStart()
    }

    override fun onStop() {
        aiDeal.stopLogging(this)
        super.onStop()
    }
}
