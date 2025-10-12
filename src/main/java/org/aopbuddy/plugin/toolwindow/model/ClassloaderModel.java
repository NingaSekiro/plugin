package org.aopbuddy.plugin.toolwindow.model;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.ArrayList;
import java.util.List;

public class ClassloaderModel implements ComboBoxModel<String> {

    private String classloader;
    private List<String> classloaders = new ArrayList<>();
    private List<ListDataListener> listeners = new ArrayList<>();

    public ClassloaderModel() {
        // 默认构造函数
    }

    public ClassloaderModel(List<String> classloaders) {
        this.classloaders = new ArrayList<>(classloaders);
        if (!classloaders.isEmpty()) {
            this.classloader = classloaders.get(0);
        }
    }

    @Override
    public void setSelectedItem(Object anItem) {
        this.classloader = (String) anItem;
    }

    @Override
    public Object getSelectedItem() {
        return classloader;
    }

    @Override
    public int getSize() {
        return classloaders.size();
    }

    @Override
    public String getElementAt(int index) {
        if (index >= 0 && index < classloaders.size()) {
            return classloaders.get(index);
        }
        return null;
    }

    public void setClassloaders(List<String> classloaders) {
        this.classloaders = new ArrayList<>(classloaders);
        // 通知监听器数据已更改
        fireContentsChanged();
    }

    public List<String> getClassloaders() {
        return new ArrayList<>(classloaders);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        listeners.remove(l);
    }

    protected void fireContentsChanged() {
        for (ListDataListener listener : listeners) {
            listener.contentsChanged(new ListDataEvent(
                    this,
                    ListDataEvent.CONTENTS_CHANGED,
                    -1,
                    -1
            ));
        }
    }
}
