package com.wuyr.pathview;

import android.graphics.Path;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by wuyr on 17-12-17 上午12:44.
 */

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main_view);
        init();
    }

    PathView pathView1;
    PathView pathView2;
    PathView pathView3;
    PathView pathView4;
    PathView pathView5;
    PathView pathView6;

    private void init() {
        pathView1 = findViewById(R.id.path_view1);
        pathView2 = findViewById(R.id.path_view2);
        pathView3 = findViewById(R.id.path_view3);
        pathView4 = findViewById(R.id.path_view4);
        pathView5 = findViewById(R.id.path_view5);
        pathView6 = findViewById(R.id.path_view6);

        Path path1 = new Path();
        Path path2 = new Path();
        Path path3;
        Path path4;
        Path path5;
        Path path6;

        path1.moveTo(310, 0);
        path2.moveTo(410, 0);

        path1.lineTo(310, 400);
        path1.lineTo(210, 500);
        path1.lineTo(210, 600);
        path1.lineTo(310, 700);
        path1.lineTo(310, 1280);

        path2.lineTo(410, 400);
        path2.lineTo(510, 500);
        path2.lineTo(510, 600);
        path2.lineTo(410, 700);
        path2.lineTo(410, 1280);

        path3 = new Path(path1);
        path3.offset(-100, 0);
        path5 = new Path(path1);
        path5.offset(-200, 0);
        path4 = new Path(path2);
        path4.offset(+100, 0);
        path6 = new Path(path2);
        path6.offset(+200, 0);

        pathView1.setPath(path1);
        pathView2.setPath(path2);
        pathView3.setPath(path3);
        pathView4.setPath(path4);
        pathView5.setPath(path5);
        pathView6.setPath(path6);

        pathView1.setLineWidth(5);
        pathView2.setLineWidth(5);
        pathView3.setLineWidth(5);
        pathView4.setLineWidth(5);
        pathView5.setLineWidth(5);
        pathView6.setLineWidth(5);

        pathView3.setMode(PathView.TRAIN_MODE);
        pathView4.setMode(PathView.TRAIN_MODE);

        pathView6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pathView1.startAnimation();
                pathView2.startAnimation();
                pathView3.startAnimation();
                pathView4.startAnimation();
                pathView5.startAnimation();
                pathView6.startAnimation();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        pathView6.performClick();
    }
}
