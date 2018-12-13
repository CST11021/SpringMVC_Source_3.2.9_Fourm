
package org.springframework.beans.factory.support;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeanMetadataAttributeAccessor;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.core.io.DescriptiveResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;


@SuppressWarnings("serial")
public abstract class AbstractBeanDefinition extends BeanMetadataAttributeAccessor implements BeanDefinition, Cloneable {

	// scope 默认值为""，等同于单例状态
	public static final String SCOPE_DEFAULT = "";

	//不使用自动装配(默认不使用自动装配)
	public static final int AUTOWIRE_NO = AutowireCapableBeanFactory.AUTOWIRE_NO;
	//通过名称自动装配
	public static final int AUTOWIRE_BY_NAME = AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;
	//通过类型自动装配
	public static final int AUTOWIRE_BY_TYPE = AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE;
	//构造器装配
	public static final int AUTOWIRE_CONSTRUCTOR = AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR;
	@Deprecated
	public static final int AUTOWIRE_AUTODETECT = AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT;

	// 表示没有任何依赖性检查
	public static final int DEPENDENCY_CHECK_NONE = 0;
	// 依赖检查对象的引用
	public static final int DEPENDENCY_CHECK_OBJECTS = 1;
	// 表示对“简单”属性的依赖性检查
	public static final int DEPENDENCY_CHECK_SIMPLE = 2;
	// 常表示为所有属性的依赖性检查
	public static final int DEPENDENCY_CHECK_ALL = 3;

	public static final String INFER_METHOD = "(inferred)";


	// 表示这个bean的Class类型，注意是Class<T>类型，而不是实例
	private volatile Object beanClass;
	private String scope = SCOPE_DEFAULT;
	private boolean singleton = true;
	private boolean prototype = false;
	private boolean abstractFlag = false;
	private boolean lazyInit = false;
	private int autowireMode = AUTOWIRE_NO;
	private int dependencyCheck = DEPENDENCY_CHECK_NONE;
	// 表示这个依赖的所有bean
	private String[] dependsOn;
	// 对应的配置：<bean autowire-candidate="true/false">
	// Spring在实例化这个bean的时候会在容器中查找匹配的bean对autowire bean进行属性注入，这些被查找的bean我们称为候选bean。
	// 作为候选bean，我凭什么就要被你用，老子不给你用。所以候选bean给自己增加了autowire-candidate="false"属性（默认是true），
	// 那么容器就不会把这个bean当做候选bean了，即这个bean不会被当做自动装配对象。同样，<beans/>标签可以定义
	// default-autowire-candidate="false"属性让它包含的所有bean都不做为候选bean。我的地盘我做主。
	private boolean autowireCandidate = true;
	// 自动装配时当出现多个Bean候选者时，被注解为@Primary的Bean将作为首选者，否则将抛出异常（@Primary对应bean配置的中primary属性配置）
	private boolean primary = false;
	private final Map<String, AutowireCandidateQualifier> qualifiers = new LinkedHashMap<String, AutowireCandidateQualifier>(0);
	private boolean nonPublicAccessAllowed = true;
	private boolean lenientConstructorResolution = true;
	// 表示实例化bean的时候，要使用的构造函数的参数
	private ConstructorArgumentValues constructorArgumentValues;
	private MutablePropertyValues propertyValues;
	// 该类其实是对lookup-method、replace-method 配置的一个封装
	private MethodOverrides methodOverrides = new MethodOverrides();
	// 对应 factory-bean 配置
	private String factoryBeanName;
	// 对应 factory-method 配置
	private String factoryMethodName;
	private String initMethodName;
	private String destroyMethodName;
	private boolean enforceInitMethod = true;
	private boolean enforceDestroyMethod = true;
	// 标识这个BeanDefinition是否是“合成”的（合成的意思：不是由应用程序本身定义的）
	private boolean synthetic = false;
	private int role = BeanDefinition.ROLE_APPLICATION;
	private String description;
	// 表示这个BeanDefinition来自于哪个配置文件
	private Resource resource;


