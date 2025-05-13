import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.habittrackerapplication.Habit
import com.example.habittrackerapplication.R
import com.example.habittrackerapplication.databinding.DialogEditHabitBinding
import com.example.habittrackerapplication.databinding.ItemHabitBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class HabitAdapter(
    private val habits: MutableList<Habit> // لازم MutableList علشان نقدر نعدّل
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    var onHabitClickListener: ((Habit) -> Unit)? = null

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val binding = ItemHabitBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HabitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]
        holder.bind(habit, position)
    }

    override fun getItemCount(): Int = habits.size






    inner class HabitViewHolder(private val binding: ItemHabitBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(habit: Habit, position: Int) {
            binding.habitNameText.text = habit.name
            binding.habitDescriptionText.text = habit.description
            binding.habitCheckBox.isChecked = habit.isCompleted

            binding.habitCard.setOnClickListener {
                onHabitClickListener?.invoke(habit)
            }

            // Edit Habit
            binding.editHabitButton.setOnClickListener {
                showEditHabitDialog(habit, position)
            }

            // Delete Habit
            binding.deleteButton.setOnClickListener {
                showDeleteHabitDialog(habit, position)
            }

            // Handle the checkbox click to update the habit completion status
            binding.habitCheckBox.setOnCheckedChangeListener { _, isChecked ->
                habit.isCompleted = isChecked // تحديث الحالة المحلية للعادة
                updateHabitCompletion(habit) // تحديث الحالة في Firestore
            }
        }

        private fun showEditHabitDialog(habit: Habit, position: Int) {
            val dialogBinding = DialogEditHabitBinding.inflate(LayoutInflater.from(binding.root.context))

            // عرض البيانات الحالية في الحقول
            dialogBinding.editHabitName.setText(habit.name)
            dialogBinding.editHabitDescription.setText(habit.description)

            val dialog = AlertDialog.Builder(binding.root.context)
                .setTitle(R.string.edit)
                .setView(dialogBinding.root)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, null)
                .create()

            dialog.setOnShowListener {
                val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                saveButton.setOnClickListener {
                    val newName = dialogBinding.editHabitName.text.toString().trim()
                    val newDesc = dialogBinding.editHabitDescription.text.toString().trim()

                    if (newName.isEmpty()) {
                        dialogBinding.editHabitName.error = binding.root.context.getString(R.string.name_required)
                        return@setOnClickListener
                    }

                    // تحديث بيانات العادة محلياً
                    habit.name = newName
                    habit.description = newDesc
                    notifyItemChanged(position)

                    // التحقق من وجود المستخدم وتحديث البيانات في Firestore
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val habitRef = db.collection("users").document(userId)
                            .collection("habits").document(habit.id)

                        // استخدم MutableMap<String, Any>
                        val updatedHabit: MutableMap<String, Any> = mutableMapOf(
                            "habitName" to newName,
                            "habitDescription" to newDesc,
                            "isCompleted" to habit.isCompleted
                        )

                        habitRef.update(updatedHabit)
                            .addOnSuccessListener {
                                // تم التحديث بنجاح
                            }
                            .addOnFailureListener {
                                // فشل التحديث
                                Toast.makeText(binding.root.context, "Failed to update habit in Firestore.", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(binding.root.context, "User not logged in.", Toast.LENGTH_SHORT).show()
                    }

                    dialog.dismiss()
                }
            }

            dialog.show()
        }

        // Show a dialog to confirm deletion
        private fun showDeleteHabitDialog(habit: Habit, position: Int) {
            val dialog = AlertDialog.Builder(binding.root.context)
                .setTitle("Delete Habit")
                .setMessage("Are you sure you want to delete this habit?")
                .setPositiveButton("Yes") { _, _ ->
                    deleteHabit(habit, position)
                }
                .setNegativeButton("No", null)
                .create()

            dialog.show()
        }

        // Function to delete the habit from Firestore and the list
        private fun deleteHabit(habit: Habit, position: Int) {
            // Get the user ID
            val userId = auth.currentUser?.uid
            if (userId != null) {
                // Delete from Firestore
                val habitRef = db.collection("users").document(userId)
                    .collection("habits").document(habit.id)

                habitRef.delete()
                    .addOnSuccessListener {
                        // Remove the habit from the local list
                        habits.removeAt(position)
                        notifyItemRemoved(position)
                        Toast.makeText(binding.root.context, "Habit deleted", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(binding.root.context, "Failed to delete habit from Firestore.", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(binding.root.context, "User not logged in.", Toast.LENGTH_SHORT).show()
            }
        }

        // Function to update the habit completion status in Firestore
        private fun updateHabitCompletion(habit: Habit) {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                val habitRef = db.collection("users").document(userId)
                    .collection("habits").document(habit.id)

                // Update the isCompleted status in Firestore
                habitRef.update("isCompleted", habit.isCompleted)
                    .addOnSuccessListener {
                        // Successfully updated
                    }
                    .addOnFailureListener {
                        Toast.makeText(binding.root.context, "Failed to update habit completion status.", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(binding.root.context, "User not logged in.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
