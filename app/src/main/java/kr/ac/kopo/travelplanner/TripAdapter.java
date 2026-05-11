package kr.ac.kopo.travelplanner;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

public class TripAdapter extends ArrayAdapter<Trip> {
    private Context context;
    private List<Trip> trips;

    public TripAdapter(Context context, List<Trip> trips) {
        super(context, 0, trips);
        this.context = context;
        this.trips = trips;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.list_item_trip, parent, false);
            holder = new ViewHolder();
            holder.ivThumb = convertView.findViewById(R.id.ivTripThumb);
            holder.tvName = convertView.findViewById(R.id.tvTripName);
            holder.tvDestination = convertView.findViewById(R.id.tvTripDestination);
            holder.tvDate = convertView.findViewById(R.id.tvTripDate);
            holder.tvCount = convertView.findViewById(R.id.tvScheduleCount);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Trip trip = trips.get(position);
        holder.tvName.setText(trip.getName());
        holder.tvDestination.setText(trip.getDestination());
        holder.tvDate.setText(trip.getDateRange());
        holder.tvCount.setText(trip.getScheduleCount() + "개 일정");

        // 대표 이미지 로드 (설정된 경우)
        if (trip.getCoverImagePath() != null && !trip.getCoverImagePath().isEmpty()) {
            File imgFile = new File(trip.getCoverImagePath());
            if (imgFile.exists()) {
                holder.ivThumb.setImageBitmap(
                        BitmapFactory.decodeFile(trip.getCoverImagePath()));
            } else {
                holder.ivThumb.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            holder.ivThumb.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        return convertView;
    }

    static class ViewHolder {
        ImageView ivThumb;
        TextView tvName, tvDestination, tvDate, tvCount;
    }
}