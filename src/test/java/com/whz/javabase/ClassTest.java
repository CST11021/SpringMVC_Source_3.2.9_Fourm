package com.whz.javabase;

import org.codehaus.jackson.map.Serializers;
import org.junit.Test;

/**
 * Created by wb-whz291815 on 2017/7/10.
 */
public class ClassTest {

    interface BaseInterface {
    }

    class BaseClass implements BaseInterface {
    }

    class SubClass extends BaseClass {
    }

    BaseClass baseClass;
    SubClass subClass = new SubClass();

    @Test
    public void asSubclassTest() {
        // <U> Class<? extends U>	asSubclass(Class<U> clazz)
        // 使用该方法可以将 clazz 转化为对应的子类类型
        Class baseClass = subClass.getClass().asSubclass(BaseClass.class);
        Class baseInterface = subClass.getClass().asSubclass(BaseInterface.class);
    }

    @Test
    public void castTest() {
        // T	cast(Object obj)
        // 将一个对象强制转换成此 Class 对象所表示的类或接口。
        BaseClass base = BaseClass.class.cast(subClass);
    }

    @Test
    public void desiredAssertionStatusTest() {
        // boolean	desiredAssertionStatus()
        // 如果要在调用此方法时将要初始化该类，则返回将分配给该类的断言状态。

        System.out.println("status = " + subClass.getClass().desiredAssertionStatus());

//        baseClass = new BaseClass();
//        System.out.println("status = " + baseClass.getClass().desiredAssertionStatus());
        System.out.println("status = " + BaseClass.class.desiredAssertionStatus());
    }


