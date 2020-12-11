package com.appier.aidealapp.util

import android.annotation.SuppressLint
import java.util.*

@SuppressLint("SyntheticAccessor")
class ShoppingCart {
    companion object {
        private val cart: MutableMap<String, Int> = HashMap()

        fun addToCart(item: String) {
            var amount = cart[item]
            if (amount == null) amount = 1 else amount++
            cart[item] = amount
        }

        fun getCart(): List<Pair<String, Int>> {
            return cart.toList()
        }

        /**
         * return is the Cart still having this item
         */
        fun removeOneFromCart(item: String): Boolean {
            val amount: Int? = cart[item] ?: return false
            return if (amount == 1) {
                cart.remove(item)
                false
            } else {
                cart[item] = amount!! - 1
                true
            }
        }

        @JvmStatic
        fun clearCart() {
            cart.clear()
        }
    }
}
