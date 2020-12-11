package com.appier.aidealapp.ui.my_page

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import com.appier.aidealapp.R

class MyPageActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        val fragment = MyPageTopFragment()
        val transaction = supportFragmentManager!!.beginTransaction()
        transaction.replace(R.id.content_aideal, fragment)
        transaction.commit()
    }
}
