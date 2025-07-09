package com.joel.siasat

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.joel.siasat.databinding.ActivityAddMatkulBinding
import com.joel.siasat.models.Jadwal
import com.joel.siasat.models.MataKuliah
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddMatkulActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMatkulBinding
    private var mMataKuliahDatabase: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMatkulBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mMataKuliahDatabase = FirebaseDatabase.getInstance("https://siasat-3d1a7-default-rtdb.asia-southeast1.firebasedatabase.app")
            .reference.child("matakuliah")

        binding.buttonSimpanMatkul.setOnClickListener {
            saveNewCourse()
        }
    }

    private fun saveNewCourse() {
        val kodeMatkul = binding.editTextKodeMatkul.text.toString().trim()
        val namaMatkul = binding.editTextNamaMatkul.text.toString().trim()
        val hari = binding.editTextHari.text.toString().trim()
        val jam = binding.editTextJam.text.toString().trim()
        val idDosen = binding.editTextIdDosen.text.toString().trim()

        if (kodeMatkul.isEmpty() || namaMatkul.isEmpty() || hari.isEmpty() || jam.isEmpty() || idDosen.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val jadwal = Jadwal(hari, jam)
        val mataKuliah = MataKuliah(namaMatkul, idDosen, jadwal)

        mMataKuliahDatabase?.child(kodeMatkul)?.setValue(mataKuliah)
            ?.addOnSuccessListener {
                Toast.makeText(this, "Mata kuliah berhasil disimpan", Toast.LENGTH_SHORT).show()
                finish()
            }
            ?.addOnFailureListener {
                Toast.makeText(this, "Gagal menyimpan: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}