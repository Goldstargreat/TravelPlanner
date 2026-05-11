package kr.ac.kopo.travelplanner;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.List;

public class PhotoGalleryActivity extends AppCompatActivity {

    private static final int REQUEST_PICK_PHOTO = 200;

    private Gallery galleryPhotos;
    private GridView gridViewPhotos;
    private ImageView ivPhotoDetail;
    private RelativeLayout layoutPhotoDetail;
    private TextView tvNoPhotos;
    private ImageButton btnAddPhoto, btnCloseDetail, btnDeletePhoto;

    private Trip trip;
    private DataManager dm;
    private int selectedPhotoIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_gallery);
        setTitle("사진첩");

        dm = DataManager.getInstance();
        int tripId = getIntent().getIntExtra("trip_id", -1);
        trip = dm.getTripById(tripId);

        galleryPhotos     = findViewById(R.id.galleryPhotos);
        gridViewPhotos    = findViewById(R.id.gridViewPhotos);
        ivPhotoDetail     = findViewById(R.id.ivPhotoDetail);
        layoutPhotoDetail = findViewById(R.id.layoutPhotoDetail);
        tvNoPhotos        = findViewById(R.id.tvNoPhotos);
        btnAddPhoto       = findViewById(R.id.btnAddPhoto);
        btnCloseDetail    = findViewById(R.id.btnCloseDetail);
        btnDeletePhoto    = findViewById(R.id.btnDeletePhoto);

        refreshGallery();

        // Gallery 슬라이더 선택 → GridView 스크롤 연동
        galleryPhotos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                gridViewPhotos.smoothScrollToPosition(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // GridView 클릭 → FrameLayout 오버레이로 상세 보기
        gridViewPhotos.setOnItemClickListener((parent, view, position, id) -> {
            selectedPhotoIndex = position;
            showPhotoDetail(position);
        });

        // GridView 롱클릭 → 삭제 확인
        gridViewPhotos.setOnItemLongClickListener((parent, view, position, id) -> {
            confirmDeletePhoto(position);
            return true;
        });

        // 사진 추가 버튼 → Intent ACTION_PICK으로 기기 갤러리 접근
        btnAddPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_PICK_PHOTO);
        });

        btnCloseDetail.setOnClickListener(v -> layoutPhotoDetail.setVisibility(View.GONE));

        btnDeletePhoto.setOnClickListener(v -> {
            if (selectedPhotoIndex >= 0) confirmDeletePhoto(selectedPhotoIndex);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_PHOTO && resultCode == RESULT_OK && data != null) {
            Uri selectedUri = data.getData();
            if (selectedUri != null) {
                trip.addPhoto(selectedUri.toString());
                refreshGallery();
                Toast.makeText(this, "사진이 추가되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void refreshGallery() {
        List<String> photos = trip.getPhotoPaths();
        boolean isEmpty = photos.isEmpty();
        tvNoPhotos.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        gridViewPhotos.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        gridViewPhotos.setAdapter(new PhotoGridAdapter());
        galleryPhotos.setAdapter(new GalleryPhotoAdapter());
    }

    private void showPhotoDetail(int position) {
        String path = trip.getPhotoPaths().get(position);
        try {
            Bitmap bm = getBitmapFromPath(path);
            if (bm != null) {
                ivPhotoDetail.setImageBitmap(bm);
                layoutPhotoDetail.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Toast.makeText(this, "이미지를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDeletePhoto(int index) {
        new AlertDialog.Builder(this)
                .setTitle("사진 삭제")
                .setMessage("이 사진을 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> {
                    trip.removePhoto(index);
                    layoutPhotoDetail.setVisibility(View.GONE);
                    selectedPhotoIndex = -1;
                    refreshGallery();
                    Toast.makeText(this, getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private Bitmap getBitmapFromPath(String path) {
        try {
            if (path.startsWith("content://")) {
                return MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(path));
            } else {
                return BitmapFactory.decodeFile(path);
            }
        } catch (IOException e) {
            return null;
        }
    }

    // ── GridView 어댑터 (내부 클래스) ──────────────────────────────
    private class PhotoGridAdapter extends BaseAdapter {
        @Override public int getCount()             { return trip.getPhotoPaths().size(); }
        @Override public Object getItem(int pos)    { return trip.getPhotoPaths().get(pos); }
        @Override public long getItemId(int pos)    { return pos; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView iv;
            if (convertView == null) {
                iv = new ImageView(PhotoGalleryActivity.this);
                int size = (int)(getResources().getDisplayMetrics().widthPixels / 3f);
                iv.setLayoutParams(new GridView.LayoutParams(size, size));
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                iv.setPadding(2, 2, 2, 2);
            } else {
                iv = (ImageView) convertView;
            }
            Bitmap bm = getBitmapFromPath(trip.getPhotoPaths().get(position));
            iv.setImageResource(bm != null ? 0 : android.R.drawable.ic_menu_gallery);
            if (bm != null) iv.setImageBitmap(bm);
            return iv;
        }
    }

    // ── Gallery 슬라이더 어댑터 (내부 클래스) ───────────────────────
    private class GalleryPhotoAdapter extends BaseAdapter {
        @Override public int getCount()             { return trip.getPhotoPaths().size(); }
        @Override public Object getItem(int pos)    { return trip.getPhotoPaths().get(pos); }
        @Override public long getItemId(int pos)    { return pos; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView iv = new ImageView(PhotoGalleryActivity.this);
            iv.setLayoutParams(new Gallery.LayoutParams(180, 160));
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setPadding(4, 4, 4, 4);
            Bitmap bm = getBitmapFromPath(trip.getPhotoPaths().get(position));
            if (bm != null) iv.setImageBitmap(bm);
            else iv.setImageResource(android.R.drawable.ic_menu_gallery);
            return iv;
        }
    }
}