package com.example.dietjoggingapp.adapters

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.dietjoggingapp.database.Jogging
import com.example.dietjoggingapp.database.domains.FoodSuggest
import com.example.dietjoggingapp.database.domains.ListFoodSuggest
import com.example.dietjoggingapp.databinding.ItemFoodSuggestBinding
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.squareup.picasso.Picasso
import java.util.*

class FoodSuggestAdapter: RecyclerView.Adapter<FoodSuggestAdapter.FoodSuggestViewHolder>() {
    var listFoodSuggest: MutableList<FoodSuggest> = arrayListOf()
    private var onClickListener: OnClickListener? = null
    var listener :RecyclerViewClickListener? = null
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
        // Create an English-German translator:
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.INDONESIAN)
            .build()
        val englishIndonesianTranslator = Translation.getClient(options)
        var conditions = DownloadConditions.Builder()
            .build()
        holder.titleText.text = listFoodSuggest.get(position).name
        holder.descText.text = listFoodSuggest.get(position).description
//        englishIndonesianTranslator.downloadModelIfNeeded()
//        if (!listFoodSuggest.isEmpty()) {
//            englishIndonesianTranslator.translate(listFoodSuggest.get(position).name.toString())
//                .addOnSuccessListener {
//                    listFoodSuggest.get(position).name = it.toString()
//                    Log.d("TAG", "getFoodSuggestion: ${listFoodSuggest.get(position).name.trim()}")
//                    holder.titleText.text = listFoodSuggest.get(position).name
//                }
//            englishIndonesianTranslator.translate(listFoodSuggest.get(position).description.toString())
//                .addOnSuccessListener {
//                    listFoodSuggest.get(position).description = it.toString().toLowerCase()
//                    Log.d("TAG", "getFoodSuggestion: ${listFoodSuggest.get(position).description.trim()}")
//                    holder.descText.text = listFoodSuggest.get(position).description
//                }
//                .addOnFailureListener {
//                    Log.d("TAG", "onBindViewHolder: ${it.localizedMessage?.trim()}")
//                    Log.d("TAG", "onBindViewHolder: ${it.message?.trim()}")
//                }
//        }
        Log.d("TAG", "onBindViewHolder: ${listFoodSuggest.get(1).ingredients.size}")
        holder.itemView.setOnClickListener {
            onClickListener?.onClick(position, listFoodSuggest.get(position))
        }
        holder.itemView.setOnClickListener {
            listener?.onItemClicked(it, listFoodSuggest.get(position))
        }
    }
    override fun getItemCount(): Int {
        return listFoodSuggest.size
    }
    
    fun setOnClickListener(listener: OnClickListener?) {
        this.onClickListener = listener
    }

    interface OnClickListener {
        fun onClick(position: Int, model: FoodSuggest)
    }

    interface RecyclerViewClickListener {

        // method yang akan dipanggil di MainActivity
        fun onItemClicked(view: View, foodSuggest: FoodSuggest)

    }
}