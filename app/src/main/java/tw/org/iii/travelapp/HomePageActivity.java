package tw.org.iii.travelapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.githang.statusbar.StatusBarCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class HomePageActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager pager;
    private FragmentManager manager;
    private HotPage page1;
    private AttrPage page2;
    private FoodPage page3;
    private ArrayList<Fragment> fragments;
    private LinearLayout iv_home, iv_guide, iv_camera, iv_favorite, iv_setting;
    private float screenWidth, screenHeight, newHeight;
    public static String urlIP = "http://125.230.19.49:8080";
    public static String userID = "1";
    private File photoFile, storageDir;
    private Uri photoURI;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private boolean issignin;
    private String memberid;
    private String memberemail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        //變更通知列底色
        StatusBarCompat.setStatusBarColor(this, Color.parseColor("#4f4f4f"));

        findView();
        getScreenSize();
        setIconListener();
        //toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.main_toolbar_menu);
        setSupportActionBar(toolbar);

        //詢問權限
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        } else {
            initdata();
        }
        //因為toolbar 而使用 tablayout
        tabLayout = findViewById(R.id.tablayout);
        //viewpager 滑動
        pager = findViewById(R.id.container);
        page1 = new HotPage();
        page2 = new AttrPage();
        page3 = new FoodPage();
        //fragment頁面
        manager = getSupportFragmentManager();
        MyAdapter adapter = new MyAdapter(manager);
        pager.setAdapter(adapter);
        pager.setCurrentItem(0);
        //與tablayout連動
        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
//        pager.setOffscreenPageLimit(5);
        inittablayout();
        //更新
//        editor.putBoolean("signin",false);
//        editor.putString("memberid","0");
//        editor.putString("memberemail","xxx");
//        editor.commit();
    }

    //menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar_menu,menu);
        MenuItem searchItem = menu.findItem(R.id.search_btn);
        MenuItem shareItem = menu.findItem(R.id.share_btn);
        //搜尋按鈕事件
        searchItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.search_btn:
                        Intent intent = new Intent(HomePageActivity.this,SearchPage.class);
                        startActivity(intent);
                        break;
                }
                return false;
            }
        });
        //分享按鈕事件
        shareItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(HomePageActivity.this, SharePhotoActivity.class);
                startActivity(intent);
                return false;
            }
        });
        return true;
    }

    //詢問權限
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            initdata();
        }else{
            finish();
        }
    }

    @Override
    public void finish() {
        super.finish();
    }

    private void initdata() {
        fragments = new ArrayList<Fragment>();
        fragments.add(new HotPage());
        fragments.add(new AttrPage());
        fragments.add(new FoodPage());

        sp = getSharedPreferences("memberdata",MODE_PRIVATE);
        editor = sp.edit();
        issignin = sp.getBoolean("signin",false);
        memberid = sp.getString("memberid","0");
        memberemail = sp.getString("memberemail","xxx");
    }

    private void inittablayout(){
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener(){
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //使tab按鍵和pager連動
                if(pager!=null){
                    pager.setCurrentItem(tab.getPosition());
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        tabLayout.addTab(tabLayout.newTab().setText("熱門"));
        tabLayout.addTab(tabLayout.newTab().setText("景點"));
        tabLayout.addTab(tabLayout.newTab().setText("美食"));
    }

    private class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            if (fragments != null) {
                return fragments.size();
            }
            return 0;
        }
    }
    //找出view
    private void findView(){
        iv_home = findViewById(R.id.btn_home);
        iv_guide = findViewById(R.id.btn_guide);
        iv_camera = findViewById(R.id.btn_camera);
        iv_favorite = findViewById(R.id.btn_favorite);
        iv_setting = findViewById(R.id.btn_setting);
    }
    //設定按鈕事件
    private void setIconListener(){
        //導覽
        iv_guide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });
        //相機
        iv_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoTakePhoto();
            }
        });
        //我的最愛
        iv_favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageActivity.this, FavoriteActivity.class);
                startActivity(intent);
            }
        });
        //設定
        iv_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });
    }
    //取得螢幕大小
    private void getScreenSize(){
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
        newHeight = screenWidth / 16 * 9;
    }
    //使用相機拍照
    private void gotoTakePhoto(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        // 確保有相機來處理intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File...
                Log.v("brad", ex.toString());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(
                        HomePageActivity.this,
                        "tw.org.iii.travelapp",
                        photoFile);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivity(Intent.createChooser(takePictureIntent, "TakePhoto"));
                Log.v("brad", "photoURI = " + photoURI);
            }
        }
    }
    //取得日期格式的照片名稱
    private File createImageFile() throws IOException {
        // Create an image file name
        // timeStamp的格式-> 20180420_004839
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        /**  getExternalFilesDir() 需要給的是type參數
         *   傳回的是該app packagename 底下,參數的系統位置
         *  storage/emulated/0/Android/data/tw.org.iii.takepicturetest/files/Pictures
         */
        storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        Log.v("brad", "image = " + image.getAbsolutePath());
        // Save a file: path for use with ACTION_VIEW intents
        String mCurrentPhotoPath = image.getAbsolutePath();
        Log.v("brad", "storageDir = " + storageDir);
        image = new File(storageDir, "sticker.jpg");
        return image;
    }
}

