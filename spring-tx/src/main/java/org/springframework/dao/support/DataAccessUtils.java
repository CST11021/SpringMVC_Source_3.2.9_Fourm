/*
 * Copyright 2002-2012 the original author or authors.
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

package org.springframework.dao.support;

import java.util.Collection;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.TypeMismatchDataAccessException;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.NumberUtils;

/**
 * Miscellaneous utility methods for DAO implementations.
 * Useful with any data access technology.
 *
 * @author Juergen Hoeller
 * @since 1.0.2
 */
public abstract class DataAccessUtils {

	// 返回results的第一个元素；如果results大小为0，返回null；如果results存在多个元素，则抛出异常
	public static <T> T singleResult(Collection<T> results) throws IncorrectResultSizeDataAccessException {
		int size = (results != null ? results.size() : 0);
		if (size == 0) {
			return null;
		}
		if (results.size() > 1) {
			throw new IncorrectResultSizeDataAccessException(1, size);
		}
		return results.iterator().next();
	}
	// 返回results的第一个元素；如果results大小不为1，则抛出异常
	public static <T> T requiredSingleResult(Collection<T> results) throws IncorrectResultSizeDataAccessException {
		int size = (results != null ? results.size() : 0);
		if (size == 0) {
			throw new EmptyResultDataAccessException(1);
		}
		if (results.size() > 1) {
			throw new IncorrectResultSizeDataAccessException(1, size);
		}
		return results.iterator().next();
	}
	// 返回results的第一个元素；如果results不唯一则抛出异常
	public static <T> T uniqueResult(Collection<T> results) throws IncorrectResultSizeDataAccessException {
		int size = (results != null ? results.size() : 0);
		if (size == 0) {
			return null;
		}
		if (!CollectionUtils.hasUniqueObject(results)) {
			throw new IncorrectResultSizeDataAccessException(1, size);
		}
		return results.iterator().next();
	}
	public static <T> T requiredUniqueResult(Collection<T> results) throws IncorrectResultSizeDataAccessException {
		int size = (results != null ? results.size() : 0);
		if (size == 0) {
			throw new EmptyResultDataAccessException(1);
		}
		if (!CollectionUtils.hasUniqueObject(results)) {
			throw new IncorrectResultSizeDataAccessException(1, size);
		}
		return results.iterator().next();
	}

	// 返回results的第一个元素，并按指定的类型返回
	@SuppressWarnings("unchecked")
	public static <T> T objectResult(Collection<?> results, Class<T> requiredType)
			throws IncorrectResultSizeDataAccessException, TypeMismatchDataAccessException {

		Object result = requiredUniqueResult(results);
		if (requiredType != null && !requiredType.isInstance(result)) {
			if (String.class.equals(requiredType)) {
				result = result.toString();
			}
			else if (Number.class.isAssignableFrom(requiredType) && Number.class.isInstance(result)) {
				try {
					result = NumberUtils.convertNumberToTargetClass(((Number) result), (Class<? extends Number>) requiredType);
				}
				catch (IllegalArgumentException ex) {
					throw new TypeMismatchDataAccessException(ex.getMessage());
				}
			}
			else {
				throw new TypeMismatchDataAccessException(
						"Result object is of type [" + result.getClass().getName() +
						"] and could not be converted to required type [" + requiredType.getName() + "]");
			}
		}
		return (T) result;
	}
	// 返回results的第一个元素，并转为int类型
	public static int intResult(Collection results)
			throws IncorrectResultSizeDataAccessException, TypeMismatchDataAccessException {

		return objectResult(results, Number.class).intValue();
	}
	// 返回results的第一个元素，并转为long类型
	public static long longResult(Collection results)
			throws IncorrectResultSizeDataAccessException, TypeMismatchDataAccessException {

		return objectResult(results, Number.class).longValue();
	}


	/**
	 * Return a translated exception if this is appropriate,
	 * otherwise return the input exception.
	 * @param rawException exception we may wish to translate
	 * @param pet PersistenceExceptionTranslator to use to perform the translation
	 * @return a translated exception if translation is possible, or
	 * the raw exception if it is not
	 */
	public static RuntimeException translateIfNecessary(RuntimeException rawException, PersistenceExceptionTranslator pet) {

		Assert.notNull(pet, "PersistenceExceptionTranslator must not be null");
		DataAccessException dex = pet.translateExceptionIfPossible(rawException);
		return (dex != null ? dex : rawException);
	}

}
