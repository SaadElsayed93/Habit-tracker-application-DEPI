import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.db.williamchart.view.BarChartView
import com.example.habittrackerapplication.R
import com.example.habittrackerapplication.databinding.FragmentStaticsBinding

class StaticsFragment : Fragment(R.layout.fragment_statics) {

    private var _binding: FragmentStaticsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentStaticsBinding.bind(view)

        binding.totalHabitsText.text = "Total Habits: ${getTotalHabits()}"
        binding.completedHabitsText.text = "Completed Habits: ${getCompletedHabits()}"
        binding.progressBar.progress = getProgressPercentage()
        binding.bestHabitText.text = "Best Habit: ${getBestHabit()}"
        binding.achievementsText.text = "Achievements: ${getAchievements()}"

        setUpChart()
    }

    private fun getTotalHabits(): Int {
        return 5
    }

    private fun getCompletedHabits(): Int {
        return 3
    }

    private fun getProgressPercentage(): Int {
        return 75
    }

    private fun getBestHabit(): String {
        return "Exercise"
    }

    private fun getAchievements(): String {
        return "7 Consecutive Days!"
    }

    private fun setUpChart() {
        val chart = binding.barChart
        val data = listOf(
            "Mon" to 3f,
            "Tue" to 5f,
            "Wed" to 2f,
            "Thu" to 4f,
            "Fri" to 6f
        )
        chart.animation.duration = 1000L
        chart.animate(data)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
