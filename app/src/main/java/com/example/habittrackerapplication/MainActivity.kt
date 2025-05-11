package com.example.habittrackerapplication

import HabitAdapter
import android.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habittrackerapplication.databinding.ActivityMainBinding
import com.example.habittrackerapplication.databinding.DialogAddHabitBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var habitAdapter: HabitAdapter
    private val habitList = mutableListOf<Habit>()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.title = getString(R.string.app_name)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val user = firebaseAuth.currentUser
        user?.let {
            val uid = it.uid
            firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val firstName = document.getString("firstName")
                        val lastName = document.getString("lastName")
                        binding.usernameText.text = getString(R.string.hello_user, "$firstName $lastName")
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "فشل في تحميل البيانات", Toast.LENGTH_SHORT).show()
                }
        }

        habitAdapter = HabitAdapter(habitList)
        binding.habitRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.habitRecyclerView.adapter = habitAdapter

        populateHabits()

        binding.addHabitButton.setOnClickListener {
            showAddHabitDialog()
        }
    }

    private fun populateHabits() {
        habitList.add(Habit("Drink Water", "Drink at least 8 cups of water daily", false))
        habitList.add(Habit("Exercise", "30 minutes of exercise every day", false))
        habitList.add(Habit("Read a Book", "Read 20 pages of a book daily", false))
        habitList.add(Habit("Jogging", "Jog for 30 minutes every morning", false))

        habitAdapter.notifyDataSetChanged()
    }

    private fun addNewHabit(name: String, description: String) {
        habitList.add(Habit(name, description, false))
        habitAdapter.notifyItemInserted(habitList.size - 1)
    }

    private fun showAddHabitDialog() {
        val dialogBinding = DialogAddHabitBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add New Habit")
            .setView(dialogBinding.root)
            .setPositiveButton("Add") { _, _ ->
                val name = dialogBinding.etHabitName.text.toString()
                val description = dialogBinding.etHabitDescription.text.toString()

                if (name.isNotBlank() && description.isNotBlank()) {
                    addNewHabit(name, description)
                } else {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }
}
