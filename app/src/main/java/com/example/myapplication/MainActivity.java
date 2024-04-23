package com.example.myapplication;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.myapplication.databinding.ActivityMainBinding;
import com.win16.bluetoothclass2.BlueToothController;
import android.Manifest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private com.win16.bluetoothclass2.BlueToothController BlueToothController;
    private Toast toast;
    int REQUEST_CODE_PERMISSION = 1 ;
    private myLvAdatapter mLvAdatapter;
    private ListView lv_my_listview;
    private List<BluetoothDevice> mDeviceData ;
    private mScanCallBack myScanCallBack;
    private mBluetoothGattCallback myGattCallBack;
    private Handler mtimeHandler = new Handler();

    //蓝牙的特征值，发送?id 蓝牙设备的UUID
    private final static String SERVICE_EIGENVALUE_SEND = "0000fff0-0000-1000-8000-00805f9b34fb";
    private final static String SERVICE_EIGENVALUE_READ = "0000fff0-0000-1000-8000-00805f9b34fb";

    private BluetoothGatt mBluetoothGatt;  //通讯协议 下面有可能重复
    private BluetoothGattCharacteristic mNeedGattCharacteristic;
    //通讯协议

    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;   //动态申请权限
    private BluetoothGatt mGatt;//通讯协议




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mycheckPermission();
        BlueToothController = new BlueToothController(this);
        initView();
        initData();
        myGattCallBack = new mBluetoothGattCallback();


        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);




    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.bottom_nav_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == BlueToothController.REQUEST_CODE_ENABLE_BLUETOOTH){
            if (resultCode == RESULT_OK){
                showToast("您的蓝牙打开成功了");
            } else {
                showToast("您的蓝牙打开失败" );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i =0; i<permissions.length; i++) {
            System.out.println("onRequestPermissionsResult->"+ permissions[i] + "{{}}"+grantResults[i]);
            /** if i=-1,程序退出
             *
             */
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        if (item.getItemId() == R.id.menu_is_support_blue) {
//           boolean flagSupportBlue = BlueToothController.isSupportBlueTooth();
//           if (flagSupportBlue){
//               showToast("该设备支持蓝牙");
//           }else{
//               showToast("该设备不支持蓝牙");
//           }
//        }
        if (item.getItemId() == R.id.menu_scan_blue) {
            turnOffBlue();
        }
//        if (item.getItemId() == R.id.menu_turn_on_blue) {
//            turnOnBlue();
//        }
        if (item.getItemId() == R.id.menu_turn_on_blue) {

            isOpenBlue();
        }
//        if (item.getItemId() == R.id.menu_scan_blue) {
//            scanBlue();
//        }
//        if (item.getItemId() == R.id.menu_send_data) {
//            BlueToothController.sendData(mBluetoothGatt,mNeedGattCharacteristic,"Sucess");
//        }
        return super.onOptionsItemSelected(item);
    }

    private void scanBlue() {
        BlueToothController.scanBlueTooth(myScanCallBack);
    }

    private void turnOnBlue() {
        BlueToothController.turnOnBlueTooth();
    }
    private void turnOffBlue() {
        BlueToothController.turnOffBlueTooth();
    }
    private void isOpenBlue(){
        if(BlueToothController.isOpenBlueTooth()){
            showToast("您的蓝牙已打开");
        }else {
            showToast("您的蓝牙没打开");
        }

    }

    /**对toast进行封装
     *
     * @param text
     */
    private void showToast(String text){
        if(toast == null){
            toast = Toast.makeText(this,null,Toast.LENGTH_LONG);
        }
        toast.setText(text);
        toast.show();
    }
    private void mycheckPermission() {
        if(!((ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED))) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE_PERMISSION);
        }
    }
    private void initView() {
        lv_my_listview = findViewById(R.id.lv_my_listview);
    }
    private void initData() {
        myScanCallBack = new mScanCallBack();
        mDeviceData =new ArrayList<>();
        mLvAdatapter = new myLvAdatapter();
        lv_my_listview.setAdapter(mLvAdatapter);
        lv_my_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                BlueToothController.stopBlueTooth(myScanCallBack);
                BluetoothDevice bluetoothDevice =mDeviceData.get(i);
                mBluetoothGatt = BlueToothController.connectBlueTooth(bluetoothDevice,false,myGattCallBack);

            }
        });

    }
    private class myLvAdatapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mDeviceData.size();
        }

        @Override
        public Object getItem(int i) {
            return mDeviceData.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            TextView tv;
            if(view == null){
                tv = new TextView(MainActivity.this);
            }else {
                tv = (TextView) view;
            }
            tv.setText(mDeviceData.get(i)+"||"+mDeviceData.get(i).getAddress());
            /*LinearLayout.LayoutParams params= new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.MATCH_PARENT);
            tv.setLayoutParams(params);
            params.setMargins(10,10,10,10);
             */

            tv.setHeight(200);
            return tv;
        }
    }
    private class mScanCallBack extends ScanCallback{
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            System.out.print("omScanResult->"+result.getDevice().getName()+"||"+result.getDevice().getAddress());
            BluetoothDevice curBlueDevice = result.getDevice();
            if (mDeviceData.contains(curBlueDevice)) {
                mDeviceData.add(curBlueDevice);
                mLvAdatapter.notifyDataSetChanged();
            }
            super.onScanResult(callbackType, result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    }

    private class mBluetoothGattCallback extends BluetoothGattCallback {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            System.out.println("您的蓝牙连接成功");
            super.onConnectionStateChange(gatt, status, newState);

            mtimeHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothGatt.discoverServices();
                }
            },1000);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            System.out.println("onServicesDiscovered->"+ mBluetoothGatt + "{{||}}" +gatt);
            List<BluetoothGattService> services = mBluetoothGatt.getServices();
            for (int i = 0; i < services.size(); i++) {
                BluetoothGattService curBluetoothGattService = services.get(i);
            //    System.out.println("第" + i + "个" + curBluetoothGattService);
                List<BluetoothGattCharacteristic> characteristics = curBluetoothGattService.getCharacteristics();
                for (int j = 0; j < characteristics.size(); j++) {
                    BluetoothGattCharacteristic curBluetoothGattCharacteristic = characteristics.get(j);
                    //System.out.println("第" + i + "个服务" + "第" + j + "个特征值" + curBluetoothGattCharacteristic.getUuid());
                    if (curBluetoothGattCharacteristic.getUuid().toString().equals(SERVICE_EIGENVALUE_SEND)){
                        System.out.println("我找到我需要的特征了");
                        mNeedGattCharacteristic = curBluetoothGattCharacteristic;
                        mBluetoothGatt.setCharacteristicNotification(mNeedGattCharacteristic,true);
                     //   List<BluetoothGattDescriptor> descriptors = mNeedGattCharacteristic.getDescriptors();
                   //     for (int k = 0; k < descriptors.size(); k++) {
                      //      System.out.println("第" + i + "个服务" + "第" + j + "个特征值第" + k + "个客户端配置UUID" + descriptors.get(k).getUuid());
                     mtimeHandler.postDelayed(new Runnable() {
                         @Override
                         public void run() {
                             BluetoothGattDescriptor clientComfig = mNeedGattCharacteristic.getDescriptor(UUID.fromString(SERVICE_EIGENVALUE_READ));
//获取客户端配置
                             System.out.println(clientComfig);
                             if (clientComfig != null){
                                 clientComfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                 mBluetoothGatt.writeDescriptor(clientComfig);
                             }
                         }
                     },500);

                        //}


                    }
                }

            }
            super.onServicesDiscovered(gatt, status);
        }

        @Override
        public void onCharacteristicRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value, int status) {
            System.out.println("onCharacteristicRead");
            super.onCharacteristicRead(gatt, characteristic, value, status);
        }

        //接受数据
        @Override
        public void onCharacteristicChanged(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value) {
            byte[] value1 = characteristic.getValue();
            String res = new String(value1);
            System.out.println("onCharacteristicChanged->"+ res);
            super.onCharacteristicChanged(gatt, characteristic, value);
        }
    }
}