/*
 * Copyright 2002-2013 the original author or authors.
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

package org.springframework.aop.framework;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aopalliance.aop.Advice;

import org.springframework.aop.Advisor;
import org.springframework.aop.DynamicIntroductionAdvice;
import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.IntroductionInfo;
import org.springframework.aop.TargetSource;
import org.springframework.aop.support.DefaultIntroductionAdvisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.target.EmptyTargetSource;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

/**
 * Base class for AOP proxy configuration managers.
 * These are not themselves AOP proxies, but subclasses of this class are
 * normally factories from which AOP proxy instances are obtained directly.
 *
 * <p>This class frees subclasses of the housekeeping of Advices
 * and Advisors, but doesn't actually implement proxy creation
 * methods, which are provided by subclasses.
 *
 * <p>This class is serializable; subclasses need not be.
 * This class is used to hold snapshots of proxies.
 *
 *  AdvisedSupport继承了ProxyConfig并实现了Advised接口，所以AdvisedSupport所承载的信息可以划分为两类：
 *  一个类是ProxyConfig，记载生成代理对象的控制信息；
 *  一类是Advised，承载生成代理对象所需要的必要信息，如相关目标类、Advice、Advisor等。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.aop.framework.AopProxy
 */

@SuppressWarnings("unchecked")
public class AdvisedSupport extends ProxyConfig implements Advised {

	private static final long serialVersionUID = 2651364800145442165L;

	// 当没有目标类时，使用 EmptyTargetSource 规范目标类
	public static final TargetSource EMPTY_TARGET_SOURCE = EmptyTargetSource.INSTANCE;
	// 表示要带被代理的目标类
	TargetSource targetSource = EMPTY_TARGET_SOURCE;
	//** Whether the Advisors are already filtered for the specific target class */
	private boolean preFiltered = false;
	AdvisorChainFactory advisorChainFactory = new DefaultAdvisorChainFactory();
	// 增强不一定可以作用在所有的方法中，这里用于缓存每个方法对应的方法拦截器
	private transient Map<MethodCacheKey, List<Object>> methodCache;
	// 保存代理类将要实现的接口。保存在列表中以保持注册顺序，用指定的接口顺序创建JDK代理
	private List<Class> interfaces = new ArrayList<Class>();
	// 表示增强链，在增强Advice添加到 advisors 前都会被包装成 Advisor 的实例
	private List<Advisor> advisors = new LinkedList<Advisor>();
	// 数组更新了对 advisors 列表的更改，这在内部更容易操作
	private Advisor[] advisorArray = new Advisor[0];

	// 构造器
	public AdvisedSupport() {
		initMethodCache();
	}
	public AdvisedSupport(Class[] interfaces) {
		this();
		setInterfaces(interfaces);
	}


	/** -------------------------------- 代理目标对象相关 -------------------------------- */
	// 设置目标类接目标类对象
	public void setTarget(Object target) {
		setTargetSource(new SingletonTargetSource(target));
	}
	public void setTargetSource(TargetSource targetSource) {
		this.targetSource = (targetSource != null ? targetSource : EMPTY_TARGET_SOURCE);
	}
	public TargetSource getTargetSource() {
		return this.targetSource;
	}
	public void setTargetClass(Class<?> targetClass) {
		this.targetSource = EmptyTargetSource.forClass(targetClass);
	}
	public Class<?> getTargetClass() {
		return this.targetSource.getTargetClass();
	}


	/** -------------------------------- 代理接口相关 -------------------------------- */
	// 设置代理类要实现的接口
	public void setInterfaces(Class<?>... interfaces) {
		Assert.notNull(interfaces, "Interfaces must not be null");
		this.interfaces.clear();
		for (Class ifc : interfaces) {
			addInterface(ifc);
		}
	}
	public void addInterface(Class<?> intf) {
		Assert.notNull(intf, "Interface must not be null");
		if (!intf.isInterface()) {
			throw new IllegalArgumentException("[" + intf.getName() + "] is not an interface");
		}
		if (!this.interfaces.contains(intf)) {
			this.interfaces.add(intf);
			adviceChanged();
		}
	}
	public boolean removeInterface(Class<?> intf) {
		return this.interfaces.remove(intf);
	}
	// 获取所有被代理的接口
	public Class<?>[] getProxiedInterfaces() {
		return this.interfaces.toArray(new Class[this.interfaces.size()]);
	}
	// 确定给定的class是否为被代理接口的一个对象或子类
	public boolean isInterfaceProxied(Class<?> intf) {
		for (Class proxyIntf : this.interfaces) {
			if (intf.isAssignableFrom(proxyIntf)) {
				return true;
			}
		}
		return false;
	}


