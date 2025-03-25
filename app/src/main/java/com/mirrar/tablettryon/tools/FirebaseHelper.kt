package com.mirrar.tablettryon.tools

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mirrar.tablettryon.utility.AppConstraint
import com.mirrar.tablettryon.view.fragment.email.EmailHelper

class FirebaseHelper {

    private val TAG = "FirebaseHelper"

    private var database: FirebaseDatabase = Firebase.database
    private var myRef: DatabaseReference = database.getReference("/")

    fun getTermAndCondition(res: (String?) -> Unit) {
        myRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                AppConstraint.BREVO_API_KEY = snapshot.child("brevoApiKey").getValue<String>() ?: ""
                AppConstraint.SENDER_NAME = snapshot.child("senderName").getValue<String>() ?: ""
                AppConstraint.SENDER_EMAIL = snapshot.child("senderEmail").getValue<String>() ?: ""
                AppConstraint.WELCOME_MESSAGE = snapshot.child("welcomeMsg").getValue<String>() ?: ""
                AppConstraint.CLUB_AVOLTA = snapshot.child("clubAvoltaUrl").getValue<String>() ?: ""
                AppConstraint.ALGOLIA_INDEX = snapshot.child("algoliaIndex").getValue<String>() ?: ""
                AppConstraint.IS_3D_ENABLED = snapshot.child("is3DEnabled").getValue<Boolean>() ?: false

                res(snapshot.child("termAndCondition").getValue<String>())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, error.message)
            }
        })
    }
}