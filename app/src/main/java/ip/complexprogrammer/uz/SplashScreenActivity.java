package ip.complexprogrammer.uz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Objects;

import ip.complexprogrammer.uz.services.ChangeLanguage;

public class SplashScreenActivity extends AppCompatActivity {

    Animation topAnim,bottomAnim;
    ImageView logo;
    TextView title,sub_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);
        ChangeLanguage.getLangCode(this);
        Objects.requireNonNull(getSupportActionBar()).hide();
        logo=findViewById(R.id.imageView);
        title=findViewById(R.id.textView2);
        sub_title=findViewById(R.id.textView);
        topAnim= AnimationUtils.loadAnimation(this,R.anim.top_animation);
        bottomAnim= AnimationUtils.loadAnimation(this,R.anim.bottom_animation);

        logo.setAnimation(topAnim);
        title.setAnimation(bottomAnim);
        sub_title.setAnimation(bottomAnim);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent parchment = new Intent(SplashScreenActivity.this, MainActivity.class);
                SplashScreenActivity.this.startActivity(parchment);
                SplashScreenActivity.this.finish();
//                overridePendingTransition(R.anim.fade_main_in, R.anim.fade_splash_out);
            }
        }, 3000);
    }
}