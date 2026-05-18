package kr.ac.kopo.travelplanner;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.List;

public class PhotoGalleryActivity extends AppCompatActivity {

    private static final int REQUEST_PICK_PHOTO       = 200;
    private static final int REQUEST_PERMISSION_READ  = 300;

    private GridView gridViewPhotos;
    private ImageView ivPhotoDetail;
    private RelativeLayout layoutPhotoDetail;
    private TextView tvNoPhotos;
    private ImageButton btnAddPhoto;
    private ImageButton btnCloseDetail;
    private ImageButton btnDeletePhoto;

    private Trip trip;
    private DataManager dm;
    private int selectedPhotoIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_gallery);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        dm = DataManager.getInstance();
        int tripId = getIntent().getIntExtra("trip_id", -1);
        trip = dm.getTripById(tripId);

        ImageButton btnBack = (ImageButton) findViewById(R.id.btnBackGallery);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        gridViewPhotos    = (GridView)       findViewById(R.id.gridViewPhotos);
        ivPhotoDetail     = (ImageView)      findViewById(R.id.ivPhotoDetail);
        layoutPhotoDetail = (RelativeLayout) findViewById(R.id.layoutPhotoDetail);
        tvNoPhotos        = (TextView)       findViewById(R.id.tvNoPhotos);
        btnAddPhoto       = (ImageButton)    findViewById(R.id.btnAddPhoto);
        btnCloseDetail    = (ImageButton)    findViewById(R.id.btnCloseDetail);
        btnDeletePhoto    = (ImageButton)    findViewById(R.id.btnDeletePhoto);

        refreshGallery();

        gridViewPhotos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedPhotoIndex = position;
                showPhotoDetail(position);
            }
        });

        gridViewPhotos.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                confirmDeletePhoto(position);
                return true;
            }
        });

        btnAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionAndPickPhoto();
            }
        });

        btnCloseDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutPhotoDetail.setVisibility(View.GONE);
            }
        });

        btnDeletePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedPhotoIndex >= 0) {
                    confirmDeletePhoto(selectedPhotoIndex);
                }
            }
        });
    }

    // 권한 확인 후 갤러리 열기
    private void checkPermissionAndPickPhoto() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 이상
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            // Android 12 이하
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_GRANTED) {
            openPhotoPicker();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{permission}, REQUEST_PERMISSION_READ);
        }
    }

    private void openPhotoPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_PHOTO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_READ) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openPhotoPicker();
            } else {
                Toast.makeText(this,
                        "사진 접근 권한이 필요합니다. 설정에서 권한을 허용해주세요.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (layoutPhotoDetail.getVisibility() == View.VISIBLE) {
            layoutPhotoDetail.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
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
    }

    private void showPhotoDetail(int position) {
        String path = trip.getPhotoPaths().get(position);
        Bitmap bm = getBitmapFromPath(path);
        if (bm != null) {
            ivPhotoDetail.setImageBitmap(bm);
            layoutPhotoDetail.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "이미지를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDeletePhoto(final int index) {
        new AlertDialog.Builder(this)
                .setTitle("사진 삭제")
                .setMessage("이 사진을 삭제하시겠습니까?")
                .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        trip.removePhoto(index);
                        layoutPhotoDetail.setVisibility(View.GONE);
                        selectedPhotoIndex = -1;
                        refreshGallery();
                        Toast.makeText(PhotoGalleryActivity.this,
                                getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private Bitmap getBitmapFromPath(String path) {
        try {
            if (path.startsWith("content://")) {
                return MediaStore.Images.Media.getBitmap(
                        getContentResolver(), Uri.parse(path));
            } else {
                return BitmapFactory.decodeFile(path);
            }
        } catch (IOException e) {
            return null;
        }
    }

    private class PhotoGridAdapter extends BaseAdapter {
        @Override
        public int getCount() { return trip.getPhotoPaths().size(); }
        @Override
        public Object getItem(int position) { return trip.getPhotoPaths().get(position); }
        @Override
        public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView iv;
            if (convertView == null) {
                iv = new ImageView(PhotoGalleryActivity.this);
                int size = (int) (getResources().getDisplayMetrics().widthPixels / 3f);
                iv.setLayoutParams(new GridView.LayoutParams(size, size));
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                iv.setPadding(2, 2, 2, 2);
            } else {
                iv = (ImageView) convertView;
            }
            Bitmap bm = getBitmapFromPath(trip.getPhotoPaths().get(position));
            if (bm != null) {
                iv.setImageBitmap(bm);
            } else {
                iv.setImageResource(android.R.drawable.ic_menu_gallery);
            }
            return iv;
        }
    }
}