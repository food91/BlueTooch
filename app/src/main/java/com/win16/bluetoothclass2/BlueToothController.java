package com.win16.bluetoothclass2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.Context;
import android.content.Intent;


/**蓝牙适配器
 *
 */

public class BlueToothController {
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private Activity mActivity;
    public static final int REQUEST_CODE_ENABLE_BLUETOOTH=0;

    public BlueToothController(Activity activity){
        BluetoothManager BlueBlueMG = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        this.mBluetoothAdapter = BlueBlueMG.getAdapter();
        this.mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        this.mActivity=activity;
    }
    public  boolean isSupportBlueTooth(){
        if (mBluetoothAdapter==null){
            return false;
        }
        return  true ;
    }
    public boolean isOpenBlueTooth(){
        return mBluetoothAdapter.isEnabled();
    }

    /**打开蓝牙
     *
     */
    public void turnOnBlueTooth(){
        Intent intent = new Intent();
        intent.setAction(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        this.mActivity.startActivityForResult(intent,REQUEST_CODE_ENABLE_BLUETOOTH);
    }


    /**关闭蓝牙
     *好像是和动态权限有关
     */
    public void  turnOffBlueTooth(){
        mBluetoothAdapter.disable();
    }
/**
 * 扫描蓝牙设备
 */
    public void scanBlueTooth(ScanCallback scanCallback){
        mBluetoothLeScanner.startScan(scanCallback);
    }
    public void stopBlueTooth(ScanCallback scaCallback){
        mBluetoothLeScanner.stopScan(scaCallback);
    }
    /**
     * 连接蓝牙
     * @param bluetoothDevice
     * @param autoConnect
     * @param gattCallBack
     */
    public BluetoothGatt connectBlueTooth(BluetoothDevice bluetoothDevice, boolean autoConnect, BluetoothGattCallback gattCallBack){
        return bluetoothDevice.connectGatt(this.mActivity,autoConnect,gattCallBack);

    }

    public void sendData(BluetoothGatt bluetoothGatt , BluetoothGattCharacteristic bluetoothGattCharacteristic, String text){
        bluetoothGattCharacteristic.setValue(text);
        bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);

    }



}


