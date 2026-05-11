package kr.ac.kopo.travelplanner;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddScheduleActivity extends AppCompatActivity {

    private EditText etDate, etTime, etPlaceName, etAddress, etMemo;
    private Spinner spinnerCategory;
    private int tripId;
    private DataManager dm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_schedule);
        setTitle("일정 추가");

        dm = DataManager.getInstance();
        tripId = getIntent().getIntExtra("trip_id", -1);

        etDate        = findViewById(R.id.etDate);
        etTime        = findViewById(R.id.etTime);
        etPlaceName   = findViewById(R.id.etPlaceName);
        etAddress     = findViewById(R.id.etAddress);
        etMemo        = findViewById(R.id.etMemo);
        spinnerCategory = findViewById(R.id.spinnerCategory);

        Button btnSave   = findViewById(R.id.btnSaveSchedule);
        Button btnCancel = findViewById(R.id.btnCancelSchedule);

        btnSave.setOnClickListener(v -> {
            String date     = etDate.getText().toString().trim();
            String time     = etTime.getText().toString().trim();
            String place    = etPlaceName.getText().toString().trim();
            String address  = etAddress.getText().toString().trim();
            String memo     = etMemo.getText().toString().trim();
            String category = spinnerCategory.getSelectedItem().toString();

            if (place.isEmpty()) {
                Toast.makeText(this, "장소명을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 저장 확인 AlertDialog
            new AlertDialog.Builder(this)
                    .setTitle("일정 저장")
                    .setMessage("'" + place + "' 일정을 저장하시겠습니까?")
                    .setPositiveButton("저장", (dialog, which) -> {
                        Trip trip = dm.getTripById(tripId);
                        if (trip != null) {
                            Schedule s = new Schedule(
                                    date.isEmpty()    ? "-"         : date,
                                    time.isEmpty()    ? "-"         : time,
                                    place,
                                    address.isEmpty() ? "주소 미입력" : address,
                                    memo,
                                    category);
                            trip.addSchedule(s);
                            Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK, new Intent()); // 결과 반환
                            finish();
                        }
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });

        btnCancel.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("작성 취소")
                        .setMessage("입력 내용을 버리고 나가시겠습니까?")
                        .setPositiveButton("나가기", (dialog, which) -> finish())
                        .setNegativeButton("계속 작성", null)
                        .show()
        );
    }
}