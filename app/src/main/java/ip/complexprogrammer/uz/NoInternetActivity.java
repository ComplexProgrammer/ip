package ip.complexprogrammer.uz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import java.util.Objects;

import ip.complexprogrammer.uz.services.ChangeLanguage;

public class NoInternetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ChangeLanguage.setLocal(this,ChangeLanguage.getLangCode(this));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Objects.requireNonNull(getSupportActionBar()).hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internet);
    }
    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(this, MainActivity.class));
        super.onBackPressed();
    }
}