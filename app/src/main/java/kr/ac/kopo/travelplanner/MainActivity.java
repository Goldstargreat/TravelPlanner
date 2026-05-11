package kr.ac.kopo.travelplanner;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView listViewTrips;
    private TripAdapter adapter;
    private Spinner spinnerSort;
    private ImageButton btnAddTrip;
    private TextView tvEmpty;
    private DataManager dm;
    private List<Trip> currentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("여행 플래너");

        dm = DataManager.getInstance();

        listViewTrips = findViewById(R.id.listViewTrips);
        spinnerSort = findViewById(R.id.spinnerSort);
        btnAddTrip = findViewById(R.id.btnAddTrip);
        tvEmpty = findViewById(R.id.tvEmptyTrips);

        refreshList(0);

        // Spinner: 정렬 기준 변경
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) view).setTextColor(0xFFFFFFFF);
                refreshList(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // ListView 아이템 클릭 → TripDetailActivity
        listViewTrips.setOnItemClickListener((parent, view, position, id) -> {
            Trip trip = currentList.get(position);
            Intent intent = new Intent(MainActivity.this, TripDetailActivity.class);
            intent.putExtra("trip_id", trip.getId());
            startActivity(intent);
        });

        // ListView 아이템 롱클릭 → 삭제 AlertDialog
        listViewTrips.setOnItemLongClickListener((parent, view, position, id) -> {
            Trip trip = currentList.get(position);
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("여행 삭제")
                    .setMessage("'" + trip.getName() + "' 여행을 삭제하시겠습니까?")
                    .setPositiveButton("삭제", (dialog, which) -> {
                        dm.removeTrip(trip.getId());
                        refreshList(spinnerSort.getSelectedItemPosition());
                        Toast.makeText(MainActivity.this,
                                getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("취소", null)
                    .show();
            return true;
        });

        // FAB 버튼 → 새 여행 추가 다이얼로그
        btnAddTrip.setOnClickListener(v -> showAddTripDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList(spinnerSort.getSelectedItemPosition());
    }

    private void refreshList(int sortType) {
        currentList = dm.getSortedTrips(sortType);
        adapter = new TripAdapter(this, currentList);
        listViewTrips.setAdapter(adapter);
        tvEmpty.setVisibility(currentList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showAddTripDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_trip, null);
        EditText etName = dialogView.findViewById(R.id.etTripName);
        EditText etDest = dialogView.findViewById(R.id.etTripDestination);
        EditText etStart = dialogView.findViewById(R.id.etStartDate);
        EditText etEnd = dialogView.findViewById(R.id.etEndDate);

        new AlertDialog.Builder(this)
                .setTitle("새 여행 추가")
                .setView(dialogView)
                .setPositiveButton("추가", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String dest = etDest.getText().toString().trim();
                    String start = etStart.getText().toString().trim();
                    String end = etEnd.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(this, "여행 이름을 입력하세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dm.addTrip(name, dest.isEmpty() ? "미정" : dest,
                            start.isEmpty() ? "-" : start,
                            end.isEmpty() ? "-" : end);
                    refreshList(spinnerSort.getSelectedItemPosition());
                    Toast.makeText(this, "여행이 추가되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("취소", null)
                .show();
    }
}