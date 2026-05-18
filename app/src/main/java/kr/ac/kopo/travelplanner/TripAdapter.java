package kr.ac.kopo.travelplanner;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.List;

public class TripAdapter extends ArrayAdapter<Trip> {

    private static final int TYPE_TRIP = 0;
    private static final int TYPE_ADD  = 1;

    private Context context;
    private List<Trip> trips;

    public TripAdapter(Context context, List<Trip> trips) {
        super(context, 0, trips);
        this.context = context;
        this.trips = trips;
    }

    @Override
    public int getCount() {
        return trips.size() + 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == trips.size()) ? TYPE_ADD : TYPE_TRIP;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public Trip getItem(int position) {
        if (position < trips.size()) return trips.get(position);
        return null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (getItemViewType(position) == TYPE_ADD) {
            return getAddCardView(convertView, parent);
        } else {
            return getTripView(position, convertView, parent);
        }
    }

    private View getTripView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null || convertView.getTag() instanceof AddViewHolder) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.list_item_trip, parent, false);
            holder = new ViewHolder();
            holder.ivThumb       = (ImageView)   convertView.findViewById(R.id.ivTripThumb);
            holder.tvName        = (TextView)    convertView.findViewById(R.id.tvTripName);
            holder.tvDestination = (TextView)    convertView.findViewById(R.id.tvTripDestination);
            holder.tvDate        = (TextView)    convertView.findViewById(R.id.tvTripDate);
            holder.tvCount       = (TextView)    convertView.findViewById(R.id.tvScheduleCount);
            holder.btnGallery    = (ImageButton) convertView.findViewById(R.id.btnItemGallery);
            holder.btnDiary      = (ImageButton) convertView.findViewById(R.id.btnItemDiary);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Trip trip = trips.get(position);

        holder.tvName.setText(trip.getName());
        holder.tvDestination.setText(trip.getDestination());
        holder.tvDate.setText(trip.getDateRange());
        holder.tvCount.setText(trip.getScheduleCount() + "개 일정");

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

        // 갤러리 버튼 — setTag로 trip 저장해 재사용 오류 방지
        holder.btnGallery.setTag(trip);
        holder.btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Trip t = (Trip) v.getTag();
                Intent intent = new Intent(context, PhotoGalleryActivity.class);
                intent.putExtra("trip_id", t.getId());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        // 일기 버튼
        holder.btnDiary.setTag(trip);
        holder.btnDiary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Trip t = (Trip) v.getTag();
                Intent intent = new Intent(context, DiaryActivity.class);
                intent.putExtra("trip_id", t.getId());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        return convertView;
    }

    private View getAddCardView(View convertView, ViewGroup parent) {
        if (convertView == null || !(convertView.getTag() instanceof AddViewHolder)) {
            float density = context.getResources().getDisplayMetrics().density;

            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setGravity(Gravity.CENTER);
            layout.setPadding(
                    (int)(16 * density), (int)(20 * density),
                    (int)(16 * density), (int)(20 * density));
            layout.setBackgroundColor(Color.WHITE);

            TextView tvPlus = new TextView(context);
            tvPlus.setText("일정표 +");
            tvPlus.setTextSize(16f);
            tvPlus.setTextColor(Color.parseColor("#1976D2"));
            tvPlus.setTypeface(null, Typeface.BOLD);
            tvPlus.setGravity(Gravity.CENTER);

            layout.addView(tvPlus);
            layout.setTag(new AddViewHolder());
            convertView = layout;
        }
        return convertView;
    }

    static class ViewHolder {
        ImageView ivThumb;
        TextView tvName;
        TextView tvDestination;
        TextView tvDate;
        TextView tvCount;
        ImageButton btnGallery;
        ImageButton btnDiary;
    }

    static class AddViewHolder { }
}