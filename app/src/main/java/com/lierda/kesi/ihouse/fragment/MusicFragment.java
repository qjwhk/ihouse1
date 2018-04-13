package com.lierda.kesi.ihouse.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.lierda.kesi.ihouse.R;
import com.lierda.kesi.ihouse.activity.MainActivity;

/**
 * Created by Administrator on 2017/12/14.
 */

public class MusicFragment extends Fragment{
    private Button btn_swicth;


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_app, container, false);
        init(view);
        return view;
    }

    private void init(View view){
        btn_swicth=view.findViewById(R.id.btn_swicth);
        btn_swicth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).switchPage("main");
            }
        });
    }
}
