package com.example.android.fingerprintutil;

import android.hardware.fingerprint.FingerprintManager;
import android.util.Log;

import com.example.android.com.example.android.bean.MyFingerprintBean;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by howell on 2016/10/21.
 */

public class FingerprintUtil {
    public static List<MyFingerprintBean> getFingerPrint(FingerprintManager fm) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        List<MyFingerprintBean> list = new ArrayList<MyFingerprintBean>();
        Class<FingerprintManager> fpmClass = FingerprintManager.class;
        Method fpmMethod = fpmClass.getMethod("getEnrolledFingerprints");
        fpmMethod.setAccessible(true);
        Object o = fpmMethod.invoke(fm);
        List l = (List) o;
        Class fingerprint = Class.forName("android.hardware.fingerprint.Fingerprint");
        Method idMethod = fingerprint.getMethod("getFingerId");
        idMethod.setAccessible(true);
        Method nameMethod = fingerprint.getMethod("getName");
        nameMethod.setAccessible(true);
        for (int i=0;i<l.size();i++){
           Object idObj = idMethod.invoke(l.get(i));
           Object nameObj = nameMethod.invoke(l.get(i));
           int id = Integer.valueOf(idObj.toString());
           String name = nameObj.toString();
           MyFingerprintBean bean = new MyFingerprintBean(id,name);
           list.add(bean);
        }
        return list;
    }

    public static MyFingerprintBean getFingerprintRes(FingerprintManager.AuthenticationResult result) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        MyFingerprintBean bean = new MyFingerprintBean();
        Class<FingerprintManager.AuthenticationResult> c = FingerprintManager.AuthenticationResult.class;
        Method fpMethod = c.getMethod("getFingerprint");
        fpMethod.setAccessible(true);
        Object fpObj = fpMethod.invoke(result);
        String objName = fpObj.getClass().getName();
        Class fingerprint = Class.forName(objName);
        Method idMethod = fingerprint.getMethod("getFingerId");
        Method nameMethod = fingerprint.getMethod("getName");
        Method groupIdMethod = fingerprint.getMethod("getGroupId");
        Method deviceMethod = fingerprint.getMethod("getDeviceId");
        idMethod.setAccessible(true);
        nameMethod.setAccessible(true);
        groupIdMethod.setAccessible(true);
        deviceMethod.setAccessible(true);
        Object idObj = idMethod.invoke(fpObj);
        Object nameObj = nameMethod.invoke(fpObj);
        Object groupObj = groupIdMethod.invoke(fpObj);
        Object deviceObj = deviceMethod.invoke(fpObj);
        int id = Integer.valueOf(idObj.toString());
        String name = nameObj.toString();
        int groupId = Integer.valueOf(groupObj.toString());
        long deviceId = Long.valueOf(deviceObj.toString());

        Log.i("123","name = "+nameObj+" gourpid="+groupId+" deviceId="+deviceId);
        bean.setId(id);
        bean.setName(name);
        return  bean;
    }


}
