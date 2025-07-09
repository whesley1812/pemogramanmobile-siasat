package com.joel.siasat

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.joel.siasat.databinding.ActivityMainBinding
import com.joel.siasat.models.User
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var userId: String? = null
    private var userRole: String? = null
    private var userNama: String? = null
    private var isMahasiswaRegistered: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getStringExtra("EXTRA_ID")
        userRole = intent.getStringExtra("EXTRA_ROLE")
        userNama = intent.getStringExtra("EXTRA_NAMA")

        if (userId == null) {
            Toast.makeText(this, "Kesalahan: ID Pengguna tidak ada.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        if (userRole == "mahasiswa") {
            fetchRegistrationStatus(userId!!)
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val selectedFragment: Fragment = when (item.itemId) {
                R.id.nav_jadwal -> JadwalFragment()
                R.id.nav_matakuliah -> {
                    if (userRole == "mahasiswa" && !isMahasiswaRegistered) {
                        RegistrasiUlangFragment()
                    } else {
                        MatakuliahFragment()
                    }
                }
                else -> BiodataFragment()
            }
            replaceFragment(selectedFragment)
            true
        }

        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.nav_biodata
        }
    }

    private fun fetchRegistrationStatus(userId: String) {
        val db = FirebaseDatabase.getInstance("https://siasat-3d1a7-default-rtdb.asia-southeast1.firebasedatabase.app").reference
        db.child("users").child(userId).get().addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(User::class.java)
            isMahasiswaRegistered = user?.statusRegistrasiUlang ?: false
        }
    }

    fun openMataKuliahFragment() {
        isMahasiswaRegistered = true
        binding.bottomNavigation.selectedItemId = R.id.nav_matakuliah
    }

    private fun replaceFragment(fragment: Fragment) {
        val bundle = Bundle().apply {
            putString("EXTRA_ID", userId)
            putString("EXTRA_ROLE", userRole)
            putString("EXTRA_NAMA", userNama)
        }
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}