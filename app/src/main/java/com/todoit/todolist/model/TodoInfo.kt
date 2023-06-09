package com.todoit.todolist.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class TodoInfo {
    var todoContent : String = "" // 메모 내용
    var todoDate : String = "" // 메모 일자
    var todoCompleted : Boolean = false // 완료 여부
    var todoPriority : Int = 0 // 우선순위

    @PrimaryKey(autoGenerate = true)
    var id : Int = 0
}