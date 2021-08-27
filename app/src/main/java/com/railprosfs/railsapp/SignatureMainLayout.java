package com.railprosfs.railsapp;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import com.railprosfs.railsapp.utility.ExpClass;
import com.railprosfs.railsapp.utility.Functions;
import com.railprosfs.railsapp.utility.KTime;

import static com.railprosfs.railsapp.utility.Constants.DOC_SIGNATURES;

public class SignatureMainLayout extends LinearLayout implements OnClickListener {
    public String PICTURE_NAME;
    LinearLayout buttonsLayout;
        public SignatureView signatureView;
    public Button exitButton;
    public Button saveBtn;

    public SignatureMainLayout(Context context, AttributeSet attr) {
        super(context, attr);

        this.setOrientation(LinearLayout.VERTICAL);

        this.buttonsLayout = this.buttonsLayout();
        this.signatureView = new SignatureView(context);

        // add the buttons and signature views
        this.addView(this.buttonsLayout);
        this.addView(signatureView);

    }


    private LinearLayout buttonsLayout() {

        // create the UI programatically
        LinearLayout linearLayout = new LinearLayout(this.getContext());
        saveBtn = new Button(this.getContext());
        Button clearBtn = new Button(this.getContext());
        exitButton = new Button(this.getContext());

        // set orientation
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setBackgroundColor(Color.GRAY);

        // set texts, tags and OnClickListener
        saveBtn.setText("Save");
        saveBtn.setTag("Save");
        saveBtn.setOnClickListener(this);

        clearBtn.setText("Clear");
        clearBtn.setTag("Clear");
        clearBtn.setOnClickListener(this);

        exitButton.setText("Exit");
        exitButton.setText("Exit");

        linearLayout.addView(saveBtn);
        linearLayout.addView(clearBtn);
        linearLayout.addView(exitButton);
        // return the whoe layout
        return linearLayout;
    }

    // the on click listener of 'save' and 'clear' buttons
    @Override
    public void onClick(View v) {
        String tag = v.getTag().toString().trim();

        // save the signature
        if (tag.equalsIgnoreCase("save")) {
            this.saveImage(this.signatureView.getSignature());
        }

        // empty the canvas
        else {
            this.signatureView.clearSignature();
        }

    }

    final public void saveImg() {
        this.saveImage(signatureView.getSignature());
    }


    /**
     * save the signature to an sd card directory
     * @param signature bitmap
     */
    final void saveImage(Bitmap signature) {

        PICTURE_NAME = KTime.ParseNow(KTime.KT_fmtFileNameFromTime).toString();
        try {
            Functions.SaveImageToFile(DOC_SIGNATURES, PICTURE_NAME, signature, 85);
        } catch (ExpClass kx) {
            PICTURE_NAME = null;  // Note having a file name will let the user know there is a problem.
        } catch (Exception ex) {
            ExpClass.LogEX(ex, "SignatureMainLayout.saveImage - Unable to create image file.");
            PICTURE_NAME = null;  // Note having a file name will let the user know there is a problem.
        }
    }


    /**
     * @return True if the external storage is available. False otherwise.
     */
    public static boolean isAvailable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public static String getSdCardPath() {
        return Environment.getExternalStorageDirectory().getPath() + "/";
    }

