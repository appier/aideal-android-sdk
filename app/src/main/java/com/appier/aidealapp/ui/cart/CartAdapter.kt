package com.appier.aidealapp.ui.cart

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.appier.aidealapp.R
import com.appier.aidealapp.util.ShoppingCart

class CartAdapter : RecyclerView.Adapter<CartAdapter.MyViewHolder>() {
    private var list = ShoppingCart.getCart()

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class MyViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyViewHolder {
        // create a new view
        val textView = LayoutInflater.from(parent.context)
                .inflate(R.layout.my_text_view, parent, false) as TextView
        // set the view's size, margins, paddings and layout parameters
        //...
        textView.textSize = 24f
        return MyViewHolder(textView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        val pair = list[position]
        val item = pair.first
        val amount = pair.second
        holder.textView.text = "Item: $item | Amount: $amount"


        holder.textView.setOnClickListener {
            if (ShoppingCart.removeOneFromCart(item)) {
                list = ShoppingCart.getCart()
                notifyItemChanged(position)
            } else {
                list = ShoppingCart.getCart()
                notifyDataSetChanged()
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = list.size
}
