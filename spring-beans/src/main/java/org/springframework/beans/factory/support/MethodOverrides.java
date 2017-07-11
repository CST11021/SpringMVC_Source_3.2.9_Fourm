
package org.springframework.beans.factory.support;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

// 该类其实是对lookup-method、replace-method 配置的一个封装
// 表示bean的一系列方法覆盖设置，如果bean有配置方法覆盖，Spring IOC容器将在运行时重写
public class MethodOverrides {

	private final Set<MethodOverride> overrides = new HashSet<MethodOverride>(0);

	public MethodOverrides() {}
	public MethodOverrides(MethodOverrides other) {
		addOverrides(other);
	}

	// 添加一个覆盖方法
	public void addOverrides(MethodOverrides other) {
		if (other != null) {
			this.overrides.addAll(other.getOverrides());
		}
	}
	public void addOverride(MethodOverride override) {
		this.overrides.add(override);
	}

	// 获取bean的所有覆盖方法
	public Set<MethodOverride> getOverrides() {
		return this.overrides;
	}

	// 判断这个bean是否有方法覆盖设置
	public boolean isEmpty() {
		return this.overrides.isEmpty();
	}

	// 查找这个方法的覆盖方法，如果没有返回null
	public MethodOverride getOverride(Method method) {
		for (MethodOverride override : this.overrides) {
			if (override.matches(method)) {
				return override;
			}
		}
		return null;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof MethodOverrides)) {
			return false;
		}
		MethodOverrides that = (MethodOverrides) other;
		return this.overrides.equals(that.overrides);

	}
	@Override
	public int hashCode() {
		return this.overrides.hashCode();
	}

}
