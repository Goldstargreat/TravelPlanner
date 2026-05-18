package kr.ac.kopo.travelplanner;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
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
    private RadioButton radioPrivate;
    private boolean isEditorOpen = false;

    // 수정 모드일 때 해당 인덱스 저장 (-1이면 새 일기)
    private int editingIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
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
        radioPrivate      = (RadioButton) findViewById(R.id.radioPrivate);

        ImageButton btnBack = (ImageButton) findViewById(R.id.btnBackDiary);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button btnSave   = (Button) findViewById(R.id.btnSaveDiary);
        Button btnShare  = (Button) findViewById(R.id.btnShareDiary);
        Button btnCancel = (Button) findViewById(R.id.btnCancelDiary);

        refreshDiaryCards();

        // 취소 버튼
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeEditor();
            }
        });

        // 저장 버튼 (새 일기 / 수정 모두 처리)
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

                String dialogTitle = editingIndex >= 0 ? "일기 수정" : "일기 저장";
                String dialogMsg   = editingIndex >= 0 ? "일기를 수정하시겠습니까?" : "일기를 저장하시겠습니까?";

                new AlertDialog.Builder(DiaryActivity.this)
                        .setTitle(dialogTitle)
                        .setMessage(dialogMsg)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                boolean isPublic = radioPublic.isChecked();
                                String finalDate = date.isEmpty() ? "-" : date;

                                if (editingIndex >= 0) {
                                    // 수정 모드: 기존 항목 덮어쓰기
                                    DiaryEntry entry = trip.getDiaryEntries().get(editingIndex);
                                    entry.setTitle(title);
                                    entry.setDate(finalDate);
                                    entry.setContent(content);
                                    entry.setPublic(isPublic);
                                    Toast.makeText(DiaryActivity.this,
                                            "일기가 수정되었습니다.", Toast.LENGTH_SHORT).show();
                                } else {
                                    // 새 일기 추가
                                    DiaryEntry entry = new DiaryEntry(
                                            title, finalDate, content, isPublic);
                                    trip.addDiary(entry);
                                    Toast.makeText(DiaryActivity.this,
                                            getString(R.string.diary_saved),
                                            Toast.LENGTH_SHORT).show();
                                }

                                refreshDiaryCards();
                                closeEditor();
                            }
                        })
                        .setNegativeButton("취소", null)
                        .show();
            }
        });

        // 공유 버튼
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
    public void onBackPressed() {
        if (isEditorOpen) {
            closeEditor();
        } else {
            super.onBackPressed();
        }
    }

    private void closeEditor() {
        scrollDiaryEditor.setVisibility(View.GONE);
        isEditorOpen = false;
        editingIndex = -1;
    }

    private void openEditorForNew() {
        editingIndex = -1;
        etTitle.setText("");
        etDate.setText("");
        etContent.setText("");
        radioPublic.setChecked(true);
        scrollDiaryEditor.setVisibility(View.VISIBLE);
        isEditorOpen = true;
    }

    private void openEditorForEdit(int index) {
        DiaryEntry entry = trip.getDiaryEntries().get(index);
        editingIndex = index;
        etTitle.setText(entry.getTitle());
        etDate.setText(entry.getDate());
        etContent.setText(entry.getContent());
        if (entry.isPublic()) {
            radioPublic.setChecked(true);
        } else {
            radioPrivate.setChecked(true);
        }
        scrollDiaryEditor.setVisibility(View.VISIBLE);
        isEditorOpen = true;
    }

    private void refreshDiaryCards() {
        gridDiaryCards.removeAllViews();

        float density = getResources().getDisplayMetrics().density;
        int cols = 2;
        int cardSize = (int) ((getResources().getDisplayMetrics().widthPixels
                - (int)(16 * density)) / cols);
        int cardHeight = (int)(160 * density);

        for (int i = 0; i < trip.getDiaryEntries().size(); i++) {
            final int index = i;
            final DiaryEntry entry = trip.getDiaryEntries().get(i);

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(
                    (int)(10*density), (int)(10*density),
                    (int)(10*density), (int)(10*density));

            GradientDrawable cardBg = new GradientDrawable();
            cardBg.setColor(Color.WHITE);
            cardBg.setStroke((int)(1*density), Color.parseColor("#E0E0E0"));
            cardBg.setCornerRadius(8 * density);
            card.setBackground(cardBg);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width  = cardSize - (int)(8*density);
            params.height = cardHeight;
            params.setMargins(
                    (int)(4*density), (int)(4*density),
                    (int)(4*density), (int)(4*density));
            card.setLayoutParams(params);

            TextView tvCardTitle = new TextView(this);
            tvCardTitle.setText(entry.getTitle());
            tvCardTitle.setTextSize(14f);
            tvCardTitle.setTextColor(Color.parseColor("#212121"));
            tvCardTitle.setTypeface(null, Typeface.BOLD);
            tvCardTitle.setMaxLines(1);

            TextView tvCardDate = new TextView(this);
            tvCardDate.setText(entry.getDate());
            tvCardDate.setTextSize(11f);
            tvCardDate.setTextColor(Color.parseColor("#757575"));
            LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            dateParams.setMargins(0, (int)(2*density), 0, (int)(4*density));
            tvCardDate.setLayoutParams(dateParams);

            TextView tvPreview = new TextView(this);
            tvPreview.setText(entry.getPreview());
            tvPreview.setTextSize(12f);
            tvPreview.setTextColor(Color.parseColor("#757575"));
            tvPreview.setMaxLines(3);
            LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
            tvPreview.setLayoutParams(previewParams);

            TextView tvVisibility = new TextView(this);
            tvVisibility.setText(entry.isPublic() ? "공개" : "비공개");
            tvVisibility.setTextSize(11f);
            tvVisibility.setTextColor(entry.isPublic()
                    ? Color.parseColor("#1976D2")
                    : Color.parseColor("#757575"));

            card.addView(tvCardTitle);
            card.addView(tvCardDate);
            card.addView(tvPreview);
            card.addView(tvVisibility);

            // 길게 누르면 수정 / 삭제 선택 다이얼로그
            card.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    new AlertDialog.Builder(DiaryActivity.this)
                            .setTitle(entry.getTitle())
                            .setItems(new String[]{"수정하기", "삭제하기"},
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == 0) {
                                                // 수정
                                                openEditorForEdit(index);
                                            } else {
                                                // 삭제 확인
                                                new AlertDialog.Builder(DiaryActivity.this)
                                                        .setTitle("일기 삭제")
                                                        .setMessage("'" + entry.getTitle() + "' 일기를 삭제하시겠습니까?")
                                                        .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface d, int w) {
                                                                trip.removeDiary(index);
                                                                refreshDiaryCards();
                                                                Toast.makeText(DiaryActivity.this,
                                                                        getString(R.string.deleted),
                                                                        Toast.LENGTH_SHORT).show();
                                                            }
                                                        })
                                                        .setNegativeButton("취소", null)
                                                        .show();
                                            }
                                        }
                                    })
                            .show();
                    return true;
                }
            });

            gridDiaryCards.addView(card);
        }

        // + 카드
        LinearLayout addCard = new LinearLayout(this);
        addCard.setOrientation(LinearLayout.VERTICAL);
        addCard.setGravity(Gravity.CENTER);

        GradientDrawable addCardBg = new GradientDrawable();
        addCardBg.setColor(Color.WHITE);
        addCardBg.setStroke((int)(1.5f*density), Color.parseColor("#BDBDBD"));
        addCardBg.setCornerRadius(8 * density);
        addCard.setBackground(addCardBg);

        GridLayout.LayoutParams addParams = new GridLayout.LayoutParams();
        addParams.width  = cardSize - (int)(8*density);
        addParams.height = cardHeight;
        addParams.setMargins(
                (int)(4*density), (int)(4*density),
                (int)(4*density), (int)(4*density));
        addCard.setLayoutParams(addParams);

        TextView tvPlus = new TextView(this);
        tvPlus.setText("+");
        tvPlus.setTextSize(36f);
        tvPlus.setTextColor(Color.parseColor("#1976D2"));
        tvPlus.setGravity(Gravity.CENTER);
        addCard.addView(tvPlus);

        addCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditorForNew();
            }
        });

        gridDiaryCards.addView(addCard);
    }
}