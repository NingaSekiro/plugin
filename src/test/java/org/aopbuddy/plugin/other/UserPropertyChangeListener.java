package org.aopbuddy.plugin.other;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class UserPropertyChangeListener implements PropertyChangeListener {
    private String changedProperty;
    private Object oldValue;
    private Object newValue;
    private boolean propertyChanged = false;
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        this.changedProperty = evt.getPropertyName();
        this.oldValue = evt.getOldValue();
        this.newValue = evt.getNewValue();
        this.propertyChanged = true;
        System.out.println("属性 [" + changedProperty + "] 从 [" + oldValue + "] 变更为 [" + newValue + "]");
    }
    
    // Getters for testing
    public String getChangedProperty() {
        return changedProperty;
    }
    
    public Object getOldValue() {
        return oldValue;
    }
    
    public Object getNewValue() {
        return newValue;
    }
    
    public boolean isPropertyChanged() {
        return propertyChanged;
    }
    
    public void reset() {
        this.changedProperty = null;
        this.oldValue = null;
        this.newValue = null;
        this.propertyChanged = false;
    }
}