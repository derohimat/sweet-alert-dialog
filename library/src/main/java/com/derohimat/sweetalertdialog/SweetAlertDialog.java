package com.derohimat.sweetalertdialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;

public class SweetAlertDialog extends Dialog implements View.OnClickListener {
    private AnimationSet mModalInAnim, mModalOutAnim, mErrorXInAnim, mSuccessLayoutAnimSet;
    private Animation mOverlayOutAnim, mErrorInAnim, mSuccessBowAnim;

    private View mDialogView, mSuccessLeftMask, mSuccessRightMask;
    private TextView mTitleTextView, mContentTextView;
    private FrameLayout mErrorFrame, mSuccessFrame, mProgressFrame, mWarningFrame;
    private SuccessTickView mSuccessTick;
    private ImageView mErrorX, mCustomImage;
    private Button mConfirmButton, mCancelButton;
    private ProgressHelper mProgressHelper;

    private String mTitleText, mContentText;
    private String mCancelText, mConfirmText;
    private boolean mShowTitle, mShowContent;
    private boolean mShowCancel, mShowConfirm = true;
    private boolean mCloseFromCancel;
    private boolean mHideKeyBoardOnDismiss = true;

    private int mAlertType;

    private Drawable mCustomImgDrawable;

    private OnSweetClickListener mCancelClickListener;
    private OnSweetClickListener mConfirmClickListener;

    public static final int NORMAL_TYPE = 0;
    public static final int ERROR_TYPE = 1;
    public static final int SUCCESS_TYPE = 2;
    public static final int WARNING_TYPE = 3;
    public static final int CUSTOM_IMAGE_TYPE = 4;
    public static final int PROGRESS_TYPE = 5;

    public interface OnSweetClickListener {
        void onClick(SweetAlertDialog sweetAlertDialog);
    }

    public SweetAlertDialog(Context context) {
        this(context, NORMAL_TYPE);
    }

