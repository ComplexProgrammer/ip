package ip.complexprogrammer.uz.services;

import android.os.Handler;
import android.widget.ImageView;

import ip.complexprogrammer.uz.R;

public class LoadingScreen {private ImageView loading;

    public LoadingScreen(ImageView loading) {
        this.loading = loading;
    }

    public void setLoadScreen(){
        final Integer[] loadingImages = {R.mipmap.ic_launcher, R.mipmap.ic_launcher_foreground, R.mipmap.ic_launcher_round, R.mipmap.ic_launcher_foreground};
        final Handler loadingHandler = new Handler();
        Runnable runnable = new Runnable() {
            int loadingImgIndex = 0;
            public void run() {
                loading.setImageResource(loadingImages[loadingImgIndex]);
                loadingImgIndex++;
                if (loadingImgIndex >= loadingImages.length)
                    loadingImgIndex = 0;
                loadingHandler.postDelayed(this, 500);
            }
        };
        loadingHandler.postDelayed(runnable, 500);
    }}