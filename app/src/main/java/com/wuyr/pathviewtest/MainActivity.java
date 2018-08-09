package com.wuyr.pathviewtest;

import android.graphics.Path;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import com.wuyr.pathview.PathView;

/**
 * @author wuyr
 * @since 2018-08-08 下午12:17
 */
public class MainActivity extends AppCompatActivity {

    private ViewGroup mContainer;
    private PathView[] mPathViews;
    private CanvasView mCanvasView;
    private int mLineWidth = 5;
    private long mDuration = 1000;
    private int mMode = PathView.MODE_AIRPLANE;
    private boolean isRepeat;
    private Toast mToast;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main_view);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        mContainer = findViewById(R.id.container);
        mCanvasView = findViewById(R.id.canvas);
        ((Switch) findViewById(R.id.mode)).setOnCheckedChangeListener((compoundButton, b) ->
                mMode = b ? PathView.MODE_TRAIN : PathView.MODE_AIRPLANE);
        ((Switch) findViewById(R.id.repeat)).setOnCheckedChangeListener((compoundButton, b) -> isRepeat = b);
        SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                switch (seekBar.getId()) {
                    case R.id.duration:
                        mDuration = i;
                        break;
                    case R.id.line_width:
                        mLineWidth = i;
                        mCanvasView.setLineWidth(i);
                        break;
                    default:
                        break;
                }
                mToast.setText(String.valueOf(i));
                mToast.show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };
        ((SeekBar) findViewById(R.id.duration)).setOnSeekBarChangeListener(listener);
        ((SeekBar) findViewById(R.id.line_width)).setOnSeekBarChangeListener(listener);
    }

    public void draw(View view) {
        mCanvasView.clear();
        mCanvasView.setVisibility(View.VISIBLE);
        stopAnimations();
    }

    public void play(View view) {
        stopAnimations();
        Path[] paths = mCanvasView.getPaths();
        if (paths.length > 0) {
            mCanvasView.setVisibility(View.INVISIBLE);
            mPathViews = new PathView[paths.length];
            for (int i = 0; i < paths.length; i++) {
                Path path = paths[i];
                PathView pathView = new PathView(this);
                //设置线宽
                pathView.setLineWidth(mLineWidth);
                //动画时长
                pathView.setDuration(mDuration);
                //动画模式
                pathView.setMode(mMode);
                //设置路径
                pathView.setPath(path);
                //重复播放
                pathView.setRepeat(isRepeat);

                mPathViews[i] = pathView;
                mContainer.addView(pathView);
            }
            for (PathView pathView : mPathViews) {
                pathView.start();
            }
        }
    }

    private void stopAnimations() {
        if (mPathViews != null) {
            for (PathView tmp : mPathViews) {
                if (tmp != null) {
                    tmp.stop();
                    mContainer.removeView(tmp);
                }
            }
        }
    }
}
