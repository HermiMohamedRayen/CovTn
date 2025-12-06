package com.iset.covtn.ui.home

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iset.covtn.databinding.RideItemBinding
import com.iset.covtn.models.GeoLocation
import com.iset.covtn.models.Ride
import com.iset.covtn.models.User
import java.text.DateFormat
import java.text.SimpleDateFormat

class RecycleViewAdapter(
    val rides : MutableList<Ride>,
    val getAdress : (Ride) -> Unit,
    val viewDetails : (Ride) -> Unit
) : RecyclerView.Adapter<RecycleViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = RideItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    val dateconv = DateFormat.getDateTimeInstance()

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        holder.bind(rides[position])

    }


    override fun getItemCount(): Int {
        return rides.size
    }

    fun updateList(newRides : List<Ride>){
        rides.clear()
        rides.addAll(newRides)
        notifyDataSetChanged()
    }


    inner class ViewHolder(
        val binding : RideItemBinding
    ) : RecyclerView.ViewHolder(binding.root){

        init {
            itemView.setOnClickListener {
                if(adapterPosition != RecyclerView.NO_POSITION){
                    viewDetails(rides[adapterPosition])
                }
            }
        }
        fun bind(ride : Ride){
            val adr1 = addressMap[ride.departure]   ?: "loading ..."
            val  adr2 = addressMap[ride.destination]   ?: "loading ..."
            if(adr1.equals("loading ...") || adr2.equals("loading ...")){
                getAdress(ride)
            }

            binding.dep.text = adr1
            binding.dest.text = adr2
            binding.depTime.text = dateconv.format(ride.departureTime)
            binding.arrivalTime.text = dateconv.format(ride.arrivalTime)

            binding.avplaces.text = (ride.driver.car?.seats?.minus(ride.rideParticipations.size)).toString()

        }

    }




    private val addressMap = mutableMapOf<GeoLocation, String>()

    fun updateAddresses(newMap: Map<GeoLocation, String>) {
        // find which keys changed
        val changedKeys = newMap.filter { (k, v) ->
            addressMap[k] != v
        }.keys



        if (changedKeys.isEmpty()) return

        // notify only the affected rows
        rides.forEachIndexed { index, ride ->
            if (ride.departure in changedKeys || ride.destination in changedKeys) {
                addressMap.putAll(newMap)
                notifyItemChanged(index)
            }
        }
    }

}