package com.CYK;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

class triangleTable {
    volatile tableItem [][] table;
    ReentrantLock lock = new ReentrantLock();
    final int size;
    final GRAMMAR_SERIAL grammar;
    triangleTable(int _size,String filename) throws IOException {
        size = _size;
        table = new tableItem[size][size];
        for(int i = 0 ; i < size;i++){
            for(int j = 0; j < size;j++){
                table[j][i] = new tableItem(String.valueOf(j) + String.valueOf(i),false);
                if(i+j >= size){
                    table[j][i].coordinate = "~";
                }
                table[j][i].xCor = j;
                table[j][i].yCor = i;
            }
        }
        grammar = new GRAMMAR_SERIAL(filename);
    }

    tableItem get(int x, int y){
        return table[x][y];
    }

    tableItem multiply(tableItem first, tableItem second,int x, int y){
        if(first.isNull || second.isNull){
            return new tableItem(String.valueOf(x)+String.valueOf(y),true);
        }
        ArrayList<String> answer = new ArrayList<String>();
        first.vals.forEach((String f)->{
            second.vals.forEach((String s) ->{
                answer.add(f+s);
            });
        });

        tableItem ans = new tableItem(String.valueOf(x)+String.valueOf(y),false);
        ans.vals = answer;
        return ans;
    }

    tableItem union(ArrayList<tableItem> items,int x, int y){
        tableItem answer = new tableItem(String.valueOf(x)+String.valueOf(y),false);
        items.forEach(tableItem -> {
            if(!tableItem.isNull){
                tableItem.vals.forEach((String val)->{
                    answer.vals.add(val);
                });
            }
        });
        return reduce(answer);
    }

    tableItem reduce(tableItem item){
        ArrayList<String> reducedVals = new ArrayList<String>();
        item.vals.forEach((String val)->{
            grammar.getReverse(val).forEach(reducedVals::add);
        });
        Set<String> s = new HashSet<String>(reducedVals);
        reducedVals.clear();
        reducedVals.addAll(s);
        item.vals = reducedVals;

        item.isNull = item.vals.size() == 0;
        return item;
    }

    void print(){
        for(int i = size-1;i >=0 ; i--){
            for(int j = 0 ; j < size;j++){
                System.out.print(table[j][i].coordinate + " ");
            }
            System.out.println();
        }
    }

    void set(int x, int y,String _val){
        table[x][y].coordinate = _val;
    }

    void setItem(int x, int y, tableItem item){
        lock.lock();
        table[x][y] = item;
        lock.unlock();
    }
}

