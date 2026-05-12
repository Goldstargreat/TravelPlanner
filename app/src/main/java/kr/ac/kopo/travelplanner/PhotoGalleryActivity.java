package kr.ac.kopo.travelplanner;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
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
            getSupportActionBar().setTitle("사진첩");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dm = DataManager.getInstance();
        int tripId = getIntent().getIntExtra("trip_id", -1);
        trip = dm.getTripById(tripId);

        galleryPhotos     = (Gallery)        findViewById(R.id.galleryPhotos);
        gridViewPhotos    = (GridView)       findViewById(R.id.gridViewPhotos);
        ivPhotoDetail     = (ImageView)      findViewById(R.id.ivPhotoDetail);
        layoutPhotoDetail = (RelativeLayout) findViewById(R.id.layoutPhotoDetail);
        tvNoPhotos        = (TextView)       findViewById(R.id.tvNoPhotos);
        btnAddPhoto       = (ImageButton)    findViewById(R.id.btnAddPhoto);
        btnCloseDetail    = (ImageButton)    findViewById(R.id.btnCloseDetail);
        btnDeletePhoto    = (ImageButton)    findViewById(R.id.btnDeletePhoto);

        refreshGallery();

        galleryPhotos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                gridViewPhotos.smoothScrollToPosition(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

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
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_PICK_PHOTO);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    private class GalleryPhotoAdapter extends BaseAdapter {
        @Override
        public int getCount() { return trip.getPhotoPaths().size(); }
        @Override
        public Object getItem(int position) { return trip.getPhotoPaths().get(position); }
        @Override
        public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView iv = new ImageView(PhotoGalleryActivity.this);
            iv.setLayoutParams(new Gallery.LayoutParams(180, 160));
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setPadding(4, 4, 4, 4);
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