
package org.springframework.beans.factory.support;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCreationNotAllowedException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.core.SimpleAliasRegistry;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

// 默认的单例 Bean 注册实现类
public class DefaultSingletonBeanRegistry extends SimpleAliasRegistry implements SingletonBeanRegistry {

	protected final Log logger = LogFactory.getLog(getClass());

	// 一个空对象内部标记：用于标记concurrent Maps的值（不支持空值）
	protected static final Object NULL_OBJECT = new Object();

	//用于保存BeanName和Bean实例之间的关系，BeanName --> beaninstance（表示已经注册）
	private final Map<String, Object> singletonObjects = new ConcurrentHashMap<String, Object>(64);
	//用于保存BeanName和创建bean的工厂之间的关系，BeanName-->ObjectFactory
	private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<String, ObjectFactory<?>>(16);
	//用于保存BeanName和Bean实例之间的关系，不同于singletonObjects在于，当一个单例bean被放到这里后，那么当bean还在创建过程中，就可以通过getBean方法获取了，其目的是用来检测循环引用
	// 是存放 singletonFactory 制造出来的 singleton 的缓存
	private final Map<String, Object> earlySingletonObjects = new HashMap<String, Object>(16);

	//用来保存当前所有已注册的bean(即：单例bean的注册表)
	private final Set<String> registeredSingletons = new LinkedHashSet<String>(64);

	//目前正在创建中的单例bean的名称的集合
	private final Map<String, Boolean> singletonsCurrentlyInCreation = new ConcurrentHashMap<String, Boolean>(16);
	//** Names of beans currently excluded from in creation checks (using a ConcurrentHashMap as a Set) */
	private final Map<String, Boolean> inCreationCheckExclusions = new ConcurrentHashMap<String, Boolean>(16);
	//存放异常出现的相关的原因的集合
	private Set<Exception> suppressedExceptions;
	//标志，指示我们目前是否在销毁单例中
	private boolean singletonsCurrentlyInDestruction = false;
	//存放一次性bean的缓存
	private final Map<String, Object> disposableBeans = new LinkedHashMap<String, Object>();
	//** Map between containing bean names: bean name --> Set of bean names that the bean contains */
	private final Map<String, Set<String>> containedBeanMap = new ConcurrentHashMap<String, Set<String>>(16);
	//** Map between dependent bean names: bean name --> Set of dependent bean names */
	// 保存这个beanA 到 需要依赖这个beanA所有bean的集合
	private final Map<String, Set<String>> dependentBeanMap = new ConcurrentHashMap<String, Set<String>>(64);
	// 保存这个beanA 到 这个beanA需要依赖的所有bean的集合
	private final Map<String, Set<String>> dependenciesForBeanMap = new ConcurrentHashMap<String, Set<String>>(64);

	// 将 singletonObject 注册到 this.singletonObjects
	public void registerSingleton(String beanName, Object singletonObject) throws IllegalStateException {
		Assert.notNull(beanName, "'beanName' must not be null");
		synchronized (this.singletonObjects) {
			Object oldObject = this.singletonObjects.get(beanName);
			//如果singletonObjects缓存找到有指定名称为beanName的对象，则表示该名称已被占用
			if (oldObject != null) {
				throw new IllegalStateException("Could not register object [" + singletonObject + "] under bean name '" + beanName + "': there is already object [" + oldObject + "] bound");
			}
			//若该名称没被占用，真正的注册操作在这里实现
			addSingleton(beanName, singletonObject);
		}
	}
	// 向this.singletonObjects 添加一个 singletonObject
	protected void addSingleton(String beanName, Object singletonObject) {
		synchronized (this.singletonObjects) {
			// 因为singletonObjects类型是ConcurrentHashMap,并发Map不支持空值作为标志值，所以用NULL_OBJECT来代替
			this.singletonObjects.put(beanName, (singletonObject != null ? singletonObject : NULL_OBJECT));
			// beanName已被注册存放在singletonObjects缓存中，那么singletonFactories不应该再持有名称为beanName的工厂
			this.singletonFactories.remove(beanName);
			// beanName已被注册存放在singletonObjects缓存，那么earlySingletonObjects不应该再持有名称为beanName的bean
			this.earlySingletonObjects.remove(beanName);
			// beanName放进单例注册表中
			this.registeredSingletons.add(beanName);
		}
	}



