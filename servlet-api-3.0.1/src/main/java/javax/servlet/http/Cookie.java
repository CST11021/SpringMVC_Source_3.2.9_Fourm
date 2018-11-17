/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.servlet.http;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Creates a cookie, a small amount of information sent by a servlet to
 * a Web browser, saved by the browser, and later sent back to the server.
 * A cookie's value can uniquely
 * identify a client, so cookies are commonly used for session management.
 *
 * <p>A cookie has a name, a single value, and optional attributes
 * such as a comment, path and domain qualifiers, a maximum age, and a
 * version number. Some Web browsers have bugs in how they handle the
 * optional attributes, so use them sparingly to improve the interoperability
 * of your servlets.
 *
 * <p>The servlet sends cookies to the browser by using the
 * {@link HttpServletResponse#addCookie} method, which adds
 * fields to HTTP response headers to send cookies to the
 * browser, one at a time. The browser is expected to
 * support 20 cookies for each Web server, 300 cookies total, and
 * may limit cookie size to 4 KB each.
 *
 * <p>The browser returns cookies to the servlet by adding
 * fields to HTTP request headers. Cookies can be retrieved
 * from a request by using the {@link HttpServletRequest#getCookies} method.
 * Several cookies might have the same name but different path attributes.
 *
 * <p>Cookies affect the caching of the Web pages that use them.
 * HTTP 1.0 does not cache pages that use cookies created with
 * this class. This class does not support the cache control
 * defined with HTTP 1.1.
 *
 * <p>This class supports both the Version 0 (by Netscape) and Version 1
 * (by RFC 2109) cookie specifications. By default, cookies are
 * created using Version 0 to ensure the best interoperability.
 *
 * @author Various
 */
public class Cookie implements Cloneable, Serializable {

    private static final long serialVersionUID = -6454587001725327448L;

    /** 表示cookie的名称不允许包含的的特殊字符 */
    private static final String TSPECIALS;

    private static final String LSTRING_FILE = "javax.servlet.http.LocalStrings";

    private static ResourceBundle lStrings = ResourceBundle.getBundle(LSTRING_FILE);

    static {
        if (Boolean.valueOf(System.getProperty("org.glassfish.web.rfc2109_cookie_names_enforced", "true"))) {
            TSPECIALS = "/()<>@,;:\\\"[]?={} \t";
        } else {
            TSPECIALS = ",; ";
        }
    }

    /** cookie的名称 */
    private String name;
    /** cookie的值 */
    private String value;

    /** 对该cookie进行描述的信息(说明作用)，浏览器显示cookie信息时能看到 */
    private String comment;
    /** cookie是基于域名，该字段表示该cookie的有效域名 */
    private String domain;
    /** 表示该cookie的过期时间，比如：
     * 60：表示60秒后删除cookie；
     * 0：立马删除；
     * -1：关闭浏览器后删除 */
    private int maxAge = -1;
    /** 表示cookie的路径 */
    private String path;
    /**
     * 在setSecure(true); 的情况下，只有https才传递到服务器端。http是不会传递的
     * HTTP Cookie      设置了secure ，   该cookie只能在HTTPS通道下被写入浏览器。
     * HTTPS Cookie     设置了secure ，   该cookie只能在HTTPS通道下被写入浏览器。
     * HTTP Cookie      未设置了secure ，该cookie既可以在HTTPS也可以在HTTP通道下被写入浏览器。
     *  HTTPS Cookie     未设置了secure ，该cookie既可以在HTTPS也可以在HTTP通道下被写入浏览器(可能会泄露信息安全)。
     */
    private boolean secure;
    /** 表示cookie的版本 */
    private int version = 0;
    /** 如果在Cookie中设置了"HttpOnly"属性，那么通过JavaScript脚本将无法读取到Cookie信息，这样能有效的防止XSS攻击，让网站应用更加安全 */
    private boolean isHttpOnly = false;


    public Cookie(String name, String value) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException(lStrings.getString("err.cookie_name_blank"));
        }
        // 判断是不是有效的cookie名
        if (!isToken(name) ||
                name.equalsIgnoreCase("Comment") || // rfc2019
                name.equalsIgnoreCase("Discard") || // 2019++
                name.equalsIgnoreCase("Domain") ||
                name.equalsIgnoreCase("Expires") || // (old cookies)
                name.equalsIgnoreCase("Max-Age") || // rfc2019
                name.equalsIgnoreCase("Path") ||
                name.equalsIgnoreCase("Secure") ||
                name.equalsIgnoreCase("Version") ||
                name.startsWith("$")) {
            String errMsg = lStrings.getString("err.cookie_name_is_token");
            Object[] errArgs = new Object[1];
            errArgs[0] = name;
            errMsg = MessageFormat.format(errMsg, errArgs);
            throw new IllegalArgumentException(errMsg);
        }

        this.name = name;
        this.value = value;
    }

    /**
     * 判断这个value是不是可以作为cookie的名称
     *
     * Tests a string and returns true if the string counts as a
     * reserved token in the Java language.
     *
     * @param value the <code>String</code> to be tested
     *
     * @return <code>true</code> if the <code>String</code> is a reserved
     * token; <code>false</code> otherwise
     */
    private boolean isToken(String value) {
        int len = value.length();
        for (int i = 0; i < len; i++) {
            char c = value.charAt(i);
            if (c < 0x20 || c >= 0x7f || TSPECIALS.indexOf(c) != -1) {
                return false;
            }
        }

        return true;
    }

    // getter and setter ...

    public void setComment(String purpose) {
        comment = purpose;
    }
    public String getComment() {
        return comment;
    }
    public void setDomain(String domain) {
        // IE allegedly needs this
        this.domain = domain.toLowerCase(Locale.ENGLISH);
    }
    public String getDomain() {
        return domain;
    }
    public void setMaxAge(int expiry) {
        maxAge = expiry;
    }
    public int getMaxAge() {
        return maxAge;
    }
    public void setPath(String uri) {
        path = uri;
    }
    public String getPath() {
        return path;
    }
    public void setSecure(boolean flag) {
        secure = flag;
    }
    public boolean getSecure() {
        return secure;
    }
    public String getName() {
        return name;
    }
    public void setValue(String newValue) {
        value = newValue;
    }
    public String getValue() {
        return value;
    }
    public int getVersion() {
        return version;
    }
    public void setVersion(int v) {
        version = v;
    }
    public void setHttpOnly(boolean isHttpOnly) {
        this.isHttpOnly = isHttpOnly;
    }
    public boolean isHttpOnly() {
        return isHttpOnly;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}

