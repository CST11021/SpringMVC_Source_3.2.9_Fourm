package com.whz.spring.jmx.anno;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;  
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

@Component
@ManagedResource(objectName="manager:name=ManagerRuntimeMBean", description = "VM运行状态")
public class ManagerRuntimeMBean {  
  
    private long getMax() {  
        return Runtime.getRuntime().maxMemory() >> 20;  
    }
    private long getTotal() {  
        return Runtime.getRuntime().totalMemory() >> 20;  
    }
    private long getFree() {  
        return Runtime.getRuntime().freeMemory() >> 20;  
    }
    private long getUsed() {  
        return getTotal() - getFree();  
    }  
  
    @ManagedAttribute(description = "可分配的最大内存")  
    public String getMaxMemory() {  
        return getMax() + " M";  
    }  
  
    @ManagedAttribute(description = "已分配内存")  
    public String getTotalMemory() {  
        return getTotal() + " M";  
    }  
  
    @ManagedAttribute(description = "已使用内存")  
    public String getUsedMemory() {  
        return getUsed() + " M";  
    }  
  
    @ManagedOperation(description = "Shutdown VM")  
    public void shutdown() {  
        Runtime.getRuntime().exit(0);  
    }
}  