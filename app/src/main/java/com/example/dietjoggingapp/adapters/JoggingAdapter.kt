package com.example.dietjoggingapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dietjoggingapp.R
import com.example.dietjoggingapp.database.Jogging
import com.example.dietjoggingapp.databinding.ItemRunBinding
import com.example.dietjoggingapp.other.TrackingUtil
import com.example.dietjoggingapp.other.UiState
import java.text.SimpleDateFormat
import java.util.*

class JoggingAdapter: RecyclerView.Adapter<JoggingAdapter.JoggingViewHolder>() {

//    val ivRunImage: ImageView = R.id.ivRunImage
    inner class JoggingViewHolder(val binding: ItemRunBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Jogging) {
            binding.ivRunImage.apply {
                Glide.with(this).load(item.img).into(binding.ivRunImage)
            }
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

    val diffCallback = object: DiffUtil.ItemCallback<Jogging>() {
        override fun areItemsTheSame(oldItem: Jogging, newItem: Jogging): Boolean {
            return  oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Jogging, newItem: Jogging): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }
    val differ = AsyncListDiffer(this, diffCallback)

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
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}