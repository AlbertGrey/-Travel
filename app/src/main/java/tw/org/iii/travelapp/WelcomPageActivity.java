package tw.org.iii.travelapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.githang.statusbar.StatusBarCompat;

public class WelcomPageActivity extends AppCompatActivity {
    private ImageView imageView;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private boolean issignin;
    private String memberid;
    private String memberemail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcom_page);
        //變更通知列底色
        StatusBarCompat.setStatusBarColor(this, Color.parseColor("#4f4f4f"));
        init();
        imageView = findViewById(R.id.welcome_image);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomPageActivity.this,
                        HomePageActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void init(){
        sp = getSharedPreferences("memberdata",MODE_PRIVATE);
        editor = sp.edit();
        issignin = sp.getBoolean("signin",false);
        memberid = sp.getString("memberid","0");
        memberemail = sp.getString("memberemail","xxx");

        editor.putBoolean("signin",false);
        editor.putString("memberid","");
        editor.putString("memberemail","");
        editor.commit();
    }
}
