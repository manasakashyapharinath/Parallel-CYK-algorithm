package com.CYK;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by irtazasafi on 11/22/16.
 */
public class CYK_PARALLEL_TWO {

    // impose JOB ORDERING

    public static ArrayList<Long> times = new ArrayList<Long>();


    public static ArrayList<tableItem> jobs;
    public static triangleTable table;

    public static int NUM_THREADS = 4;

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
            while(!diagonalItem.evaluated.get()){}
            tableItem multiplied = table.multiply(downItem,diagonalItem,x,y);
            forUnion.add(multiplied);
            downStartY+=1;
            diagonalStartX+=1;
            diagonalStartY-=1;
        }
        return table.union(forUnion,x,y);
    }

    public static boolean jobsRemaining() {
        for(tableItem job:jobs){
            if(job.evaluated.get()){
                return true;
            }
        }
        return false;
    }

    public static tableItem getQueuedJob(){
        for(tableItem job:jobs){
            if(!job.evaluated.get()){

                // try and set it to inProgress, if this fails that means another thread is doing this Job
                if(!job.inProgress.compareAndSet(false,true)){
                    return null;
                } else {
                    return job;
                }

            }
        }
        return null;
    }

    public static void runCYKOrderedJobs(String fileName, String toCheck) throws IOException {

        int SIZE = toCheck.length();
        jobs = new ArrayList<>();
        table = new triangleTable(toCheck.length(),fileName);
        int jobPriority = 0;
        for(int i = 0; i < SIZE;i++){
            for(int j = 0; j < SIZE;j++){
                if(table.table[j][i].coordinate.equals("~")){
                    continue;
                } else {
                    table.table[j][i].jobPriority = jobPriority;
                    jobs.add(table.table[j][i]);
                    jobPriority++;
                }
            }
        }

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

        AtomicInteger jobsLeft = new AtomicInteger(jobPriority - SIZE);



//                jobs.forEach(item->{
//            System.out.println(item.jobPriority);
//        });

        ArrayList<Thread> workerThreads = new ArrayList<Thread>();

        for(int i = 0; i < NUM_THREADS;i++){
            workerThreads.add(new Thread(()->{
                while(jobsLeft.get()!=0){
                    // ask a thread to do a job in the list.

                    tableItem toSolve = getQueuedJob();
                    if(toSolve == null){
                        continue;
                    } else {
                        try {
                            tableItem solved = computeForSlot(toSolve.xCor,toSolve.yCor);
                            toSolve.vals = solved.vals;
                            toSolve.isNull = solved.isNull;
                            toSolve.evaluated.compareAndSet(false,true);
                            jobsLeft.decrementAndGet();

                            //System.out.println("Jobs Left " + jobsLeft.get());
                            if(jobsLeft.get() == 1){
                                //table.table[0][SIZE-1].print();
                            }

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }));
        }

        workerThreads.forEach(Thread::start);
        long startTime = System.currentTimeMillis();
        workerThreads.forEach((thread) -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        long endTime   = System.currentTimeMillis();
        times.add(endTime-startTime);
       // System.out.println("Time Taken " + (endTime-startTime));
        table.table[0][SIZE-1].print();

    }

    public static void main(String[] args) throws IOException {
        String testTwo = "baaba";
        String test = "baabbaabbaabbaabbaabbaabbaabbaabbaabbaabbaabbaabbaabbaabbaabbaabbaabbaabbaabbababababababbababababababaabbaabababababbaababababbaaab";



        System.out.println("RUNNING CYK ORDERED JOBS ALGORITHM");




        String test0 = "baaba";
        String test1 = "ababbaabbabababababa";
        String test2 = "ababbaabbabababababaababbaabbabababababa";
        String test3 = "ababbaabbabababababaababbaabbabababababaababbaabbabababababa";
        String test4 = "ababbaabbabababababaababbaabbabababababaababbaabbabababababaababbaabbabababababa";
        String test5 = "ababbaabbabababababaababbaabbabababababaababbaabbabababababaababbaabbabababababaababbaabbabababababa";
        String test6 = test5+ "ababbaabbabababababa";
        String test7 = test6 + "ababbaabbabababababa";


        String [] tests = {test0,test1,test2,test3,test4,test5,test6,test7};


       // System.out.println("RUNNING CYK SERIAL ");
        //System.out.println("STRING TO TEST IS: " + test);

        for(int x = 0 ; x < tests.length;x++) {
            for (int i = 0; i < 10; i++) {
                //System.out.println("Test Number: " + i);
                runCYKOrderedJobs("grammar.txt", tests[x]);
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
