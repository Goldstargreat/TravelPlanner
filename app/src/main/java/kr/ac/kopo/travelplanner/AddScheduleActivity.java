package kr.ac.kopo.travelplanner;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddScheduleActivity extends AppCompatActivity {

    private EditText etDate;
    private EditText etTime;
    private EditText etPlaceName;
    private EditText etAddress;
    private EditText etMemo;
    private Spinner spinnerCategory;
    private int tripId;
    private DataManager dm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_schedule);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("일정 추가");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dm = DataManager.getInstance();
        tripId = getIntent().getIntExtra("trip_id", -1);

        etDate          = (EditText) findViewById(R.id.etDate);
        etTime          = (EditText) findViewById(R.id.etTime);
        etPlaceName     = (EditText) findViewById(R.id.etPlaceName);
        etAddress       = (EditText) findViewById(R.id.etAddress);
        etMemo          = (EditText) findViewById(R.id.etMemo);
        spinnerCategory = (Spinner)  findViewById(R.id.spinnerCategory);

        Button btnSave   = (Button) findViewById(R.id.btnSaveSchedule);
        Button btnCancel = (Button) findViewById(R.id.btnCancelSchedule);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String date     = etDate.getText().toString().trim();
                final String time     = etTime.getText().toString().trim();
                final String place    = etPlaceName.getText().toString().trim();
                final String address  = etAddress.getText().toString().trim();
                final String memo     = etMemo.getText().toString().trim();
                final String category = spinnerCategory.getSelectedItem().toString();

                if (place.isEmpty()) {
                    Toast.makeText(AddScheduleActivity.this,
                            "장소명을 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                new AlertDialog.Builder(AddScheduleActivity.this)
                        .setTitle("일정 저장")
                        .setMessage("'" + place + "' 일정을 저장하시겠습니까?")
                        .setPositiveButton("저장", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Trip trip = dm.getTripById(tripId);
                                if (trip != null) {
                                    String finalDate    = date.isEmpty()    ? "-"         : date;
                                    String finalTime    = time.isEmpty()    ? "-"         : time;
                                    String finalAddress = address.isEmpty() ? "주소 미입력" : address;

                                    Schedule s = new Schedule(
                                            finalDate, finalTime, place,
                                            finalAddress, memo, category);
                                    trip.addSchedule(s);
                                    Toast.makeText(AddScheduleActivity.this,
                                            getString(R.string.saved),
                                            Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK, new Intent());
                                    finish();
                                }
                            }
                        })
                        .setNegativeButton("취소", null)
                        .show();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCancelDialog();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            showCancelDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showCancelDialog() {
        new AlertDialog.Builder(this)
                .setTitle("작성 취소")
                .setMessage("입력 내용을 버리고 나가시겠습니까?")
                .setPositiveButton("나가기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("계속 작성", null)
                .show();
    }
}