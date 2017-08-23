package com.CYK;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;
@SuppressWarnings("ALL")
public class CYK {

   public static ArrayList<Long> times = new ArrayList<Long>();

    public static tableItem computeForSlot(int x,int y,triangleTable table){
        // collect all items going down and diagonal from this slot
        ArrayList<tableItem> forUnion = new ArrayList<tableItem>();
        int downStartX = x;
        int downStartY = 0;

        int diagonalStartX = x + 1;
        int diagonalStartY = y - 1;
        for(int steps = 0; steps < y;steps++){
            tableItem downItem = table.get(downStartX,downStartY);
            tableItem diagonalItem = table.get(diagonalStartX,diagonalStartY);
            tableItem multiplied = table.multiply(downItem,diagonalItem,x,y);
            forUnion.add(multiplied);
            downStartY+=1;
            diagonalStartX+=1;
            diagonalStartY-=1;
        }

        tableItem finalitem = table.union(forUnion,x,y);

        return finalitem;
    }

    public static boolean runCYK(String grammarFile,String toCheck) throws IOException {
        int SIZE = toCheck.length();
        triangleTable table = new triangleTable(SIZE,grammarFile);

        // compute the bottom row first.
        for(int i = 0; i < SIZE;i++){
            Collection<String> RHS = table.grammar.
                    getReverse(String.valueOf(toCheck.charAt(i)));
            if(RHS.size() == 0) {
                table.table[i][0].isNull = true;
            } else {
                table.table[i][0].vals = new ArrayList<String>(RHS);
            }
            table.table[i][0].evaluated.compareAndSet(false,true);
        }



        // now do all the rest.

        long startTime = System.currentTimeMillis();

       for(int  y = 1; y < SIZE;y++)
           for (int x = 0; x < SIZE; x++) {
               if (x + y >= SIZE) {
                   continue;
               } else {
                   //System.out.println(x);
                   table.table[x][y] = computeForSlot(x, y, table);
               }
           }


        long endTime   = System.currentTimeMillis();

        long totalTime = endTime - startTime;
        //System.out.println("Time Taken " + totalTime);
        times.add(endTime-startTime);

        //table.table[0][SIZE-1].print();


        return true;
    }


    public static void main(String[] args) throws IOException {

        String test = "baabbaabbaabbaabbaabbaabbaabbaabbaabbaabbaabbaabbaabbaabbaabbaabbaabbaabbaabbababababababbababababababaabbaabababababbaababababbaaab";
        String testTwo = "baaba";

        String test0 = "baaba";
        String test1 = "ababbaabbabababababa";
        String test2 = "ababbaabbabababababaababbaabbabababababa";
        String test3 = "ababbaabbabababababaababbaabbabababababaababbaabbabababababa";
        String test4 = "ababbaabbabababababaababbaabbabababababaababbaabbabababababaababbaabbabababababa";
        String test5 = "ababbaabbabababababaababbaabbabababababaababbaabbabababababaababbaabbabababababaababbaabbabababababa";
        String test6 = test5+ "ababbaabbabababababa";
        String test7 = test6 + "ababbaabbabababababa";

        String [] tests = {test0,test1,test2,test3,test4,test5,test6,test7};


        System.out.println("RUNNING CYK SERIAL ");
        //System.out.println("STRING TO TEST IS: " + test);

        for(int x = 0 ; x < tests.length;x++) {
            for (int i = 0; i < 10; i++) {
                //System.out.println("Test Number: " + i);
                runCYK("grammar.txt", tests[x]);
            }
            long sum = 0;
            for(long time:times){
                sum+=time;
            }
            System.out.println("Average Time " + sum/times.size());
            times.clear();
        }




    }
}