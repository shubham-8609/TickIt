package com.codeleg.tickit.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.codeleg.tickit.database.model.Todo
import com.codeleg.tickit.databinding.FragmentHomeBinding
import com.codeleg.tickit.ui.adapter.TodoListAdapter
import com.codeleg.tickit.ui.viewmodel.MainViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private val mainVM: MainViewModel by activityViewModels()
    var _binding: FragmentHomeBinding? = null
    val binding get() = _binding!!
    private lateinit var todoAdapter: TodoListAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(layoutInflater, container, false);
        todoAdapter = TodoListAdapter(
                onCheckedChange = { todo, isChecked ->
                    onItemCheckedChange(todo, isChecked)
                },
                onItemClick = { todo ->
                    onItemDelete(todo)
                }
            );
            binding.rvTodos.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = todoAdapter
            };

        mainVM.loadTodos(){ isCompleted , msg ->
            if(isCompleted) Snackbar.make(binding.root ,"Todos loaded Successfully" , Snackbar.LENGTH_SHORT).show()
            else Snackbar.make(binding.root , msg.toString() , Snackbar.LENGTH_SHORT).show()
        }



        binding.fabAddTodo.setOnClickListener {
            addNewTodo()
        }
        mainVM.allTodos.observe(viewLifecycleOwner){ todos ->
                todoAdapter.submitList(todos)
        }
        return binding.root
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


    private fun addNewTodo() {
        val title = binding.etAddTodo.text.toString().trim()
        if (title.isEmpty()) {
            binding.etAddTodo.error = "Title cannot be empty"
            binding.etAddTodo.requestFocus()
            return
        }
        val todo = Todo(title = title)
        mainVM.addTodo(todo) { isSuccess, errorMsg ->
            if (isSuccess) {
                binding.etAddTodo.text?.clear()
                Snackbar.make(
                    binding.root,
                    "Todo added successfully",
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                Snackbar.make(
                    binding.root,
                    errorMsg ?: "Error adding todo",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun onItemCheckedChange(todo: Todo , isChecked: Boolean){

    }
    private fun onItemDelete(todo: Todo){

    }
}
