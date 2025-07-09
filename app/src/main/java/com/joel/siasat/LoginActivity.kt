package com.joel.siasat

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.joel.siasat.databinding.ActivityLoginBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var mUsersDatabase: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mFirebaseInstance = FirebaseDatabase.getInstance("https://siasat-3d1a7-default-rtdb.asia-southeast1.firebasedatabase.app")
        mUsersDatabase = mFirebaseInstance.getReference("users")

        binding.buttonLogin.setOnClickListener {
            val idInput = binding.editTextId.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if (idInput.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "ID dan Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            mUsersDatabase?.child(idInput)?.get()?.addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val storedPassword = snapshot.child("password").getValue(String::class.java)
                    if (password == storedPassword) {
                        Toast.makeText(this, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                        val role = snapshot.child("role").getValue(String::class.java)
                        val namaLengkap = snapshot.child("namaLengkap").getValue(String::class.java)
                        val userId = snapshot.key
                        val intent = Intent(this, MainActivity::class.java).apply {
                            putExtra("EXTRA_ROLE", role)
                            putExtra("EXTRA_NAMA", namaLengkap)
                            putExtra("EXTRA_ID", userId)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Password salah", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "ID Pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }?.addOnFailureListener {
                Toast.makeText(this, "Gagal terhubung: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}