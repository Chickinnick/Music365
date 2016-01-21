package com.divarc.music365.player;

public class SwipeHandler{} /*extends Fragment implements
        View.OnTouchListener {

     private static int cur_index_brightness = 1, cur_index_volume = 1;

    float videoRatio;
    float zoomCoefX, zoomCoefY;
    private boolean inTouch;
    private float oldSpacing, newSpacing;
    private boolean fadeInHelper = false;
    private int mVideoWidth;
    private int mVideoHeight;

    private SurfaceView mPreview;
    private SurfaceHolder holder;
    private Handler f;
    private int mVideoLayout = 0;
    private int tmpVHeight, tmpVWidth;
    private float oldDifferentSpacing = 0, differentSpacing = 0;
    private float accuracySpacing = 0.1f;
    private RelativeLayout layoutZoom;
    private FrameLayout rangeBarContainer, controllerContainer;
    private RelativeLayout controllerContainer1;
    private int heightFrameL;
    private RangeBar volumeBar, lightBar;
    private float downY = 0;
    private AudioManager am;
    private Point size;
    private Handler h;
    private CenterLayout centerLayout;
    private Context mContext;
    private ProgressBar progressBar;
    private boolean stateChange = false;
    private RelativeLayout volumeContainer;
    private RelativeLayout brightnessContainer;
    private float xPre = 0;
    private int sX, sY;

     @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View v = inflater.inflate(R.layout.fragment_surface, container, false);

        mContext = getActivity();
        am = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);

        mPreview = (SurfaceView) v.findViewById(R.id.surface);
        holder = mPreview.getHolder();
        holder.addCallback(a);
        holder.setFormat(PixelFormat.RGBA_8888);

        rangeBarContainer = (FrameLayout) getActivity().findViewById(R.id.rangeBarContainer);

        controllerContainer1 = (RelativeLayout) getActivity().findViewById(R.id.controllerContainer1);
//        centerLayout = (CenterLayout) v.findViewById(R.id.container);

        volumeContainer = (RelativeLayout) getActivity().findViewById(R.id.volume_bar_container);
        brightnessContainer = (RelativeLayout) getActivity().findViewById(R.id.light_bar_container);

        lightBar = (RangeBar) getActivity().findViewById(R.id.light_bar);
        volumeBar = (RangeBar) getActivity().findViewById(R.id.volume_bar);

        volumeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex,
                                              String leftPinValue, String rightPinValue) {
                GlobalUtils.setSystemStreamVolume(am, rightPinIndex * 0.1f);
            }
        });

        lightBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex,
                                              String leftPinValue, String rightPinValue) {
                WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
                lp.screenBrightness = rightPinIndex * 0.1f;
                getActivity().getWindow().setAttributes(lp);
            }
        });

        volumeBar.setSeekPinByIndex((int) cur_index_volume);
        cur_index_brightness = (int) (MainActivity.getCurBrightnessValue() * 10) / 255;
        lightBar.setSeekPinByIndex(cur_index_brightness);

        layoutZoom = (RelativeLayout) getActivity().findViewById(R.id.zoommy);
        layoutZoom.setOnTouchListener(this);

        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.screenBrightness = cur_index_brightness * 0.1f;
        getActivity().getWindow().setAttributes(lp);
        GlobalUtils.setSystemStreamVolume(am, cur_index_volume * 0.1f);

        f = new Handler();

        if (flag == true) {
            holder = mPreview.getHolder();
            holder.addCallback(a);
            holder.setFormat(PixelFormat.RGBA_8888);
            setSizeVideo();
        }

        Display display = getActivity().getWindowManager().getDefaultDisplay();

        size = new Point();
        display.getSize(size);

        h = new Handler();
        if (hideControllerBar)
            h.postDelayed(fadeOut, 5000);
        progressBar = (ProgressBar) getActivity().findViewById(R.id.progress);
        return v;
    }

     public void setVideoLayout(int layout, float aspectRatio) {
        ViewGroup.LayoutParams lp = mPreview.getLayoutParams();
        Pair<Integer, Integer> res = ScreenResolution.getResolution(getActivity());
        int windowWidth = res.first.intValue(), windowHeight = res.second.intValue();

        float windowRatio = windowWidth / (float) windowHeight;
        float videoRatio = aspectRatio <= 0.01f ? 2f : aspectRatio;

        int mSurfaceHeight = mVideoHeight;
        int mSurfaceWidth = mVideoWidth;

        if (VideoView.VIDEO_LAYOUT_ORIGIN == layout
                && mSurfaceWidth < windowWidth
                && mSurfaceHeight < windowHeight) {
            lp.width = (int) (mSurfaceHeight * videoRatio);
            lp.height = mSurfaceHeight;
            tmpVWidth = (int) (mSurfaceHeight * videoRatio);
            tmpVHeight = mSurfaceHeight;

        } else if (layout == VideoView.VIDEO_LAYOUT_ZOOM) {
            lp.width = windowRatio > videoRatio ? windowWidth : (int) (videoRatio * windowHeight);
            lp.height = windowRatio < videoRatio ? windowHeight : (int) (windowWidth / videoRatio);
            tmpVWidth = windowRatio < videoRatio ? windowHeight : (int) (windowWidth * videoRatio);
            tmpVHeight = windowRatio < videoRatio ? windowHeight : (int) (windowWidth / videoRatio);

        } else if (layout == VideoView.VIDEO_LAYOUT_FIT_PARENT) {
            ViewGroup parent = (ViewGroup) mPreview.getParent();
            float parentRatio = ((float) parent.getWidth()) / ((float) parent.getHeight());
            lp.width = (parentRatio < videoRatio) ? parent.getWidth() : Math.round(((float) parent.getHeight()) * videoRatio);
            lp.height = (parentRatio > videoRatio) ? parent.getHeight() : Math.round(((float) parent.getWidth()) / videoRatio);
            tmpVHeight = (parentRatio > videoRatio) ? parent.getHeight() : Math.round(((float) parent.getWidth()) / videoRatio);
            tmpVWidth = (parentRatio < videoRatio) ? parent.getWidth() : Math.round(((float) parent.getHeight()) * videoRatio);
        } else {
            boolean full = layout == VideoView.VIDEO_LAYOUT_STRETCH;
            lp.width = (full || windowRatio < videoRatio) ? windowWidth : (int) (videoRatio * windowHeight);
            lp.height = (full || windowRatio > videoRatio) ? windowHeight : (int) (windowWidth / videoRatio);
            tmpVHeight = (full || windowRatio > videoRatio) ? windowHeight : (int) (windowWidth / videoRatio);
            tmpVWidth = (full || windowRatio < videoRatio) ? windowWidth : (int) (videoRatio * windowHeight);
        }

        mPreview.setLayoutParams(lp);
        holder.setFixedSize(mSurfaceWidth, mSurfaceHeight);
        io.vov.vitamio.utils.Log.d("VIDEO: %dx%dx%f, Surface: %dx%d, LP: %dx%d, Window: %dx%dx%f",
                mVideoWidth, mVideoHeight, 1, mSurfaceWidth, mSurfaceHeight, lp.width, lp.height,
                windowWidth, windowHeight, windowRatio);
        mVideoLayout = layout;
    }


    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // событие
        centerLayout.post(new Runnable() {
            @Override
            public void run() {
                sX = centerLayout.getWidth();
                sY = centerLayout.getHeight();
            }
        });

        int actionMask = event.getActionMasked();
        float x = event.getX();
        float y = event.getY();

        heightFrameL = volumeBar.getHeight() / 10;

        switch (actionMask) {
            case MotionEvent.ACTION_DOWN: // первое касание

                inTouch = true;
                if (event.getPointerCount() <= 1) {
                    xPre = x;
                    if (event.getX() < (sY / 2)) {
                        lightBar.setVisibility(View.VISIBLE);
                        volumeBar.setVisibility(View.INVISIBLE);
                        downY = y;
                    } else {
                        lightBar.setVisibility(View.INVISIBLE);
                        volumeBar.setVisibility(View.VISIBLE);
                        downY = y;
                    }
                }

            case MotionEvent.ACTION_POINTER_DOWN: // последующие касания

                if (event.getPointerCount() > 1) {
                    oldSpacing = spacing(event);
                    stateChange = true;
                }
                lightBar.setVisibility(View.INVISIBLE);
                volumeBar.setVisibility(View.INVISIBLE);
                break;

            case MotionEvent.ACTION_UP: // прерывание последнего касания

                if (event.getPointerCount() == 1)
                    if ((int) (x - xPre) <= 10 || (int) (-1 * (x - xPre)) <= 10) {
                        if (!stateChange) {
                            hideControllerContainer();
                        }
                    }

                brightnessContainer.setVisibility(View.INVISIBLE);
                volumeContainer.setVisibility(View.INVISIBLE);
                lightBar.setVisibility(View.INVISIBLE);
                volumeBar.setVisibility(View.INVISIBLE);

                if (hideControllerBar)
                    h.postDelayed(fadeOut, 5000);

                stateChange = false;
                break;

            case MotionEvent.ACTION_POINTER_UP: // прерывания касаний
                oldDifferentSpacing = 0;
                break;

            case MotionEvent.ACTION_MOVE: // движение
                changeStateRangeBars(event, y);
                break;
        }
        return true;

    }

    private void changeStateRangeBars(MotionEvent event, float y) {

        if (event.getPointerCount() > 1 && event.getPointerCount() < 3) {

            newSpacing = spacing(event);
            oldDifferentSpacing = differentSpacing;
            differentSpacing = (newSpacing / oldSpacing);

            if (differentSpacing > oldDifferentSpacing) {

                accuracySpacing = differentSpacing - oldDifferentSpacing;

                if (accuracySpacing > 0.0035) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            for (int i = 0; i < 15; i++) {
                                f.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        if (tmpVWidth < mVideoWidth * 4 && tmpVHeight < mVideoHeight * 4) {

                                            ViewGroup.LayoutParams lp = mPreview.getLayoutParams();
                                            tmpVHeight += 1;
                                            //  tmpVWidth += 1;

                                            tmpVWidth = (int) (tmpVHeight * videoRatio);

                                            lp.height = tmpVHeight;
                                            lp.width = tmpVWidth;

                                            mPreview.setLayoutParams(lp);
                                        }
                                    }

                                });
                                try {
                                    TimeUnit.MILLISECONDS.sleep(10);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();
                }
            } else {
                accuracySpacing = differentSpacing - oldDifferentSpacing;

                if (accuracySpacing < -0.0035) {

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < 15; i++) {
                                f.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        ViewGroup.LayoutParams lp = mPreview.getLayoutParams();

                                        if (tmpVWidth > mVideoWidth / 4 && tmpVHeight > mVideoHeight / 4) {

                                            tmpVHeight -= 1;

                                            tmpVWidth = (int) (tmpVHeight * videoRatio);

                                            lp.height = tmpVHeight;
                                            lp.width = tmpVWidth;

                                            mPreview.setLayoutParams(lp);
                                        }
                                    }
                                });
                                try {
                                    TimeUnit.MILLISECONDS.sleep(10);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();
                }
            }
        } else {
            if (event.getX() < (sX / 2)) {
                if (heightFrameL < (downY - y)) {
                    if (cur_index_brightness < 10) {
                        cur_index_brightness++;
                        brightnessContainer.setVisibility(View.VISIBLE);
                        lightBar.setVisibility(View.VISIBLE);
                        lightBar.setSeekPinByIndex((int) cur_index_brightness);
                        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
                        lp.screenBrightness = cur_index_brightness * 0.1f;// 100 / 100.0f;
                        getActivity().getWindow().setAttributes(lp);
                        downY = y;

                        stateChange = true;
                    }
                }
                if (heightFrameL < (-1 * (downY - y))) {
                    if (cur_index_brightness > 0) {
                        cur_index_brightness--;
                        brightnessContainer.setVisibility(View.VISIBLE);
                        lightBar.setVisibility(View.VISIBLE);
                        lightBar.setSeekPinByIndex((int) cur_index_brightness);
                        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
                        lp.screenBrightness = cur_index_brightness * 0.1f;// 100 / 100.0f;
                        getActivity().getWindow().setAttributes(lp);
                        downY = y;

                        stateChange = true;
                    }
                }
            } else {
                if (heightFrameL < (downY - y)) {
                    if (cur_index_volume < 10) {
                        cur_index_volume++;
                        volumeContainer.setVisibility(View.VISIBLE);
                        volumeBar.setVisibility(View.VISIBLE);
                        volumeBar.setSeekPinByIndex((int) cur_index_volume);
                        GlobalUtils.setSystemStreamVolume(am, cur_index_volume * 0.1f);

                        downY = y;

                        stateChange = true;
                    }
                }
                if (heightFrameL < (-1 * (downY - y))) {
                    if (cur_index_volume > 0) {
                        cur_index_volume--;
                        volumeContainer.setVisibility(View.VISIBLE);
                        volumeBar.setVisibility(View.VISIBLE);
                        volumeBar.setSeekPinByIndex((int) cur_index_volume);
                        GlobalUtils.setSystemStreamVolume(am, cur_index_volume * 0.1f);

                        downY = y;

                        stateChange = true;
                    }
                }
            }
        }
    }

*/



