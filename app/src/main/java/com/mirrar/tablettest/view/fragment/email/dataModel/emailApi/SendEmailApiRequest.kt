package com.mirrar.tablettest.view.fragment.email.dataModel.emailApi

data class SendEmailApiRequest(
    val email: String,
    val name: String,
    val objects: List<Object>,
    val purpose: String
)