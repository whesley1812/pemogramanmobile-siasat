package com.joel.siasat

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class Siasat : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance("https://siasat-3d1a7-default-rtdb.asia-southeast1.firebasedatabase.app").setPersistenceEnabled(true)
    }
}