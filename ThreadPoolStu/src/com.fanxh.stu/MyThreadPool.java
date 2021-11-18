package com.fanxh.stu;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * 模拟线程池实现类：ThreadPoolExecutor
 */
public class MyThreadPool implements Executor {
    private static final AtomicInteger COUNT = new AtomicInteger();
    private BlockingQueue<Runnable> queues;
    private int coreSize; // 核心数量
    private int threadSize = 0; // 记录当前创建线程数量

    public MyThreadPool(int coreSize,BlockingQueue queues){
        this.coreSize = coreSize;
        if(null == queues){
            queues = new LinkedBlockingQueue();
        }
        this.queues = queues;
    }

    public MyThreadPool(int coreSize){
        this(coreSize,null);
    }

    @Override
    public void execute(Runnable command) {
        if(++ threadSize < coreSize){
            // 如果这里用Thread直接执行 那queues队列里面的任务将无法得到执行
//            new Thread(command).start();
            // 所以新建一个worker类代替Thread的角色执行任务，并且循环任务队列 执行任务队列里的任务
            new Worker(command).start();
        }else {
            // 如果当前线程数超过了核心线程数量 则将此任务放入队列 等待有空闲核心线程之后执行
            queues.add(command);
        }
    }

    public static void main(String[] args) {
        MyThreadPool pool = new MyThreadPool(5);
        IntStream.range(0,10).forEach(i->pool.execute(()->{
            if(i == 3){
                try {
                    throw new Exception("hhh");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }  
            System.out.println(i);
        }));
    }

    private class Worker extends Thread {
        private Runnable task;

        public Worker(Runnable command){
            // 定义线程名称
            super(String.format("Worker-%d",COUNT.getAndIncrement()));
            task = command;
        }

        private Runnable getTask(){
            try {
                return queues.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new IllegalStateException(e);
            }
        }

        @Override
        public void run() {
            // 优先执行传入的task task执行完成后置为null 则迭代任务队列里的任务 依次取出执行
            while (null != task || (task = getTask()) != null){
                // 加try包装，以便保证不管task是否运行成功，都能继续执行下面的任务，而不至于无限循环
                // 如果将try去掉 当执行任务异常时，程序将无限循环执行
                try {
                    task.run();
                }finally {
//                    task = null; // 将task置为null 以便循环执行任务队列
                }
            }

        }
    }
}
