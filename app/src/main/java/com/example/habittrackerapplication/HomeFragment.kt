package com.example.habittrackerapplication

import HabitAdapter
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habittrackerapplication.databinding.DialogAddHabitBinding
import com.example.habittrackerapplication.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val habitList = mutableListOf<Habit>()
    private lateinit var habitAdapter: HabitAdapter

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupAddHabitButton()
        loadUserName()  // تحميل الاسم
    }

    private fun setupRecyclerView() {
        habitAdapter = HabitAdapter(habitList)
        binding.habitRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.habitRecyclerView.adapter = habitAdapter

        populateInitialHabits()
    }

    private fun setupAddHabitButton() {
        binding.addHabitButton.setOnClickListener {
            showAddHabitDialog()
        }
    }

    private fun populateInitialHabits() {
        habitList.clear()
        habitList.add(Habit("Drink Water", "8 cups daily", false))
        habitList.add(Habit("Exercise", "30 mins daily", false))
        habitAdapter.notifyDataSetChanged()
    }

    private fun showAddHabitDialog() {
        val dialogBinding = DialogAddHabitBinding.inflate(layoutInflater)

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.add_habit_button))
            .setView(dialogBinding.root)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val name = dialogBinding.etHabitName.text.toString().trim()
                val desc = dialogBinding.etHabitDescription.text.toString().trim()
                if (name.isNotEmpty() && desc.isNotEmpty()) {
                    addNewHabit(name, desc)
                } else {
                    Toast.makeText(requireContext(),
                        getString(R.string.error), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun addNewHabit(name: String, description: String) {
        habitList.add(Habit(name, description, false))
        habitAdapter.notifyItemInserted(habitList.size - 1)
        binding.habitRecyclerView.scrollToPosition(habitList.size - 1)
    }

    private fun loadUserName() {
        val uid = auth.currentUser?.uid ?: return
        val userRef = db.collection("users").document(uid)

        userRef.get()
            .addOnSuccessListener { document ->
                if (!isAdded) return@addOnSuccessListener // ← تحقق ضروري

                if (document != null && document.exists()) {
                    val firstName = document.getString("firstName") ?: ""
                    val lastName = document.getString("lastName") ?: ""
                    val fullName = getString(R.string.hello_user, "$firstName $lastName")

                    val nameTextView = requireActivity().findViewById<TextView>(R.id.userNameText)
                    nameTextView.text = fullName
                    nameTextView.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                if (!isAdded) return@addOnFailureListener
                Toast.makeText(requireContext(), getString(R.string.error_loading_name), Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
