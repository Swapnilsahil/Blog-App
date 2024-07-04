package com.example.blogapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.blogapp.Modal.BlogItemModel
import com.example.blogapp.Modal.UserData
import com.example.blogapp.databinding.ActivityAddArticleBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date

class AddArticleActivity : AppCompatActivity() {
    private val binding:ActivityAddArticleBinding by lazy{
        ActivityAddArticleBinding.inflate(layoutInflater)
    }

    private val databaseReference:DatabaseReference=
        FirebaseDatabase.getInstance("https://blog-app-75782-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("blogs")
    private val userReference:DatabaseReference=
        FirebaseDatabase.getInstance("https://blog-app-75782-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users")
    private val auth=FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        binding.imageButton.setOnClickListener{
            finish()
        }

        binding.addArticleButton.setOnClickListener{
            val title =binding.blogTitle.editText?.text.toString().trim()
            val description=binding.blogDescription.editText?.text.toString().trim()

            if(title.isEmpty() || description.isEmpty()){
                Toast.makeText(this,"Please fill all the fields",Toast.LENGTH_SHORT).show()
            }

            //get current user
            val user: FirebaseUser?=auth.currentUser

            if(user!=null){
                val userId=user.uid
                val userName=user.displayName?:"Anonymous"
                val userImageUrl=user.photoUrl?:""

                //fetch user name and user profile from database

                userReference.child(userId).addListenerForSingleValueEvent(object:ValueEventListener{
                    @SuppressLint("SimpleDateFormat")
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userData=snapshot.getValue(UserData::class.java)
                        if(userData!=null){
                            val userNameFromDB=userData.name
                            val userImageUrlFromDB=userData.profileImage

                            val currentDate= SimpleDateFormat("yyyy-MM-dd").format(Date())

                            //create a blog item model
                            val blogItem=BlogItemModel(
                                title,
                                userNameFromDB,
                                currentDate,
                                userId,
                                description,
                                likeCount=0,
                                profileImage = userImageUrlFromDB
                            )
                            //generate a unique key for the blog post
                            val key=databaseReference.push().key
                            if(key!=null){

                                blogItem.postId=key
                                val blogReference:DatabaseReference=databaseReference.child(key)
                                blogReference.setValue(blogItem).addOnCompleteListener{
                                    if(it.isSuccessful){
                                        finish()
                                    }else{
                                        Toast.makeText(
                                            this@AddArticleActivity,
                                            "failed to add blog",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
            }
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}