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

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        dm = DataManager.getInstance();

        listViewTrips = (ListView)    findViewById(R.id.listViewTrips);
        spinnerSort   = (Spinner)     findViewById(R.id.spinnerSort);
        btnAddTrip    = (ImageButton) findViewById(R.id.btnAddTrip);
        tvEmpty       = (TextView)    findViewById(R.id.tvEmptyTrips);

        refreshList(0);

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(0xFFFFFFFF);
                }
                refreshList(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        listViewTrips.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Trip trip = currentList.get(position);
                Intent intent = new Intent(MainActivity.this, TripDetailActivity.class);
                intent.putExtra("trip_id", trip.getId());
                startActivity(intent);
            }
        });

        listViewTrips.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                final Trip trip = currentList.get(position);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("여행 삭제")
                        .setMessage("'" + trip.getName() + "' 여행을 삭제하시겠습니까?")
                        .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dm.removeTrip(trip.getId());
                                refreshList(spinnerSort.getSelectedItemPosition());
                                Toast.makeText(MainActivity.this,
                                        getString(R.string.deleted),
                                        Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("취소", null)
                        .show();
                return true;
            }
        });

        btnAddTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTripDialog();
            }
        });
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
        if (currentList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private void showAddTripDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_trip, null);
        final EditText etName  = (EditText) dialogView.findViewById(R.id.etTripName);
        final EditText etDest  = (EditText) dialogView.findViewById(R.id.etTripDestination);
        final EditText etStart = (EditText) dialogView.findViewById(R.id.etStartDate);
        final EditText etEnd   = (EditText) dialogView.findViewById(R.id.etEndDate);

        new AlertDialog.Builder(this)
                .setTitle("새 여행 추가")
                .setView(dialogView)
                .setPositiveButton("추가", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name  = etName.getText().toString().trim();
                        String dest  = etDest.getText().toString().trim();
                        String start = etStart.getText().toString().trim();
                        String end   = etEnd.getText().toString().trim();

                        if (name.isEmpty()) {
                            Toast.makeText(MainActivity.this,
                                    "여행 이름을 입력하세요.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (dest.isEmpty())  dest  = "미정";
                        if (start.isEmpty()) start = "-";
                        if (end.isEmpty())   end   = "-";

                        dm.addTrip(name, dest, start, end);
                        refreshList(spinnerSort.getSelectedItemPosition());
                        Toast.makeText(MainActivity.this,
                                "여행이 추가되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }
}