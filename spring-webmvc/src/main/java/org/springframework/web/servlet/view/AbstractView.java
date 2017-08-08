
package org.springframework.web.servlet.view;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.support.RequestContext;


/*
  	Spring MVC提供的View实现类都直接或者间接继承自AbstractView。该类定义了大多数View实现类都需要的一些属性和简单的模板化的实现流程。

	主要属性如下几个：
		contentType：
		requestContextAttribute：
			requestContextAttribute属性是要公开给视图模板使用的RequestContext对应的属性名，
			比如，如果setRequestContextAttribute("rc")的话，那么，相应的RequestContext实例将以rc作为键放入模型中。
			这样，我们就可以在视图模板中通过rc引用到该RequestContext。通常情况下，如果我们使用Spring提供的自定义标签，
			那么不需要公开相应的RequestContext。但如果不使用Spring提供的自定义标签，那么为了能够访问处理过程中所返回的错误信息等，
			就需要通过公开给视图模板的RequestContext来进行了。

 		staticAttributes：
 			如果视图有某些静态属性，比如页眉、页脚的固定信息等，只要将它们加入staticAttributes，那么，AbstractView将保证
 			这些静态属性将一并放入模型数据中，最终一起公开给视图模板。既然所有的View实现子类都继承自AbstractView，那么它
 			们也就都拥有了指定静态属性的能力。

			比如我们在“面向多视图类型支持的ViewResolver”中定义了视图映射的映射的时候，为某些具体视图定义指定了静态属性，
			如下所示：

				<bean name="viewTemplate"
					class="org.springframework.web.servlet.view.InternalResourceViewResolver"
					abstract="true"
					p:attributesCSV="copyRight=spring21.cn, author=wanghongzhan">
				</bean>

			那么，现在我们就可以像普通的模型数据那样，在视图模板中访问这些静态属性，如下所示：
			...
			Author:${author}
			<br/>
			Copyright:${copyRight}
			...
			不过，除了通过attributesCSV属性以CSV字符串形式传入多个静态属性，我们还可以通过attributes属性以Properties的形
			式传入静态属性，或者通过attributesMap属性以Map的形式传入静态参数。




	 此外，在AbstractView中还定义了一个简单的模板化的方法流程：
		1、将添加的静态属性全部导入到现有的模型数据Map中，以便后及流程在合并视图模板的时候可以获取这些数据；
		2、如果requestContextAttribute被设置（默认为null），则将其一并导入现有的模型数据Map中；
		3、根据是否要产生下载内容，设置相应的HTTP Header
		4、公开renderMergedOutputModel(..)模板方法给子类实现
 */
public abstract class AbstractView extends WebApplicationObjectSupport implements View, BeanNameAware {

	//** Default content type. Overridable as bean property.
	public static final String DEFAULT_CONTENT_TYPE = "text/html;charset=ISO-8859-1";
	//** Initial size for the temporary output byte array (if any) 临时输出字节数组的初始大小
	private static final int OUTPUT_BYTE_ARRAY_INITIAL_SIZE = 4096;

	private String beanName;
	private String contentType = DEFAULT_CONTENT_TYPE;
	private String requestContextAttribute;

