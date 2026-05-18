package kr.ac.kopo.travelplanner;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class TripDetailActivity extends AppCompatActivity {

    private Trip trip;
    private DataManager dm;
    private TableLayout tableSchedules;
    private TextView tvEmpty;
    private TextView tvDetailTripName;
    private TextView tvDetailDestination;
    private TextView tvDetailDate;
    private ImageView ivTripCover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);

        dm = DataManager.getInstance();
        int tripId = getIntent().getIntExtra("trip_id", -1);
        trip = dm.getTripById(tripId);

        if (trip == null) {
            Toast.makeText(this, "여행 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        tvDetailTripName    = (TextView)    findViewById(R.id.tvDetailTripName);
        tvDetailDestination = (TextView)    findViewById(R.id.tvDetailDestination);
        tvDetailDate        = (TextView)    findViewById(R.id.tvDetailDate);
        ivTripCover         = (ImageView)   findViewById(R.id.ivTripCover);
        tableSchedules      = (TableLayout) findViewById(R.id.tableSchedules);
        tvEmpty             = (TextView)    findViewById(R.id.tvEmptySchedules);

        tvDetailTripName.setText(trip.getName());
        tvDetailDestination.setText(trip.getDestination());
        tvDetailDate.setText(trip.getDateRange());

        ImageButton btnBack = (ImageButton) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ImageButton btnGallery = (ImageButton) findViewById(R.id.btnGoGallery);
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TripDetailActivity.this, PhotoGalleryActivity.class);
                intent.putExtra("trip_id", trip.getId());
                startActivity(intent);
            }
        });

        ImageButton btnDiary = (ImageButton) findViewById(R.id.btnGoDiary);
        btnDiary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TripDetailActivity.this, DiaryActivity.class);
                intent.putExtra("trip_id", trip.getId());
                startActivity(intent);
            }
        });

        refreshTable();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTable();
    }

    private void refreshTable() {
        int childCount = tableSchedules.getChildCount();
        if (childCount > 1) {
            tableSchedules.removeViews(1, childCount - 1);
        }

        List<Schedule> schedules = trip.getSchedules();
        if (schedules.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }
        tvEmpty.setVisibility(View.GONE);

        for (int i = 0; i < schedules.size(); i++) {
            addScheduleRow(schedules.get(i), i);
        }
    }

    private void addScheduleRow(final Schedule s, final int index) {
        TableRow row = new TableRow(this);
        row.setPadding(4, 4, 4, 4);
        row.setBackgroundColor(index % 2 == 0 ? Color.WHITE : 0xFFF5F5F5);

        TextView tvDate = makeCell(s.getDate(), 60);
        tvDate.setTextSize(11);

        TextView tvTime = makeCell(s.getTime(), 50);
        tvTime.setTextSize(11);

        TextView tvPlace = new TextView(this);
        tvPlace.setText(s.getPlaceName() + "\n" + s.getAddress());
        tvPlace.setTextSize(12);
        tvPlace.setPadding(6, 6, 6, 6);
        tvPlace.setTextColor(Color.parseColor("#212121"));
        TableRow.LayoutParams placeParams = new TableRow.LayoutParams(
                0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        tvPlace.setLayoutParams(placeParams);

        CheckBox cbDone = new CheckBox(this);
        cbDone.setChecked(s.isCompleted());
        cbDone.setPadding(6, 6, 6, 6);
        cbDone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                s.setCompleted(isChecked);
            }
        });

        row.addView(tvDate);
        row.addView(tvTime);
        row.addView(tvPlace);
        row.addView(cbDone);

        row.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(TripDetailActivity.this)
                        .setTitle("일정 삭제")
                        .setMessage("'" + s.getPlaceName() + "' 일정을 삭제하시겠습니까?")
                        .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                trip.removeSchedule(index);
                                refreshTable();
                                Toast.makeText(TripDetailActivity.this,
                                        getString(R.string.deleted),
                                        Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("취소", null)
                        .show();
                return true;
            }
        });

        tableSchedules.addView(row);
    }

    private TextView makeCell(String text, int widthDp) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(6, 6, 6, 6);
        tv.setTextColor(Color.parseColor("#212121"));
        float density = getResources().getDisplayMetrics().density;
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                (int) (widthDp * density), TableRow.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(params);
        return tv;
    }
}