package com.example.habittrackerapplication

import HabitAdapter
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
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
        loadHabits() // تحميل العادات عند فتح الشاشة
    }

    override fun onResume() {
        super.onResume()
        Log.d("HabitTracker", "HomeFragment resumed, loading habits...")  // إضافة Log هنا
    }


    private fun setupRecyclerView() {
        habitAdapter = HabitAdapter(habitList)
        binding.habitRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.habitRecyclerView.adapter = habitAdapter
    }

    private fun setupAddHabitButton() {
        binding.addHabitButton.setOnClickListener {
            showAddHabitDialog()
        }
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
                    addNewHabitToFirestore(name, desc) // رفع العادة لFirestore
                } else {
                    Toast.makeText(requireContext(),
                        getString(R.string.error), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun addNewHabitToFirestore(name: String, description: String) {
        val uid = auth.currentUser?.uid ?: return
        val habitRef = db.collection("users").document(uid).collection("habits").document()  // معرف فريد يتم إنشاؤه هنا

        val habit = hashMapOf(
            "habitName" to name,
            "habitDescription" to description,
            "isCompleted" to false // بتكون مبدئياً false
        )

        habitRef.set(habit)
            .addOnSuccessListener {
                // إضافة الـ habit.id بعد إضافتها ل Firestore
                val habitId = habitRef.id

                // تسجيل عملية إضافة العادة
                Log.d("HabitTracker", "Habit added to Firestore with ID: $habitId")

                // إضافة العادة للقائمة بعد التأكد من أنها غير مكررة
                if (habitList.none { it.id == habitId }) {
                    Log.d("HabitTracker", "Adding habit to local list: $name")
                    habitList.add(Habit(name, description, false, habitId))  // إضافة المعرف الجديد إلى العادة
                    habitAdapter.notifyDataSetChanged() // تحديث الـ RecyclerView بعد إضافة العادة
                    Toast.makeText(requireContext(), "Habit Added Successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d("HabitTracker", "Habit already exists in local list, not adding.")
                }
            }
            .addOnFailureListener {
                // حصل خطأ
                Toast.makeText(requireContext(), "Error adding habit", Toast.LENGTH_SHORT).show()
            }
    }






    private fun  loadHabits()  {
        habitList.clear()  // مسح البيانات القديمة
        Log.d("HabitTracker", "Loading habits from Firestore...")  // إضافة Log هنا
        val uid = auth.currentUser?.uid ?: return
        val habitsRef = db.collection("users").document(uid).collection("habits")

        habitsRef.get()
            .addOnSuccessListener { result ->
                Log.d("HabitTracker", "Habits fetched from Firestore: ${result.size()}")  // تسجيل عدد العادات التي تم جلبها
                result.forEach { document ->
                    val habitName = document.getString("habitName") ?: ""
                    val habitDescription = document.getString("habitDescription") ?: ""
                    val isCompleted = document.getBoolean("isCompleted") ?: false
                    val currentValue = document.getLong("currentProgress")?.toInt() ?: 0  // تحميل القيمة الحالية
                    val targetValue = document.getLong("target")?.toInt() ?: 1         // تحميل الهدف
                    val habitId = document.id // الحصول على المعرف الفريد للعاده

                    Log.d("HabitTracker", "Adding habit: $habitName, isCompleted: $isCompleted, currentValue: $currentValue, targetValue: $targetValue")  // سجل العادة التي يتم إضافتها
                    habitList.add(Habit(habitName, habitDescription, isCompleted, habitId, currentValue, targetValue))
                }
                Log.d("HabitTracker", "Habit list size after loading: ${habitList.size}")  // تحقق من حجم القائمة بعد التحميل
                habitAdapter.notifyDataSetChanged() // تحديث الـ RecyclerView بعد تحميل العادات
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error loading habits", Toast.LENGTH_SHORT).show()
            }
    }






    private fun loadUserName() {
        val uid = auth.currentUser?.uid ?: return
        val userRef = db.collection("users").document(uid)

        userRef.get()
            .addOnSuccessListener { document ->
                if (!isAdded) return@addOnSuccessListener

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

    // عندما يتم تغيير حالة العادة إلى مكتملة
    private fun toggleHabitCompletion(habit: Habit, position: Int) {
        val uid = auth.currentUser?.uid ?: return
        val habitsRef = db.collection("users").document(uid).collection("habits")
        val habitRef = habitsRef.document(habit.id)  // استخدام documentId بدلاً من habit.name

        // تغيير حالة العادة
        habitRef.update("isCompleted", !habit.isCompleted)
            .addOnSuccessListener {
                habit.isCompleted = !habit.isCompleted  // تحديث الحالة محلياً
                habitAdapter.notifyItemChanged(position)  // تحديث العادة في الـ RecyclerView

                // تحديث صفحة الإحصائيات بعد التغيير
                loadHabits()  // تحميل العادات المحدثة
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error updating habit", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
