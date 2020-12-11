package com.shivam.emotions.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.shivam.emotions.R
import com.shivam.emotions.databinding.ImageEmotionItemBinding
import com.shivam.emotions.model.ImageEmotionModel

class ImageEmotionAdapter(private var emotionList: List<ImageEmotionModel>) :
    RecyclerView.Adapter<ImageEmotionAdapter.EmotionViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EmotionViewHolder {
        val dataBinding =
            ImageEmotionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EmotionViewHolder(dataBinding)
    }

    override fun onBindViewHolder(holder: EmotionViewHolder, position: Int) {
        holder.bind(emotionList[position])
    }

    class EmotionViewHolder(private var dataBinding: ImageEmotionItemBinding) :
        RecyclerView.ViewHolder(dataBinding.root) {

        fun bind(singleImageEmotionModel: ImageEmotionModel) {
            dataBinding.ivFace.load(singleImageEmotionModel.imageBitmap)
            dataBinding.tvEmotion.text = singleImageEmotionModel.emotion
            dataBinding.tvCompoundEmotion.text = singleImageEmotionModel.compoundEmotion
        }
    }

    override fun getItemCount(): Int {
        return emotionList.size
    }

}


