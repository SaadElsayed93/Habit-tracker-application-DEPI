import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.habittrackerapplication.Habit
import com.example.habittrackerapplication.R
import com.example.habittrackerapplication.databinding.DialogEditHabitBinding
import com.example.habittrackerapplication.databinding.ItemHabitBinding

class HabitAdapter(
    private val habits: MutableList<Habit> // لازم MutableList علشان نقدر نعدّل
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    var onHabitClickListener: ((Habit) -> Unit)? = null

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

            binding.editHabitButton.setOnClickListener {
                showEditHabitDialog(habit, position)
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

                    // تحديث بيانات العادة
                    habit.name = newName
                    habit.description = newDesc
                    notifyItemChanged(position)

                    // TODO: لو بتستخدم Room أو Firebase ضيف الكود هنا لتحديث البيانات هناك

                    dialog.dismiss()
                }
            }

            dialog.show()
        }
    }
}
