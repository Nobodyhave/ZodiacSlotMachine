package com.zodiac.slotmachine;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.AbstractWheelAdapter;


public class MainActivity extends Activity {

    private final static int[] mWheels = {
            R.id.slot_1,
            R.id.slot_2,
            R.id.slot_3
    };

    private final static int[] mImageIds = {
            R.drawable.img_cherry,
            R.drawable.img_gold_single,
            R.drawable.img_gold_single,
            R.drawable.img_gold_single,
            R.drawable.img_gold_double,
            R.drawable.img_gold_double,
            R.drawable.img_gold_triple,
            R.drawable.img_seven
    };

    private static final int MIN_BET = 1;
    private static final int MAX_BET = 3;
    private static final int BET_STEP = 1;
    private static final int INITIAL_BALANCE = 100;
    private static final int SLOT_ITEMS_COUNT = mImageIds.length*2;

    private int mBet;
    private int mBalance;
    private int mWinAmount;

    private WheelView mFirstWheel;
    private WheelView mSecondWheel;
    private WheelView mThirdWheel;
    private Button mMinButton;
    private Button mDecreaseButton;
    private Button mIncreaseButton;
    private Button mMaxButton;
    private Button mSpinButton;
    private Button mMaxAndSpinButton;
    private Button mCollectButton;
    private TextView mBalanceTextView;
    private TextView mResultTextView;
    private TextView mBetTextView;

