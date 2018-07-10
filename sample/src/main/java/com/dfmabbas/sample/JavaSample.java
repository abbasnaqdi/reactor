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

        reactor = new Reactor(getContext())                        //application context
                //.setDatabaseName("simple_db")                    //optional database name
                //.setSecurityLevel(SecurityLevel.POWERFUL)        //optional cryptographic algorithm
                .build();                                          //build class
    }

    private void sampleCode() {

        //simple

        reactor.remove("age", 0);                        //remove key age -> 0 is a type number
        reactor.clearAll();                                         //clear all key-value

        reactor.put("name", "abbas");                               //put name to string json
        reactor.put("age", 23);                                     //put age to int json
        reactor.put("is_man", true);                                //put is_man to boolean json

        //or ...

        String name = reactor.get("name", "");              //get name of string json
        Integer age = reactor.get("age", 1);                //get age of int json
        Boolean man = reactor.get("is_man", false);         //get is_man of boolean json

        //or ...

        Log.i("name -> ", name);
        Log.i("age -> ", age.toString());
        Log.i("is_man -> ", name);
    }
}
