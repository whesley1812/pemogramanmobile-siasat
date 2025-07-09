package com.joel.siasat

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.joel.siasat.databinding.ActivityGantiPasswordBinding

class GantiPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGantiPasswordBinding
    private var mUsersDatabase: DatabaseReference? = null
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGantiPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }


        userId = intent.getStringExtra("EXTRA_ID")
        if (userId == null) {
            Toast.makeText(this, "Error: User ID tidak ditemukan.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        mUsersDatabase = FirebaseDatabase.getInstance("https://siasat-3d1a7-default-rtdb.asia-southeast1.firebasedatabase.app")
            .reference.child("users").child(userId!!)

        binding.buttonSimpanPassword.setOnClickListener {
            changePassword()
        }
    }

    private fun changePassword() {
        val passLama = binding.editTextPasswordLama.text.toString()
        val passBaru = binding.editTextPasswordBaru.text.toString()
        val konfirmasiPass = binding.editTextKonfirmasiPassword.text.toString()

        if (passLama.isEmpty() || passBaru.isEmpty() || konfirmasiPass.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        if (passBaru != konfirmasiPass) {
            Toast.makeText(this, "Password baru dan konfirmasi tidak cocok", Toast.LENGTH_SHORT).show()
            return
        }

        mUsersDatabase?.child("password")?.get()?.addOnSuccessListener { snapshot ->
            val storedPassword = snapshot.getValue(String::class.java)
            if (storedPassword == passLama) {
                mUsersDatabase?.child("password")?.setValue(passBaru)
                    ?.addOnSuccessListener {
                        Toast.makeText(this, "Password berhasil diubah", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    ?.addOnFailureListener {
                        Toast.makeText(this, "Gagal mengubah password: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Password lama salah", Toast.LENGTH_SHORT).show()
            }
        }?.addOnFailureListener {
            Toast.makeText(this, "Gagal memverifikasi: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}