package com.example.dietjoggingapp.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.dietjoggingapp.R
import com.example.dietjoggingapp.database.Jogging
import com.example.dietjoggingapp.databinding.ItemRunBinding
import com.example.dietjoggingapp.other.TrackingUtil
import com.squareup.picasso.Picasso
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class JoggingAdapter(): RecyclerView.Adapter<JoggingAdapter.JoggingViewHolder>() {

//    val ivRunImage: ImageView = R.id.ivRunImage
    inner class JoggingViewHolder(val binding: ItemRunBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Jogging) {
            val requestOption = RequestOptions()
            requestOption.placeholder(R.drawable.ic_baseline_delete_24)
            requestOption.error(R.drawable.ic_baseline_delete_24)
            if (item.img != "" || item.img != null) {
                Picasso.get().load(item.img).into(binding.ivRunImage)
            }
            Log.d("TAG", "bind: ${item.img}")
            val calendar = Calendar.getInstance().apply {
                timeInMillis = item.timeInMillis
            }
            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            binding.tvDate.text = dateFormat.format(calendar.time)

            val avgSpeed = "${item.avgSpeedInKmh}Km/h"
            binding.tvAvgSpeed.text = avgSpeed

            val distanceInKm = "${item.distanceInMeters / 1000f}Km"
            binding.tvDistance.text = distanceInKm

            binding.tvTime.text = TrackingUtil.getFormattedStopWatchTime(item.timeInMillis)

            val caloriesBurned = "${item.caloriesBurned}kcal"
            binding.tvCalories.text = caloriesBurned
        }
    }

    private var list: MutableList<Jogging> = arrayListOf()

    fun submitList(list: MutableList<Jogging>){
        this.list = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JoggingViewHolder {
        val itemView = ItemRunBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JoggingViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: JoggingViewHolder, position: Int) {
        val item = list[position]
        if (item!= null) {
            holder.bind(item)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}