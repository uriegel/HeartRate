package de.uriegel.heartrate

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter

class DevicesAdapter()
    : Adapter<DevicesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var deviceNameView: TextView = view.findViewById(R.id.name)
        var deviceAddressView: TextView = view.findViewById(R.id.address)
    }

    fun addDevice(device: BluetoothDevice) {
        if (!devices.contains(device)) {
            devices.add(device)
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.device, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.deviceNameView.text = devices[position].name
        holder.deviceAddressView.text = devices[position].address
    }

    override fun getItemCount(): Int {
        return devices.count()
    }

    private val devices: MutableList<BluetoothDevice> = mutableListOf()
}