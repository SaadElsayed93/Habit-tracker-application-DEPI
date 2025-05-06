package com.example.habittrackerapplication

import HabitAdapter
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habittrackerapplication.databinding.ActivityMainBinding
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

        // Set up the Toolbar
        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.title = getString(R.string.app_name)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set username dynamically (fetch from Firestore)
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

        // Initialize RecyclerView
        habitAdapter = HabitAdapter(habitList)
        binding.habitRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.habitRecyclerView.adapter = habitAdapter

        // Populate habit list with some sample data
        populateHabits()

        // Add Habit Button click listener
        binding.addHabitButton.setOnClickListener {
            addNewHabit()
        }
    }

    // Function to populate the habit list with some sample data
    private fun populateHabits() {
        habitList.add(Habit("Drink Water", "Drink at least 8 cups of water daily", false))
        habitList.add(Habit("Exercise", "30 minutes of exercise every day", false))
        habitList.add(Habit("Read a Book", "Read 20 pages of a book daily", false))
        habitList.add(Habit("Jogging", "Jog for 30 minutes every morning", false))

        habitAdapter.notifyDataSetChanged()
    }

    // Function to simulate adding a new habit
    private fun addNewHabit() {
        habitList.add(Habit("New Habit", "Description of the new habit", false))
        habitAdapter.notifyItemInserted(habitList.size - 1)
    }
}
