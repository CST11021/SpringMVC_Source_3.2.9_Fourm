/*
 * Copyright 2002-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.aop;

import org.aopalliance.aop.Advice;

/**
 * Common marker interface for before advice, such as {@link MethodBeforeAdvice}.
 *
 * <p>Spring supports only method before advice. Although this is unlikely to change,
 * this API is designed to allow field advice in future if desired.
 *
 * @author Rod Johnson
 * @see AfterAdvice
 */
/* spring aop的前置增强接口：
    Before Advice是在Joinpoint指定位置之前执行的Advice类型。通常，他不会中断程序执行流程，但如果必要，
    可以通过在Before Advice中抛出异常的方式来中断当前程序流程。如果当前Before Advice将被织入到方法执行类型的Joinpoint，
    那么这个Before Advice就会由于方法执行而执行。通常，可以使用Before Advice做一些系统的初始化工作，比如设置系统初始值，
    获取必要系统资源。当然，并非就限于这些情况。如果要用Before Advice来封装安全检查的逻辑，也不是不可以的，但通常情况下，
    我们会使用另一种形式的Advice。
*/
public interface BeforeAdvice extends Advice {

}