    /**
     * @return True if the external storage is writable. False otherwise.
     */
    public static boolean isWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;

    }

    /**
     * The View where the signature will be drawn
     */
    private class SignatureView extends View {

        // set the stroke width
        private static final float STROKE_WIDTH = 5f;
        private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;

        private Paint paint = new Paint();
        private Path path = new Path();

        private float lastTouchX;
        private float lastTouchY;
        private final RectF dirtyRect = new RectF();

        public SignatureView(Context context) {

            super(context);

            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);

            // set the bg color as white
            this.setBackgroundColor(Color.WHITE);

            // width and height should cover the screen
            this.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        }

        /**
         * Get signature
         *
         * @return
         */
        protected Bitmap getSignature() {

            Bitmap signatureBitmap = null;
            signatureBitmap = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.RGB_565);

            // important for saving signature
            if(signatureBitmap != null) {
                final Canvas canvas = new Canvas(signatureBitmap);
                this.draw(canvas);
                signatureBitmap = removeMargins(signatureBitmap, Color.WHITE);
            }
            return signatureBitmap;
        }

        /**
         * clear signature canvas
         */
        private void clearSignature() {
            path.reset();
            this.invalidate();
        }

        // all touch events during the drawing
        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawPath(this.path, this.paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float eventX = event.getX();
            float eventY = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:

                    path.moveTo(eventX, eventY);

                    lastTouchX = eventX;
                    lastTouchY = eventY;
                    return true;

                case MotionEvent.ACTION_MOVE:

                case MotionEvent.ACTION_UP:

                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++) {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);

                        expandDirtyRect(historicalX, historicalY);
                        path.lineTo(historicalX, historicalY);
                    }
                    path.lineTo(eventX, eventY);
                    break;

                default:

                    return false;
            }

            invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

            lastTouchX = eventX;
            lastTouchY = eventY;

            return true;
        }

        private void expandDirtyRect(float historicalX, float historicalY) {
            if (historicalX < dirtyRect.left) {
                dirtyRect.left = historicalX;
            } else if (historicalX > dirtyRect.right) {
                dirtyRect.right = historicalX;
            }

            if (historicalY < dirtyRect.top) {
                dirtyRect.top = historicalY;
            } else if (historicalY > dirtyRect.bottom) {
                dirtyRect.bottom = historicalY;
            }
        }

        private void resetDirtyRect(float eventX, float eventY) {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }

    }

    public void setExitButton(boolean toggle) {
        if(toggle) {
            exitButton.setVisibility(View.VISIBLE);
        }
        else {
            exitButton.setVisibility(View.INVISIBLE);
        }
    }


    //Basic Remove Margin Class
    private static Bitmap removeMargins(Bitmap bmp, int color) {
        long dtMili = System.currentTimeMillis();
        int MTop = 0, MBot = 0, MLeft = 0, MRight = 0;
        boolean found1 = false, found2 = false;

        int[] bmpIn = new int[bmp.getWidth() * bmp.getHeight()];
        int[][] bmpInt = new int[bmp.getWidth()][bmp.getHeight()];

        bmp.getPixels(bmpIn, 0, bmp.getWidth(), 0, 0, bmp.getWidth(),
                bmp.getHeight());

        for (int ii = 0, contX = 0, contY = 0; ii < bmpIn.length; ii++) {
            bmpInt[contX][contY] = bmpIn[ii];
            contX++;
            if (contX >= bmp.getWidth()) {
                contX = 0;
                contY++;
                if (contY >= bmp.getHeight()) {
                    break;
                }
            }
        }

        for (int hP = 0; hP < bmpInt[0].length && !found2; hP++) {
            // looking for MTop
            for (int wP = 0; wP < bmpInt.length && !found2; wP++) {
                if (bmpInt[wP][hP] != color) {
                    Log.e("MTop 2", "Pixel found @" + hP);
                    MTop = hP;
                    found2 = true;
                    break;
                }
            }
        }
        found2 = false;

        for (int hP = bmpInt[0].length - 1; hP >= 0 && !found2; hP--) {
            // looking for MBot
            for (int wP = 0; wP < bmpInt.length && !found2; wP++) {
                if (bmpInt[wP][hP] != color) {
                    Log.e("MBot 2", "Pixel found @" + hP);
                    MBot = bmp.getHeight() - hP;
                    found2 = true;
                    break;
                }
            }
        }
        found2 = false;

        for (int wP = 0; wP < bmpInt.length && !found2; wP++) {
            // looking for MLeft
            for (int hP = 0; hP < bmpInt[0].length && !found2; hP++) {
                if (bmpInt[wP][hP] != color) {
                    Log.e("MLeft 2", "Pixel found @" + wP);
                    MLeft = wP;
                    found2 = true;
                    break;
                }
            }
        }
        found2 = false;

        for (int wP = bmpInt.length - 1; wP >= 0 && !found2; wP--) {
            // looking for MRight
            for (int hP = 0; hP < bmpInt[0].length && !found2; hP++) {
                if (bmpInt[wP][hP] != color) {
                    Log.e("MRight 2", "Pixel found @" + wP);
                    MRight = bmp.getWidth() - wP;
                    found2 = true;
                    break;
                }
            }

        }
        found2 = false;

        int sizeY = bmp.getHeight() - MBot - MTop, sizeX = bmp.getWidth()
                - MRight - MLeft;
        // Not sure why, but occasionally get zero hight/width and do not want to crash.
        if(sizeX == 0 || sizeY == 0) {
            return bmp;
        }
        // This is the case where nothing was entered on the canvas.
        // Pretty sure bottom and right will be minimum 1 with input.
        // By not saving empty signature, easier to do validation.
        if((MBot + MTop + MRight + MLeft) == 0 ) {
            return null;
        }
        Bitmap bmp2 = Bitmap.createBitmap(bmp, MLeft, MTop, sizeX, sizeY);
        dtMili = (System.currentTimeMillis() - dtMili);
        Log.e("Margin   2",
                "Time needed " + dtMili + "mSec\nh:" + bmp.getWidth() + "w:"
                        + bmp.getHeight() + "\narray x:" + bmpInt.length + "y:"
                        + bmpInt[0].length);
        return bmp2;
    }


}