	/** -------------------------------- Advisor相关 -------------------------------- */
	// 返回用于此代理配置的Advisor，Advisor封装了Advice和Pointcut信息
	public final Advisor[] getAdvisors() {
		return this.advisorArray;
	}
	public void addAdvisor(Advisor advisor) {
		int pos = this.advisors.size();
		addAdvisor(pos, advisor);
	}
	public void addAdvisor(int pos, Advisor advisor) throws AopConfigException {
		// 判断是否为引介增强
		if (advisor instanceof IntroductionAdvisor) {
			validateIntroductionAdvisor((IntroductionAdvisor) advisor);
		}
		addAdvisorInternal(pos, advisor);
	}
	public boolean removeAdvisor(Advisor advisor) {
		int index = indexOf(advisor);
		if (index == -1) {
			return false;
		}
		else {
			removeAdvisor(index);
			return true;
		}
	}
	public void removeAdvisor(int index) throws AopConfigException {
		if (isFrozen()) {
			throw new AopConfigException("Cannot remove Advisor: Configuration is frozen.");
		}
		if (index < 0 || index > this.advisors.size() - 1) {
			throw new AopConfigException("Advisor index " + index + " is out of bounds: " +
					"This configuration only has " + this.advisors.size() + " advisors.");
		}

		Advisor advisor = this.advisors.get(index);
		if (advisor instanceof IntroductionAdvisor) {
			IntroductionAdvisor ia = (IntroductionAdvisor) advisor;
			// We need to remove introduction interfaces.
			for (int j = 0; j < ia.getInterfaces().length; j++) {
				removeInterface(ia.getInterfaces()[j]);
			}
		}

		this.advisors.remove(index);
		updateAdvisorArray();
		adviceChanged();
	}
	public int indexOf(Advisor advisor) {
		Assert.notNull(advisor, "Advisor must not be null");
		return this.advisors.indexOf(advisor);
	}
	public boolean replaceAdvisor(Advisor a, Advisor b) throws AopConfigException {
		Assert.notNull(a, "Advisor a must not be null");
		Assert.notNull(b, "Advisor b must not be null");
		int index = indexOf(a);
		if (index == -1) {
			return false;
		}
		removeAdvisor(index);
		addAdvisor(index, b);
		return true;
	}
	@Deprecated
	public void addAllAdvisors(Advisor[] advisors) {
		addAdvisors(Arrays.asList(advisors));
	}
	public void addAdvisors(Advisor... advisors) {
		addAdvisors(Arrays.asList(advisors));
	}
	public void addAdvisors(Collection<Advisor> advisors) {
		if (isFrozen()) {
			throw new AopConfigException("Cannot add advisor: Configuration is frozen.");
		}
		if (!CollectionUtils.isEmpty(advisors)) {
			for (Advisor advisor : advisors) {
				if (advisor instanceof IntroductionAdvisor) {
					validateIntroductionAdvisor((IntroductionAdvisor) advisor);
				}
				Assert.notNull(advisor, "Advisor must not be null");
				this.advisors.add(advisor);
			}
			updateAdvisorArray();
			adviceChanged();
		}
	}
	// 更新advisorArray数组和advisors列表
	protected final void updateAdvisorArray() {
		this.advisorArray = this.advisors.toArray(new Advisor[this.advisors.size()]);
	}
	// 返回 advisors
	protected final List<Advisor> getAdvisorsInternal() {
		return this.advisors;
	}