	// 构造器
	protected AbstractBeanDefinition() {
		this(null, null);
	}
	protected AbstractBeanDefinition(ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
		setConstructorArgumentValues(cargs);
		setPropertyValues(pvs);
	}
	@Deprecated
	protected AbstractBeanDefinition(AbstractBeanDefinition original) {
		this((BeanDefinition) original);
	}
	protected AbstractBeanDefinition(BeanDefinition original) {
		setParentName(original.getParentName());
		setBeanClassName(original.getBeanClassName());
		setFactoryBeanName(original.getFactoryBeanName());
		setFactoryMethodName(original.getFactoryMethodName());
		setScope(original.getScope());
		setAbstract(original.isAbstract());
		setLazyInit(original.isLazyInit());
		setRole(original.getRole());
		setConstructorArgumentValues(new ConstructorArgumentValues(original.getConstructorArgumentValues()));
		setPropertyValues(new MutablePropertyValues(original.getPropertyValues()));
		setSource(original.getSource());
		copyAttributesFrom(original);

		if (original instanceof AbstractBeanDefinition) {
			AbstractBeanDefinition originalAbd = (AbstractBeanDefinition) original;
			if (originalAbd.hasBeanClass()) {
				setBeanClass(originalAbd.getBeanClass());
			}
			setAutowireMode(originalAbd.getAutowireMode());
			setDependencyCheck(originalAbd.getDependencyCheck());
			setDependsOn(originalAbd.getDependsOn());
			setAutowireCandidate(originalAbd.isAutowireCandidate());
			copyQualifiersFrom(originalAbd);
			setPrimary(originalAbd.isPrimary());
			setNonPublicAccessAllowed(originalAbd.isNonPublicAccessAllowed());
			setLenientConstructorResolution(originalAbd.isLenientConstructorResolution());
			setInitMethodName(originalAbd.getInitMethodName());
			setEnforceInitMethod(originalAbd.isEnforceInitMethod());
			setDestroyMethodName(originalAbd.getDestroyMethodName());
			setEnforceDestroyMethod(originalAbd.isEnforceDestroyMethod());
			setMethodOverrides(new MethodOverrides(originalAbd.getMethodOverrides()));
			setSynthetic(originalAbd.isSynthetic());
			setResource(originalAbd.getResource());
		}
		else {
			setResourceDescription(original.getResourceDescription());
		}
	}


	// 从给定的 other 覆盖此 bean，可能是来自父子继承关系的复制父类中的设置
	@Deprecated
	public void overrideFrom(AbstractBeanDefinition other) {
		overrideFrom((BeanDefinition) other);
	}
	public void overrideFrom(BeanDefinition other) {
		if (StringUtils.hasLength(other.getBeanClassName())) {
			setBeanClassName(other.getBeanClassName());
		}
		if (StringUtils.hasLength(other.getFactoryBeanName())) {
			setFactoryBeanName(other.getFactoryBeanName());
		}
		if (StringUtils.hasLength(other.getFactoryMethodName())) {
			setFactoryMethodName(other.getFactoryMethodName());
		}
		if (StringUtils.hasLength(other.getScope())) {
			setScope(other.getScope());
		}
		setAbstract(other.isAbstract());
		setLazyInit(other.isLazyInit());
		setRole(other.getRole());
		getConstructorArgumentValues().addArgumentValues(other.getConstructorArgumentValues());
		getPropertyValues().addPropertyValues(other.getPropertyValues());
		setSource(other.getSource());
		copyAttributesFrom(other);

		if (other instanceof AbstractBeanDefinition) {
			AbstractBeanDefinition otherAbd = (AbstractBeanDefinition) other;
			if (otherAbd.hasBeanClass()) {
				setBeanClass(otherAbd.getBeanClass());
			}
			setAutowireCandidate(otherAbd.isAutowireCandidate());
			setAutowireMode(otherAbd.getAutowireMode());
			copyQualifiersFrom(otherAbd);
			setPrimary(otherAbd.isPrimary());
			setDependencyCheck(otherAbd.getDependencyCheck());
			setDependsOn(otherAbd.getDependsOn());
			setNonPublicAccessAllowed(otherAbd.isNonPublicAccessAllowed());
			setLenientConstructorResolution(otherAbd.isLenientConstructorResolution());
			if (StringUtils.hasLength(otherAbd.getInitMethodName())) {
				setInitMethodName(otherAbd.getInitMethodName());
				setEnforceInitMethod(otherAbd.isEnforceInitMethod());
			}
			if (StringUtils.hasLength(otherAbd.getDestroyMethodName())) {
				setDestroyMethodName(otherAbd.getDestroyMethodName());
				setEnforceDestroyMethod(otherAbd.isEnforceDestroyMethod());
			}
			getMethodOverrides().addOverrides(otherAbd.getMethodOverrides());
			setSynthetic(otherAbd.isSynthetic());
			setResource(otherAbd.getResource());
		}
		else {
			setResourceDescription(other.getResourceDescription());
		}
	}

