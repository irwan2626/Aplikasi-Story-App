package com.irwan.aplikasistoryapp.api

data class LoginResponse(
	val error: Boolean,
	val message: String,
	val loginResult: LoginResult?
)

data class LoginResult(
	val userId: String,
	val name: String,
	val token: String
)


data class ResponseRegister(
	val error: Boolean,
	val message: String
)

