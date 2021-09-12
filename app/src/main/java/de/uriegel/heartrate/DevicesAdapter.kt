package de.uriegel.heartrate

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter

class DevicesAdapter(private val clickListener: ((track: BluetoothDevice)->Unit))
    : Adapter<DevicesAdapter.ViewHolder>() {

    class ViewHolder(view: View, val clickListener: ((track: BluetoothDevice)->Unit)) : RecyclerView.ViewHolder(view) {
        var deviceNameView: TextView = view.findViewById(R.id.name)
        var device: BluetoothDevice? = null
        var deviceAddressView: TextView = view.findViewById(R.id.address)
        init {
            view.setOnClickListener{clickListener(device!!)}
        }
    }

    fun addDevice(device: BluetoothDevice) {
        if (!devices.any { it.address == device.address}) {
            devices.add(device)
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.device, parent, false)
        return ViewHolder(v, clickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.device = devices[position]
        holder.deviceNameView.text = devices[position].name
        holder.deviceAddressView.text = devices[position].address
    }

    override fun getItemCount(): Int {
        return devices.count()
    }

    private val devices: MutableList<BluetoothDevice> = mutableListOf()
}