	// 给bean设置默认值
	public void applyDefaults(BeanDefinitionDefaults defaults) {
		setLazyInit(defaults.isLazyInit());
		setAutowireMode(defaults.getAutowireMode());
		setDependencyCheck(defaults.getDependencyCheck());
		setInitMethodName(defaults.getInitMethodName());
		setEnforceInitMethod(false);
		setDestroyMethodName(defaults.getDestroyMethodName());
		setEnforceDestroyMethod(false);
	}


	// 判断这个bean的beanClass是否是类类型
	public boolean hasBeanClass() {
		return (this.beanClass instanceof Class);
	}

	public void setBeanClass(Class<?> beanClass) {
		this.beanClass = beanClass;
	}
	public Class<?> getBeanClass() throws IllegalStateException {
		Object beanClassObject = this.beanClass;
		if (beanClassObject == null) {
			throw new IllegalStateException("No bean class specified on bean definition");
		}
		if (!(beanClassObject instanceof Class)) {
			throw new IllegalStateException("Bean class name [" + beanClassObject + "] has not been resolved into an actual Class");
		}
		return (Class<?>) beanClassObject;
	}

	public void setBeanClassName(String beanClassName) {
		this.beanClass = beanClassName;
	}
	public String getBeanClassName() {
		Object beanClassObject = this.beanClass;
		if (beanClassObject instanceof Class) {
			return ((Class<?>) beanClassObject).getName();
		}
		else {
			return (String) beanClassObject;
		}
	}

	// 返回这个bean的类型
	public Class<?> resolveBeanClass(ClassLoader classLoader) throws ClassNotFoundException {
		String className = getBeanClassName();
		if (className == null) {
			return null;
		}
		Class<?> resolvedClass = ClassUtils.forName(className, classLoader);
		this.beanClass = resolvedClass;
		return resolvedClass;
	}


	public void setScope(String scope) {
		this.scope = scope;
		this.singleton = SCOPE_SINGLETON.equals(scope) || SCOPE_DEFAULT.equals(scope);
		this.prototype = SCOPE_PROTOTYPE.equals(scope);
	}
	public String getScope() {
		return this.scope;
	}


	@Deprecated
	public void setSingleton(boolean singleton) {
		this.scope = (singleton ? SCOPE_SINGLETON : SCOPE_PROTOTYPE);
		this.singleton = singleton;
		this.prototype = !singleton;
	}
	public boolean isSingleton() {
		return this.singleton;
	}
	public boolean isPrototype() {
		return this.prototype;
	}

	public void setAbstract(boolean abstractFlag) {
		this.abstractFlag = abstractFlag;
	}
	public boolean isAbstract() {
		return this.abstractFlag;
	}

