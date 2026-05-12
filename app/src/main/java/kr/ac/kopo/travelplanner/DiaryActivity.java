package kr.ac.kopo.travelplanner;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DiaryActivity extends AppCompatActivity {

    private Trip trip;
    private DataManager dm;
    private GridLayout gridDiaryCards;
    private ScrollView scrollDiaryEditor;
    private EditText etTitle;
    private EditText etDate;
    private EditText etContent;
    private RadioButton radioPublic;
    private ImageButton btnNewDiary;
    private boolean isEditorOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("여행 일기");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dm = DataManager.getInstance();
        int tripId = getIntent().getIntExtra("trip_id", -1);
        trip = dm.getTripById(tripId);

        gridDiaryCards    = (GridLayout)  findViewById(R.id.gridDiaryCards);
        scrollDiaryEditor = (ScrollView)  findViewById(R.id.scrollDiaryEditor);
        etTitle           = (EditText)    findViewById(R.id.etDiaryTitle);
        etDate            = (EditText)    findViewById(R.id.etDiaryDate);
        etContent         = (EditText)    findViewById(R.id.etDiaryContent);
        radioPublic       = (RadioButton) findViewById(R.id.radioPublic);
        btnNewDiary       = (ImageButton) findViewById(R.id.btnNewDiary);

        Button btnSave  = (Button) findViewById(R.id.btnSaveDiary);
        Button btnShare = (Button) findViewById(R.id.btnShareDiary);

        refreshDiaryCards();

        btnNewDiary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditorOpen) {
                    scrollDiaryEditor.setVisibility(View.GONE);
                    isEditorOpen = false;
                } else {
                    clearEditor();
                    scrollDiaryEditor.setVisibility(View.VISIBLE);
                    isEditorOpen = true;
                }
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String title   = etTitle.getText().toString().trim();
                final String date    = etDate.getText().toString().trim();
                final String content = etContent.getText().toString().trim();

                if (title.isEmpty() || content.isEmpty()) {
                    Toast.makeText(DiaryActivity.this,
                            "제목과 내용을 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                new AlertDialog.Builder(DiaryActivity.this)
                        .setTitle("일기 저장")
                        .setMessage("일기를 저장하시겠습니까?")
                        .setPositiveButton("저장", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                boolean isPublic = radioPublic.isChecked();
                                String finalDate = date.isEmpty() ? "-" : date;
                                DiaryEntry entry = new DiaryEntry(
                                        title, finalDate, content, isPublic);
                                trip.addDiary(entry);
                                refreshDiaryCards();
                                scrollDiaryEditor.setVisibility(View.GONE);
                                isEditorOpen = false;
                                Toast.makeText(DiaryActivity.this,
                                        getString(R.string.diary_saved),
                                        Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("취소", null)
                        .show();
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title   = etTitle.getText().toString();
                String content = etContent.getText().toString();
                if (content.isEmpty()) {
                    Toast.makeText(DiaryActivity.this,
                            "공유할 내용을 먼저 작성하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "[여행일기] " + title);
                shareIntent.putExtra(Intent.EXTRA_TEXT,
                        "[" + trip.getName() + " 여행일기]\n\n" + content);
                startActivity(Intent.createChooser(shareIntent, "일기 공유하기"));
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshDiaryCards() {
        gridDiaryCards.removeAllViews();
        if (trip.getDiaryEntries().isEmpty()) {
            return;
        }

        int cols = 2;
        float density = getResources().getDisplayMetrics().density;
        int cardSize = (int) ((getResources().getDisplayMetrics().widthPixels
                - (int) (16 * density)) / cols);

        for (int i = 0; i < trip.getDiaryEntries().size(); i++) {
            final int index = i;
            final DiaryEntry entry = trip.getDiaryEntries().get(i);

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(
                    (int)(8*density), (int)(8*density),
                    (int)(8*density), (int)(8*density));
            card.setBackgroundColor(Color.WHITE);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width  = cardSize - (int)(8*density);
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.setMargins(
                    (int)(4*density), (int)(4*density),
                    (int)(4*density), (int)(4*density));
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
            if (entry.isPublic()) {
                tvVisibility.setTextColor(Color.parseColor("#1976D2"));
            } else {
                tvVisibility.setTextColor(Color.parseColor("#757575"));
            }

            card.addView(tvCardTitle);
            card.addView(tvCardDate);
            card.addView(tvPreview);
            card.addView(tvVisibility);

            card.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    new AlertDialog.Builder(DiaryActivity.this)
                            .setTitle("일기 삭제")
                            .setMessage("'" + entry.getTitle() + "' 일기를 삭제하시겠습니까?")
                            .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    trip.removeDiary(index);
                                    refreshDiaryCards();
                                    Toast.makeText(DiaryActivity.this,
                                            getString(R.string.deleted),
                                            Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("취소", null)
                            .show();
                    return true;
                }
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