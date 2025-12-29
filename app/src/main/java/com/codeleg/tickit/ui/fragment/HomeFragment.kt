package com.codeleg.tickit.ui.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.codeleg.tickit.database.model.Todo
import com.codeleg.tickit.databinding.DialogTodoDetailBinding
import com.codeleg.tickit.databinding.FragmentHomeBinding
import com.codeleg.tickit.ui.adapter.TodoListAdapter
import com.codeleg.tickit.ui.viewmodel.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private val mainVM: MainViewModel by activityViewModels()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var searchJob: Job? = null
    private var fullTodoList: List<Todo> = emptyList()

    private lateinit var todoAdapter: TodoListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupRecyclerView()
        observeTodos()
        loadTodos()
        setupFiltering()
        setUpSearchFeature()

        binding.fabAddTodo.setOnClickListener { addNewTodo() }

        return binding.root
    }

    private fun setupFiltering() {
        binding.chipGroupStatus.setOnCheckedStateChangeListener { _, checkedIds ->
            when (checkedIds.firstOrNull()) {
                binding.chipAll.id -> {
                    todoAdapter.submitList(fullTodoList)
                }

                binding.chipIncomplete.id -> {
                    val filteredList = fullTodoList.filter { todo ->
                        !todo.completed
                    }
                    todoAdapter.submitList(filteredList)
                }

                binding.chipCompleted.id -> {
                    val filteredList = fullTodoList.filter { todo ->
                        todo.completed
                    }
                    todoAdapter.submitList(filteredList)
                }
            }
        }
    }


    private fun setUpSearchFeature() {
        binding.etAddTodo.doOnTextChanged { text, _, _, _ ->

            searchJob?.cancel()
            searchJob = viewLifecycleOwner.lifecycleScope.launch {
                delay(700)
                binding.chipAll.isChecked = true

                val query = text.toString().trim().lowercase()

                if (query.isEmpty()) {
                    todoAdapter.submitList(fullTodoList)
                } else {
                    val filteredList = fullTodoList.filter { todo ->
                        todo.title.lowercase().contains(query)
                    }
                    todoAdapter.submitList(filteredList)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // -------------------- SETUP --------------------

    private fun setupRecyclerView() {
        todoAdapter = TodoListAdapter(
            onCheckedChange = ::onItemCheckedChange,
            onItemClick = ::onItemClick
        )

        binding.rvTodos.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = todoAdapter
        }
    }

    private fun observeTodos() {
        mainVM.allTodos.observe(viewLifecycleOwner) {
            fullTodoList = it
            todoAdapter.submitList(it)
        }
    }

    private fun loadTodos() {
        if (_binding == null || !isAdded) return

        binding.loadingBar.show()
        binding.rvTodos.visibility = View.GONE

        mainVM.loadTodos { isSuccess, msg ->
            if (_binding == null || !isAdded) return@loadTodos
            binding.loadingBar.hide()
            binding.rvTodos.visibility = View.VISIBLE

            if (!isSuccess) showSnack("Error loading todos: ${msg ?: "Unknown error"}")
        }
    }

    // -------------------- ACTIONS --------------------

    private fun addNewTodo() {
        val title = binding.etAddTodo.text.toString().trim()
        if (title.isEmpty()) {
            binding.etAddTodo.error = "Title cannot be empty"
            return
        }

        mainVM.addTodo(Todo(title = title)) { isSuccess, msg ->
            if (isSuccess) binding.etAddTodo.text?.clear()

            showSnack(
                if (isSuccess) "Todo added successfully"
                else msg ?: "Error adding todo"
            )
        }
    }

    private fun onItemCheckedChange(todo: Todo, isChecked: Boolean): Boolean {
        mainVM.updateTodoComplete(todo.id, isChecked) { isDone, msg ->
            if (!isDone) showSnack(msg ?: "Error updating todo")
        }
        return true
    }

    private fun onItemClick(todo: Todo) {
        showDetailDialog(todo)
    }

    // -------------------- DIALOG --------------------

    private fun showDetailDialog(todo: Todo) {
        val dialogBinding =
            DialogTodoDetailBinding.inflate(layoutInflater)

        setupDialogData(dialogBinding, todo)
        setupPriorityDropdown(dialogBinding, todo)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()

        handleSave(dialogBinding, dialog, todo)
        handleDelete(dialog, todo, dialogBinding)

        dialog.show()
    }

    private fun setupDialogData(
        binding: DialogTodoDetailBinding,
        todo: Todo
    ) {
        binding.etTitle.setText(todo.title)
        binding.etTodoId.setText(todo.id)
        binding.switchCompleted.isChecked = todo.completed

        binding.tvCreatedAt.text =
            "Created at: ${formatDate(todo.createdAt)}"

        binding.tvUpdatedAt.text =
            "Last updated: ${formatDate(todo.updatedAt)}"
    }

    private fun setupPriorityDropdown(
        binding: DialogTodoDetailBinding,
        todo: Todo
    ) {
        val priorities = listOf("Low", "Normal", "High")

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            priorities
        )

        binding.spPriority.setAdapter(adapter)
        binding.spPriority.setText(priorities[todo.priority], false)
    }

    private fun handleSave(
        binding: DialogTodoDetailBinding,
        dialog: Dialog,
        todo: Todo
    ) {
        val priorities = listOf("Low", "Normal", "High")

        binding.btnSave.setOnClickListener {

            val title = binding.etTitle.text.toString().trim()
            if (title.isEmpty()) {
                binding.etTitle.error = "Title cannot be empty"
                return@setOnClickListener
            }

            val updatedTodo = todo.copy(
                title = title,
                completed = binding.switchCompleted.isChecked,
                priority = priorities.indexOf(
                    binding.spPriority.text.toString()
                ).coerceAtLeast(1)
            )

            mainVM.updateTodo(updatedTodo) { isUpdated, msg ->
                if (isUpdated) dialog.dismiss()

                showSnack(
                    if (isUpdated) "Todo updated successfully"
                    else msg ?: "Error updating todo"
                )
            }
            dialog.dismiss()
        }
    }

    private fun handleDelete(
        dialog: Dialog,
        todo: Todo,
        binding: DialogTodoDetailBinding
    ) {
        binding.btnDelete.setOnClickListener {
            mainVM.deleteTodo(todo.id) { isDeleted, msg ->
                showSnack(
                    if (isDeleted) "Todo deleted successfully"
                    else msg ?: "Error deleting todo"
                )
            }
            dialog.dismiss()
        }
    }

    // -------------------- UTIL --------------------

    private fun showSnack(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun formatDate(timestamp: Long): String {
        if (timestamp == 0L) return "--"

        val formatter = SimpleDateFormat(
            "dd MMM yyyy, hh:mm a",
            Locale.getDefault()
        )
        return formatter.format(Date(timestamp))
    }
}
