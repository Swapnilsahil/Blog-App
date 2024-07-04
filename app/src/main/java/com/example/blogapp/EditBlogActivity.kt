package com.example.blogapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.blogapp.Modal.BlogItemModel
import com.example.blogapp.databinding.ActivityEditBlogBinding
import com.google.firebase.database.FirebaseDatabase

class EditBlogActivity : AppCompatActivity() {
    private val binding:ActivityEditBlogBinding by lazy{
        ActivityEditBlogBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.imageButton.setOnClickListener{
            finish()
        }

        val blogItemModel=intent.getParcelableExtra<BlogItemModel>("blogItem")

        binding.blogTitle.editText?.setText(blogItemModel?.heading)
        binding.blogDescription.editText?.setText(blogItemModel?.post)

        binding.SaveBlogButton.setOnClickListener{
            val updatedTitle=binding.blogTitle.editText?.text.toString().trim()
            val updatedDescription=binding.blogDescription.editText?.text.toString().trim()

            if(updatedTitle.isEmpty() || updatedDescription.isEmpty()){
                Toast.makeText(this,"Please Fill All the Details", Toast.LENGTH_SHORT).show()
            }else{
                blogItemModel?.heading=updatedTitle
                blogItemModel?.post=updatedDescription

                if(blogItemModel != null){
                    updateDataInFirebase(blogItemModel)
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun updateDataInFirebase(blogItemModel: BlogItemModel) {
        val databaseReference=FirebaseDatabase.getInstance("https://blog-app-75782-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("blogs")

        val postId=blogItemModel.postId

        databaseReference.child(postId).setValue(blogItemModel)
            .addOnSuccessListener {
                Toast.makeText(this,"Blog updated Successfully",Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener{
                Toast.makeText(this,"Blog update Unsuccessfull",Toast.LENGTH_SHORT).show()
            }
    }
}