    public SweetAlertDialog(Context context, int alertType) {
        super(context, R.style.alert_dialog);
        setCancelable(true);
        setCanceledOnTouchOutside(false);
        mProgressHelper = new ProgressHelper(context);
        mAlertType = alertType;
        mErrorInAnim = OptAnimationLoader.loadAnimation(getContext(), R.anim.error_frame_in);
        mErrorXInAnim = (AnimationSet) OptAnimationLoader.loadAnimation(getContext(), R.anim.error_x_in);
        mSuccessBowAnim = OptAnimationLoader.loadAnimation(getContext(), R.anim.success_bow_roate);
        mSuccessLayoutAnimSet = (AnimationSet) OptAnimationLoader.loadAnimation(getContext(), R.anim.success_mask_layout);
        mModalInAnim = (AnimationSet) OptAnimationLoader.loadAnimation(getContext(), R.anim.modal_in);
        mModalOutAnim = (AnimationSet) OptAnimationLoader.loadAnimation(getContext(), R.anim.modal_out);
        mModalOutAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (mHideKeyBoardOnDismiss) {
                    hideSoftKeyboard();
                }
                mDialogView.setVisibility(View.GONE);
                mDialogView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mCloseFromCancel) {
                            SweetAlertDialog.super.cancel();
                        } else {
                            SweetAlertDialog.super.dismiss();
                        }
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        // dialog overlay fade out
        mOverlayOutAnim = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                WindowManager.LayoutParams wlp = getWindow().getAttributes();
                wlp.alpha = 1 - interpolatedTime;
                getWindow().setAttributes(wlp);
            }
        };
        mOverlayOutAnim.setDuration(120);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_dialog);
        getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        mDialogView = getWindow().getDecorView().findViewById(android.R.id.content);
        mTitleTextView = findViewById(R.id.title_text);
        mContentTextView = findViewById(R.id.content_text);
        mErrorFrame = findViewById(R.id.error_frame);
        mErrorX = mErrorFrame.findViewById(R.id.error_x);
        mSuccessFrame = findViewById(R.id.success_frame);
        mProgressFrame = findViewById(R.id.progress_dialog);
        mSuccessTick = mSuccessFrame.findViewById(R.id.success_tick);
        mSuccessLeftMask = mSuccessFrame.findViewById(R.id.mask_left);
        mSuccessRightMask = mSuccessFrame.findViewById(R.id.mask_right);
        mCustomImage = findViewById(R.id.custom_image);
        mWarningFrame = findViewById(R.id.warning_frame);
        mConfirmButton = findViewById(R.id.confirm_button);
        mCancelButton = findViewById(R.id.cancel_button);
        mProgressHelper.setProgressWheel((ProgressWheel) findViewById(R.id.progressWheel));
        mConfirmButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);

        setTitleText(mTitleText);
        setContentText(mContentText);
        setCancelText(mCancelText);
        setConfirmText(mConfirmText);
        changeAlertType(mAlertType, true);

    }

    private void restore() {
        mCustomImage.setVisibility(View.GONE);
        mErrorFrame.setVisibility(View.GONE);
        mSuccessFrame.setVisibility(View.GONE);
        mWarningFrame.setVisibility(View.GONE);
        mProgressFrame.setVisibility(View.GONE);
        mConfirmButton.setVisibility(View.VISIBLE);

        mConfirmButton.setBackgroundResource(R.drawable.confirm_btn_background);
        mCancelButton.setBackgroundResource(R.drawable.cancel_btn_background);
        mErrorFrame.clearAnimation();
        mErrorX.clearAnimation();
        mSuccessTick.clearAnimation();
        mSuccessLeftMask.clearAnimation();
        mSuccessRightMask.clearAnimation();
    }

    /**
     * Setup Alert Type Configuration
     */
    private void changeAlertType(int alertType, boolean fromCreate) {
        mAlertType = alertType;
        // call after created views
        if (mDialogView != null) {
            if (!fromCreate) {
                // restore all of views state before switching alert type
                restore();
            }
            switch (mAlertType) {
                case ERROR_TYPE:
                    mErrorFrame.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS_TYPE:
                    mSuccessFrame.setVisibility(View.VISIBLE);
                    // initial rotate layout of success mask
                    mSuccessLeftMask.startAnimation(mSuccessLayoutAnimSet.getAnimations().get(0));
                    mSuccessRightMask.startAnimation(mSuccessLayoutAnimSet.getAnimations().get(1));
                    break;
                case WARNING_TYPE:
                    mConfirmButton.setBackgroundResource(R.drawable.warning_btn_background);
                    mWarningFrame.setVisibility(View.VISIBLE);
                    break;
                case CUSTOM_IMAGE_TYPE:
                    setCustomImage(mCustomImgDrawable);
                    mConfirmButton.setVisibility(mShowConfirm ? View.VISIBLE : View.GONE);
                    break;
                case PROGRESS_TYPE:
                    mProgressFrame.setVisibility(View.VISIBLE);
                    mConfirmButton.setVisibility(View.GONE);
                    break;
            }
            if (!fromCreate) {
                playAnimation();
            }
        }
    }

    public int getAlertType() {
        return mAlertType;
    }

    public void changeAlertType(int alertType) {
        changeAlertType(alertType, false);
    }

    /**
     * Setup Title Text Configuration
     */
    public String getTitleText() {
        return mTitleText;
    }

    public SweetAlertDialog setTitleText(String text) {
        mTitleText = text;
        if (mTitleTextView != null && mTitleText != null) {
            showTitleText(true);
            mTitleTextView.setText(mTitleText);
        }
        return this;
    }

    public SweetAlertDialog setTitleTextAppearance(int resId) {
        if (mTitleTextView != null) {
            mTitleTextView.setTextAppearance(this.getContext(), resId);
        }
        return this;
    }

    public boolean isShowTitleText() {
        return mShowTitle;
    }

    public SweetAlertDialog showTitleText(boolean isShow) {
        mShowTitle = isShow;
        if (mTitleTextView != null) {
            mTitleTextView.setVisibility(mShowTitle ? View.VISIBLE : View.GONE);
        }
        return this;
    }

    /**
     * Setup Custom Image Configuration
     */
    public SweetAlertDialog setCustomImage(Drawable drawable) {
        mCustomImgDrawable = drawable;
        if (mCustomImage != null && mCustomImgDrawable != null) {
            mCustomImage.setVisibility(View.VISIBLE);
            mCustomImage.setImageDrawable(mCustomImgDrawable);
        }
        return this;
    }

    public SweetAlertDialog setCustomImage(int resourceId) {
        return setCustomImage(getContext().getResources().getDrawable(resourceId));
    }

    /**
     * Setup Content Text Configuration
     */
    public String getContentText() {
        return mContentText;
    }

    public SweetAlertDialog setContentText(String text) {
        mContentText = text;
        if (mContentTextView != null && mContentText != null) {
            showContentText(true);
            mContentTextView.setText(mContentText);
        }
        return this;
    }

    public SweetAlertDialog setContentTextAppearance(int resId) {
        if (mContentTextView != null) {
            mContentTextView.setTextAppearance(this.getContext(), resId);
        }
        return this;
    }

    public SweetAlertDialog setContentTextGravity(int gravity) {
        if (mContentTextView != null) {
            mContentTextView.setGravity(gravity);
        }
        return this;
    }

    public boolean isShowContentText() {
        return mShowContent;
    }

    public SweetAlertDialog showContentText(boolean isShow) {
        mShowContent = isShow;
        if (mContentTextView != null) {
            mContentTextView.setVisibility(mShowContent ? View.VISIBLE : View.GONE);
        }
        return this;
    }

    /**
     * Setup Cancel Button Configuration
     */
    public String getCancelText() {
        return mCancelText;
    }

    public SweetAlertDialog setCancelText(String text) {
        mCancelText = text;
        if (mCancelButton != null && mCancelText != null) {
            showCancelButton(true);
            mCancelButton.setText(mCancelText);
        }
        return this;
    }

    public SweetAlertDialog setCancelTextAppearance(int resId) {
        if (mCancelButton != null) {
            mCancelButton.setTextAppearance(this.getContext(), resId);
        }
        return this;
    }

    public SweetAlertDialog setCancelButtonBackground(int resId) {
        if (mCancelButton != null) {
            mCancelButton.setBackgroundResource(resId);
        }
        return this;
    }

    public SweetAlertDialog setCancelButtonWidth(int width) {
        if (mCancelButton != null) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mCancelButton.getLayoutParams();
            params.width = width;
            mCancelButton.setLayoutParams(params);
        }
        return this;
    }

    public SweetAlertDialog setCancelButtonHeight(int height) {
        if (mCancelButton != null) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mCancelButton.getLayoutParams();
            params.height = height;
            mCancelButton.setLayoutParams(params);
        }
        return this;
    }

    public SweetAlertDialog setCancelButtonPadding(int start, int top, int end, int bottom) {
        if (mCancelButton != null) {
            mCancelButton.setPadding(start, top, end, bottom);
        }
        return this;
    }

    public boolean isShowCancelButton() {
        return mShowCancel;
    }

    public SweetAlertDialog showCancelButton(boolean isShow) {
        mShowCancel = isShow;
        if (mCancelButton != null) {
            mCancelButton.setVisibility(mShowCancel ? View.VISIBLE : View.GONE);
        }
        return this;
    }

    public SweetAlertDialog setCancelClickListener(OnSweetClickListener listener) {
        mCancelClickListener = listener;
        return this;
    }

    /**
     * Setup Confirm Button Configuration
     */
    public String getConfirmText() {
        return mConfirmText;
    }

    public SweetAlertDialog setConfirmText(String text) {
        mConfirmText = text;
        if (mConfirmButton != null && mConfirmText != null) {
            showConfirmButton(true);
            mConfirmButton.setText(mConfirmText);
        }
        return this;
    }

    public SweetAlertDialog setConfirmTextAppearance(int resId) {
        if (mConfirmButton != null) {
            mConfirmButton.setTextAppearance(this.getContext(), resId);
        }
        return this;
    }

    public SweetAlertDialog setConfirmButtonBackground(int resId) {
        if (mConfirmButton != null) {
            mConfirmButton.setBackgroundResource(resId);
        }
        return this;
    }

    public SweetAlertDialog setConfirmButtonWidth(int width) {
        if (mConfirmButton != null) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mConfirmButton.getLayoutParams();
            params.width = width;
            mConfirmButton.setLayoutParams(params);
        }
        return this;
    }

    public SweetAlertDialog setConfirmButtonHeight(int height) {
        if (mConfirmButton != null) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mConfirmButton.getLayoutParams();
            params.height = height;
            mConfirmButton.setLayoutParams(params);
        }
        return this;
    }

    public SweetAlertDialog setConfirmButtonPadding(int start, int top, int end, int bottom) {
        if (mConfirmButton != null) {
            mConfirmButton.setPadding(start, top, end, bottom);
        }
        return this;
    }

    public boolean isShowConfirmButton() {
        return mShowConfirm;
    }

    public SweetAlertDialog showConfirmButton(boolean isShow) {
        mShowConfirm = isShow;
        if (mConfirmButton != null) {
            mConfirmButton.setVisibility(mShowConfirm ? View.VISIBLE : View.GONE);
        }
        return this;
    }

    public SweetAlertDialog setConfirmClickListener(OnSweetClickListener listener) {
        mConfirmClickListener = listener;
        return this;
    }

    /**
     * Setup Animation Configurations
     */
    private void playAnimation() {
        if (mAlertType == ERROR_TYPE) {
            mErrorFrame.startAnimation(mErrorInAnim);
            mErrorX.startAnimation(mErrorXInAnim);
        } else if (mAlertType == SUCCESS_TYPE) {
            mSuccessTick.startTickAnim();
            mSuccessRightMask.startAnimation(mSuccessBowAnim);
        }
    }

    protected void onStart() {
        mDialogView.startAnimation(mModalInAnim);
        playAnimation();
    }

    /**
     * The real Dialog.cancel() will be invoked async-ly after the animation finishes.
     */
    @Override
    public void cancel() {
        dismissWithAnimation(true);
    }

    /**
     * The real Dialog.dismiss() will be invoked async-ly after the animation finishes.
     */
    public void dismissWithAnimation() {
        dismissWithAnimation(false);
    }

    private void dismissWithAnimation(boolean fromCancel) {
        mCloseFromCancel = fromCancel;
        mConfirmButton.startAnimation(mOverlayOutAnim);
        mDialogView.startAnimation(mModalOutAnim);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.cancel_button) {
            if (mCancelClickListener != null) {
                mCancelClickListener.onClick(SweetAlertDialog.this);
            } else {
                dismissWithAnimation();
            }
        } else if (v.getId() == R.id.confirm_button) {
            if (mConfirmClickListener != null) {
                mConfirmClickListener.onClick(SweetAlertDialog.this);
            } else {
                dismissWithAnimation();
            }
        }
    }

    public ProgressHelper getProgressHelper() {
        return mProgressHelper;
    }

    public SweetAlertDialog setHideKeyBoardOnDismiss(boolean hide) {
        this.mHideKeyBoardOnDismiss = hide;
        return this;
    }

    public boolean isHideKeyBoardOnDismiss() {
        return this.mHideKeyBoardOnDismiss;
    }

    private void hideSoftKeyboard() {
        Activity activity = getOwnerActivity();
        if (activity != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null && activity.getCurrentFocus() != null) {
                inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
            }
        }
    }

}
