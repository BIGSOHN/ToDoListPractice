package com.todoit.todolist.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.todoit.todolist.model.TodoInfo

@Dao
interface TodoDao {

    // database table에 삽입 (추가)
    @Insert
    fun insertTodoData(todoInfo: TodoInfo)


    // database table에 기존에 존재하는 데이터를 수정
    @Update
    fun updateTodoData(todoInfo: TodoInfo)

    // database table에 기존에 존재하는 데이터를 삭제
    @Delete
    fun deleteTodoData(todoInfo: TodoInfo)

    // 데이터베이스 테이블에서 todoCompleted 값을 수정
    @Query("UPDATE TodoInfo SET todoCompleted = :completed WHERE id = :itemId")
    suspend fun updateTodoCompleted(itemId: Int, completed: Boolean)

    // 데이터베이스 테이블에서 전체 데이터 조회
    @Query("SELECT * FROM TodoInfo ORDER BY todoDate")
    fun getAllReadData(): List<TodoInfo>

    // 데이터베이스 테이블에서 특정 id에 해당하는 todoCompleted 값을 가져옴
    @Query("SELECT todoCompleted FROM TodoInfo WHERE id = :itemId")
    suspend fun getTodoCompleted(itemId: Int): Boolean
}