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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_java_sample, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //init reactor db
//        Reactor reactor = new Reactor(this.getContext(), "database", SecurityLevel.NONE);
//
//        //insert or update value data by key
//        reactor.put("name", "abbas");
//        reactor.put("age", 23);
//        reactor.put("is_man", true);
//
//        //get value by key
//        String name = reactor.getString("name", "ghazal");
//        int age = reactor.getInt("age", 1);
//        boolean man = reactor.getBoolean("is_man", false);
//
//        Log.e("name ->", name);
//        Log.e("age ->", String.valueOf(age));
//        Log.e("is_man ->", String.valueOf(man));
    }
}
