package com.joel.siasat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.joel.siasat.databinding.FragmentRegistrasiUlangBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegistrasiUlangFragment : Fragment() {

    private var _binding: FragmentRegistrasiUlangBinding? = null
    private val binding get() = _binding!!
    private var mUsersDatabase: DatabaseReference? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegistrasiUlangBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userId = arguments?.getString("EXTRA_ID") ?: return

        mUsersDatabase = FirebaseDatabase.getInstance("https://siasat-3d1a7-default-rtdb.asia-southeast1.firebasedatabase.app")
            .reference.child("users")

        binding.buttonLakukanRegistrasi.setOnClickListener {
            mUsersDatabase?.child(userId)?.child("statusRegistrasiUlang")?.setValue(true)
                ?.addOnSuccessListener {
                    Toast.makeText(context, "Registrasi Ulang Berhasil!", Toast.LENGTH_SHORT).show()
                    (activity as? MainActivity)?.openMataKuliahFragment()
                }
                ?.addOnFailureListener {
                    Toast.makeText(context, "Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}