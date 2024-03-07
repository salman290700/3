package com.example.dietjoggingapp.adapters

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.dietjoggingapp.database.Jogging
import com.example.dietjoggingapp.database.domains.FoodSuggest
import com.example.dietjoggingapp.database.domains.ListFoodSuggest
import com.example.dietjoggingapp.databinding.ItemFoodSuggestBinding
import com.squareup.picasso.Picasso
import java.util.*

class FoodSuggestAdapter: RecyclerView.Adapter<FoodSuggestAdapter.FoodSuggestViewHolder>() {
    var listFoodSuggest: MutableList<FoodSuggest> = arrayListOf()
    private lateinit var context: Context
    inner class FoodSuggestViewHolder(val binding: ItemFoodSuggestBinding): RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        var titleText = binding.tvNamgeOfFood
        var descText = binding.tvDescription
        var image = binding.ivFoodSuggestImage
        override fun onClick(v: View?) {
            binding.root.setOnClickListener(this)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodSuggestViewHolder {
        val itemView = ItemFoodSuggestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodSuggestViewHolder(itemView)
    }

    fun submitList(list: List<FoodSuggest>){
        for (i in 0..list.size-1) {
            this.listFoodSuggest.add(list.get(i))
        }
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: FoodSuggestViewHolder, position: Int) {
        var requestOptions = RequestOptions()
        requestOptions = requestOptions.transform(CenterCrop(), RoundedCorners(30))

//        Glide.with(context)
//            .load(listFoodSuggest.get(position).image)
//            .apply(requestOptions)
//            .into(holder.image)
        Log.d("TAG", "onBindViewHolder: ${listFoodSuggest.get(position).image}")
        Picasso.get().load(listFoodSuggest.get(position).image).into(holder.image)

        holder.titleText.text = listFoodSuggest.get(position).name
        holder.descText.text = listFoodSuggest.get(position).description
    }

    override fun getItemCount(): Int {
        return listFoodSuggest.size
    }
}