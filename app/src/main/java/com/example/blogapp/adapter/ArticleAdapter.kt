package com.example.blogapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogapp.Modal.BlogItemModel
import com.example.blogapp.databinding.ActivityArticleBinding
import com.example.blogapp.databinding.ArticleItemBinding
import java.util.ArrayList


class ArticleAdapter(
    private val context: Context,
    private var blogList:List<BlogItemModel>,
    private val itemClickListener:OnItemClickListener
):RecyclerView.Adapter<ArticleAdapter.BlogViewHolder>() {

    interface OnItemClickListener{
        fun onEditClick(blogItem: BlogItemModel)
        fun onReadMoreClick(blogItem: BlogItemModel)
        fun onDeleteClick(blogItem: BlogItemModel)
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ArticleAdapter.BlogViewHolder {
        val inflater=LayoutInflater.from(parent.context)
        val binding=ArticleItemBinding.inflate(inflater,parent,false)
        return  BlogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArticleAdapter.BlogViewHolder, position: Int) {
        val blogItem=blogList[position]
        holder.bind(blogItem)

    }

    override fun getItemCount(): Int {
        return blogList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(blogSavedList: ArrayList<BlogItemModel>) {
        this.blogList=blogSavedList
        notifyDataSetChanged()
    }

    inner class BlogViewHolder(private val binding: ArticleItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(blogItem: BlogItemModel) {
            binding.heading.text = blogItem.heading
            Glide.with(binding.profile.context)
                .load(blogItem.profileImage)
                .into(binding.profile)
            binding.userName.text=blogItem.userName
            binding.date.text=blogItem.date
            binding.post.text=blogItem.post

            //handle Read more
            binding.readMoreButton.setOnClickListener {
                itemClickListener.onReadMoreClick(blogItem)
            }
            //handle Edit click
            binding.editButton.setOnClickListener {
                itemClickListener.onEditClick(blogItem)
            }
            //handle delete click
            binding.deleteButton.setOnClickListener {
                itemClickListener.onDeleteClick(blogItem)
            }




        }

    }
}