	public void setLazyInit(boolean lazyInit) {
		this.lazyInit = lazyInit;
	}
	public boolean isLazyInit() {
		return this.lazyInit;
	}

	public void setAutowireMode(int autowireMode) {
		this.autowireMode = autowireMode;
	}
	public int getAutowireMode() {
		return this.autowireMode;
	}


	// 返回使用哪种形式的注入，如：AUTOWIRE_AUTODETECT、AUTOWIRE_CONSTRUCTOR、AUTOWIRE_BY_TYPE等
	public int getResolvedAutowireMode() {
		if (this.autowireMode == AUTOWIRE_AUTODETECT) {
			// Work out whether to apply setter autowiring or constructor autowiring.
			// If it has a no-arg constructor it's deemed to be setter autowiring,
			// otherwise we'll try constructor autowiring.
			Constructor<?>[] constructors = getBeanClass().getConstructors();
			for (Constructor<?> constructor : constructors) {
				if (constructor.getParameterTypes().length == 0) {
					return AUTOWIRE_BY_TYPE;
				}
			}
			return AUTOWIRE_CONSTRUCTOR;
		}
		else {
			return this.autowireMode;
		}
	}

	public void setDependencyCheck(int dependencyCheck) {
		this.dependencyCheck = dependencyCheck;
	}
	public int getDependencyCheck() {
		return this.dependencyCheck;
	}

	public void setDependsOn(String[] dependsOn) {
		this.dependsOn = dependsOn;
	}
	public String[] getDependsOn() {
		return this.dependsOn;
	}

	public void setAutowireCandidate(boolean autowireCandidate) {
		this.autowireCandidate = autowireCandidate;
	}
	public boolean isAutowireCandidate() {
		return this.autowireCandidate;
	}

	public void setPrimary(boolean primary) {
		this.primary = primary;
	}
	public boolean isPrimary() {
		return this.primary;
	}

	/**
	 * Register a qualifier to be used for autowire candidate resolution,
	 * keyed by the qualifier's type name.
	 * @see AutowireCandidateQualifier#getTypeName()
	 */
	public void addQualifier(AutowireCandidateQualifier qualifier) {
		this.qualifiers.put(qualifier.getTypeName(), qualifier);
	}

	/**
	 * Return whether this bean has the specified qualifier.
	 */
	public boolean hasQualifier(String typeName) {
		return this.qualifiers.keySet().contains(typeName);
	}

	public AutowireCandidateQualifier getQualifier(String typeName) {
		return this.qualifiers.get(typeName);
	}
	public Set<AutowireCandidateQualifier> getQualifiers() {
		return new LinkedHashSet<AutowireCandidateQualifier>(this.qualifiers.values());
	}

	/**
	 * Copy the qualifiers from the supplied AbstractBeanDefinition to this bean definition.
	 * @param source the AbstractBeanDefinition to copy from
	 */
	public void copyQualifiersFrom(AbstractBeanDefinition source) {
		Assert.notNull(source, "Source must not be null");
		this.qualifiers.putAll(source.qualifiers);
	}

	public void setNonPublicAccessAllowed(boolean nonPublicAccessAllowed) {
		this.nonPublicAccessAllowed = nonPublicAccessAllowed;
	}
	public boolean isNonPublicAccessAllowed() {
		return this.nonPublicAccessAllowed;
	}

	public void setLenientConstructorResolution(boolean lenientConstructorResolution) {
		this.lenientConstructorResolution = lenientConstructorResolution;
	}
	public boolean isLenientConstructorResolution() {
		return this.lenientConstructorResolution;
	}

	public void setConstructorArgumentValues(ConstructorArgumentValues constructorArgumentValues) {
		this.constructorArgumentValues =
				(constructorArgumentValues != null ? constructorArgumentValues : new ConstructorArgumentValues());
	}
	public ConstructorArgumentValues getConstructorArgumentValues() {
		return this.constructorArgumentValues;
	}

