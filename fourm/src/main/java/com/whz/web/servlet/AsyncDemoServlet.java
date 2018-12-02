package com.whz.web.servlet;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

@WebServlet(urlPatterns = "/asyncDemo", asyncSupported = true)
public class AsyncDemoServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.println("进入Servlet的时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()) + ".<br/>");
        out.println();
        out.flush();

        //在子线程中执行业务调用，并由其负责输出响应，主线程退出  
        AsyncContext ctx = req.startAsync();
        ctx.addListener(new AsyncListener() {
            @Override
            public void onStartAsync(AsyncEvent event) throws IOException {
                System.out.println("onStartAsync");
            }

            @Override
            public void onComplete(AsyncEvent event) throws IOException {
                System.out.println("onComplete");
            }

            @Override
            public void onTimeout(AsyncEvent event) throws IOException {
                System.out.println("onTimeout");
            }

            @Override
            public void onError(AsyncEvent event) throws IOException {
                System.out.println("onError");
            }
        });
        // 方式一：
        new Executor(ctx).start();

        // 方式二：
        // ctx.start(new Executor(ctx));

        out.println("结束Servlet的时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()) + ".<br/>");
        out.flush();
    }

    class Executor extends Thread {

        private AsyncContext ctx;

        public Executor(AsyncContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run() {
            try {
                //等待十秒钟，以模拟业务方法的执行
                Thread.sleep(5000);
                PrintWriter out = ctx.getResponse().getWriter();
                out.println("业务处理完毕的时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()) + ".<br/>");
                out.flush();
                ctx.complete();   //完成异步调用。
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}  