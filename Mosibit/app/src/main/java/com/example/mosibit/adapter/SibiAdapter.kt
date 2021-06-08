package com.example.mosibit.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.mosibit.data.Sibi
import com.example.mosibit.databinding.ItemSibiBinding


class SibiAdapter(val listSibi: ArrayList<Sibi>) : RecyclerView.Adapter<SibiAdapter.ViewHolder>(){
    inner class ViewHolder(private val binding: ItemSibiBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(data:Sibi){
            with(binding){
                Glide.with(itemView)
                    .load(data.Image)
                    .apply(RequestOptions().override(180,180))
                    .into(img)
                tvItemList.text = data.title
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
       ViewHolder(ItemSibiBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(listSibi[position])

    override fun getItemCount(): Int = listSibi.size
}