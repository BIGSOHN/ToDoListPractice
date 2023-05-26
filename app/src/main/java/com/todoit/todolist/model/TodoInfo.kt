package com.todoit.todolist.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class TodoInfo {
    var todoContent : String = "" // 메모 내용
    var todoDate : String = "" // 메모 일자

    @PrimaryKey(autoGenerate = true)
    var id : Int = 0
}