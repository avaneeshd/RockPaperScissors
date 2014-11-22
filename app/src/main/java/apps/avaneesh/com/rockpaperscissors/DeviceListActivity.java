package apps.avaneesh.com.rockpaperscissors;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class DeviceListActivity extends Activity {
    BluetoothAdapter mBluetoothAdapter;
    int REQUEST_ENABLE_BT = 1;

    List<String> s = new ArrayList<String>();
    ArrayAdapter<String> mArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        TurnOnBluetooth();
    }

    @Override
    protected void onPause() {
        this.unregisterReceiver(mReceiver);
        super.onPause();

    }

    @Override
    protected void onResume() {
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        super.onResume();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Auto-generated method stub
        if(requestCode == REQUEST_ENABLE_BT){
            if(mBluetoothAdapter.isEnabled()) {
                Toast.makeText(this, "Bluetooth is Enabled", Toast.LENGTH_SHORT).show();
                mBluetoothAdapter.startDiscovery();
                if(mBluetoothAdapter.isDiscovering())
                    System.out.println("is discovering");
                getDevices();
            } else {
                Toast.makeText(this, "Bluetooth is Disabled", Toast.LENGTH_SHORT).show();
            }

        }
    }

    public void TurnOnBluetooth(){

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_LONG).show();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public void getDevices(){

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        for(BluetoothDevice bt : pairedDevices)
            s.add(bt.getName());
        ((ListView)findViewById(R.id.listView)).setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, s));

    }

    // Create a BroadcastReceiver for ACTION_FOUND
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            System.out.println("discovering devices...");
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                s.add(device.getName());
                ((ListView)findViewById(R.id.listView)).setAdapter(new ArrayAdapter<String>(DeviceListActivity.this, android.R.layout.simple_list_item_1, s));

            }
        }
    };


}