    /**
     boolean	desiredAssertionStatus()
     如果要在调用此方法时将要初始化该类，则返回将分配给该类的断言状态。



     -----------------------------------------------------------------------------------------------
     String	getCanonicalName()
     返回 Java Language Specification 中所定义的底层类的规范化名称。
     String	getSimpleName()
     返回源代码中给出的底层类的简称。
     String	getName()
     以 String 的形式返回此 Class 对象所表示的实体（类、接口、数组类、基本类型或 void）名称。

     Fruit apple=new Apple();
     System.out.println(apple.getClass().getCanonicalName());//返回com.test.Apple
     System.out.println(apple.getClass().getSimpleName());//Apple
     System.out.println(apple.getClass().getName());//返回com.test.Apple
     Apple[] arrApple=new Apple[]{};
     System.out.println(arrApple.getClass().getCanonicalName());//返回com.test.Apple[]
     System.out.println(arrApple.getClass().getSimpleName());//返回Apple[]
     System.out.println(arrApple.getClass().getName());//返回[Lcom.test.Apple;
     -----------------------------------------------------------------------------------------------



     Class<?>[]	getClasses()
     返回一个包含某些 Class 对象的数组，这些对象表示属于此 Class 对象所表示的类的成员的所有公共类和接口。

     Class<?>	getComponentType()
     返回表示数组组件类型的 Class。



     Annotation[]	getDeclaredAnnotations()
     返回直接存在于此元素上的所有注释。
     Class<?>[]	getDeclaredClasses()
     返回 Class 对象的一个数组，这些对象反映声明为此 Class 对象所表示的类的成员的所有类和接口。
     Constructor<T>	getDeclaredConstructor(Class<?>... parameterTypes)
     返回一个 Constructor 对象，该对象反映此 Class 对象所表示的类或接口的指定构造方法。
     Constructor<?>[]	getDeclaredConstructors()
     返回 Constructor 对象的一个数组，这些对象反映此 Class 对象表示的类声明的所有构造方法。
     Field	getDeclaredField(String name)
     返回一个 Field 对象，该对象反映此 Class 对象所表示的类或接口的指定已声明字段。
     Field[]	getDeclaredFields()
     返回 Field 对象的一个数组，这些对象反映此 Class 对象所表示的类或接口所声明的所有字段。
     Method	getDeclaredMethod(String name, Class<?>... parameterTypes)
     返回一个 Method 对象，该对象反映此 Class 对象所表示的类或接口的指定已声明方法。
     Method[]	getDeclaredMethods()
     返回 Method 对象的一个数组，这些对象反映此 Class 对象表示的类或接口声明的所有方法，包括公共、保护、默认（包）访问和私有方法，但不包括继承的方法。
     Class<?>	getDeclaringClass()
     如果此 Class 对象所表示的类或接口是另一个类的成员，则返回的 Class 对象表示该对象的声明类。
     Class<?>	getEnclosingClass()
     返回底层类的立即封闭类。
     Constructor<?>	getEnclosingConstructor()
     如果该 Class 对象表示构造方法中的一个本地或匿名类，则返回 Constructor 对象，它表示底层类的立即封闭构造方法。
     Method	getEnclosingMethod()
     如果此 Class 对象表示某一方法中的一个本地或匿名类，则返回 Method 对象，它表示底层类的立即封闭方法。
     T[]	getEnumConstants()
     如果此 Class 对象不表示枚举类型，则返回枚举类的元素或 null。
     Field	getField(String name)
     返回一个 Field 对象，它反映此 Class 对象所表示的类或接口的指定公共成员字段。
     Field[]	getFields()
     返回一个包含某些 Field 对象的数组，这些对象反映此 Class 对象所表示的类或接口的所有可访问公共字段。
     Type[]	getGenericInterfaces()
     返回表示某些接口的 Type，这些接口由此对象所表示的类或接口直接实现。
     Type	getGenericSuperclass()
     返回表示此 Class 所表示的实体（类、接口、基本类型或 void）的直接超类的 Type。
     Class<?>[]	getInterfaces()
     确定此对象所表示的类或接口实现的接口。
     Method	getMethod(String name, Class<?>... parameterTypes)
     返回一个 Method 对象，它反映此 Class 对象所表示的类或接口的指定公共成员方法。
     Method[]	getMethods()
     返回一个包含某些 Method 对象的数组，这些对象反映此 Class 对象所表示的类或接口（包括那些由该类或接口声明的以及从超类和超接口继承的那些的类或接口）的公共 member 方法。
     int	getModifiers()
     返回此类或接口以整数编码的 Java 语言修饰符。

     Package	getPackage()
     获取此类的包。
     ProtectionDomain	getProtectionDomain()
     返回该类的 ProtectionDomain。
     URL	getResource(String name)
     查找带有给定名称的资源。
     InputStream	getResourceAsStream(String name)
     查找具有给定名称的资源。
     Object[]	getSigners()
     获取此类的标记。

     Class<? super T>	getSuperclass()
     返回表示此 Class 所表示的实体（类、接口、基本类型或 void）的超类的 Class。
     TypeVariable<Class<T>>[]	getTypeParameters()
     按声明顺序返回 TypeVariable 对象的一个数组，这些对象表示用此 GenericDeclaration 对象所表示的常规声明来声明的类型变量。
     boolean	isAnnotation()
     如果此 Class 对象表示一个注释类型则返回 true。
     boolean	isAnnotationPresent(Class<? extends Annotation> annotationClass)
     如果指定类型的注释存在于此元素上，则返回 true，否则返回 false。
     boolean	isAnonymousClass()
     当且仅当底层类是匿名类时返回 true。
     boolean	isArray()
     判定此 Class 对象是否表示一个数组类。
     boolean	isAssignableFrom(Class<?> cls)
     判定此 Class 对象所表示的类或接口与指定的 Class 参数所表示的类或接口是否相同，或是否是其超类或超接口。
     boolean	isEnum()
     当且仅当该类声明为源代码中的枚举时返回 true。
     boolean	isInstance(Object obj)
     判定指定的 Object 是否与此 Class 所表示的对象赋值兼容。
     boolean	isInterface()
     判定指定的 Class 对象是否表示一个接口类型。
     boolean	isLocalClass()
     当且仅当底层类是本地类时返回 true。
     boolean	isMemberClass()
     当且仅当底层类是成员类时返回 true。
     boolean	isPrimitive()
     判定指定的 Class 对象是否表示一个基本类型。
     boolean	isSynthetic()
     如果此类是复合类，则返回 true，否则 false。
     T	newInstance()
     创建此 Class 对象所表示的类的一个新实例。
     String	toString()
     将对象转换为字符串。
     */



}
