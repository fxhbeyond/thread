package com.fanxh.stu;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * ģ���̳߳�ʵ���ࣺThreadPoolExecutor
 */
public class MyThreadPool implements Executor {
    private static final AtomicInteger COUNT = new AtomicInteger();
    private BlockingQueue<Runnable> queues;
    private int coreSize; // ��������
    private int threadSize = 0; // ��¼��ǰ�����߳�����

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
            // ���������Threadֱ��ִ�� ��queues��������������޷��õ�ִ��
//            new Thread(command).start();
            // �����½�һ��worker�����Thread�Ľ�ɫִ�����񣬲���ѭ��������� ִ����������������
            new Worker(command).start();
        }else {
            // �����ǰ�߳��������˺����߳����� �򽫴����������� �ȴ��п��к����߳�֮��ִ��
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
            // �����߳�����
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
            // ����ִ�д����task taskִ����ɺ���Ϊnull ������������������� ����ȡ��ִ��
            while (null != task || (task = getTask()) != null){
                // ��try��װ���Ա㱣֤����task�Ƿ����гɹ������ܼ���ִ����������񣬶�����������ѭ��
                // �����tryȥ�� ��ִ�������쳣ʱ����������ѭ��ִ��
                try {
                    task.run();
                }finally {
//                    task = null; // ��task��Ϊnull �Ա�ѭ��ִ���������
                }
            }

        }
    }
}
