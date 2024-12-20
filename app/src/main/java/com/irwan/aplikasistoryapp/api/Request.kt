package com.irwan.aplikasistoryapp.api

data class LoginRq (
    val email       : String,
    val password    : String
)

data class Register (
    val name        : String,
    val email       : String,
    val password    : String
)

data class RequestAddStory(
    val description : String,
    val photo       : String,
    val lat         : Float?,
    val lon         : Float?
)
