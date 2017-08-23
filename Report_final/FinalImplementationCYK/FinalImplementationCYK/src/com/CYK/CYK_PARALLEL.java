package com.CYK;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;


@SuppressWarnings("ALL")
public class CYK_PARALLEL {

    volatile static triangleTable table;

    public static ArrayList<Long> times = new ArrayList<Long>();


    public static tableItem computeForSlot(int x,int y) throws InterruptedException {
        // collect all items going down and diagonal from this slot
        ArrayList<tableItem> forUnion = new ArrayList<tableItem>();
        int downStartX = x;
        int downStartY = 0;

        int diagonalStartX = x + 1;
        int diagonalStartY = y - 1;
        for(int steps = 0; steps < y;steps++){
            tableItem downItem = table.table[downStartX][downStartY];
            tableItem diagonalItem = table.table[diagonalStartX][diagonalStartY];

          //  System.out.println(Thread.currentThread().getName() + " is waiting for " + diagonalStartX + " " + diagonalStartY + " " + diagonalItem);
            int mark = 0;
            while(diagonalItem.evaluated.get() == false){
                //Thread.sleep(15000);

//                System.out.println(Thread.currentThread().getName() + " IN WAIT LOOP FOR (" + diagonalStartX + ", " + diagonalStartY
//                 + ") VALUE OF evaluated is " + diagonalItem.evaluated.get() + " value of check " + diagonalItem.checkCount +
//                        " object is " + diagonalItem + " " + diagonalItem.coordinate + " " + diagonalItem.vals.size());

                //diagonalItem.print();


            }
            //System.out.println(Thread.currentThread().getName() + " is DONE WAITING for " + diagonalStartX + " " + diagonalStartY);
            tableItem multiplied = table.multiply(downItem,diagonalItem,x,y);
            forUnion.add(multiplied);
            downStartY+=1;
            diagonalStartX+=1;
            diagonalStartY-=1;
        }

        tableItem finalitem = table.union(forUnion,x,y);

        return finalitem;
    }



    public static void runLockFreeCYK(String grammarFile, String toCheck) throws IOException {

        final int SIZE = toCheck.length();
        table = new triangleTable(SIZE,grammarFile);

        // compute bottom row first.

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

        // start a thread to go up each ladder.

        ArrayList<Thread> workers = new ArrayList<Thread>();

        for(int i = 0; i < SIZE-1;i++){
            final int ladderIndex = i;
            workers.add(new Thread(()->{
                final int xCor = ladderIndex;
                int yCor = 1;

                while(xCor + yCor < SIZE) {
                    // climb the ladder;

                    try {
                        tableItem obj = computeForSlot(xCor,yCor);
                        table.table[xCor][yCor].vals = obj.vals;
                        table.table[xCor][yCor].isNull = obj.isNull;
                        table.table[xCor][yCor].coordinate = obj.coordinate;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    table.table[xCor][yCor].evaluated.compareAndSet(false,true);
                    //table.table[xCor][yCor].evaluated.set(true);
                    //System.out.println(xCor + " " + yCor + " evaluated with value of evaluated SET TO " + table.table[xCor][yCor].evaluated.get() + " object is " + table.table[xCor][yCor]);
//                    for(int x = 0 ; x< 10;x++){
//                        table.table[xCor][yCor].checkCount+=1;
//                    }

                    yCor++;
//                    if(xCor + yCor >= SIZE){
//                        System.out.println(Thread.currentThread().getName() + " exiting at " + xCor + " " + (yCor-1));
//                    }
                }
            }));
        }

        workers.forEach(thread -> thread.start());

        long startTime = System.currentTimeMillis();
        workers.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        long endTime   = System.currentTimeMillis();
       // System.out.println();
        //System.out.println("Time Taken " + (endTime-startTime));
        //table.table[0][SIZE-1].print();
        times.add(endTime-startTime);

    }

    public static void test(tableItem item){
        System.out.println(item.coordinate);
    }

    public static void main(String[] args) throws IOException {

        System.out.println("RUNNING CYK LADDER CLIMBING  ALGORITHM");


       String testTwo = "baabaa";
        String test = "baabbaabbaabbaabbaabbaabbaabbaabbaabbaabbaabbaabbaabbaabbaabbaabbaabbaabbaabbaab";

        System.out.println("STRING TO TEST IS " + test);

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
                runLockFreeCYK("grammar.txt", tests[x]);
            }
            long sum = 0;
            for(long time:times){
                sum+=time;
            }
            System.out.println("Average Time " + sum/times.size());
            times.clear();
        }






//        tableItem item = new tableItem("sex",false);
//
//        System.out.println(item.coordinate);
//
//        test(item);
//
//        item.coordinate = "fucki";
//        item.evaluated.compareAndSet(false,true);

        //System.out.println(item);



    }


}
