package com.todoit.todolist.activity

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.room.RoomDatabase
import com.todoit.todolist.adapter.TodoAdapter
import com.todoit.todolist.database.TodoDatabase
import com.todoit.todolist.databinding.ActivityListMainBinding
import com.todoit.todolist.databinding.DialogEditBinding
import com.todoit.todolist.model.TodoInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

class ListMainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityListMainBinding
    private lateinit var todoAdapter : TodoAdapter
    private lateinit var roomDatabase: TodoDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //어댑터 인스턴스 생성
        todoAdapter = TodoAdapter()

        // 리사이클러뷰에 어댑터 세팅
        binding.rvTodo.adapter = todoAdapter

        // 룸 데이터베이스 초기화
        roomDatabase = TodoDatabase.getInstance(applicationContext)!!

        // 전체 데이터 load (비동기)
        CoroutineScope(Dispatchers.IO).launch {
            val lstTodo = roomDatabase.todoDao().getAllReadData() as ArrayList<TodoInfo>
            for (todoItem in lstTodo) {
                todoAdapter.addListItem(todoItem)
            }

            // UI thread에서 처리
            runOnUiThread{
                todoAdapter.notifyDataSetChanged()
            }

        }


        // 작성하기 버튼 클릭
        binding.btnWrite.setOnClickListener{
            val bindingDialog = DialogEditBinding.inflate(LayoutInflater.from(binding.root.context), binding.root, false)

            AlertDialog.Builder(this)
                .setTitle("To-Do 남기기")
                .setView(bindingDialog.root)
                .setPositiveButton("작성완료", DialogInterface.OnClickListener { dialogInterface, i ->
                    val todoItem = TodoInfo()
                    todoItem.todoContent = bindingDialog.etMemo.text.toString()
                    todoItem.todoDate = SimpleDateFormat("yyyy-MM-dd").format(Date())
                    todoAdapter.addListItem(todoItem) // 어댑터의 전역변수 arrayList 쪽에 아이템 추가하기 위한 메소드 호출
                    CoroutineScope(Dispatchers.IO).launch {
                        roomDatabase.todoDao().insertTodoData(todoItem) // 데이터베이스 또한 클래스 데이터 삽입
                        runOnUiThread{
                            todoAdapter.notifyDataSetChanged() // 리스트 새로고침
                        }
                    }

                })
                .setNegativeButton("취소", DialogInterface.OnClickListener { dialogInterface, i ->

                })
                .show()
        }
    }
}