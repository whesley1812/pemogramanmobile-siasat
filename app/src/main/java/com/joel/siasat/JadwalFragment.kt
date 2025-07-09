package com.joel.siasat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.joel.siasat.databinding.FragmentJadwalBinding
import com.joel.siasat.models.MataKuliah
import com.joel.siasat.models.Nilai
import com.google.firebase.database.*

class JadwalFragment : Fragment() {
    private var _binding: FragmentJadwalBinding? = null
    private val binding get() = _binding!!

    private var mDatabase: DatabaseReference? = null
    private var mJadwalListener: ValueEventListener? = null
    private var mQueryRef: Query? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentJadwalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userId = arguments?.getString("EXTRA_ID") ?: return
        val userRole = arguments?.getString("EXTRA_ROLE") ?: return

        mDatabase = FirebaseDatabase.getInstance("https://siasat-3d1a7-default-rtdb.asia-southeast1.firebasedatabase.app").reference

        when (userRole) {
            "mahasiswa" -> attachJadwalMahasiswaListener(userId)
            "dosen" -> attachJadwalDosenListener(userId)
            "kaprogdi" -> attachJadwalDosenListener(userId)
        }
    }

    private fun attachJadwalMahasiswaListener(userId: String) {
        mQueryRef = mDatabase?.child("jadwalMahasiswa")?.child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    displayEmptyJadwal()
                    return
                }
                for (courseIdSnapshot in snapshot.children) {
                    val courseId = courseIdSnapshot.key
                    courseId?.let { fetchCourseDetails(it) }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("JadwalFragment", "Gagal memuat jadwal mahasiswa: ${error.message}")
            }
        }
        mQueryRef?.addValueEventListener(listener)
        mJadwalListener = listener
    }

    private fun attachJadwalDosenListener(userId: String) {
        mQueryRef = mDatabase?.child("matakuliah")?.orderByChild("dosenId")?.equalTo(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    displayEmptyJadwal()
                    return
                }
                for (courseSnapshot in snapshot.children) {
                    val course = courseSnapshot.getValue(MataKuliah::class.java)
                    val courseId = courseSnapshot.key
                    if (course != null && courseId != null) {
                        addJadwalView(course, courseId)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("JadwalFragment", "Gagal memuat jadwal dosen: ${error.message}")
            }
        }
        mQueryRef?.addValueEventListener(listener)
        mJadwalListener = listener
    }

    private fun fetchCourseDetails(courseId: String) {
        mDatabase?.child("matakuliah")?.child(courseId)?.get()?.addOnSuccessListener {
            val course = it.getValue(MataKuliah::class.java)
            if (course != null) {
                addJadwalView(course, courseId)
            }
        }
    }

    private fun addJadwalView(course: MataKuliah, courseId: String) {
        if (_binding == null) return
        val card = layoutInflater.inflate(R.layout.item_matakuliah, binding.jadwalContainer, false) as CardView
        val textNamaMatkul: TextView = card.findViewById(R.id.text_nama_matkul)
        val textJadwal: TextView = card.findViewById(R.id.text_jadwal)
        val textNilai: TextView = card.findViewById(R.id.text_nilai) // Ambil TextView nilai
        card.findViewById<Button>(R.id.button_ambil).visibility = View.GONE

        textNamaMatkul.text = course.namaMatkul
        textJadwal.text = "Jadwal: ${course.jadwal?.hari}, ${course.jadwal?.jam}"

        // Logika untuk menampilkan nilai jika user adalah mahasiswa
        val userRole = arguments?.getString("EXTRA_ROLE")
        val userId = arguments?.getString("EXTRA_ID")
        if (userRole == "mahasiswa" && userId != null) {
            mDatabase?.child("nilai")?.child(courseId)?.child(userId)?.get()?.addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val nilai = snapshot.getValue(Nilai::class.java)
                    textNilai.text = "Nilai: ${nilai?.nilaiHuruf}"
                    textNilai.visibility = View.VISIBLE
                }
            }
        }

        card.setOnClickListener {
            val intent = Intent(activity, PesertaMatkulActivity::class.java).apply {
                putExtra("EXTRA_COURSE_ID", courseId)
                putExtra("EXTRA_COURSE_NAME", course.namaMatkul)
                putExtra("EXTRA_ROLE", userRole)
            }
            startActivity(intent)
        }

        binding.jadwalContainer.addView(card)
    }

    private fun displayEmptyJadwal() {
        if (_binding == null) return
        val emptyText = TextView(context).apply { text = "Belum ada jadwal." }
        binding.jadwalContainer.addView(emptyText)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mJadwalListener?.let { mQueryRef?.removeEventListener(it) }
        _binding = null
    }
}