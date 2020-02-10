package com.example.attempt_feb6

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import java.util.*
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.util.Log
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        BluetoothServerController(this, textView).start()

    }

    val uuid: UUID= UUID.fromString("8989063a-c9af-463a-b3f1-f21d9b2b827b")

    class BluetoothServerController(activity: MainActivity, textview: TextView) : Thread() {
        private var cancelled: Boolean
        private val serverSocket: BluetoothServerSocket?
        private val activity = activity
        val uuid: UUID= UUID.fromString("8989063a-c9af-463a-b3f1-f21d9b2b827b")
        val textV : TextView = textview



        init {
            val btAdapter = BluetoothAdapter.getDefaultAdapter()
            if (btAdapter != null) {
                this.serverSocket = btAdapter.listenUsingRfcommWithServiceRecord("test", uuid) // 1
                this.cancelled = false
            } else {
                this.serverSocket = null
                this.cancelled = true
            }

        }


        override fun run() {
            var socket: BluetoothSocket

            while(true) {
                if (this.cancelled) {
                    break
                }

                try {
                    socket = serverSocket!!.accept()  // 2
                } catch(e: IOException) {
                    break
                }

                if (!this.cancelled && socket != null) {
                    Log.i("server", "Connecting")
                    BluetoothServer(this.activity, socket, textV).start() // 3
                }
            }
        }

        fun cancel() {
            this.cancelled = true
            this.serverSocket!!.close()
        }
    }



    class BluetoothServer(private val activity: MainActivity, private val socket: BluetoothSocket, private val textview : TextView): Thread() {
        private val inputStream = this.socket.inputStream
        private val outputStream = this.socket.outputStream
        val textV : TextView = textview


        override fun run() {
            try {
                val available = inputStream.available()
                val bytes = ByteArray(available)
                Log.i("server", "Reading")
                inputStream.read(bytes, 0, available)
                val text = String(bytes)
                Log.i("server", "Message received")
                Log.i("server", text)

                textV.text = text
                //activity.appendText(text)
            } catch (e: Exception) {
                Log.e("client", "Cannot read data", e)
            } finally {
                inputStream.close()
                outputStream.close()
                socket.close()
            }
        }
    }



}
