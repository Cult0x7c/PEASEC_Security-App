package com.peasec.securityapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.maps.android.SphericalUtil;
import com.peasec.securityapp.Activities.Activity_DetailEvent;
import com.peasec.securityapp.Activities.Activity_EventList;
import com.peasec.securityapp.Objects.Event;
import com.peasec.securityapp.R;

import java.text.DecimalFormat;
import java.util.List;

public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.EventHolder>{
    private List<Event> eventList;
    private Context context;
    private LatLng myLocation;

    public EventListAdapter(Context context,List<Event> eventList,LatLng myLoc){
        this.context = context;
        this.eventList=eventList;
        this.myLocation= myLoc;
    }

    @NonNull
    @Override
    public EventHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventListAdapter.EventHolder(view);
    }

    // Inflates the appropriate layout according to the ViewType.
    @Override
    public void onBindViewHolder(@NonNull EventHolder holder, int position) {
        Event event = eventList.get(position);
        ((EventHolder) holder).bind(event);
    }

    @Override
    public int getItemCount() {
        return (eventList == null) ? 0 : this.eventList.size();
    }


    protected class EventHolder extends ViewHolder {
        TextView tvCategory, tvDescription, tvDistance;
        ImageView ivThumbnail;

        private EventHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = (TextView) itemView.findViewById(R.id.tvCategory);
            tvDescription = (TextView) itemView.findViewById(R.id.tvDescription);
            tvDistance = (TextView) itemView.findViewById(R.id.tvDistance);
            ivThumbnail = (ImageView) itemView.findViewById(R.id.ivThumbnail);

        }

        private void bind(Event event) {
            //fill in individual data from event
            tvCategory.setText(event.getType());
            tvDescription.setText (event.getContent());
            tvDistance.setText(calculateDistance(event,myLocation));
            //decode b64 to bitmap and set image to thumbnail image view
            if(!event.getImage1().equals("")) {
                byte[] decodedImage1 = Base64.decode(event.getImage1(), Base64.DEFAULT);
                Bitmap bitmapImage1 = BitmapFactory.decodeByteArray(decodedImage1, 0, decodedImage1.length);
                ivThumbnail.setImageBitmap(bitmapImage1);
            }

            //serialize event object and open activity detail event
            itemView.setOnClickListener(v-> {
                Intent i = new Intent(context, Activity_DetailEvent.class);
                Gson gson = new Gson();
                String jsonEvent = gson.toJson(event);
                i.putExtra("JsonEvent", jsonEvent);
                context.startActivity(i);
            });
        }

        private String calculateDistance(Event event, LatLng myLocation) {
            if (myLocation == null){
                return "Distance to event";
            }
            DecimalFormat df = new DecimalFormat("0.00");
            LatLng eventLocation = new LatLng(event.getLat(),event.getLng());
            Double distance = SphericalUtil.computeDistanceBetween(myLocation, eventLocation);
            return df.format(distance) + " km";
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
