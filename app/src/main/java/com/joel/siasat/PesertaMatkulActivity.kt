package com.joel.siasat

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.joel.siasat.databinding.ActivityPesertaMatkulBinding
import com.joel.siasat.models.Nilai
import com.joel.siasat.models.User

class PesertaMatkulActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPesertaMatkulBinding
    private var mDatabase: DatabaseReference? = null
    private var mListener: ValueEventListener? = null
    private var mQuery: Query? = null
    private var userRole: String? = null
    private var courseId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPesertaMatkulBinding.inflate(layoutInflater)
        setContentView(binding.root)

        courseId = intent.getStringExtra("EXTRA_COURSE_ID")
        val courseName = intent.getStringExtra("EXTRA_COURSE_NAME")
        userRole = intent.getStringExtra("EXTRA_ROLE")

        if (courseId == null) {
            Toast.makeText(this, "Error: ID Mata Kuliah tidak ditemukan.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = courseName ?: "Peserta Mata Kuliah"
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        mDatabase = FirebaseDatabase.getInstance("https://siasat-3d1a7-default-rtdb.asia-southeast1.firebasedatabase.app").reference

        attachParticipantsListener(courseId!!)
    }

    private fun attachParticipantsListener(courseId: String) {
        binding.progressBar.visibility = View.VISIBLE
        mQuery = mDatabase?.child("jadwalMahasiswa")?.orderByChild(courseId)?.equalTo(true)

        mListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.progressBar.visibility = View.GONE
                binding.pesertaContainer.removeAllViews()

                if (!snapshot.exists()) {
                    val emptyText = TextView(this@PesertaMatkulActivity).apply {
                        text = "Belum ada peserta."
                        textAlignment = View.TEXT_ALIGNMENT_CENTER
                        setPadding(16, 16, 16, 16)
                    }
                    binding.pesertaContainer.addView(emptyText)
                    return
                }

                for (participantSnapshot in snapshot.children) {
                    val studentId = participantSnapshot.key
                    studentId?.let { fetchStudentDetails(it, courseId) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBar.visibility = View.GONE
                Log.e("PesertaMatkulActivity", "Gagal memuat data peserta: ${error.message}")
                Toast.makeText(this@PesertaMatkulActivity, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        mQuery?.addValueEventListener(mListener!!)
    }

    private fun fetchStudentDetails(studentId: String, courseId: String) {
        mDatabase?.child("users")?.child(studentId)?.get()?.addOnSuccessListener { userSnapshot ->
            val user = userSnapshot.getValue(User::class.java)
            user?.namaLengkap?.let { studentName ->
                val nameTextView = TextView(this).apply {
                    text = "• $studentName ($studentId)"
                    textSize = 16f
                    setPadding(16, 16, 16, 16)
                }

                if (userRole == "dosen" || userRole == "kaprogdi") {
                    nameTextView.isClickable = true
                    nameTextView.setBackgroundResource(android.R.drawable.list_selector_background)
                    nameTextView.setOnClickListener {
                        showInputNilaiDialog(studentId, studentName, courseId)
                    }
                }
                binding.pesertaContainer.addView(nameTextView)
            }
        }?.addOnFailureListener {
            Log.e("PesertaMatkulActivity", "Gagal mengambil detail user $studentId", it)
        }
    }

    private fun showInputNilaiDialog(studentId: String, studentName: String, courseId: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Input Nilai untuk $studentName")

        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            hint = "Contoh: A, AB, C"
            setPadding(50, 50, 50, 50)
        }
        builder.setView(input)

        builder.setPositiveButton("Simpan") { dialog, _ ->
            val nilaiHuruf = input.text.toString().trim().uppercase()
            if (nilaiHuruf.isNotEmpty()) {
                val nilai = Nilai(nilaiHuruf)
                mDatabase?.child("nilai")?.child(courseId)?.child(studentId)?.setValue(nilai)
                    ?.addOnSuccessListener {
                        Toast.makeText(this, "Nilai berhasil disimpan", Toast.LENGTH_SHORT).show()
                    }?.addOnFailureListener { e ->
                        Toast.makeText(this, "Gagal menyimpan nilai: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Nilai tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Batal") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mListener?.let { mQuery?.removeEventListener(it) }
    }
}
