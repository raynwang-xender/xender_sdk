package cn.xender.core.ap.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;


/**
 * Created by Administrator on 2017/1/18.
 */

public class BluetoothUtil {

    private static final String TAG = "BluetoothUtil";



    public static void closeBluetoothIfOpened(Context context){
        try {
            BluetoothAdapter adapter = getMyBlueAdapter(context);

            if(adapter != null && adapter.isEnabled()){
                adapter.disable();
            }
        }catch (Exception e){
        }

    }



    public static void cancelDiscovery(Context context){
        try {
            BluetoothAdapter adapter = getMyBlueAdapter(context);
            if(adapter != null && adapter.isEnabled() && adapter.isDiscovering()){
                adapter.cancelDiscovery();
            }
        }catch (Exception e){

        }
    }


    public static BluetoothManager getBluetoothManager(Context context){
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return null;
        }

        return (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
    }

    public static BluetoothAdapter getMyBlueAdapter(Context context){
        BluetoothManager manager = getBluetoothManager(context);
        if(manager == null || android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2){

            return null;
        }

        return manager.getAdapter();
    }


    public static BluetoothAdapter getDefaultBluetoothAdapter(){
        return BluetoothAdapter.getDefaultAdapter();//这个只能用于读，无法修改。
    }


}