	public Object getSingleton(String beanName) {
		return getSingleton(beanName, true);
	}
	protected Object getSingleton(String beanName, boolean allowEarlyReference) {
		Object singletonObject = this.singletonObjects.get(beanName);
		// 如果singletonObjects指定beanName的对象是不存在的
		if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
			synchronized (this.singletonObjects) {
				singletonObject = this.earlySingletonObjects.get(beanName);
				// 如果earlySingletonObjects指定的beanName的对象是不存在的且allowEarlyReference是允许的
				if (singletonObject == null && allowEarlyReference) {
					ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
					// 如果存在指定beanName的singletonFactory对象
					if (singletonFactory != null) {
						singletonObject = singletonFactory.getObject();
						// 这里可以看出earlySingletonObjects缓存应该是存放singletonFactory产生的singleton
						this.earlySingletonObjects.put(beanName, singletonObject);
						// 这里表示指定的beanName已被占用，所以要在singletonFactories移除该名称
						this.singletonFactories.remove(beanName);
					}
				}
			}
		}
		return (singletonObject != NULL_OBJECT ? singletonObject : null);
	}

	/**
	 * Return the (raw) singleton object registered under the given name,
	 * creating and registering a new one if none registered yet.
	 * @param beanName the name of the bean
	 * @param singletonFactory the ObjectFactory to lazily create the singleton
	 * with, if necessary
	 * @return the registered singleton object
	 */
	public Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) {
		Assert.notNull(beanName, "'beanName' must not be null");
		synchronized (this.singletonObjects) {
			Object singletonObject = this.singletonObjects.get(beanName);
			// 如果singetonObjects缓存不存在名称为beanName的对象
			if (singletonObject == null) {
				// 如果目前在销毁singellton
				if (this.singletonsCurrentlyInDestruction) {
					throw new BeanCreationNotAllowedException(beanName,
							"Singleton bean creation not allowed while the singletons of this factory are in destruction " +
							"(Do not request a bean from a BeanFactory in a destroy method implementation!)");
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Creating shared instance of singleton bean '" + beanName + "'");
				}
				// 单例对象创建前的回调,默认实现注册正在创建的单例
				beforeSingletonCreation(beanName);
				// 判断存储异常相关原因的集合是否已存在
				boolean recordSuppressedExceptions = (this.suppressedExceptions == null);
				// 若没有，则创建异常集合的实例
				if (recordSuppressedExceptions) {
					this.suppressedExceptions = new LinkedHashSet<Exception>();
				}
				try {
					// 由参数给定的singletonFactory创建singleton对象,getObject方法的具体实现由ObjectFactory的子类决定
					singletonObject = singletonFactory.getObject();
				}
				catch (BeanCreationException ex) {
					// 如果异常被抓取，在这里将出现异常的原因抛出
					if (recordSuppressedExceptions) {
						for (Exception suppressedException : this.suppressedExceptions) {
							ex.addRelatedCause(suppressedException);
						}
					}
					throw ex;
				}
				finally {
					// 结束前，将异常集合销毁掉
					if (recordSuppressedExceptions) {
						this.suppressedExceptions = null;
					}
					// 单例创建之后的回调,默认的实现标志单例不要在创建了。
					afterSingletonCreation(beanName);
				}
				// 注册创建后的单例
				addSingleton(beanName, singletonObject);
			}
			return (singletonObject != NULL_OBJECT ? singletonObject : null);
		}
	}

	// 注册 发生在singeton bean 实例创建之间发生的异常
	protected void onSuppressedException(Exception ex) {
		synchronized (this.singletonObjects) {
			if (this.suppressedExceptions != null) {
				this.suppressedExceptions.add(ex);
			}
		}
	}

	// 移除名称为beanName的单例,主要在四个集合中移除，如singletonObjects,singletonFactories,earlySingletonObjects,registeredSingletons
	protected void removeSingleton(String beanName) {
		synchronized (this.singletonObjects) {
			this.singletonObjects.remove(beanName);
			this.singletonFactories.remove(beanName);
			this.earlySingletonObjects.remove(beanName);
			this.registeredSingletons.remove(beanName);
		}
	}

	// 判断this.singletonObjects 是否包含这个bean
	public boolean containsSingleton(String beanName) {
		return (this.singletonObjects.containsKey(beanName));
	}

	public String[] getSingletonNames() {
		// 对singletonObjects加锁，可能是为了防止registeredSingletons和singletonObjects出现不一致的问题
		synchronized (this.singletonObjects) {
			return StringUtils.toStringArray(this.registeredSingletons);
		}
	}

	public int getSingletonCount() {
		synchronized (this.singletonObjects) {
			return this.registeredSingletons.size();
		}
	}


	public void setCurrentlyInCreation(String beanName, boolean inCreation) {
		Assert.notNull(beanName, "Bean name must not be null");
		if (!inCreation) {
			this.inCreationCheckExclusions.put(beanName, Boolean.TRUE);
		}
		else {
			this.inCreationCheckExclusions.remove(beanName);
		}
	}

	// 返回存放正在创建单例的集合是否包含指定名称为beanName的单例存在
	public boolean isCurrentlyInCreation(String beanName) {
		Assert.notNull(beanName, "Bean name must not be null");
		return (!this.inCreationCheckExclusions.containsKey(beanName) && isActuallyInCreation(beanName));
	}

	protected boolean isActuallyInCreation(String beanName) {
		return isSingletonCurrentlyInCreation(beanName);
	}

	// 判断这个这个bean是不是正在被注册的bean
	public boolean isSingletonCurrentlyInCreation(String beanName) {
		return this.singletonsCurrentlyInCreation.containsKey(beanName);
	}

	// 在这个bean被创建前调用这个方法
	protected void beforeSingletonCreation(String beanName) {
		if (!this.inCreationCheckExclusions.containsKey(beanName) &&
				this.singletonsCurrentlyInCreation.put(beanName, Boolean.TRUE) != null) {
			throw new BeanCurrentlyInCreationException(beanName);
		}
	}
	// 在这个bean被创建后调用这个方法
	protected void afterSingletonCreation(String beanName) {
		if (!this.inCreationCheckExclusions.containsKey(beanName) &&
				!this.singletonsCurrentlyInCreation.remove(beanName)) {
			throw new IllegalStateException("Singleton '" + beanName + "' isn't currently in creation");
		}
	}


	/**
	 * Add the given bean to the list of disposable beans in this registry.
	 * <p>Disposable beans usually correspond to registered singletons,
	 * matching the bean name but potentially being a different instance
	 * (for example, a DisposableBean adapter for a singleton that does not
	 * naturally implement Spring's DisposableBean interface).
	 * @param beanName the name of the bean
	 * @param bean the bean instance
	 */
	// 一次性bean注册，存放在disponsableBeans集合中
	public void registerDisposableBean(String beanName, DisposableBean bean) {
		synchronized (this.disposableBeans) {
			this.disposableBeans.put(beanName, bean);
		}
	}

	// 注册两个bean之间的控制关系,例如内部bean和包含其的外部bean之间
	public void registerContainedBean(String containedBeanName, String containingBeanName) {
		synchronized (this.containedBeanMap) {
			// 从containedBeanMap缓存中查找外部bean名为containingBeanName的内部bean集合
			Set<String> containedBeans = this.containedBeanMap.get(containingBeanName);
			// 如果没有，刚新建一个存放内部bean的集合，并且存放在containedBeanMap缓存中
			if (containedBeans == null) {
				containedBeans = new LinkedHashSet<String>(8);
				this.containedBeanMap.put(containingBeanName, containedBeans);
			}
			// 将名为containedBeanName的内部bean存放到内部bean集合
			containedBeans.add(containedBeanName);
		}
		// 紧接着调用注册内部bean和外部bean的依赖关系的方法
		registerDependentBean(containedBeanName, containingBeanName);
	}

	// 注册给定bean的一个依赖bean，给定的bean销毁之前被销毁
	public void registerDependentBean(String beanName, String dependentBeanName) {
		// 调用SimpleAliasRegistry的canonicalName方法，这方法是将参数beanName当做别名寻找到注册名，并依此递归
		String canonicalName = canonicalName(beanName);
		synchronized (this.dependentBeanMap) {
			// 从dependentBeanMap缓存中找到依赖名为canonicalName这个bean的 依赖bean集合
			Set<String> dependentBeans = this.dependentBeanMap.get(canonicalName);
			// 如果为空，则新建一个依赖bean集合，并且存放到dependentBeanMap缓存中
			if (dependentBeans == null) {
				dependentBeans = new LinkedHashSet<String>(8);
				this.dependentBeanMap.put(canonicalName, dependentBeans);
			}
			// 依赖bean集合添加参数2指定的dependentBeanName
			dependentBeans.add(dependentBeanName);
		}
		synchronized (this.dependenciesForBeanMap) {
			// 从dependenciesForBeanMap缓存中找到dependentBeanName要依赖的所有bean集合
			Set<String> dependenciesForBean = this.dependenciesForBeanMap.get(dependentBeanName);
			if (dependenciesForBean == null) {
				dependenciesForBean = new LinkedHashSet<String>(8);
				this.dependenciesForBeanMap.put(dependentBeanName, dependenciesForBean);
			}
			dependenciesForBean.add(canonicalName);
		}
	}

	// 确定是否还存在名为beanName的被依赖关系
	protected boolean hasDependentBean(String beanName) {
		return this.dependentBeanMap.containsKey(beanName);
	}

	// 返回指定的bean依赖于所有的bean的名称，如果有的话
	public String[] getDependentBeans(String beanName) {
		Set<String> dependentBeans = this.dependentBeanMap.get(beanName);
		if (dependentBeans == null) {
			return new String[0];
		}
		return StringUtils.toStringArray(dependentBeans);
	}
	public String[] getDependenciesForBean(String beanName) {
		Set<String> dependenciesForBean = this.dependenciesForBeanMap.get(beanName);
		// 如果没有的话返回new String[0]而不是null
		if (dependenciesForBean == null) {
			return new String[0];
		}
		return dependenciesForBean.toArray(new String[dependenciesForBean.size()]);
	}

	// 销毁单例
	public void destroySingletons() {
		if (logger.isInfoEnabled()) {
			logger.info("Destroying singletons in " + this);
		}
		// 单例目前销毁标志开始
		synchronized (this.singletonObjects) {
			this.singletonsCurrentlyInDestruction = true;
		}

		String[] disposableBeanNames;
		// 销毁disponsableBeans缓存中所有单例bean
		synchronized (this.disposableBeans) {
			disposableBeanNames = StringUtils.toStringArray(this.disposableBeans.keySet());
		}
		for (int i = disposableBeanNames.length - 1; i >= 0; i--) {
			destroySingleton(disposableBeanNames[i]);
		}
		// containedBeanMap缓存清空,dependentBeanMap缓存清空，dependenciesForBeanMap缓存清空
		this.containedBeanMap.clear();
		this.dependentBeanMap.clear();
		this.dependenciesForBeanMap.clear();

		// singeltonObjects缓存清空，singletonFactories缓存清空，earlySingletonObjects缓存清空，registeredSingletons缓存清空
		synchronized (this.singletonObjects) {
			this.singletonObjects.clear();
			this.singletonFactories.clear();
			this.earlySingletonObjects.clear();
			this.registeredSingletons.clear();
			// 单例目前正在销毁标志为结束
			this.singletonsCurrentlyInDestruction = false;
		}
	}


	public void destroySingleton(String beanName) {
		// Remove a registered singleton of the given name, if any.
		removeSingleton(beanName);

		// Destroy the corresponding DisposableBean instance.
		DisposableBean disposableBean;
		synchronized (this.disposableBeans) {
			disposableBean = (DisposableBean) this.disposableBeans.remove(beanName);
		}
		destroyBean(beanName, disposableBean);
	}
	protected void destroyBean(String beanName, DisposableBean bean) {
		// Trigger destruction of dependent beans first...
		// 这段代码告诉我们先移除要销毁依赖bean
		Set<String> dependencies = this.dependentBeanMap.remove(beanName);
		if (dependencies != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Retrieved dependent beans for bean '" + beanName + "': " + dependencies);
			}
			for (String dependentBeanName : dependencies) {
				destroySingleton(dependentBeanName);
			}
		}

		// Actually destroy the bean now...
		// 销毁bean实例
		if (bean != null) {
			try {
				bean.destroy();
			}
			catch (Throwable ex) {
				logger.error("Destroy method on bean with name '" + beanName + "' threw an exception", ex);
			}
		}

		// Trigger destruction of contained beans...
		// 从containedBeanMap缓存中移除要销毁的bean，递归移除它的包含内部bean集合
		Set<String> containedBeans = this.containedBeanMap.remove(beanName);
		if (containedBeans != null) {
			for (String containedBeanName : containedBeans) {
				destroySingleton(containedBeanName);
			}
		}

		// Remove destroyed bean from other beans' dependencies.
		// 从其它bean的依赖bean集合中移除要销毁的bean
		synchronized (this.dependentBeanMap) {
			for (Iterator<Map.Entry<String, Set<String>>> it = this.dependentBeanMap.entrySet().iterator(); it.hasNext();) {
				Map.Entry<String, Set<String>> entry = it.next();
				Set<String> dependenciesToClean = entry.getValue();
				dependenciesToClean.remove(beanName);
				if (dependenciesToClean.isEmpty()) {
					it.remove();
				}
			}
		}

		// Remove destroyed bean's prepared dependency information.
		// 最后 从dependenciesForBeanMap缓存中移除要销毁的bean
		this.dependenciesForBeanMap.remove(beanName);
	}

	/**
	 * Expose the singleton mutex to subclasses.
	 * <p>Subclasses should synchronize on the given Object if they perform
	 * any sort of extended singleton creation phase. In particular, subclasses
	 * should <i>not</i> have their own mutexes involved in singleton creation,
	 * to avoid the potential for deadlocks in lazy-init situations.
	 */
	protected final Object getSingletonMutex() {
		return this.singletonObjects;
	}

	// 添加 名称为beanName的singletonFactory对象
	protected void addSingletonFactory(String beanName, ObjectFactory singletonFactory) {
		Assert.notNull(singletonFactory, "Singleton factory must not be null");
		synchronized (this.singletonObjects) {
			// 判断singletonObjects内名字为beanName是否被占用，若没有，进行注册操作
			if (!this.singletonObjects.containsKey(beanName)) {
				this.singletonFactories.put(beanName, singletonFactory);
				this.earlySingletonObjects.remove(beanName);
				this.registeredSingletons.add(beanName);
			}
		}
	}
}
