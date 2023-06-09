package com.todoit.todolist.adapter

import android.app.Activity
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.todoit.todolist.database.TodoDatabase
import com.todoit.todolist.databinding.DialogEditBinding
import com.todoit.todolist.databinding.ListItemTodoBinding
import com.todoit.todolist.model.TodoInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

class TodoAdapter : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {
    private var lstTodo : ArrayList<TodoInfo> = ArrayList()
    private lateinit var roomDatabase: TodoDatabase
    private val lock = Any()

    fun  addListItem(todoItem: TodoInfo) {
        lstTodo.add(0, todoItem)
        //sortListByPriority() // 우선순위에 따라 정렬
    }

    private fun sortListByPriority() {
        lstTodo.sortByDescending { it.todoPriority }
        notifyDataSetChanged()
    }

    private fun getPriorityString(priority: Int): String {
        return when (priority) {
            0 -> "상"
            1 -> "중"
            2 -> "하"
            else -> "예" // 필요에 따라 기본값 설정
        }
    }

    inner class TodoViewHolder(private val binding: ListItemTodoBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(todoItem: TodoInfo){
            //리스트 뷰 데이터를 UI에 연동
            binding.tvContent.setText(todoItem.todoContent)
            binding.tvDate.setText((todoItem.todoDate))
            binding.tvPriority.setText(getPriorityString(todoItem.todoPriority))

            binding.checkCompleted.setOnCheckedChangeListener(null) // 기존 리스너 제거
            binding.checkCompleted.isChecked = todoItem.todoCompleted




            // 리스트 삭제 버튼 클릭 연동
            binding.btnRemove.setOnClickListener {
                // 쓰레기통 이미지 클릭시 내부 로직 수행
                AlertDialog.Builder(binding.root.context)
                    .setTitle("[주의]")
                    .setMessage("제거하시면 데이터는 복구되지 않습니다.\n정말 제거하시겠습니까?")
                    .setPositiveButton("제거", DialogInterface.OnClickListener { dialogInterface, i ->

                        CoroutineScope(Dispatchers.IO).launch{
                            val innerLstTodo = roomDatabase.todoDao().getAllReadData()
                            for (item in innerLstTodo){
                                if (item.todoContent == todoItem.todoContent && item.todoDate == todoItem.todoDate){
                                    // delete to database item
                                    roomDatabase.todoDao().deleteTodoData(item)
                                }
                            }
                            // ui remove
                            lstTodo.remove(todoItem)
                            (binding.root.context as Activity).runOnUiThread{
                                notifyDataSetChanged() // 리스트 새로고침
                                // 토스트 팝업 메시지 표시
                                Toast.makeText(binding.root.context, "제거되었습니다", Toast.LENGTH_SHORT).show()
                            }


                        }

                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialogInterface, i ->

                    })
                    .show()

            }

            // 리스트 수정 클릭 연동
            binding.root.setOnClickListener {
                val bindingDialog = DialogEditBinding.inflate(LayoutInflater.from(binding.root.context), binding.root, false)
                // 기존에 작성된 데이터 보여주기
                bindingDialog.etMemo.setText(todoItem.todoContent)

                AlertDialog.Builder(binding.root.context)
                    .setTitle("To-Do 남기기")
                    .setView(bindingDialog.root)
                    .setPositiveButton("수정완료", DialogInterface.OnClickListener { dialogInterface, i ->
                        CoroutineScope(Dispatchers.IO).launch{
                            val innerLstTodo = roomDatabase.todoDao().getAllReadData()
                            for (item in innerLstTodo){
                                if (item.todoContent == todoItem.todoContent && item.todoDate == todoItem.todoDate){
                                    // modify to database item
                                    item.todoContent = bindingDialog.etMemo.text.toString()
                                    item.todoDate = SimpleDateFormat("yyyy-MM-dd").format(Date())
                                    roomDatabase.todoDao().updateTodoData(item)
                                }
                            }

                            // ui modify
                            todoItem.todoContent = bindingDialog.etMemo.text.toString()
                            todoItem.todoDate = SimpleDateFormat("yyyy-MM-dd").format(Date())

                            //array list 수정
                            lstTodo.set(adapterPosition, todoItem)

                            (binding.root.context as Activity).runOnUiThread{
                                notifyDataSetChanged() // 리스트 새로고침
                            }
                        }


                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialogInterface, i ->

                    })
                    .show()
            }



            // 완료 여부 클릭 연동

            binding.checkCompleted.setOnCheckedChangeListener { compoundButton, isChecked ->
                synchronized(lock) {
                    todoItem.todoCompleted = isChecked
                    CoroutineScope(Dispatchers.IO).launch {
                        val innerLstTodo = roomDatabase.todoDao().getAllReadData()
                        for (item in innerLstTodo) {
                            if (item.todoContent == todoItem.todoContent && item.todoDate == todoItem.todoDate) {
                                item.todoCompleted = isChecked
                                roomDatabase.todoDao().updateTodoData(item)
                            }
                        }
                        (binding.root.context as Activity).runOnUiThread{
                            notifyDataSetChanged() // 리스트 새로고침
                        }
                    }
                }
            }


            binding.tvPriority.setOnClickListener {
                val todoItem = lstTodo[adapterPosition]
                val priorityArray = arrayOf("상", "중", "하")

                // 현재 우선순위에 해당하는 인덱스 확인
                val currentPrioriyIndex = todoItem.todoPriority

                // 다이얼로그 생성
                val dialogBuilder = AlertDialog.Builder(binding.root.context)
                    dialogBuilder.setTitle("우선순위 변경")
                    dialogBuilder.setSingleChoiceItems(priorityArray, currentPrioriyIndex) {dialog, selectedIndex ->
                        // 선택된 우선순위로 업데이트
                        val newPriority = selectedIndex
                        if (todoItem.todoPriority != newPriority){
                            todoItem.todoPriority = newPriority
                            CoroutineScope(Dispatchers.IO).launch {
                                val innerLstTodo = roomDatabase.todoDao().getAllReadData()
                                for (item in innerLstTodo){
                                    if (item.todoContent == todoItem.todoContent && item.todoDate == todoItem.todoDate){
                                        item.todoPriority = newPriority
                                        roomDatabase.todoDao().updateTodoData(item)

                                    }
                                }
                                (binding.root.context as Activity).runOnUiThread{
                                    notifyDataSetChanged() // 리스트 새로고침
                                }
                            }
                        }
                        dialog.dismiss()
                    }
                    dialogBuilder.setNegativeButton("취소", null)
                    dialogBuilder.show()
            }

        }



    }



    // 뷰홀더가 생성됨. (각 리스트 아이템 1개씩 구성될 때마다 이 오버라이드 메소드가 호출됨)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoAdapter.TodoViewHolder {
        val binding = ListItemTodoBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        // 룸 데이터베이스 초기화
        roomDatabase = TodoDatabase.getInstance(binding.root.context)!!

        return TodoViewHolder(binding)
    }

    // 뷰홀더가 결합이 이루어질때 해줘야할 처리들을 구현.
    override fun onBindViewHolder(holder: TodoAdapter.TodoViewHolder, position: Int) {
        holder.bind(lstTodo[position])
    }

    // 리스트 총 개수
    override fun getItemCount(): Int {
        return lstTodo.size
    }

}