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
    private val habits: MutableList<Habit>
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    var onHabitClickListener: ((Habit) -> Unit)? = null

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val binding = ItemHabitBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HabitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(habits[position], position)
    }

    override fun getItemCount(): Int = habits.size

    inner class HabitViewHolder(private val binding: ItemHabitBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(habit: Habit, position: Int) {
            binding.habitNameText.text = habit.name
            binding.habitDescriptionText.text = habit.description
            binding.habitCheckBox.isChecked = habit.isCompleted

            updateProgressUI(habit)

            binding.habitCard.setOnClickListener {
                onHabitClickListener?.invoke(habit)
            }

            binding.editHabitButton.setOnClickListener {
                showEditHabitDialog(habit, position)
            }

            binding.deleteButton.setOnClickListener {
                showDeleteHabitDialog(habit, position)
            }

            binding.habitCheckBox.setOnCheckedChangeListener(null)
            binding.habitCheckBox.isChecked = habit.isCompleted

            binding.habitCheckBox.setOnCheckedChangeListener { _, isChecked ->
                habit.isCompleted = isChecked

                if (isChecked) {
                    habit.currentValue = habit.targetValue
                } else {
                    habit.currentValue = 0
                }

                updateProgressUI(habit)
                updateHabitProgress(habit)

                binding.increaseProgressButton.isEnabled = !habit.isCompleted
            }

            binding.increaseProgressButton.setOnClickListener {
                if (habit.currentValue < habit.targetValue) {
                    habit.currentValue++
                    if (habit.currentValue >= habit.targetValue) {
                        habit.currentValue = habit.targetValue
                        habit.isCompleted = true
                        binding.habitCheckBox.isChecked = true
                    }
                    updateProgressUI(habit)
                    updateHabitProgress(habit)
                }
            }

            binding.increaseProgressButton.isEnabled = !habit.isCompleted
        }

        private fun updateProgressUI(habit: Habit) {
            val progressPercent = (habit.currentValue * 100) / habit.targetValue
            binding.habitProgressBar.max = 100
            binding.habitProgressBar.progress = progressPercent
            binding.progressText.text = "${habit.currentValue}/${habit.targetValue}"
        }

        private fun updateHabitProgress(habit: Habit) {
            val userId = auth.currentUser?.uid ?: return
            val habitRef = db.collection("users").document(userId)
                .collection("habits").document(habit.id)

            habitRef.update(
                mapOf(
                    "currentProgress" to habit.currentValue,
                    "isCompleted" to habit.isCompleted
                )
            )
        }

        private fun showEditHabitDialog(habit: Habit, position: Int) {
            val dialogBinding = DialogEditHabitBinding.inflate(LayoutInflater.from(binding.root.context))

            dialogBinding.editHabitName.setText(habit.name)
            dialogBinding.editHabitDescription.setText(habit.description)
            dialogBinding.editTargetValue.setText(habit.targetValue.toString())

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
                    val newTarget = dialogBinding.editTargetValue.text.toString().trim()

                    if (newName.isEmpty()) {
                        dialogBinding.editHabitName.error = binding.root.context.getString(R.string.name_required)
                        return@setOnClickListener
                    }

                    val targetInt = newTarget.toIntOrNull() ?: 1

                    habit.name = newName
                    habit.description = newDesc
                    habit.targetValue = targetInt
                    notifyItemChanged(position)

                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val habitRef = db.collection("users").document(userId)
                            .collection("habits").document(habit.id)

                        val updatedHabit: MutableMap<String, Any> = mutableMapOf(
                            "habitName" to newName,
                            "habitDescription" to newDesc,
                            "isCompleted" to habit.isCompleted,
                            "currentProgress" to habit.currentValue,
                            "target" to targetInt
                        )

                        habitRef.update(updatedHabit)
                            .addOnFailureListener {
                                Toast.makeText(binding.root.context, "Failed to update habit.", Toast.LENGTH_SHORT).show()
                            }
                    }

                    dialog.dismiss()
                }
            }

            dialog.show()
        }

        private fun showDeleteHabitDialog(habit: Habit, position: Int) {
            AlertDialog.Builder(binding.root.context)
                .setTitle("Delete Habit")
                .setMessage("Are you sure you want to delete this habit?")
                .setPositiveButton("Yes") { _, _ -> deleteHabit(habit, position) }
                .setNegativeButton("No", null)
                .show()
        }

        private fun deleteHabit(habit: Habit, position: Int) {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                val habitRef = db.collection("users").document(userId)
                    .collection("habits").document(habit.id)

                habitRef.delete()
                    .addOnSuccessListener {
                        habits.removeAt(position)
                        notifyItemRemoved(position)
                        Toast.makeText(binding.root.context, "Habit deleted", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(binding.root.context, "Failed to delete habit.", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}
