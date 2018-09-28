package com.dfmabbas.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dfmabbas.reactor.handler.Algorithm;
import com.dfmabbas.reactor.handler.Reactor;

import org.jetbrains.annotations.NotNull;


public class JavaSample extends Fragment {

    private Reactor reactor;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_java_sample, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        reactor = new Reactor(view.getContext(), Algorithm.AES);
        view.findViewById(R.id.btn_java_click).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sampleCode();
            }
        });
    }

    private void sampleCode() {

        reactor.put("name", "abbas");
        reactor.put("age", 23);
        reactor.put("is_man", true);
        reactor.put("array", new int[]{1, 2, 3});
        reactor.put("tc", this.getClass());


        String name = reactor.get("name", "");
        Integer age = reactor.get("age", 0);
        Boolean man = reactor.get("is_man", false);
        int[] array = reactor.get("array", new int[]{0, 0, 0});
        Class tc = reactor.get("tc", this.getClass());


        Log.i("name -> ", name);
        Log.i("age -> ", age.toString());
        Log.i("is_man -> ", man.toString());
        Log.i("array -> ", String.valueOf(array.length));
        Log.i("tc -> ", tc.getName());

        reactor.remove("age", 0);
        reactor.clearAll();
    }
}
