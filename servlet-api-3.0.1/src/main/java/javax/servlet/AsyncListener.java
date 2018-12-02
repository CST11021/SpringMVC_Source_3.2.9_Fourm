/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package javax.servlet;

import java.io.IOException;
import java.util.EventListener;

/**
 * Listener that will be notified in the event that an asynchronous
 * operation initiated on a ServletRequest to which the listener had been
 * added has completed, timed out, or resulted in an error.
 *
 * @since Servlet 3.0
 */

/* https://blog.csdn.net/chszs/article/details/46643055
一、Servlet 3.0介绍

    Servlet 3.0作为 JavaEE 6规范中一部分，随着JavaEE 6规范一起发布。该版本在前一版本（Servlet 2.5）的基础上提供了若干新特性用于简化Web
    应用的开发和部署。其中有几项特性的引入让开发者感到非常兴奋，同时也获得了Java社区的一片赞誉之声：

    1）异步处理支持
    在Servlet 3.0版本之前，Servlet线程需要一直阻塞，直到业务处理完毕才能再输出响应，最后才结束该Servlet线程。而有了异步处理特性，Servlet
    线程不再需要一直阻塞，在接收到请求之后，Servlet线程可以将耗时的操作委派给另一个线程来完成，自己在不生成响应的情况下返回至容器。针对业务
    处理较耗时的情况，这可以大幅度降低服务器的资源消耗，并且提高并发处理速度。

    2）新增的注解支持
    Servlet 3.0版本新增了若干注解，用于简化 Servlet、过滤器（Filter）和监听器（Listener）的声明，这使得web.xml部署描述文件不再是必选项了。

    3）功能可插拔支持

二、服务器推技术

    在Servlet 2.5中，页面发送一次请求是顺序执行的，即在Servlet的Service中开启一个线程，线程处理后的结果是无法返回给页面的，Servlet执行完毕后，Response就关闭了，无法将后台更新数据即时更新到页面端。
    服务器推技术的实现
    1）定时发送请求，页面有刷新，不友好
    2）Ajax轮询，然后通过js更新页面数据
    相比前者虽然友好，访问量太大时，服务器会增加压力，小型应用可以考虑用
    3）反向Ajax（即Comet技术）
    利用Http 1.1长连接的特性，即通过轮询，但客户端每次发送请求时服务器不会立即返回响应，而是等待服务器有新数据时才返回或者没有新数据而连接超时返回。
    相比于ajax轮询，减少了服务端压力，但一个缺点，不是所有浏览器都支持。
    在Servlet 3.0中提供了异步支持，当数据返回页面后，Request并没有关闭，当服务器端有数据更新时，就可以推送了。

三、Servlet 3.1规范的改进

    GlassFish 4.0以上版本率先实现了Servlet 3.1规范。Servlet 3.1规范的新特性有：

    1）更便利的注解支持
    提供了@WebServlet、@WebFilter、@WebListener、@WebInitParam等注解的支持

    2）可插拔的设计
    Web模块化：可以将一个项目分成N个模块，然后通过扫描模块下的META-INF/web-fragment.xml进行装配。
    容器启动时的可插拔：使用ServletContainerInitializer实现，可以在容器启动时自动回调其onStartup方法，插入一些功能。
    零XML配置的类SpringMVC：使用ServletContainerInitializer即SpringMVC注解配置实现无XML化的SpringMVC配置。

    3）异步处理支持
    Servlet的异步支持：
    - 通过Servlet提供的异步支持完成了Comet：Streaming（长连接）和Ajax长轮询
    - 使用Servlet提供的AsyncListener进行状态回调
    - 最后通过Ajax长轮询实现了一个聊天室功能
    SpringMVC对Servlet的异步支持：
    - 使用SpringMVC框架提供的异步支持实现Comet：Streaming（长连接）和Ajax长轮询
    - 使用SpringMVC框架提供的Callable实现异步计算
    - 使用SpringMVC框架提供的DeferredResult实现延迟结果（实现Ajax长轮询）
    - Spring框架没有提供长连接实现，具体还得使用原生支持
    - 最后通过ajax长轮询实现了一个聊天室功能

    4）非阻塞I/O
    5）HTTP协议升级

四、Servlet的监听器

    Servlet的监听器即Listener，它可以监听来自客户端的请求、完成服务器端的操作等。通过监听器，可以自动触发一些操作，比如：监听在线用户的数量，当增加一个HttpSession时，就触发一个sessionCreated(HttpSessionEvent se)方法，这样就可以给在线人数加1了。
    常用的监听器接口有：
    1. ServletContextListener
    监听ServletContext。当创建ServletContext时，触发contextInitialized(ServletContextEvent sce)方法；当销毁ServletContext时，触发contextDestroyed(ServletContextEvent sce)方法。
    2. ServletContextAttributeListener
    监听对ServletContext属性的操作，比如增加、删除、修改属性。
    3. HttpSessionListener
    监听HttpSession的操作。当创建一个Session时，触发session Created(HttpSessionEvent se)方法；当销毁一个Session时，触发sessionDestroyed(HttpSessionEvent se)方法。
    4. HttpSessionAttributeListener
    监听HttpSession属性的操作。当在Session增加一个属性时，触发attributeAdded(HttpSessionBindingEvent se)方法；当在Session删除一个属性时，触发attributeRemoved(HttpSessionBindingEvent se)方法；当在Session属性被重新设置时，触发attributeReplaced(HttpSessionBindingEvent se)方法。

    Servlet 3.0的监听器跟Servlet 2.5的监听器差别不大，唯一的区别就是增加了对注解的支持。在3.0以前，监听器的配置需要配置到web.xml文件。在3.0中，监听器的配置既可以放入web.xml文件，还可以使用注解进行配置。对于使用注解的监听器就是在监听器类上使用@WebListener进行注释，这样Web容器就会把这个类当成是一个监听器进行注册和使用。
    如果是采用web.xml配置文件的方式，那么就是这样：

    <listener>
       <listener-class>com.xxx.SessionListener</listener-class>
    </listener>
    <listener>
       <listener-class>com.xxx.ContextListener</listener-class>
    </listener>


五、Servlet 3.0的异步处理

    有时Filter或Servlet在生成响应之前必须等待一些耗时的操作结果以便完成请求处理。而在Servlet中，等待是一个低效的操作，因为这是阻塞操作，会白白占用一个线程或其他一些受限资源。许多线程为了等待一个缓慢的资源（如数据库连接）经常发生阻塞，可能引起线程饥饿，从而降低整个Web容器的服务质量。
    Servlet 3.0引入了异步处理请求的能力，使线程可以返回到容器，从而执行更多的任务。当开始异步处理请求时，另一个线程或回调可以或产生响应，或调用完成（complete）或请求分派（dispatch）。这样，它可以在容器上下文使用AsyncContext.dispatch方法运行。
    一个典型的异步处理事件顺序是：
    1. 请求被接收到，通过一系列如用于验证的标准Filter之后被传递到Servlet。
    2. Servlet处理请求参数及内容体从而确定请求的类型。
    3. 该Servlet发出请求去获取一些资源或数据。
    4. Servlet不产生响应并返回。
    5. 过了一段时间后，所请求的资源变为可用，此时处理线程继续处理事件，要么在同一个线程，要么通过AsyncContext分派到容器中的某个资源上。

    @WebServlet注释和@WebFilter注释有一个属性——asyncSupported，是布尔类型，默认值为false。当asyncSupported设置为true，则应用通过执行startAsync可以启动一个单独的线程进行异步处理，并把请求和响应的引用传递给这个线程，然后退出原始线程所在的容器。这意味着响应将遍历（相反的顺序）与进入时相同的过滤器（或过滤器链）。直到AsyncContext调用complete时响应才会被提交。如果异步任务在容器启动的分派之前执行，且调用了startAsync并返回给容器，此时应用需负责处理请求和响应对象的并发访问。
    从一个 Servlet分派时，把asyncSupported=true设置为false是允许的。这种情况下，当Servlet的Service方法不支持异步退出时，响应会被提交，且容器负责调用AsyncContext的complete，以便所有感兴趣的AsyncListener能得到触发通知。过滤器作为清理要完成的异步任务持有资源的一种机制，也应该使用AsyncListener.onComplete触发。
    从一个同步Servlet分派到另一个异步Servlet是非法的。不过与该点不同的是当应用调用startAsync时将抛出IllegalStateException。这将允许 servlet 只能作为同步的或异步的 Servlet。
    应用在一个与初始请求所用的不同的线程中等待异步任务直到可以直接写响应，这个线程不知道任何过滤器。如果过滤器想处理新线程中的响应，那就必须在处理进入时的初始请求时包装 response，并且把包装的 response 传递给链中的下一个过滤器，并最终交给 Servlet。因此，如果响应是包装的（可能被包装多次，每一个过滤器一次），并且应用处理请求并直接写响应，这将只写响应的包装对象，即任何输出的响应都会由响应的包装对象处理。
    当应用在一个单独的线程中读请求时，写内容到响应的包装对象，这其实是从请求的包装对象读取，并写到响应的包装对象，因此对包装对象操作的所有输入及（或）输出将继续存在。如果应用选择这样做的话，它将可以使用 AsyncContext 从一个新线程发起到容器资源的分派请求。这将允许在容器范围内使用像 JSP 这种内容生成技术。

六、异步监听器接口AsyncListener

    Servlet 3.0为异步处理提供了一个监听器，使用AsyncListener接口表示。此接口负责管理异步事件，它可以监控如下四种事件：
    1. 异步线程开始时，调用AsyncListener的onStartAsync(AsyncEvent event)方法；
    2. 异步线程出错时，调用AsyncListener的onError(AsyncEvent event)方法；
    3. 异步线程执行超时，则调用AsyncListener的onTimeout(AsyncEvent event)方法；
    4. 异步执行完毕时，调用AsyncListener的onComplete(AsyncEvent event)方法；

    要注册一个AsyncListener，只需将准备好的AsyncListener对象传递给AsyncContext对象的addListener()方法即可，如下所示：

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res){
        AsyncContext actx = req.startAsync();
        actx.addListener(new AsyncListener(){
            public void onComplete(AsyncEvent event) throws IOException{
                // do 一些清理工作或者其他
            }

            public void onTimeout(AsyncEvent event) throws IOException{
                // do 一些超时处理的工作或者其他
            }
        });
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(10);
        executor.execute(new MyAsyncService(actx));
    }


七、异步Servlet与AJAX的区别与联系：https://blog.csdn.net/yiifaa/article/details/76642305

    在WEB应用程序的开发中，前端可以通过AJAX提供异步操作解决用户请求阻塞的问题，从而解决界面响应速度较慢的问题，既然如此，为何我们还需要后端
    的异步Servlet呢，这两个功能是否存在重复设计？

    其实对于前端来说，根本不区分后端处理请求是异步的行为还是同步的行为，但对于后端来说，对于用户的每一个请求（是的，你没有看错，不是每一个用户，
    而是每一个请求），都会启动一个独立的线程来处理任务，这样在短时间内会造成大量的线程创建与销毁，服务器将不堪重负。

    为了解决上述的问题，几乎所有的WEB服务器都会以线程池的方式来处理用户请求，那么问题来了，如果有的任务耗时较长，如NP问题，那么它占领的线程
    资源将一直得不到释放，从而导致后续大量的请求因为得不到线程资源而堵塞，服务器的吞吐量直线下降，因此我们需要一种新的机制提高服务器的处理能
    力，这也是异步Servlet的诞生原因，提高请求线程池的处理效率。

    @WebServlet(asyncSupported = true, urlPatterns = { "/sleep" })
    public class SleepServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            response.setContentType("application/json;charset=UTF-8");
            final AsyncContext context = request.startAsync();
            context.start(() -> {
                try {
                //  任务消耗的时间很长
                    Thread.sleep(1000 * 10);
                    ServletResponse resp = context.getResponse();
                    resp.getWriter().println("{\"state\": \"complete\"}");
                    context.complete();
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            });
            System.out.println("Request Completed!");
        }

    }

    此时发送AJAX请求，查看后台信息，发现Request请求已处理完成，控制台输出“Request Completed!”，但监测网络状况，Rquest Headers提示如下信息，见下图：
    Provisional headers are shown

    说明前端还在等待响应，继续等待10秒，后端的异步任务处理完毕，前端的AJAX请求也处理完毕。
    结论：AJAX与异步Servlet都能解决阻塞的问题，但面向的对象与应用的场景都不一样，对于服务器耗时较长的任务，光用AJAX只能解决页面响应的问题，不能解决多用户并发的拥堵问题，而异步Servlet则可以在长任务的场景下，提高服务器的并发数量。

 */

