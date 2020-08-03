package jp.techacademy.hiroshi.murata.taskapp

import java.io.Serializable
import java.util.Date
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Task : RealmObject(), Serializable{
    var title: String = "" //title
    var contents: String = "" //contents
    var date: Date = Date() //date
    var category: String ="" //category

    // set id as primary key

    @PrimaryKey
    var id: Int = 0
}