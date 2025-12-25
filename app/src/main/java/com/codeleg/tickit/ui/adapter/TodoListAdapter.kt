package com.codeleg.tickit.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codeleg.tickit.database.model.Todo
import com.codeleg.tickit.databinding.ItemTodoBinding

class TodoListAdapter(
    private val onCheckedChange: ((Todo, Boolean) -> Boolean),
    private val onItemClick: ((Todo) -> Unit)? = null
) : ListAdapter<Todo, TodoListAdapter.TodoViewHolder>(TodoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val binding = ItemTodoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TodoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TodoViewHolder(
        private val binding: ItemTodoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(todo: Todo) = with(binding) {
            tvTitle.text = todo.title
            tvCreatedAt.text = formatDate(todo.createdAt)
            tvPriority.text = todo.priority.toString()

            cbDone.setOnCheckedChangeListener(null)
            cbDone.isChecked = todo.completed

            cbDone.setOnCheckedChangeListener { button, isChecked ->
                button.isEnabled = false
                onCheckedChange(todo, isChecked)
                button.isEnabled = true

            }

            root.setOnClickListener {
                onItemClick?.invoke(todo)
            }
        }

        private fun formatDate(timestamp: Long): String {
            return android.text.format.DateFormat.format("dd MMM, yyyy", timestamp).toString()
        }
    }

    class TodoDiffCallback : DiffUtil.ItemCallback<Todo>() {
        override fun areItemsTheSame(oldItem: Todo, newItem: Todo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Todo, newItem: Todo): Boolean {
            return oldItem == newItem
        }
    }
}
