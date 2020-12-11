package com.appier.aidealapp.ui.my_page

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.appier.aidealapp.R
import com.appier.aidealsdk.AiDeal
import com.appier.aidealsdk.AiDeal.PageType.*
import kotlinx.android.synthetic.main.fragment_my_page_top.*

class MyPageTopFragment : Fragment() {
    private val aiDeal = AiDeal.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_page_top, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_account.setOnClickListener {
            navigate(MyPageAccountFragment())
        }
        btn_password.setOnClickListener {
            navigate(MyPagePasswordFragment())
        }
    }

    override fun onStart() {
        aiDeal.startLogging(context, AiDeal.Attributes(pageType = MYPAGE))
        super.onStart()
    }

    override fun onStop() {
        aiDeal.stopLogging(context)
        super.onStop()
    }

    private fun navigate(fragment: Fragment) {
        val transaction = fragmentManager!!.beginTransaction()
        transaction.setCustomAnimations(
                R.anim.slide_in_left,
                R.anim.slide_out_left,
                R.anim.slide_out_right,
                R.anim.slide_in_right
        )
        transaction.replace(R.id.content_aideal, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
