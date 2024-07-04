package com.example.blogapp

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.blogapp.Modal.BlogItemModel
import com.example.blogapp.adapter.ArticleAdapter
import com.example.blogapp.adapter.BlogAdapter
import com.example.blogapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private val binding :ActivityMainBinding by lazy{
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var databaseReference: DatabaseReference
    private val blogItems= mutableListOf<BlogItemModel>()
    private lateinit var auth:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        //to go on save article page
        binding.saveArticleButton.setOnClickListener{
            startActivity(Intent(this,SavedArticlesActivity::class.java))
        }
        // to go profile activity
        binding.profileImage.setOnClickListener{
            startActivity(Intent(this,ProfileActivity::class.java))
        }
        //to go profile activity
        binding.cardView2.setOnClickListener{
            startActivity(Intent(this,ProfileActivity::class.java))
        }


        auth=FirebaseAuth.getInstance()
        databaseReference=FirebaseDatabase.getInstance("https://blog-app-75782-default-rtdb.asia-southeast1.firebasedatabase.app").reference.child("blogs")

        val userId=auth.currentUser?.uid

        //set user profile
        if(userId != null){
            loadUserProfileImage(userId)



        }

        //set blog post into recycler view

        //Initialize the recycler view and set adapter
        val recyclerView=binding.blogRecyclerView
        val blogAdapter=BlogAdapter(blogItems)
        recyclerView.adapter=blogAdapter
        recyclerView.layoutManager=LinearLayoutManager(this)

        //fetch data from firebase database
        databaseReference.addValueEventListener(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                blogItems.clear()

                for(snapshot in snapshot.children){
                    val blogItem=snapshot.getValue(BlogItemModel::class.java)
                    if(blogItem != null){
                        blogItems.add(blogItem)
                    }
                }
                //reverse the list
                blogItems.reverse()

                //Notify the adapter that the data has changed
                blogAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity,"Blog loading failed",Toast.LENGTH_SHORT).show()

            }

        })


        binding.floatingAddArticleButton.setOnClickListener{
            startActivity(Intent(this,AddArticleActivity::class.java))

        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadUserProfileImage(userId: String) {
        val userReference=FirebaseDatabase.getInstance("https://blog-app-75782-default-rtdb.asia-southeast1.firebasedatabase.app").reference.child("users").child(userId)

        userReference.child("profileImage").addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val profileImageUrl=snapshot.getValue(String::class.java)

                if(profileImageUrl != null){
                    Glide.with(this@MainActivity)
                        .load(profileImageUrl)
                        .into(binding.profileImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity,"Error loading profile image",Toast.LENGTH_SHORT).show()
            }

        })

    }
}