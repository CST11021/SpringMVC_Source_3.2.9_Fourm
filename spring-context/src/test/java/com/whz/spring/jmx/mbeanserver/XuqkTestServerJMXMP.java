package com.whz.spring.jmx.mbeanserver;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.management.*;
import javax.management.remote.*;

public class XuqkTestServerJMXMP {
    public static void main(String Args[]) {
        try {
            MBeanServer mbs = MBeanServerFactory.newMBeanServer();
            String domain = mbs.getDefaultDomain();
            echo("<<domain:" + domain);
            String className = "com.whz.spring.jmx.TestMbean";
            String name = domain + ":" + "type=" + className + ",index=1";
            ObjectName objectName = ObjectName.getInstance(name);
            mbs.createMBean(className, objectName);

            String protocol = "jmxmp";
            String host = "127.0.0.1";
            int port = 1110;

            JMXServiceURL jmxURL = new JMXServiceURL(protocol, host, port);
            JMXConnectorServer connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(jmxURL, null, mbs);
            connectorServer.start();
            echo("<<mbs is started");
            echo("please click any key to stop mbs!");
            waitForEnterPressed();
            connectorServer.stop();
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (InstanceAlreadyExistsException e) {
            e.printStackTrace();
        } catch (MBeanRegistrationException e) {
            e.printStackTrace();
        } catch (NotCompliantMBeanException e) {
            e.printStackTrace();
        } catch (ReflectionException e) {
            e.printStackTrace();
        } catch (MBeanException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void echo(String msg) {
        System.out.println(msg);
    }

    private static void waitForEnterPressed() {
        try {
            echo("/nPress <Enter> to continue...");
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}