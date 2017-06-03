package in.goodiebag.floorplan;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();
    private final static int COLUMNS = 10;
    private final static int ROWS = 10;
    private boolean selectionFlag = false;
    private Handler timerHandler;
    private static View previousSelected = null;
    private static final String[] INITIAL_PERMS={
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    List<ImageButton> imageList = new ArrayList<>();

    //bluetooth requirements
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    @BindView(R.id.gl)
    GridLayout gridLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        prepareImageViews();
        getSupportActionBar().hide();

        timerHandler = new Handler(Looper.getMainLooper());


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(INITIAL_PERMS,1);
        }
        //bluetooth
        mHandler = new Handler();
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLUETOOTH NOT SUPPORTED", Toast.LENGTH_SHORT).show();
            finish();
        }
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Error bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
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
            ImageButton iv = new ImageButton(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED, 2f), GridLayout.spec(GridLayout.UNDEFINED, 2f));
            iv.setBackgroundResource((R.drawable.bg_empty_human_selector));
            //iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setAdjustViewBounds(true);
            iv.setLayoutParams(new ViewGroup.LayoutParams(200, 200));
            iv.setPadding(20, 20, 20, 20);
            //iv.setOnClickListener(this);
            imageList.add(iv);
            gridLayout.addView(iv, params);

        }
    }

    private void selectionHelper(int x, int y) {
        if (x < ROWS && y < COLUMNS) {
            if (previousSelected != null)
                previousSelected.setSelected(false);
            int position = (COLUMNS * x) + y;
            previousSelected = imageList.get(position);
            previousSelected.setSelected(true);
        }
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "Clicked");
        selectionHelper(view);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        Log.d(TAG,"Scan started");
        scanLeDevice(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"Scan stopped");
        scanLeDevice(false);
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG,"Scan callback");
                            if(device.getAddress().equals("00:A0:50:D0:73:2A")) {
                                //Update x y
                                Log.d(TAG, scanRecord.toString());
                                if(selectionFlag) {
                                    selectionHelper(scanRecord[10], scanRecord[11]);
                                    selectionFlag = false;
                                    Log.d(TAG,"x : " + scanRecord[10] + " y : " + scanRecord[11]);
                                    timerHandler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            selectionFlag = true;
                                        }
                                    },2000);
                                }
                            }
                           // selectionHelper();
                        }
                    });
                }
            };
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //mScanning = false;
                    //mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);
            mScanning = true;
            boolean bi = mBluetoothAdapter.startLeScan(mLeScanCallback);
            timerHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    selectionFlag = true;
                }
            },2000);
            Log.d(TAG,bi+"");
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }


}