// 用于异步操作的监听器

public interface AsyncListener extends EventListener {

    /**
     * Notifies this AsyncListener that a new asynchronous cycle is being
     * initiated via a call to one of the {@link ServletRequest#startAsync}
     * methods.
     *
     * <p>The {@link AsyncContext} corresponding to the asynchronous
     * operation that is being reinitialized may be obtained by calling
     * {@link AsyncEvent#getAsyncContext getAsyncContext} on the given
     * <tt>event</tt>.
     *
     * <p>In addition, if this AsyncListener had been registered via a call
     * to {@link AsyncContext#addListener(AsyncListener,
     * ServletRequest, ServletResponse)}, the supplied ServletRequest and
     * ServletResponse objects may be retrieved by calling
     * {@link AsyncEvent#getSuppliedRequest getSuppliedRequest} and
     * {@link AsyncEvent#getSuppliedResponse getSuppliedResponse},
     * respectively, on the given <tt>event</tt>.
     *
     * <p>This AsyncListener will not receive any events related to the
     * new asynchronous cycle unless it registers itself (via a call
     * to {@link AsyncContext#addListener}) with the AsyncContext that
     * is delivered as part of the given AsyncEvent.
     *
     * @param event the AsyncEvent indicating that a new asynchronous
     *              cycle is being initiated
     * @throws IOException if an I/O related error has occurred during the
     *                     processing of the given AsyncEvent
     */
    public void onStartAsync(AsyncEvent event) throws IOException;

