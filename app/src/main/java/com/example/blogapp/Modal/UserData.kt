package com.example.blogapp.Modal

data class UserData(
    val name: String="",
    val email: String="",
    val profileImage:String=""

){
    constructor():this("","","")
}
