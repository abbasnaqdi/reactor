package com.dfmabbas.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dfmabbas.reactor.handler.Reactor;
import com.dfmabbas.reactor.handler.SecurityLevel;


public class JavaSample extends Fragment {

    private Reactor reactor;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_java_sample, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btn_java_click).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sampleCode();
            }
        });

        //init reactor db
        reactor = new Reactor(this.getContext(), "database", SecurityLevel.POWERFUL);
    }

    private void sampleCode() {

        //insert or update value data by key
        reactor.put("name", "abbas");
        reactor.put("age", 23);
        reactor.put("is_man", true);

        //get value by key
        String name = reactor.get("name", "");
        int age = reactor.get("age", 1);
        boolean man = reactor.get("is_man", false);

        Log.i("name ->", name);
        Log.i("age ->", String.valueOf(age));
        Log.i("is_man ->", String.valueOf(man));
    }
}
