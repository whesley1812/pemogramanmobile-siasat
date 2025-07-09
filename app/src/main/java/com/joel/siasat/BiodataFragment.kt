package com.joel.siasat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import com.joel.siasat.databinding.FragmentBiodataBinding
import com.joel.siasat.models.User

class BiodataFragment : Fragment() {

    private var _binding: FragmentBiodataBinding? = null
    private val binding get() = _binding!!

    private var mUserRef: DatabaseReference? = null
    private var mUserListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBiodataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = arguments?.getString("EXTRA_ID")
        if (userId == null) {
            Log.e("BiodataFragment", "User ID tidak ditemukan di arguments.")
            return
        }

        mUserRef = FirebaseDatabase.getInstance("https://siasat-3d1a7-default-rtdb.asia-southeast1.firebasedatabase.app")
            .reference.child("users").child(userId)

        binding.textId.text = "ID: $userId"
        binding.textNama.text = arguments?.getString("EXTRA_NAMA")

        attachUserListener()

        binding.buttonGantiPassword.setOnClickListener {
            val intent = Intent(activity, GantiPasswordActivity::class.java).apply {
                putExtra("EXTRA_ID", userId)
            }
            startActivity(intent)
        }

        binding.buttonLogout.setOnClickListener {
            val intent = Intent(activity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()
        }
    }

    private fun attachUserListener() {
        binding.progressBar.visibility = View.VISIBLE
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.progressBar.visibility = View.GONE
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    displayUserData(user)
                } else {
                    Log.w("BiodataFragment", "Data user null untuk snapshot: ${snapshot.key}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBar.visibility = View.GONE
                Log.e("BiodataFragment", "Gagal memuat data user.", error.toException())
            }
        }
        mUserRef?.addValueEventListener(listener)
        mUserListener = listener
    }

    private fun displayUserData(user: User) {
        binding.textNama.text = user.namaLengkap
        binding.textRole.text = "Peran: ${user.role?.replaceFirstChar { it.uppercase() }}"

        if (user.role == "mahasiswa") {
            binding.textIpk.visibility = View.VISIBLE
            binding.textStatusRegis.visibility = View.VISIBLE
            binding.divider1.visibility = View.VISIBLE
            binding.divider2.visibility = View.VISIBLE

            binding.textIpk.text = "IPK: ${user.ipk}"

            val statusText = if (user.statusRegistrasiUlang == true) "Sudah Registrasi Ulang" else "Belum Registrasi Ulang"
            binding.textStatusRegis.text = "Status: $statusText"
        } else {
            binding.textIpk.visibility = View.GONE
            binding.textStatusRegis.visibility = View.GONE
            binding.divider1.visibility = View.GONE
            binding.divider2.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mUserListener?.let {
            mUserRef?.removeEventListener(it)
        }
        _binding = null
    }
}
