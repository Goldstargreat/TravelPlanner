package kr.ac.kopo.travelplanner;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DiaryActivity extends AppCompatActivity {

    private Trip trip;
    private DataManager dm;
    private GridLayout gridDiaryCards;
    private ScrollView scrollDiaryEditor;
    private EditText etTitle, etDate, etContent;
    private RadioGroup radioGroupVisibility;
    private RadioButton radioPublic, radioPrivate;
    private ImageButton btnNewDiary;
    private boolean isEditorOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);
        setTitle("여행 일기");

        dm = DataManager.getInstance();
        int tripId = getIntent().getIntExtra("trip_id", -1);
        trip = dm.getTripById(tripId);

        gridDiaryCards    = findViewById(R.id.gridDiaryCards);
        scrollDiaryEditor = findViewById(R.id.scrollDiaryEditor);
        etTitle           = findViewById(R.id.etDiaryTitle);
        etDate            = findViewById(R.id.etDiaryDate);
        etContent         = findViewById(R.id.etDiaryContent);
        radioGroupVisibility = findViewById(R.id.radioGroupVisibility);
        radioPublic       = findViewById(R.id.radioPublic);
        radioPrivate      = findViewById(R.id.radioPrivate);
        btnNewDiary       = findViewById(R.id.btnNewDiary);

        Button btnSave  = findViewById(R.id.btnSaveDiary);
        Button btnShare = findViewById(R.id.btnShareDiary);

        refreshDiaryCards();

        // 새 일기 작성 버튼 → ScrollView 에디터 토글
        btnNewDiary.setOnClickListener(v -> {
            if (isEditorOpen) {
                scrollDiaryEditor.setVisibility(View.GONE);
                isEditorOpen = false;
            } else {
                clearEditor();
                scrollDiaryEditor.setVisibility(View.VISIBLE);
                isEditorOpen = true;
            }
        });

        // 저장 버튼 → AlertDialog 확인 후 저장
        btnSave.setOnClickListener(v -> {
            String title   = etTitle.getText().toString().trim();
            String date    = etDate.getText().toString().trim();
            String content = etContent.getText().toString().trim();

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "제목과 내용을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(this)
                    .setTitle("일기 저장")
                    .setMessage("일기를 저장하시겠습니까?")
                    .setPositiveButton("저장", (dialog, which) -> {
                        boolean isPublic = radioPublic.isChecked(); // CompoundButton 확인
                        DiaryEntry entry = new DiaryEntry(title,
                                date.isEmpty() ? "-" : date, content, isPublic);
                        trip.addDiary(entry);
                        refreshDiaryCards();
                        scrollDiaryEditor.setVisibility(View.GONE);
                        isEditorOpen = false;
                        Toast.makeText(this, getString(R.string.diary_saved), Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });

        // 공유 버튼 → Intent ACTION_SEND
        btnShare.setOnClickListener(v -> {
            String title   = etTitle.getText().toString();
            String content = etContent.getText().toString();
            if (content.isEmpty()) {
                Toast.makeText(this, "공유할 내용을 먼저 작성하세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "[여행일기] " + title);
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    "[" + trip.getName() + " 여행일기]\n\n" + content);
            startActivity(Intent.createChooser(shareIntent, "일기 공유하기"));
        });
    }

    private void refreshDiaryCards() {
        gridDiaryCards.removeAllViews();
        if (trip.getDiaryEntries().isEmpty()) return;

        int cols = 2;
        float density = getResources().getDisplayMetrics().density;
        int cardSize = (int)((getResources().getDisplayMetrics().widthPixels
                - (int)(16 * density)) / cols);

        for (int i = 0; i < trip.getDiaryEntries().size(); i++) {
            final int index = i;
            DiaryEntry entry = trip.getDiaryEntries().get(i);

            // GridLayout 카드 (LinearLayout)
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding((int)(8*density),(int)(8*density),(int)(8*density),(int)(8*density));
            card.setBackgroundColor(Color.WHITE);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width  = cardSize - (int)(8*density);
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.setMargins((int)(4*density),(int)(4*density),(int)(4*density),(int)(4*density));
            card.setLayoutParams(params);

            TextView tvCardTitle = new TextView(this);
            tvCardTitle.setText(entry.getTitle());
            tvCardTitle.setTextSize(14f);
            tvCardTitle.setTextColor(Color.parseColor("#212121"));

            TextView tvCardDate = new TextView(this);
            tvCardDate.setText(entry.getDate());
            tvCardDate.setTextSize(11f);
            tvCardDate.setTextColor(Color.parseColor("#757575"));

            TextView tvPreview = new TextView(this);
            tvPreview.setText(entry.getPreview());
            tvPreview.setTextSize(12f);
            tvPreview.setTextColor(Color.parseColor("#757575"));
            tvPreview.setMaxLines(3);

            TextView tvVisibility = new TextView(this);
            tvVisibility.setText(entry.isPublic() ? "공개" : "비공개");
            tvVisibility.setTextSize(11f);
            tvVisibility.setTextColor(entry.isPublic()
                    ? Color.parseColor("#1976D2") : Color.parseColor("#757575"));

            card.addView(tvCardTitle);
            card.addView(tvCardDate);
            card.addView(tvPreview);
            card.addView(tvVisibility);

            // 롱클릭 → 일기 삭제 AlertDialog
            card.setOnLongClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("일기 삭제")
                        .setMessage("'" + entry.getTitle() + "' 일기를 삭제하시겠습니까?")
                        .setPositiveButton("삭제", (dialog, which) -> {
                            trip.removeDiary(index);
                            refreshDiaryCards();
                            Toast.makeText(this, getString(R.string.deleted),
                                    Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("취소", null)
                        .show();
                return true;
            });

            gridDiaryCards.addView(card);
        }
    }

    private void clearEditor() {
        etTitle.setText("");
        etDate.setText("");
        etContent.setText("");
        radioPublic.setChecked(true);
    }
}