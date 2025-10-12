package org.aopbuddy.plugin.other;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class UserBean {
    private String name;
    private int age;
    private final PropertyChangeSupport support;
    
    public UserBean() {
        support = new PropertyChangeSupport(this);
    }
    
    // 添加 PropertyChangeListener
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
    
    // 移除 PropertyChangeListener
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
    
    // name 属性的 getter 和 setter
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        String oldValue = this.name;
        this.name = name;
        support.firePropertyChange("name", oldValue, name);
    }
    
    // age 属性的 getter 和 setter
    public int getAge() {
        return age;
    }
    
    public void setAge(int age) {
        int oldValue = this.age;
        this.age = age;
        support.firePropertyChange("age", oldValue, age);
    }
}
