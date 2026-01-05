package com.example.nexogo.model

data class SimplePatient(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val pets: List<SimplePet> = emptyList()
)

data class SimplePet(
    val id: String = "",
    val name: String = "",
    val species: String = "",
    val breed: String = "",
    val age: Int = 0
)