	public boolean hasConstructorArgumentValues() {
		return !this.constructorArgumentValues.isEmpty();
	}

	public void setPropertyValues(MutablePropertyValues propertyValues) {
		this.propertyValues = (propertyValues != null ? propertyValues : new MutablePropertyValues());
	}
	public MutablePropertyValues getPropertyValues() {
		return this.propertyValues;
	}

	public void setMethodOverrides(MethodOverrides methodOverrides) {
		this.methodOverrides = (methodOverrides != null ? methodOverrides : new MethodOverrides());
	}
	public MethodOverrides getMethodOverrides() {
		return this.methodOverrides;
	}

	public void setFactoryBeanName(String factoryBeanName) {
		this.factoryBeanName = factoryBeanName;
	}
	public String getFactoryBeanName() {
		return this.factoryBeanName;
	}

	public void setFactoryMethodName(String factoryMethodName) {
		this.factoryMethodName = factoryMethodName;
	}
	public String getFactoryMethodName() {
		return this.factoryMethodName;
	}

	public void setInitMethodName(String initMethodName) {
		this.initMethodName = initMethodName;
	}
	public String getInitMethodName() {
		return this.initMethodName;
	}

	public void setEnforceInitMethod(boolean enforceInitMethod) {
		this.enforceInitMethod = enforceInitMethod;
	}
	public boolean isEnforceInitMethod() {
		return this.enforceInitMethod;
	}

	public void setDestroyMethodName(String destroyMethodName) {
		this.destroyMethodName = destroyMethodName;
	}
	public String getDestroyMethodName() {
		return this.destroyMethodName;
	}

	public void setEnforceDestroyMethod(boolean enforceDestroyMethod) {
		this.enforceDestroyMethod = enforceDestroyMethod;
	}
	public boolean isEnforceDestroyMethod() {
		return this.enforceDestroyMethod;
	}

	// 标识这个BeanDefinition是否是“合成”的（合成的意思：不是由应用程序本身定义的）
	public void setSynthetic(boolean synthetic) {
		this.synthetic = synthetic;
	}
	public boolean isSynthetic() {
		return this.synthetic;
	}