    /**
     * Notifies this AsyncListener that an asynchronous operation
     * has been completed.
     *
     * <p>The {@link AsyncContext} corresponding to the asynchronous
     * operation that has been completed may be obtained by calling
     * {@link AsyncEvent#getAsyncContext getAsyncContext} on the given
     * <tt>event</tt>.
     *
     * <p>In addition, if this AsyncListener had been registered via a call
     * to {@link AsyncContext#addListener(AsyncListener,
     * ServletRequest, ServletResponse)}, the supplied ServletRequest and
     * ServletResponse objects may be retrieved by calling
     * {@link AsyncEvent#getSuppliedRequest getSuppliedRequest} and
     * {@link AsyncEvent#getSuppliedResponse getSuppliedResponse},
     * respectively, on the given <tt>event</tt>.
     *
     * @param event the AsyncEvent indicating that an asynchronous
     *              operation has been completed
     * @throws IOException if an I/O related error has occurred during the
     *                     processing of the given AsyncEvent
     */
    public void onComplete(AsyncEvent event) throws IOException;

    /**
     * Notifies this AsyncListener that an asynchronous operation
     * has timed out.
     *
     * <p>The {@link AsyncContext} corresponding to the asynchronous
     * operation that has timed out may be obtained by calling
     * {@link AsyncEvent#getAsyncContext getAsyncContext} on the given
     * <tt>event</tt>.
     *
     * <p>In addition, if this AsyncListener had been registered via a call
     * to {@link AsyncContext#addListener(AsyncListener,
     * ServletRequest, ServletResponse)}, the supplied ServletRequest and
     * ServletResponse objects may be retrieved by calling
     * {@link AsyncEvent#getSuppliedRequest getSuppliedRequest} and
     * {@link AsyncEvent#getSuppliedResponse getSuppliedResponse},
     * respectively, on the given <tt>event</tt>.
     *
     * @param event the AsyncEvent indicating that an asynchronous
     *              operation has timed out
     * @throws IOException if an I/O related error has occurred during the
     *                     processing of the given AsyncEvent
     */
    public void onTimeout(AsyncEvent event) throws IOException;

    /**
     * Notifies this AsyncListener that an asynchronous operation
     * has failed to complete.
     *
     * <p>The {@link AsyncContext} corresponding to the asynchronous
     * operation that failed to complete may be obtained by calling
     * {@link AsyncEvent#getAsyncContext getAsyncContext} on the given
     * <tt>event</tt>.
     *
     * <p>In addition, if this AsyncListener had been registered via a call
     * to {@link AsyncContext#addListener(AsyncListener,
     * ServletRequest, ServletResponse)}, the supplied ServletRequest and
     * ServletResponse objects may be retrieved by calling
     * {@link AsyncEvent#getSuppliedRequest getSuppliedRequest} and
     * {@link AsyncEvent#getSuppliedResponse getSuppliedResponse},
     * respectively, on the given <tt>event</tt>.
     *
     * @param event the AsyncEvent indicating that an asynchronous
     *              operation has failed to complete
     * @throws IOException if an I/O related error has occurred during the
     *                     processing of the given AsyncEvent
     */
    public void onError(AsyncEvent event) throws IOException;

}
