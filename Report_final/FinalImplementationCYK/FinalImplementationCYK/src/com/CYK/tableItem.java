package com.CYK;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by irtazasafi on 16/10/2016.
 */
public class tableItem {
    volatile AtomicBoolean evaluated;
    volatile AtomicBoolean inProgress;
    volatile String coordinate;
    volatile ArrayList<String> vals;
    volatile int jobPriority = 0;

    volatile int xCor;
    volatile int yCor;

    boolean isNull;
    tableItem(String _value,boolean _isNull){
        isNull = _isNull;
        coordinate = _value;
        evaluated = new AtomicBoolean(false);
        inProgress = new AtomicBoolean(false);
        vals = new ArrayList<String>();
    }


    void print() {
        if (isNull) {
            System.out.print('#' + " ");
        } else {
            vals.forEach((String val) -> {
                System.out.print(val + " ");
            });
        }
    }

//    tableItem copy(){
//
//        tableItem ret = new tableItem(coordinate,isNull);
//        vals.forEach(val->{
//            ret.vals.add(val);
//        });
//
//
//
//    }
}