	// 静态属性的映射，由属性名作为key
	private final Map<String, Object> staticAttributes = new LinkedHashMap<String, Object>();
	// 标识视图是否应该在模型中添加路径变量
	private boolean exposePathVariables = true;



	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}
	public String getBeanName() {
		return this.beanName;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public String getContentType() {
		return this.contentType;
	}
	public void setRequestContextAttribute(String requestContextAttribute) {
		this.requestContextAttribute = requestContextAttribute;
	}
	public String getRequestContextAttribute() {
		return this.requestContextAttribute;
	}

	/**
	 * Set static attributes as a CSV string.
	 * Format is: attname0={value1},attname1={value1}
	 * <p>"Static" attributes are fixed attributes that are specified in
	 * the View instance configuration. "Dynamic" attributes, on the other hand,
	 * are values passed in as part of the model.
	 */
	public void setAttributesCSV(String propString) throws IllegalArgumentException {
		if (propString != null) {
			StringTokenizer st = new StringTokenizer(propString, ",");
			while (st.hasMoreTokens()) {
				String tok = st.nextToken();
				int eqIdx = tok.indexOf("=");
				if (eqIdx == -1) {
					throw new IllegalArgumentException("Expected = in attributes CSV string '" + propString + "'");
				}
				if (eqIdx >= tok.length() - 2) {
					throw new IllegalArgumentException(
							"At least 2 characters ([]) required in attributes CSV string '" + propString + "'");
				}
				String name = tok.substring(0, eqIdx);
				String value = tok.substring(eqIdx + 1);

				// Delete first and last characters of value: { and }
				value = value.substring(1);
				value = value.substring(0, value.length() - 1);

				addStaticAttribute(name, value);
			}
		}
	}

	/**
	 * Set static attributes for this view from a
	 * {@code java.util.Properties} object.
	 * <p>"Static" attributes are fixed attributes that are specified in
	 * the View instance configuration. "Dynamic" attributes, on the other hand,
	 * are values passed in as part of the model.
	 * <p>This is the most convenient way to set static attributes. Note that
	 * static attributes can be overridden by dynamic attributes, if a value
	 * with the same name is included in the model.
	 * <p>Can be populated with a String "value" (parsed via PropertiesEditor)
	 * or a "props" element in XML bean definitions.
	 * @see org.springframework.beans.propertyeditors.PropertiesEditor
	 */
	public void setAttributes(Properties attributes) {
		CollectionUtils.mergePropertiesIntoMap(attributes, this.staticAttributes);
	}

	/**
	 * Set static attributes for this view from a Map. This allows to set
	 * any kind of attribute values, for example bean references.
	 * <p>"Static" attributes are fixed attributes that are specified in
	 * the View instance configuration. "Dynamic" attributes, on the other hand,
	 * are values passed in as part of the model.
	 * <p>Can be populated with a "map" or "props" element in XML bean definitions.
	 * @param attributes Map with name Strings as keys and attribute objects as values
	 */
	public void setAttributesMap(Map<String, ?> attributes) {
		if (attributes != null) {
			for (Map.Entry<String, ?> entry : attributes.entrySet()) {
				addStaticAttribute(entry.getKey(), entry.getValue());
			}
		}
	}

	/**
	 * Allow Map access to the static attributes of this view,
	 * with the option to add or override specific entries.
	 * <p>Useful for specifying entries directly, for example via
	 * "attributesMap[myKey]". This is particularly useful for
	 * adding or overriding entries in child view definitions.
	 */
	public Map<String, Object> getAttributesMap() {
		return this.staticAttributes;
	}

	/**
	 * Add static data to this view, exposed in each view.
	 * <p>"Static" attributes are fixed attributes that are specified in
	 * the View instance configuration. "Dynamic" attributes, on the other hand,
	 * are values passed in as part of the model.
	 * <p>Must be invoked before any calls to {@code render}.
	 * @param name the name of the attribute to expose
	 * @param value the attribute value to expose
	 * @see #render
	 */
	public void addStaticAttribute(String name, Object value) {
		this.staticAttributes.put(name, value);
	}

	/**
	 * Return the static attributes for this view. Handy for testing.
	 * <p>Returns an unmodifiable Map, as this is not intended for
	 * manipulating the Map but rather just for checking the contents.
	 * @return the static attributes in this view
	 */
	public Map<String, Object> getStaticAttributes() {
		return Collections.unmodifiableMap(this.staticAttributes);
	}

	/**
	 * Whether to add path variables in the model or not.
	 * <p>Path variables are commonly bound to URI template variables through the {@code @PathVariable}
	 * annotation. They're are effectively URI template variables with type conversion applied to
	 * them to derive typed Object values. Such values are frequently needed in views for
	 * constructing links to the same and other URLs.
	 * <p>Path variables added to the model override static attributes (see {@link #setAttributes(Properties)})
	 * but not attributes already present in the model.
	 * <p>By default this flag is set to {@code true}. Concrete view types can override this.
	 * @param exposePathVariables {@code true} to expose path variables, and {@code false} otherwise.
	 */
	public void setExposePathVariables(boolean exposePathVariables) {
		this.exposePathVariables = exposePathVariables;
	}

	/**
	 * Returns the value of the flag indicating whether path variables should be added to the model or not.
	 */
	public boolean isExposePathVariables() {
		return this.exposePathVariables;
	}


	// 将模型数据以某种MIME类型渲染出来
	public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (logger.isTraceEnabled()) {
			logger.trace("Rendering view with name '" + this.beanName + "' with model " + model +
				" and static attributes " + this.staticAttributes);
		}
		// 创建一个动态值和静态属性的map
		Map<String, Object> mergedModel = createMergedOutputModel(model, request, response);
		// 设置 response 报文头
		prepareResponse(request, response);
		// 把渲染view的工作放到renderMergedOutputModel()实现中
		renderMergedOutputModel(mergedModel, request, response);
	}
	protected Map<String, Object> createMergedOutputModel(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) {

		@SuppressWarnings("unchecked")
		Map<String, Object> pathVars = (this.exposePathVariables ?
			(Map<String, Object>) request.getAttribute(View.PATH_VARIABLES) : null);

		// Consolidate static and dynamic model attributes.
		int size = this.staticAttributes.size();
		size += (model != null) ? model.size() : 0;
		size += (pathVars != null) ? pathVars.size() : 0;
		Map<String, Object> mergedModel = new LinkedHashMap<String, Object>(size);
		mergedModel.putAll(this.staticAttributes);
		if (pathVars != null) {
			mergedModel.putAll(pathVars);
		}
		if (model != null) {
			mergedModel.putAll(model);
		}

		// Expose RequestContext?
		if (this.requestContextAttribute != null) {
			mergedModel.put(this.requestContextAttribute, createRequestContext(request, response, mergedModel));
		}

		return mergedModel;
	}
	protected RequestContext createRequestContext(HttpServletRequest request, HttpServletResponse response, Map<String, Object> model) {
		return new RequestContext(request, response, getServletContext(), model);
	}
	protected void prepareResponse(HttpServletRequest request, HttpServletResponse response) {
		if (generatesDownloadContent()) {
			response.setHeader("Pragma", "private");
			response.setHeader("Cache-Control", "private, must-revalidate");
		}
	}
	protected boolean generatesDownloadContent() {
		return false;
	}
	// 将最终渲染 view 的工作留给子类实现
	protected abstract void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception;










	// 将 model 中的属性和值作为属性放入 request
	protected void exposeModelAsRequestAttributes(Map<String, Object> model, HttpServletRequest request) throws Exception {
		for (Map.Entry<String, Object> entry : model.entrySet()) {
			String modelName = entry.getKey();
			Object modelValue = entry.getValue();
			if (modelValue != null) {
				request.setAttribute(modelName, modelValue);
				if (logger.isDebugEnabled()) {
					logger.debug("Added model object '" + modelName + "' of type [" + modelValue.getClass().getName() + "] to request in view with name '" + getBeanName() + "'");
				}
			}
			else {
				request.removeAttribute(modelName);
				if (logger.isDebugEnabled()) {
					logger.debug("Removed model object '" + modelName + "' from request in view with name '" + getBeanName() + "'");
				}
			}
		}
	}

	/**
	 * Create a temporary OutputStream for this view.
	 * <p>This is typically used as IE workaround, for setting the content length header
	 * from the temporary stream before actually writing the content to the HTTP response.
	 */
	protected ByteArrayOutputStream createTemporaryOutputStream() {
		return new ByteArrayOutputStream(OUTPUT_BYTE_ARRAY_INITIAL_SIZE);
	}

	/**
	 * Write the given temporary OutputStream to the HTTP response.
	 * @param response current HTTP response
	 * @param baos the temporary OutputStream to write
	 * @throws IOException if writing/flushing failed
	 */
	protected void writeToResponse(HttpServletResponse response, ByteArrayOutputStream baos) throws IOException {
		// Write content type and also length (determined via byte array).
		response.setContentType(getContentType());
		response.setContentLength(baos.size());

		// Flush byte array to servlet output stream.
		ServletOutputStream out = response.getOutputStream();
		baos.writeTo(out);
		out.flush();
	}

	/**
	 * Set the content type of the response to the configured
	 * {@link #setContentType(String) content type} unless the
	 * {@link View#SELECTED_CONTENT_TYPE} request attribute is present and set
	 * to a concrete media type.
	 */
	protected void setResponseContentType(HttpServletRequest request, HttpServletResponse response) {
		MediaType mediaType = (MediaType) request.getAttribute(View.SELECTED_CONTENT_TYPE);
		if (mediaType != null && mediaType.isConcrete()) {
			response.setContentType(mediaType.toString());
		}
		else {
			response.setContentType(getContentType());
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getName());
		if (getBeanName() != null) {
			sb.append(": name '").append(getBeanName()).append("'");
		}
		else {
			sb.append(": unnamed");
		}
		return sb.toString();
	}

}
