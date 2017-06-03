package in.goodiebag.floorplan;

import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();
    private final static int COLUMNS = 6;
    private final static int ROWS = 6;
    private static View previousSelected = null;
    List<Button> imageList = new ArrayList<>();

    @BindView(R.id.gl)
    GridLayout gridLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        prepareImageViews();
        getSupportActionBar().hide();
        new CountDownTimer(5000,1000){

            @Override
            public void onTick(long l) {
                Random rand = new Random();

                selectionHelper(rand.nextInt(4),rand.nextInt(5));
            }

            @Override
            public void onFinish() {

            }
        }.start();
    }

    private void selectionHelper(View selectThis) {
        if (!imageList.isEmpty()) {
            for (View iv : imageList) {
                iv.setSelected(false);
            }
            selectThis.setSelected(true);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void prepareImageViews() {
        for (int i = 0; i < (COLUMNS * ROWS); i++) {
            Button iv = new Button(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED, 1f), GridLayout.spec(GridLayout.UNDEFINED, 1f));
            iv.setBackgroundResource((R.drawable.bg_empty_human_selector));
            //iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            //iv.setAdjustViewBounds(false);
            iv.setLayoutParams(params);
            iv.setOnClickListener(this);
            imageList.add(iv);
            gridLayout.addView(iv);

        }
    }

    private void selectionHelper(int x, int y){
        if(x < ROWS && y < COLUMNS) {
            if (previousSelected != null)
                previousSelected.setSelected(false);
            int position = (COLUMNS * x) + y;
            previousSelected= imageList.get(position);
            previousSelected.setSelected(true);
        }
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "Clicked");
        selectionHelper(view);
    }



}