	public void setRole(int role) {
		this.role = role;
	}
	public int getRole() {
		return this.role;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	public String getDescription() {
		return this.description;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}
	public Resource getResource() {
		return this.resource;
	}

	public void setResourceDescription(String resourceDescription) {
		this.resource = new DescriptiveResource(resourceDescription);
	}
	public String getResourceDescription() {
		return (this.resource != null ? this.resource.getDescription() : null);
	}

	public void setOriginatingBeanDefinition(BeanDefinition originatingBd) {
		this.resource = new BeanDefinitionResource(originatingBd);
	}
	public BeanDefinition getOriginatingBeanDefinition() {
		return (this.resource instanceof BeanDefinitionResource ?
				((BeanDefinitionResource) this.resource).getBeanDefinition() : null);
	}

	// 校验这个bean，如果methodOverrides和factoryMethodName同时存在则抛异常
	public void validate() throws BeanDefinitionValidationException {
		if (!getMethodOverrides().isEmpty() && getFactoryMethodName() != null) {
			throw new BeanDefinitionValidationException(
					"Cannot combine static factory method with method overrides: " +
					"the static factory method must create the instance");
		}

		if (hasBeanClass()) {
			prepareMethodOverrides();
		}
	}


	// 检查是否有配置 lookup-method 和 replace-method 覆盖方法，并判断这个覆盖方法是否存在
	public void prepareMethodOverrides() throws BeanDefinitionValidationException {
		MethodOverrides methodOverrides = getMethodOverrides();
		if (!methodOverrides.isEmpty()) {
			for (MethodOverride mo : methodOverrides.getOverrides()) {
				prepareMethodOverride(mo);
			}
		}
	}
	protected void prepareMethodOverride(MethodOverride mo) throws BeanDefinitionValidationException {
		int count = ClassUtils.getMethodCountForName(getBeanClass(), mo.getMethodName());
		if (count == 0) {
			throw new BeanDefinitionValidationException("Invalid method override: no method with name '" + mo.getMethodName() +
					"' on class [" + getBeanClassName() + "]");
		}
		else if (count == 1) {
			// Mark override as not overloaded, to avoid the overhead of arg type checking.
			mo.setOverloaded(false);
		}
	}

	public abstract AbstractBeanDefinition cloneBeanDefinition();

	@Override
	public Object clone() {
		return cloneBeanDefinition();
	}
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof AbstractBeanDefinition)) {
			return false;
		}

		AbstractBeanDefinition that = (AbstractBeanDefinition) other;

		if (!ObjectUtils.nullSafeEquals(getBeanClassName(), that.getBeanClassName())) return false;
		if (!ObjectUtils.nullSafeEquals(this.scope, that.scope)) return false;
		if (this.abstractFlag != that.abstractFlag) return false;
		if (this.lazyInit != that.lazyInit) return false;

		if (this.autowireMode != that.autowireMode) return false;
		if (this.dependencyCheck != that.dependencyCheck) return false;
		if (!Arrays.equals(this.dependsOn, that.dependsOn)) return false;
		if (this.autowireCandidate != that.autowireCandidate) return false;
		if (!ObjectUtils.nullSafeEquals(this.qualifiers, that.qualifiers)) return false;
		if (this.primary != that.primary) return false;

		if (this.nonPublicAccessAllowed != that.nonPublicAccessAllowed) return false;
		if (this.lenientConstructorResolution != that.lenientConstructorResolution) return false;
		if (!ObjectUtils.nullSafeEquals(this.constructorArgumentValues, that.constructorArgumentValues)) return false;
		if (!ObjectUtils.nullSafeEquals(this.propertyValues, that.propertyValues)) return false;
		if (!ObjectUtils.nullSafeEquals(this.methodOverrides, that.methodOverrides)) return false;

		if (!ObjectUtils.nullSafeEquals(this.factoryBeanName, that.factoryBeanName)) return false;
		if (!ObjectUtils.nullSafeEquals(this.factoryMethodName, that.factoryMethodName)) return false;
		if (!ObjectUtils.nullSafeEquals(this.initMethodName, that.initMethodName)) return false;
		if (this.enforceInitMethod != that.enforceInitMethod) return false;
		if (!ObjectUtils.nullSafeEquals(this.destroyMethodName, that.destroyMethodName)) return false;
		if (this.enforceDestroyMethod != that.enforceDestroyMethod) return false;

		if (this.synthetic != that.synthetic) return false;
		if (this.role != that.role) return false;

		return super.equals(other);
	}
	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(getBeanClassName());
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.scope);
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.constructorArgumentValues);
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.propertyValues);
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.factoryBeanName);
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.factoryMethodName);
		hashCode = 29 * hashCode + super.hashCode();
		return hashCode;
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("class [");
		sb.append(getBeanClassName()).append("]");
		sb.append("; scope=").append(this.scope);
		sb.append("; abstract=").append(this.abstractFlag);
		sb.append("; lazyInit=").append(this.lazyInit);
		sb.append("; autowireMode=").append(this.autowireMode);
		sb.append("; dependencyCheck=").append(this.dependencyCheck);
		sb.append("; autowireCandidate=").append(this.autowireCandidate);
		sb.append("; primary=").append(this.primary);
		sb.append("; factoryBeanName=").append(this.factoryBeanName);
		sb.append("; factoryMethodName=").append(this.factoryMethodName);
		sb.append("; initMethodName=").append(this.initMethodName);
		sb.append("; destroyMethodName=").append(this.destroyMethodName);
		if (this.resource != null) {
			sb.append("; defined in ").append(this.resource.getDescription());
		}
		return sb.toString();
	}

}