	/** -------------------------------- Advice 相关 -------------------------------- */
	public void addAdvice(Advice advice) throws AopConfigException {
		int pos = this.advisors.size();
		addAdvice(pos, advice);
	}
	public void addAdvice(int pos, Advice advice) throws AopConfigException {
		Assert.notNull(advice, "Advice must not be null");
		// 实现Introduction型的Advice的有两条分支，以DynamicIntroductionAdvice为首的动态分支和以IntroductionInfo为首的静态分支。

		// 判断是否为 IntroductionInfo 类型的引介增强
		if (advice instanceof IntroductionInfo) {
			addAdvisor(pos, new DefaultIntroductionAdvisor(advice, (IntroductionInfo) advice));
		}
		// 判断是否为 DynamicIntroductionAdvice 类型的引介增强
		else if (advice instanceof DynamicIntroductionAdvice) {
			throw new AopConfigException("DynamicIntroductionAdvice may only be added as part of IntroductionAdvisor");
		}
		// 这里是将advice封装为 DefaultPointcutAdvisor 实例，Spring中同时使用Advisor来包装增强
		else {
			// PointcutAdvisor 封装了增强和切入点信息
			addAdvisor(pos, new DefaultPointcutAdvisor(advice));
		}
	}
	public boolean removeAdvice(Advice advice) throws AopConfigException {
		int index = indexOf(advice);
		if (index == -1) {
			return false;
		}
		else {
			removeAdvisor(index);
			return true;
		}
	}
	public int indexOf(Advice advice) {
		Assert.notNull(advice, "Advice must not be null");
		for (int i = 0; i < this.advisors.size(); i++) {
			Advisor advisor = this.advisors.get(i);
			if (advisor.getAdvice() == advice) {
				return i;
			}
		}
		return -1;
	}
	// 判断是否已经配置了指定的Advice
	public boolean adviceIncluded(Advice advice) {
		if (advice != null) {
			for (Advisor advisor : this.advisors) {
				if (advisor.getAdvice() == advice) {
					return true;
				}
			}
		}
		return false;
	}
	// 返回是adviceClass对象实例的增强个数
	public int countAdvicesOfType(Class adviceClass) {
		int count = 0;
		if (adviceClass != null) {
			for (Advisor advisor : this.advisors) {
				if (adviceClass.isInstance(advisor.getAdvice())) {
					count++;
				}
			}
		}
		return count;
	}
	// 获取拦截器
	public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Method method, Class targetClass) {
		MethodCacheKey cacheKey = new MethodCacheKey(method);
		List<Object> cached = this.methodCache.get(cacheKey);
		if (cached == null) {
			// 返回目标类指定方法的一个方法拦截器对象列表
			cached = this.advisorChainFactory.getInterceptorsAndDynamicInterceptionAdvice(this, method, targetClass);
			this.methodCache.put(cacheKey, cached);
		}
		return cached;
	}
	// 当织入的增强链改变时，该方法被调用
	protected void adviceChanged() {
		this.methodCache.clear();
	}


	/** -------------------------------- 复制增强配置 -------------------------------- */
	protected void copyConfigurationFrom(AdvisedSupport other) {
		copyConfigurationFrom(other, other.targetSource, new ArrayList<Advisor>(other.advisors));
	}
	protected void copyConfigurationFrom(AdvisedSupport other, TargetSource targetSource, List<Advisor> advisors) {
		// 复制增强配置
		copyFrom(other);
		this.targetSource = targetSource;
		this.advisorChainFactory = other.advisorChainFactory;
		this.interfaces = new ArrayList<Class>(other.interfaces);
		for (Advisor advisor : advisors) {
			if (advisor instanceof IntroductionAdvisor) {
				validateIntroductionAdvisor((IntroductionAdvisor) advisor);
			}
			Assert.notNull(advisor, "Advisor must not be null");
			this.advisors.add(advisor);
		}
		updateAdvisorArray();
		adviceChanged();
	}
	AdvisedSupport getConfigurationOnlyCopy() {
		AdvisedSupport copy = new AdvisedSupport();
		copy.copyFrom(this);
		copy.targetSource = EmptyTargetSource.forClass(getTargetClass(), getTargetSource().isStatic());
		copy.advisorChainFactory = this.advisorChainFactory;
		copy.interfaces = this.interfaces;
		copy.advisors = this.advisors;
		copy.updateAdvisorArray();
		return copy;
	}


	/** -------------------------------- 其他 -------------------------------- */
	// 初始化this.methodCache
	private void initMethodCache() {
		this.methodCache = new ConcurrentHashMap<MethodCacheKey, List<Object>>(32);
	}

	public void setPreFiltered(boolean preFiltered) {
		this.preFiltered = preFiltered;
	}
	public boolean isPreFiltered() {
		return this.preFiltered;
	}

	// 将引介增强指定的接口添加到代理接口集合中
	private void validateIntroductionAdvisor(IntroductionAdvisor advisor) {
		// 判断将要织入的接口是否有被增强实现
		advisor.validateInterfaces();
		// If the advisor passed validation, we can make the change.
		Class[] ifcs = advisor.getInterfaces();
		for (Class ifc : ifcs) {
			addInterface(ifc);
		}
	}
	private void addAdvisorInternal(int pos, Advisor advisor) throws AopConfigException {
		Assert.notNull(advisor, "Advisor must not be null");
		if (isFrozen()) {
			throw new AopConfigException("Cannot add advisor: Configuration is frozen.");
		}
		if (pos > this.advisors.size()) {
			throw new IllegalArgumentException("Illegal position " + pos + " in advisor list with size " + this.advisors.size());
		}
		this.advisors.add(pos, advisor);
		updateAdvisorArray();
		adviceChanged();
	}

	public void setAdvisorChainFactory(AdvisorChainFactory advisorChainFactory) {
		Assert.notNull(advisorChainFactory, "AdvisorChainFactory must not be null");
		this.advisorChainFactory = advisorChainFactory;
	}
	public AdvisorChainFactory getAdvisorChainFactory() {
		return this.advisorChainFactory;
	}

	// 序列化相关
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		// Rely on default serialization; just initialize state after deserialization.
		ois.defaultReadObject();

		// Initialize transient fields.
		initMethodCache();
	}
	public String toProxyConfigString() {
		return toString();
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getName());
		sb.append(": ").append(this.interfaces.size()).append(" interfaces ");
		sb.append(ClassUtils.classNamesToString(this.interfaces)).append("; ");
		sb.append(this.advisors.size()).append(" advisors ");
		sb.append(this.advisors).append("; ");
		sb.append("targetSource [").append(this.targetSource).append("]; ");
		sb.append(super.toString());
		return sb.toString();
	}


	// 一个方法的简单包装类。用于缓存方法时，用于有效的equals和hashCode比较
	private static class MethodCacheKey {

		private final Method method;
		private final int hashCode;

		public MethodCacheKey(Method method) {
			this.method = method;
			this.hashCode = method.hashCode();
		}

		@Override
		public boolean equals(Object other) {
			if (other == this) {
				return true;
			}
			MethodCacheKey otherKey = (MethodCacheKey) other;
			return (this.method == otherKey.method);
		}
		@Override
		public int hashCode() {
			return this.hashCode;
		}
	}

}
