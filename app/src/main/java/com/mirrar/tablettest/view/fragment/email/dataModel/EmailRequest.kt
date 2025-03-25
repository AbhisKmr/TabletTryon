package com.mirrar.tablettest.view.fragment.email.dataModel

data class EmailRequest (
    val sender: Sender,
    val to : List<Recipient>,
    val subject: String,
    val htmlContent: String
)

data class Sender (
    val name: String,
    val email: String,
)

data class Recipient (val email: String)