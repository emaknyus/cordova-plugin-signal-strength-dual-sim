package org.apache.cordova.plugin;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.LOG;

import android.content.Context;

import android.content.pm.PackageManager;
import android.os.Build;
import android.Manifest;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class SignalStrengthDualSim extends CordovaPlugin {

    int dbm = -1;
    int asu = 0;
    int level = 0;
    SignalStrengthStateListener ssListener;
    TelephonyManager defaultTelephonyManager;

    private static final String LOG_TAG = "CordovaPluginSignalStrengthDualSim";
    private static final String SIM_ONE_ASU = "Sim1";
    private static final String SIM_TWO_ASU = "Sim2"; // not supported at the moment.
    private static final String SIM_COUNT = "SimCount";
    private static final String HAS_READ_PERMISSION = "hasReadPermission";
    private static final String REQUEST_READ_PERMISSION = "requestReadPermission";
    CallbackContext callback;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        callback = callbackContext;

        LOG.i(LOG_TAG, "STARTING");
        LOG.i(LOG_TAG, "Params: " + action);

        if (SIM_ONE_ASU.equals(action) || SIM_TWO_ASU.equals(action)) {

            Context context = cordova.getActivity().getApplicationContext();
            ssListener = new SignalStrengthStateListener();
            TelephonyManager defaultTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            defaultTelephonyManager.listen(ssListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

            PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
            result.setKeepCallback(true);
            callback.sendPluginResult(result);
            return true;
        }

        return false;
    }

    class SignalStrengthStateListener extends PhoneStateListener  {

//        CallbackContext callback;
//        Context context;
//        public void SignalStrengthStateListener(CallbackContext callbackContext, Context appContext){
//            callback = callbackContext;
//            context = appContext;
//        }

        @Override
        public void onSignalStrengthsChanged(android.telephony.SignalStrength signalStrength) {

            super.onSignalStrengthsChanged(signalStrength);
            int tsNormSignalStrength = signalStrength.getGsmSignalStrength();
            LOG.i(LOG_TAG, "Signalstrength, " + tsNormSignalStrength);
            asu = tsNormSignalStrength;
            level = signalStrength.getLevel();
            dbm = (2 * tsNormSignalStrength) - 113 ;     // -> dBm


            Context context = cordova.getActivity().getApplicationContext();
            TelephonyManager defaultTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String operatorName = defaultTelephonyManager.getNetworkOperatorName();
            String operator = defaultTelephonyManager.getNetworkOperator();
            int networkType = defaultTelephonyManager.getNetworkType();

            String netWorkTypeName;
            switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                // case TelephonyManager.NETWORK_TYPE_GSM:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    netWorkTypeName = "2G";
                    break;
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                // case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                    netWorkTypeName = "3G";
                    break;
                case TelephonyManager.NETWORK_TYPE_LTE:
                // case TelephonyManager.NETWORK_TYPE_IWLAN:
                    netWorkTypeName = "4G";
                    break;
                default:
                    netWorkTypeName = "unknown";
                    break;
            }

            JSONObject response = new JSONObject();

            try {
                response.put("operator_name", operatorName);
                response.put("operator", operator);
                response.put("networkTypeName", netWorkTypeName);
                response.put("NetworkType", networkType);
                response.put("asu", asu);
                response.put("level", level);
                response.put("dbm", dbm);
                
            } catch (JSONException e) {
                e.printStackTrace();
            }


            PluginResult result = new PluginResult(PluginResult.Status.OK, response);
            result.setKeepCallback(false);
            callback.sendPluginResult(result);
        }
    }

}
