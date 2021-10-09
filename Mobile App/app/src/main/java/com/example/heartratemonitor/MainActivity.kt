package com.example.heartratemonitor

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import android.widget.ArrayAdapter
import androidx.test.core.app.ApplicationProvider

import androidx.test.core.app.ApplicationProvider.getApplicationContext




private const val REQUEST_ENABLE_BT = 1

// Initializing the Adapter for bluetooth
private var BluetoothAdap: BluetoothAdapter? = null
private var Devices = null

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        BluetoothAdap = BluetoothAdapter.getDefaultAdapter();
    }

    private fun pairedDevices() {
        Devices = BluetoothAdap!!.bondedDevices
        val list = ArrayList<Any>()
        if ((Devices as MutableSet<BluetoothDevice>?)?.size ?:  > {
            0
        }) {
            for (bt in Devices) {
                // Add all the available devices to the list
                list.add(bt.name + "\n" + bt.address)
            }
        } else {
            // In case no device is found
            Toast.makeText(
                ApplicationProvider.getApplicationContext<Context>(),
                "No Paired Bluetooth Devices Found.",
                Toast.LENGTH_LONG
            ).show()
        }

        // Adding the devices to the list with ArrayAdapter class
        val adapter: ArrayAdapter<*> = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
        devicelist.setAdapter(adapter)

        // Method called when the device from the list is clicked
        devicelist.setOnItemClickListener(myListListener)
    }

}

// Method to fill the list with devices
