/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.example.android.fingerprintdialog;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.com.example.android.bean.MyFingerprintBean;
import com.example.android.fingerprintutil.FingerprintUtil;

import java.lang.reflect.InvocationTargetException;
import java.security.InvalidKeyException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;

/**
 * Small helper class to manage text/icon around fingerprint authentication UI.
 */
public class FingerprintUiHelper extends FingerprintManager.AuthenticationCallback {

    private static final long ERROR_TIMEOUT_MILLIS = 1600;
    private static final long SUCCESS_DELAY_MILLIS = 1300;

    private final FingerprintManager mFingerprintManager;
    private final ImageView mIcon;
    private final TextView mErrorTextView;
    private final Callback mCallback;
    private CancellationSignal mCancellationSignal;

    private boolean mSelfCancelled;
    private Context mContext;
    public void setContext(Context context){
        this.mContext = context;
    }
    private SecretKey mSecretKey;
    public void setSecretKey(SecretKey sk){
        this.mSecretKey = sk;
    }

    /**
     * Constructor for {@link FingerprintUiHelper}.
     */
    FingerprintUiHelper(FingerprintManager fingerprintManager,
            ImageView icon, TextView errorTextView, Callback callback) {
        mFingerprintManager = fingerprintManager;
        mIcon = icon;
        mErrorTextView = errorTextView;
        mCallback = callback;
    }

    public boolean isFingerprintAuthAvailable() {
        // The line below prevents the false positive inspection from Android Studio
        // noinspection ResourceType
        return mFingerprintManager.isHardwareDetected()
                && mFingerprintManager.hasEnrolledFingerprints();
    }

    public void startListening(FingerprintManager.CryptoObject cryptoObject) {
        if (!isFingerprintAuthAvailable()) {
            return;
        }
        mCancellationSignal = new CancellationSignal();
        mSelfCancelled = false;
        // The line below prevents the false positive inspection from Android Studio
        // noinspection ResourceType
        mFingerprintManager
                .authenticate(cryptoObject, mCancellationSignal, 0 /* flags */, this, null);
        mIcon.setImageResource(R.drawable.ic_fp_40px);
    }

    public void stopListening() {
        if (mCancellationSignal != null) {
            mSelfCancelled = true;
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        if (!mSelfCancelled) {
            showError(errString);
            mIcon.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mCallback.onError();
                }
            }, ERROR_TIMEOUT_MILLIS);
        }
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        showError(helpString);
    }

    @Override
    public void onAuthenticationFailed() {
        showError(mIcon.getResources().getString(
                R.string.fingerprint_not_recognized));
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {

        FingerprintManager.CryptoObject co= result.getCryptoObject();
        if (co==null){
            Log.e("123","co=null");
        }else{

            Cipher cipher = co.getCipher();

            try {
                if (mSecretKey==null){
                    Log.e("123","mSecretKey is still null");
                }
                cipher.init(Cipher.DECRYPT_MODE,mSecretKey);//error
                byte [] bytes = cipher.doFinal();
                String str = Base64.encodeToString(bytes, 0 /* flags */);
                Log.e("123","str = "+str);
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            }


            Log.e("123","co="+co.toString()+"      "+ co.getCipher());
            ////////////////////////////////////////////////////////
            MyFingerprintBean bean = null;
            try {
                 bean = FingerprintUtil.getFingerprintRes(result);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            Log.e("123", "onAuthenticationSucceeded   fingerID="+bean.getId()+"   name="+bean.getName());
            Toast.makeText(mContext,"fpId="+bean.getId()+"   name="+bean.getName(),Toast.LENGTH_LONG).show();

            //FIXME just for test

            List<MyFingerprintBean> beans = null;
            try {
                beans = FingerprintUtil.getFingerPrint(mFingerprintManager);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            for (MyFingerprintBean b:beans){
                Log.i("123","fingerprint bean="+b.toString());
            }

            Log.i("123","get oP id finish");
        }





        mErrorTextView.removeCallbacks(mResetErrorTextRunnable);
        mIcon.setImageResource(R.drawable.ic_fingerprint_success);
        mErrorTextView.setTextColor(
                mErrorTextView.getResources().getColor(R.color.success_color, null));
        mErrorTextView.setText(
                mErrorTextView.getResources().getString(R.string.fingerprint_success));
        mIcon.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCallback.onAuthenticated();
            }
        }, SUCCESS_DELAY_MILLIS);
    }

    private void showError(CharSequence error) {
        mIcon.setImageResource(R.drawable.ic_fingerprint_error);
        mErrorTextView.setText(error);
        mErrorTextView.setTextColor(
                mErrorTextView.getResources().getColor(R.color.warning_color, null));
        mErrorTextView.removeCallbacks(mResetErrorTextRunnable);
        mErrorTextView.postDelayed(mResetErrorTextRunnable, ERROR_TIMEOUT_MILLIS);
    }

    private Runnable mResetErrorTextRunnable = new Runnable() {
        @Override
        public void run() {
            mErrorTextView.setTextColor(
                    mErrorTextView.getResources().getColor(R.color.hint_color, null));
            mErrorTextView.setText(
                    mErrorTextView.getResources().getString(R.string.fingerprint_hint));
            mIcon.setImageResource(R.drawable.ic_fp_40px);
        }
    };

    public interface Callback {

        void onAuthenticated();

        void onError();
    }
}
