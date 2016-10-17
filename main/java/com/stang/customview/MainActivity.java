package com.stang.customview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    MyView myView1;
    MyView myView2;
    MyView myView4;
    MyView myView5;
    RectAnimationView myView3;

    TextView myTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myTextView = (TextView) findViewById(R.id.tv);

        myView3 = (RectAnimationView) findViewById(R.id.v3);
        myView2 = (MyView) findViewById(R.id.v2);
        myView5 = (MyView) findViewById(R.id.v5);
        myView4 = (MyView) findViewById(R.id.v4);
        myView1 = (MyView) findViewById(R.id.v1);

        myView1.setSpeed(99);
        myView1.startAnimation();

        myView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myView2.isRunning()){
                    myView2.stopAnimation();
                } else {
                    myView2.startAnimation();
                }
            }
        });

        myView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myView3.isRunning()){
                    myView3.stopAnimation();
                } else {
                    myView3.startAnimation();
                }
            }
        });

        myView4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myView4.isRunning()){
                    myView4.stopAnimation();
                } else {
                    myView4.startAnimation();
                }
            }
        });

        myView5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myView5.isRunning()){
                    myView5.stopAnimation();
                } else {
                    myView5.startAnimation();
                }
            }
        });

        myView3.setOnAnimationEventListener(new MyView.OnAnimationEventListener() {
            @Override
            public void onAnimationStarted() {
                myTextView.setText("Animation of myView3 STARTED");
            }

            @Override
            public void onAnimationStopped() {
                myTextView.setText("Animation of myView3 STOPPED");
            }

            @Override
            public void onAnimationCollapsed() {
                myTextView.setText("Animation of myView3 collapsed");
            }

            @Override
            public void onAnimationExploded() {
                myTextView.setText("Animation of myView3 exploded");
            }
        });

    }



}
