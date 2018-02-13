package com.jlanka.jltripplanner.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jlanka.jltripplanner.R;

import java.util.List;

/**
 * Created by chathura on 2/7/18.
 */

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.MyViewHolder>{

    private List<Vehicle> vehicleList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView user_id, regNo, vin, model, year;

        public MyViewHolder(View view) {
            super(view);
            regNo = (TextView) view.findViewById(R.id.reg_no);
            vin = (TextView) view.findViewById(R.id.vin);
            model = (TextView) view.findViewById(R.id.modal);
            year = (TextView) view.findViewById(R.id.year);
        }
    }

    public VehicleAdapter(List<Vehicle> vehicleList) {
        this.vehicleList = vehicleList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.vehicle_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Vehicle vehicle = vehicleList.get(position);
        holder.regNo.setText(vehicle.getRegNo());
        holder.vin.setText(vehicle.getVin());
        holder.model.setText(vehicle.getModel());
        holder.year.setText(vehicle.getYear());
    }

    @Override
    public int getItemCount() {
        return vehicleList.size();
    }

    public void addVehicle(Vehicle vehicle){
        vehicleList.add(vehicle);
        notifyDataSetChanged();
    }


}
