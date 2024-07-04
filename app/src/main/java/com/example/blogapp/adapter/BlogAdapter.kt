package com.example.blogapp.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogapp.Modal.BlogItemModel
import com.example.blogapp.R
import com.example.blogapp.ReadMoreActivity
import com.example.blogapp.databinding.BlogItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BlogAdapter(private val items:MutableList<BlogItemModel>):
    RecyclerView.Adapter<BlogAdapter.BlogViewHolder>() {

    private val databaseReference=FirebaseDatabase.getInstance("https://blog-app-75782-default-rtdb.asia-southeast1.firebasedatabase.app").reference
    private val currentUser=FirebaseAuth.getInstance().currentUser


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {
        val inflater=LayoutInflater.from(parent.context)
        val binding = BlogItemBinding.inflate(inflater,parent,false)
        return BlogViewHolder(binding)
    }



    override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {
        val blogItem=items[position]
        holder.bind(blogItem)
    }
    override fun getItemCount(): Int {
        return items.size
    }
    inner class BlogViewHolder(private val binding:BlogItemBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(blogItemModel: BlogItemModel) {
            val postId=blogItemModel.postId
            val context=binding.root.context

            binding.heading.text = blogItemModel.heading
            Glide.with(binding.profile.context)
                .load(blogItemModel.profileImage)
                .into(binding.profile)
            binding.userName.text=blogItemModel.userName
            binding.date.text=blogItemModel.date
            binding.post.text=blogItemModel.post
            binding.likeCount.text=blogItemModel.likeCount.toString()


            //set on click listener
            binding.addArticleButton.setOnClickListener{
                val context=binding.addArticleButton.context
                val intent = Intent(context,ReadMoreActivity::class.java)
                intent.putExtra("blogItem",blogItemModel)
                context.startActivity(intent)
            }

            //check if the current user has liked the post and update the like button image

            val postLikedReference=databaseReference.child("blogs").child(postId).child("likes")
            val currentUserLiked=currentUser?.uid?.let{uid->
                postLikedReference.child(uid).addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            binding.likeButton.setImageResource(R.drawable.heartred)
                        }else{
                            binding.likeButton.setImageResource(R.drawable.heartbl)
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context,"",Toast.LENGTH_SHORT).show()
                    }
                })
            }

            //handle like button clicks
            binding.likeButton.setOnClickListener{

                if(currentUser!=null){
                    handleLikeButtonClicked(postId,blogItemModel,binding)
                }else{
                Toast.makeText(context,"You have to login first",Toast.LENGTH_SHORT).show()
                }
            }

            //sat the initial icon based on the saved status

            val userReference=databaseReference.child("users").child(currentUser?.uid?:"")
            val postSaveReference=userReference.child("saveBlogPosts").child(postId)

            postSaveReference.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        //if blog already saved
                        binding.postsaveButton.setImageResource(R.drawable.savedred)

                    }else
                    {
                        //if blog not saved yet

                        binding.postsaveButton.setImageResource(R.drawable.saved)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

            //handle save button clicks
            binding.postsaveButton.setOnClickListener{
                if(currentUser!=null){
                    handleSaveButtonClicked(postId,blogItemModel,binding)
                }else{
                    Toast.makeText(context,"You have to login first",Toast.LENGTH_SHORT).show()
                }
            }


        }

    }



    private fun handleLikeButtonClicked(postId: String, blogItemModel: BlogItemModel,binding: BlogItemBinding) {
        val userReference=databaseReference.child("users").child(currentUser!!.uid)
        val postLikedReference=databaseReference.child("blogs").child(postId).child("likes")

        //check user has already liked the post ,then unlike it

        postLikedReference.child(currentUser.uid).addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    userReference.child("likes").child(postId).removeValue()
                        .addOnSuccessListener {
                            postLikedReference.child(currentUser.uid).removeValue()
                            blogItemModel.likedBy?.remove(currentUser.uid)
                            updateLikedButtonImage(binding ,false)

                            //decrement the like in the database
                            val newLikeCount=blogItemModel.likeCount-1
                            blogItemModel.likeCount=newLikeCount
                            databaseReference.child("blogs").child(postId).child("likeCount").setValue(newLikeCount)

                            notifyDataSetChanged()
                        }
                        .addOnFailureListener{e->
                            Log.e("LikedClicked","onDataChange:Failed to unlike the blog $e",)
                        }

                }
                else{
                    //user has not liked the post,so like it
                    userReference.child("likes").child(postId).setValue(true)
                        .addOnSuccessListener {
                            postLikedReference.child(currentUser.uid).setValue(true)
                            blogItemModel.likedBy?.add(currentUser.uid)
                            updateLikedButtonImage(binding,true)

                            //increment the like count int the databse
                            val newLikeCount=blogItemModel.likeCount+1
                            blogItemModel.likeCount=newLikeCount
                            databaseReference.child("blogs").child(postId).child("likeCount").setValue(newLikeCount)

                            notifyDataSetChanged()
                        }
                        .addOnFailureListener{e ->
                            Log.e("LikedClicked","onDataChange:Failed to like the blog $e",)
                        }

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun updateLikedButtonImage(binding: BlogItemBinding,liked:Boolean) {
        if(liked){
            binding.likeButton.setImageResource(R.drawable.heartbl)
        }
        else{
            binding.likeButton.setImageResource(R.drawable.heartred)
        }

    }
    private fun handleSaveButtonClicked(
        postId: String,
        blogItemModel: BlogItemModel,
        binding: BlogItemBinding
    ) {
        val userReference=databaseReference.child("users").child(currentUser!!.uid)
        userReference.child("saveBlogPosts").child(postId).addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val isCurrentlySaved = snapshot.exists()

                if(isCurrentlySaved){
                    //the blog is currently save so unSave it
                    userReference.child("saveBlogPosts").child(postId).removeValue()
                        .addOnSuccessListener {
                            //update the ui based on the read value (isCurrentlySaved)
                            blogItemModel.isSaved = false // Update model object
                            binding.postsaveButton.setImageResource(R.drawable.saved)
                            notifyDataSetChanged()

                            val context=binding.root.context
                            Toast.makeText(context,"Blog Unsaved!",Toast.LENGTH_SHORT).show()

                        }.addOnFailureListener{
                            val context=binding.root.context
                            Toast.makeText(context,"Failed to unSave the Blog ",Toast.LENGTH_SHORT).show()
                        }
                }else{
                    //the blog is not saved,so save it
                    userReference.child("saveBlogPosts").child(postId).setValue(true)
                        .addOnSuccessListener {
                            //update ui based on the read value (isCurrentlySaved)
                            blogItemModel.isSaved = true // Update model object
                            binding.postsaveButton.setImageResource(R.drawable.savedred)
                            notifyDataSetChanged()

                            val context=binding.root.context
                            Toast.makeText(context,"Blog Saved!",Toast.LENGTH_SHORT).show()

                        }
                        .addOnFailureListener{
                            val context=binding.root.context
                            Toast.makeText(context,"Failed to Save the Blog ",Toast.LENGTH_SHORT).show()
                        }
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }




    fun updateData(savedBlogsArticles: List<BlogItemModel>) {

        items.clear()
        items.addAll(savedBlogsArticles)
        notifyDataSetChanged()
    }
}