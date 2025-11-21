package com.nastya.tasks

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import com.nastya.tasks.databinding.FragmentEditTaskBinding
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EditTaskFragment : BaseTaskFragment() {
    private var _binding: FragmentEditTaskBinding? = null
    override val binding get() = _binding!!

    private lateinit var editViewModel: EditTaskViewModel
    override val fragmentViewModel: BaseTaskViewModel
        get() = editViewModel

    private val viewModel: EditTaskViewModel
        get() = editViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    val dateFormatter: DateTimeFormatter? = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditTaskBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val taskId = EditTaskFragmentArgs.fromBundle(requireArguments()).taskId

        val application = requireNotNull(this.activity).application
        val dao = TaskDatabase.getInstance(application).taskDao

        val viewModelFactory = EditTaskViewModelFactory(taskId, dao)
        editViewModel = ViewModelProvider(this, viewModelFactory)[EditTaskViewModel::class.java]

        viewModel.navigateToList.observe(viewLifecycleOwner, Observer { navigate ->
            if(navigate) {
                view.findNavController()
                    .navigate(R.id.action_editTaskFragment_to_tasksFragment)
                viewModel.onNavigateToList()
            }
        })

        binding.taskNameEdit.addTextChangedListener { str ->
            viewModel.onTaskNameChanged((str.takeIf { !it.isNullOrBlank() } ?: "").toString())
        }

        binding.taskDone.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onTaskDoneChanged(isChecked)
        }

        val currentText = binding.taskDate.text.toString()

        binding.imgCalendar.setOnClickListener {
            showMaterialDatePicker(currentText) { selectedDate ->
                onDateSelected(selectedDate)
            }
        }

        setupObservers()
        setupDeleteButton()
        switchListener()
        createNotificationChannel()

        binding.remindButton.setOnClickListener {
            val taskDate: LocalDate? = viewModel.task.value?.taskDate
            if(taskDate == null) {
                Toast.makeText(requireContext(), "Set the task date", Toast.LENGTH_LONG).show()
            } else {
                if (checkNotificationPermissions(requireContext())) {
                    scheduleNotification()
                }
            }
        }
    }

    fun isNotificationScheduled(notificationId: Int): Boolean {
        val intent = Intent(requireContext(), Notification::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            notificationId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        return pendingIntent != null
    }

    fun switchListener() {
        binding.switchRemind.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.remindButton.visibility = View.VISIBLE
                binding.timePicker.visibility = View.VISIBLE
            } else {
                binding.remindButton.visibility = View.GONE
                binding.timePicker.visibility = View.GONE
            }
        }
    }

    private fun setupDeleteButton() {
        binding.deleteButton.setOnClickListener {
            viewModel.showDeleteConfirmationDialog(requireContext())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.task.collect { task ->
                    task?.let {
                        binding.taskNameEdit.setText(it.taskName)
                        binding.taskDone.isChecked = it.taskDone
                        binding.taskDate.text = task.taskDate?.format(dateFormatter) ?: "—"

                        it.reminderTime?.let { reminderTime ->
                            binding.timePicker.hour = reminderTime.hour
                            binding.timePicker.minute = reminderTime.minute
                        }

                        binding.switchRemind.isChecked = isNotificationScheduled(it.taskId.toInt())
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onDateSelected(selection: Long) {
        val selectedDate = Instant.ofEpochMilli(selection)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        binding.taskDate.text = selectedDate.format(dateFormatter) ?: "Choose the date"
        viewModel.onTaskDateChanged(selectedDate)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val notificationManager = requireContext()
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (notificationManager.getNotificationChannel("task_reminder_channel") == null) {
            val name = "Notify Channel"
            val desc = "A Description of the Channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelID, name, importance)
            channel.description = desc

            notificationManager.createNotificationChannel(channel)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleNotification() {
        val intent = Intent(requireContext(), Notification::class.java)

        val title = "Complete the task!"
        val message = viewModel.task.value?.taskName ?: "message"

        intent.putExtra(titleExtra, title)
        intent.putExtra(messageExtra, message)

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            viewModel.task.value!!.taskId.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val time = getTime()
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            time,
            pendingIntent
        )

        showAlert(time, title, message)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getTime(): Long {
        val minute = binding.timePicker.minute
        val hour = binding.timePicker.hour
        val reminderTime = LocalTime.of(hour, minute)
        viewModel.updateReminderTime(reminderTime)

        val taskDate: LocalDate? = viewModel.task.value?.taskDate

        val day = taskDate!!.dayOfMonth
        val month = taskDate.monthValue - 1
        val year = taskDate.year

        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, hour, minute)

        return calendar.timeInMillis
    }

    private fun showAlert(time: Long, title: String, message: String) {
        val date = Date(time)
        val dateFormat = android.text.format.DateFormat.getLongDateFormat(requireContext())
        val timeFormat = android.text.format.DateFormat.getTimeFormat(requireContext())

        AlertDialog.Builder(requireContext())
            .setTitle("Notification Scheduled")
            .setMessage(
                "Title: $title\nMessage: $message\nAt: ${dateFormat.format(date)} ${timeFormat.format(date)}"
            )
            .setPositiveButton("Okay") { _, _ -> }
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun checkNotificationPermissions(context: Context): Boolean {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val isEnabled = notificationManager.areNotificationsEnabled()

        if (!isEnabled) {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            context.startActivity(intent)

            return false
        }
        return true
    }
}