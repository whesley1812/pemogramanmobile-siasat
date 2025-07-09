package com.joel.siasat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.joel.siasat.databinding.FragmentMatakuliahBinding
import com.joel.siasat.databinding.ItemMatakuliahBinding
import com.joel.siasat.models.MataKuliah
import com.joel.siasat.models.User
import com.google.firebase.database.*

class MatakuliahFragment : Fragment() {
    private var _binding: FragmentMatakuliahBinding? = null
    private val binding get() = _binding!!

    private var mDatabase: DatabaseReference? = null
    private var mMataKuliahListener: ValueEventListener? = null
    private var mMataKuliahRef: DatabaseReference? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMatakuliahBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userId = arguments?.getString("EXTRA_ID") ?: return
        val userRole = arguments?.getString("EXTRA_ROLE") ?: return

        mDatabase = FirebaseDatabase.getInstance("https://siasat-3d1a7-default-rtdb.asia-southeast1.firebasedatabase.app").reference

        if (userRole == "kaprogdi") {
            binding.buttonAddCourseSticky.visibility = View.VISIBLE
            binding.buttonAddCourseSticky.setOnClickListener {
                startActivity(Intent(activity, AddMatkulActivity::class.java))
            }
        }

        if (userRole == "mahasiswa") {
            checkRegistrationStatus(userId)
        } else {
            attachMataKuliahListener(userId, userRole)
        }
    }

    private fun checkRegistrationStatus(userId: String) {
        mDatabase?.child("users")?.child(userId)?.get()?.addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(User::class.java)
            if (user?.statusRegistrasiUlang == true) {
                attachMataKuliahListener(userId, "mahasiswa")
            } else {
                displayRegistrationWarning()
            }
        }
    }

    private fun displayRegistrationWarning() {
        binding.matakuliahContainer.removeAllViews()
        val warningText = TextView(context).apply {
            text = "Anda harus melakukan registrasi ulang terlebih dahulu untuk dapat mengambil mata kuliah."
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setPadding(16, 16, 16, 16)
        }
        binding.matakuliahContainer.addView(warningText)
    }

    private fun attachMataKuliahListener(userId: String, userRole: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.matakuliahContainer.removeAllViews()

        mMataKuliahRef = mDatabase?.child("matakuliah")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.progressBar.visibility = View.GONE
                binding.matakuliahContainer.removeAllViews()

                for (courseSnapshot in snapshot.children) {
                    val course = courseSnapshot.getValue(MataKuliah::class.java)
                    val courseId = courseSnapshot.key
                    if (course != null && courseId != null) {
                        addCourseView(course, courseId, userId, userRole)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBar.visibility = View.GONE
                Log.e("MatakuliahFragment", "Error: ${error.message}")
            }
        }
        mMataKuliahRef?.addValueEventListener(listener)
        mMataKuliahListener = listener
    }

    private fun addCourseView(course: MataKuliah, courseId: String, userId: String, userRole: String) {
        val itemBinding = ItemMatakuliahBinding.inflate(layoutInflater, binding.matakuliahContainer, false)

        itemBinding.textNamaMatkul.text = course.namaMatkul
        itemBinding.textJadwal.text = "${course.jadwal?.hari}, ${course.jadwal?.jam}"

        if (userRole == "mahasiswa") {
            itemBinding.buttonAmbil.visibility = View.VISIBLE
            itemBinding.buttonAmbil.setOnClickListener {
                ambilMataKuliah(userId, courseId)
            }
        } else {
            itemBinding.buttonAmbil.visibility = View.GONE
        }

        binding.matakuliahContainer.addView(itemBinding.root)
    }

    private fun ambilMataKuliah(userId: String, courseId: String) {
        mDatabase?.child("jadwalMahasiswa")?.child(userId)?.child(courseId)?.setValue(true)
            ?.addOnSuccessListener {
                Toast.makeText(context, "Mata kuliah berhasil diambil", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mMataKuliahListener?.let { mMataKuliahRef?.removeEventListener(it) }
        _binding = null
    }
}