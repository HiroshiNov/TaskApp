package jp.techacademy.hiroshi.murata.taskapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.app.AlarmManager
import android.app.PendingIntent

import android.content.Context
import android.widget.LinearLayout
import io.realm.*
import kotlinx.android.synthetic.main.content_input.*
import java.util.*

const val EXTRA_TASK = "jp.techacademy.hiroshi.murata.taskapp.TASK"

class MainActivity  : AppCompatActivity() {

    private lateinit var mRealm: Realm
    private val mRealmListener = object : RealmChangeListener<Realm> {
        override fun onChange(element: Realm){
            reloadListView()
        }
    }

    private lateinit var mTaskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        search_button.setOnClickListener{
            //val taskRealmResults = mRealm.where(Task::class.java).findAll().sort("date", Sort.DESCENDING)
            val search_text = search_edit_text.text.toString()

            if (search_text != ""){
                // Build the query looking at all users:
                val search_query = mRealm.where(Task::class.java)

                // Add query conditions:
                search_query.equalTo("category", search_text)
                // query.or().equalTo("name", "Peter")

                // Execute the query:
                val result = search_query.findAll()

                // Or alternatively do the same all at once (the "Fluent interface")
                //    val result2 = realm.where<User>()
                //        .equalTo("name", "John")
                //        .or()
                //        .equalTo("name", "Peter")
                //        .findAll()
                mTaskAdapter.taskList = mRealm.copyFromRealm(result)

                listView1.adapter = mTaskAdapter

                mTaskAdapter.notifyDataSetChanged()
            }else{
                reloadListView()
            }
        }

        fab.setOnClickListener { view ->
            val intent = Intent(this@MainActivity,InputActivity::class.java)
            startActivity(intent)
        //            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
        //                .setAction("Action", null).show()
        }

        //Realm settings

        //        Realm.init(this)
        //        val realmConfig = RealmConfiguration.Builder()
        //            .deleteRealmIfMigrationNeeded()
        //            .build()

        mRealm = Realm.getDefaultInstance()
        mRealm.addChangeListener(mRealmListener)

        // ListView Settings
        mTaskAdapter = TaskAdapter(this@MainActivity)

        // ListView tasks user tap it
        //        listView1.setOnItemClickListener{parent, view, position, id ->
        // move to Edit page

        listView1.setOnItemClickListener{parent, _, position, _ ->
            val task = parent.adapter.getItem(position) as Task
            val intent = Intent(this@MainActivity, InputActivity::class.java)
            intent.putExtra(EXTRA_TASK, task.id)
            startActivity(intent)
        }

        // ListView task when user tap it long
        listView1.setOnItemLongClickListener{parent, _, position, _ ->
            // move to Delete
            val task = parent.adapter.getItem(position) as Task

            val builder = AlertDialog.Builder(this@MainActivity)

            builder.setTitle("削除")
            builder.setMessage(task.title + "を削除しますか")

            builder.setPositiveButton("OK"){_, _ ->
                val results = mRealm.where(Task::class.java).equalTo("id", task.id).findAll()

                mRealm.beginTransaction()
                results.deleteAllFromRealm()
                mRealm.commitTransaction()

                val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)
                val resultPendingIntent = PendingIntent.getBroadcast(
                    this@MainActivity,
                    task.id,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(resultPendingIntent)
                reloadListView()
            }

            builder.setNegativeButton("CANCEL", null)

            val dialog = builder.create()
            dialog.show()

            true
        }
        reloadListView()
    }

    private fun reloadListView(){
        val taskRealmResults = mRealm.where(Task::class.java).findAll().sort("date", Sort.DESCENDING)

        mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)

        listView1.adapter = mTaskAdapter

        mTaskAdapter.notifyDataSetChanged()
    }

    override fun onDestroy(){
        super.onDestroy()
        mRealm.close()
    }

    //    private fun addTaskForTest(){
    //        val task = Task()
    //        task.title = "作業"
    //        task.contents = "プログラムを書いてPUSHする"
    //        task.date = Date()
    //        task.id = 0
    //        mRealm.beginTransaction()
    //        mRealm.copyToRealmOrUpdate(task)
    //        mRealm.commitTransaction()
    //    }

}
