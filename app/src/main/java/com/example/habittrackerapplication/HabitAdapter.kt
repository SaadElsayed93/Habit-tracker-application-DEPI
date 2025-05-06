import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.habittrackerapplication.Habit
import com.example.habittrackerapplication.databinding.ItemHabitBinding


class HabitAdapter(private val habits: List<Habit>) :
    RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    var onHabitClickListener: ((Habit) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val binding = ItemHabitBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HabitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]
        holder.bind(habit)
    }

    override fun getItemCount(): Int = habits.size

    inner class HabitViewHolder(private val binding: ItemHabitBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(habit: Habit) {
            binding.habitNameText.text = habit.name // Bind habit name
            binding.habitDescriptionText.text = habit.description // Bind habit description
            binding.habitCheckBox.isChecked = habit.isCompleted // Bind habit completion state

            binding.habitCard.setOnClickListener {
                onHabitClickListener?.invoke(habit)
            }
        }
    }
}