    private final OnWheelScrollListener mScrolledListener = new OnWheelScrollListener() {
        public void onScrollingStarted(WheelView wheel) {
            if(mWheels[0] == wheel.getId()) {
                onSpinStarted();
            }
        }

        public void onScrollingFinished(WheelView wheel) {
            if(mWheels[mWheels.length - 1] == wheel.getId()) {
                onSpinFinished(winOccurred());
            }
        }
    };

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == mSpinButton) {
                spinWheels();
            } else if (v == mMinButton) {
                onBetChanged(MIN_BET);
            } else if (v == mDecreaseButton) {
                onBetChanged(mBet - BET_STEP);
            } else if (v == mIncreaseButton) {
                onBetChanged(mBet + BET_STEP);
            } else if (v == mMaxButton) {
                onBetChanged(MAX_BET);
            } else if (v == mMaxAndSpinButton) {
                onBetChanged(MAX_BET);
                spinWheels();
            } else if (v == mCollectButton) {
                onWinCollected();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_main);

        mFirstWheel = (WheelView) findViewById(R.id.slot_1);
        mSecondWheel = (WheelView) findViewById(R.id.slot_2);
        mThirdWheel = (WheelView) findViewById(R.id.slot_3);

        mMinButton = (Button) findViewById(R.id.btn_min_bet);
        mDecreaseButton = (Button) findViewById(R.id.btn_decrease_bet);
        mIncreaseButton = (Button) findViewById(R.id.btn_increase_bet);
        mMaxButton = (Button) findViewById(R.id.btn_max_bet);
        mSpinButton = (Button) findViewById(R.id.btn_spin);
        mMaxAndSpinButton = (Button) findViewById(R.id.btn_max_and_spin);
        mCollectButton = (Button) findViewById(R.id.btn_collect);

        mBalanceTextView = (TextView) findViewById(R.id.lbl_balance);
        mResultTextView = (TextView) findViewById(R.id.lbl_result);
        mBetTextView = (TextView) findViewById(R.id.lbl_bet);

        setupWheels();
        onBalanceChanged(INITIAL_BALANCE, false);
        onBetChanged(MIN_BET);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mMinButton.setOnClickListener(mOnClickListener);
        mDecreaseButton.setOnClickListener(mOnClickListener);
        mIncreaseButton.setOnClickListener(mOnClickListener);
        mMaxButton.setOnClickListener(mOnClickListener);
        mSpinButton.setOnClickListener(mOnClickListener);
        mMaxAndSpinButton.setOnClickListener(mOnClickListener);
        mCollectButton.setOnClickListener(mOnClickListener);
    }

    @Override
    protected void onPause() {
        mMinButton.setOnClickListener(null);
        mDecreaseButton.setOnClickListener(null);
        mIncreaseButton.setOnClickListener(null);
        mMaxButton.setOnClickListener(null);
        mSpinButton.setOnClickListener(null);
        mMaxAndSpinButton.setOnClickListener(null);
        mCollectButton.setOnClickListener(null);

        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            final Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupWheels() {
        for (int id : mWheels) {
            final WheelView wheel = getWheel(id);
            wheel.setViewAdapter(new SlotMachineAdapter(this));
            wheel.setVisibleItems(3);
            wheel.setCurrentItem((int) (Math.random() * (SLOT_ITEMS_COUNT + 1)));

            wheel.addScrollingListener(mScrolledListener);

            wheel.setCyclic(true);
            wheel.setEnabled(false);
        }
    }

    private WheelView getWheel(int id) {
        switch (id) {
            case R.id.slot_1: {
                return mFirstWheel;
            }
            case R.id.slot_2: {
                return mSecondWheel;
            }
            case R.id.slot_3: {
                return mThirdWheel;
            }
            default: {
                throw new IllegalArgumentException("Wheel with id " + id + "doesn't exists");
            }
        }
    }

    private void spinWheels() {
        for (int id : mWheels) {
            final WheelView wheel = getWheel(id);
            final int itemsToScrollAmount = -400 + (int) (Math.random() * 100);
            int scrollTime;

            switch (id) {
                case R.id.slot_1: {
                    scrollTime = 2000 + (int) (Math.random() * 1000);
                    break;
                }
                case R.id.slot_2: {
                    scrollTime = 4000 + (int) (Math.random() * 1000);
                    break;
                }
                case R.id.slot_3: {
                    scrollTime = 6000 + (int) (Math.random() * 1000);
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Wheel with id " + id + "doesn't exists");
                }
            }

            wheel.scroll(itemsToScrollAmount, scrollTime);
        }
    }

    private void onSpinStarted() {
        mMinButton.setEnabled(false);
        mDecreaseButton.setEnabled(false);
        mIncreaseButton.setEnabled(false);
        mMaxButton.setEnabled(false);
        mSpinButton.setEnabled(false);
        mMaxAndSpinButton.setEnabled(false);
        mCollectButton.setEnabled(false);

        onBalanceChanged(mBalance - mBet, false);
    }

    private void onSpinFinished(boolean isWin) {
        mMinButton.setEnabled(!isWin);
        mDecreaseButton.setEnabled(!isWin);
        mIncreaseButton.setEnabled(!isWin);
        mMaxButton.setEnabled(!isWin);
        mSpinButton.setEnabled(!isWin);
        mMaxAndSpinButton.setEnabled(!isWin);
        mCollectButton.setEnabled(isWin);

        if(!isWin) {
            onBalanceChanged(mBalance, true);
        }
    }

    private void onWinCollected() {
        mMinButton.setEnabled(true);
        mDecreaseButton.setEnabled(true);
        mIncreaseButton.setEnabled(true);
        mMaxButton.setEnabled(true);
        mSpinButton.setEnabled(true);
        mMaxAndSpinButton.setEnabled(true);
        mCollectButton.setEnabled(false);

        onBalanceChanged(mBalance + mWinAmount, true);
    }

    private void onBalanceChanged(int balance, boolean isAfterSpin) {
        mBalance = balance;
        mBalanceTextView.setText(getString(R.string.balance, mBalance));

        if(isAfterSpin) {
            if (mBalance <= 0) {
                onGameOver();
            } else if (mBet > mBalance) {
                onBetChanged(mBalance);
            }
        }
    }

    private void onBetChanged(int bet) {
        if((MIN_BET <= bet) && (bet <= MAX_BET)) {
            mBet = bet;
            if(mBalance < bet) {
                mBet = mBalance;
            }
            mBetTextView.setText(String.valueOf(mBet));
        }
    }

    private void onResultChanged(int winAmount) {
        mWinAmount = winAmount;
        mResultTextView.setVisibility(View.VISIBLE);
        if(winAmount == 0) {
            mResultTextView.setTextColor(getResources().getColor(R.color.result_lost));
        } else {
            mResultTextView.setTextColor(getResources().getColor(R.color.result_won));
        }
        mResultTextView.setText(getString(R.string.you_won, mWinAmount));
    }

    private void onGameOver() {
        mMinButton.setEnabled(false);
        mDecreaseButton.setEnabled(false);
        mIncreaseButton.setEnabled(false);
        mMaxButton.setEnabled(false);
        mSpinButton.setEnabled(false);
        mMaxAndSpinButton.setEnabled(false);
        mCollectButton.setEnabled(false);

        final Dialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.game_over_title))
                .setMessage(getString(R.string.game_over_message))
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onBalanceChanged(INITIAL_BALANCE, false);
                        onBetChanged(MIN_BET);
                        mMinButton.setEnabled(true);
                        mDecreaseButton.setEnabled(true);
                        mIncreaseButton.setEnabled(true);
                        mMaxButton.setEnabled(true);
                        mSpinButton.setEnabled(true);
                        mMaxAndSpinButton.setEnabled(true);
                        mCollectButton.setEnabled(false);
                    }
                })
                .setCancelable(false)
                .create();

        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private boolean winOccurred() {
        final List<Integer> imageIds = new ArrayList<Integer>(3);

        for(int id : mWheels) {
            final WheelView wheel = getWheel(id);
            final SlotMachineAdapter adapter = (SlotMachineAdapter) wheel.getViewAdapter();
            imageIds.add(adapter.getImageIdForPosition(wheel.getCurrentItem()));
        }

        if(getCherryCount(imageIds) != 0) {
            onResultChanged(getCherryCount(imageIds) * mBet * 2);
        } else if(isSingleGoldLineCase(imageIds)) {
            onResultChanged(mBet * 25);
        } else if(isDoubleGoldLineCase(imageIds)) {
            onResultChanged(mBet * 50);
        } else if(isTripleGoldLineCase(imageIds)) {
            onResultChanged(mBet * 100);
        } else if(isGoldLineCase(imageIds)) {
            onResultChanged(mBet * 5);
        } else if(isSevensLineCase(imageIds)) {
            if(mBet == MAX_BET) {
                onResultChanged(mBet * 500);
            } else {
                onResultChanged(mBet * 300);
            }
        } else {
            onResultChanged(0);
        }

        return mWinAmount > 0;
    }

    private int getCherryCount(List<Integer> imageIds) {
        int cherryCount = 0;

        for(int id : imageIds) {
            cherryCount += (id == R.drawable.img_cherry) ? 1 : 0;
        }
        return cherryCount;
    }

    private boolean isTripleGoldLineCase(List<Integer> imageIds) {
        boolean isTripleGold = true;

        for(int id : imageIds) {
            isTripleGold &= (id == R.drawable.img_gold_triple);
        }

        return isTripleGold;
    }

    private boolean isDoubleGoldLineCase(List<Integer> imageIds) {
        boolean isDoubleGold = true;

        for(int id : imageIds) {
            isDoubleGold &= (id == R.drawable.img_gold_double);
        }

        return isDoubleGold;
    }

    private boolean isSingleGoldLineCase(List<Integer> imageIds) {
        boolean isSingleGold = true;

        for(int id : imageIds) {
            isSingleGold &= (id == R.drawable.img_gold_single);
        }

        return isSingleGold;
    }

    private boolean isGoldLineCase(List<Integer> imageIds) {
        return !(imageIds.contains(R.drawable.img_cherry)
                || imageIds.contains(R.drawable.img_seven)
                || imageIds.contains(R.drawable.img_blank));
    }

    private boolean isSevensLineCase(List<Integer> imageIds) {
        boolean isSeven = true;

        for(int id : imageIds) {
            isSeven &= (id == R.drawable.img_seven);
        }

        return isSeven;
    }

    private class SlotMachineAdapter extends AbstractWheelAdapter {

        final int IMAGE_SIZE = 128;

        final ViewGroup.LayoutParams mLayoutParams = new ViewGroup.LayoutParams(IMAGE_SIZE, IMAGE_SIZE);
        private final List<Bitmap> mImages;
        private final Context mContext;
        private final List<Integer> mItems = new ArrayList<Integer>();
        @SuppressLint("UseSparseArrays")
        private Map<Integer, Integer> mPositionToIdMap = new HashMap<Integer, Integer>(SLOT_ITEMS_COUNT);

        public SlotMachineAdapter(Context context) {
            mContext = context;

            for(int id : mImageIds) {
                mItems.add(id);
            }

            Collections.shuffle(mItems);

            mImages = new ArrayList<Bitmap>(SLOT_ITEMS_COUNT);
            int currentPosition = 0;
            for (int id : mItems) {
                mImages.add(loadImage(id));
                mPositionToIdMap.put(currentPosition++, id);
                mImages.add(loadImage(R.drawable.img_blank));
                mPositionToIdMap.put(currentPosition++, R.drawable.img_blank);
            }
        }

        private Bitmap loadImage(int id) {
            final Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), id);
            final Bitmap scaled = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, true);
            if (scaled != null) {
                bitmap.recycle();
            }
            return scaled;
        }

        @Override
        public int getItemsCount() {
            return mImages.size();
        }

        @Override
        public View getItem(int index, View cachedView, ViewGroup parent) {
            final ImageView img;
            if (cachedView != null) {
                img = (ImageView) cachedView;
            } else {
                img = new ImageView(mContext);
            }
            img.setLayoutParams(mLayoutParams);
            img.setImageBitmap(mImages.get(index));

            return img;
        }

        private int getImageIdForPosition(int position) {
           return mPositionToIdMap.get(position);
        }

    }

}
