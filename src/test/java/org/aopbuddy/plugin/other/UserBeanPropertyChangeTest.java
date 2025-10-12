package org.aopbuddy.plugin.other;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserBeanPropertyChangeTest {
    private UserBean userBean;
    private UserPropertyChangeListener listener;
    
    @BeforeEach
    public void setUp() {
        userBean = new UserBean();
        listener = new UserPropertyChangeListener();
        userBean.addPropertyChangeListener(listener);
    }
    
    @Test
    public void testNamePropertyChange() {
        // 初始状态检查
        assertFalse(listener.isPropertyChanged());
        
        // 修改 name 属性
        userBean.setName("张三");
        
        // 验证监听器被触发
        assertTrue(listener.isPropertyChanged());
        assertEquals("name", listener.getChangedProperty());
        assertNull(listener.getOldValue());
        assertEquals("张三", listener.getNewValue());
        
        // 再次修改，验证更新
        listener.reset();
        userBean.setName("李四");
        
        assertEquals("name", listener.getChangedProperty());
        assertEquals("张三", listener.getOldValue());
        assertEquals("李四", listener.getNewValue());
    }
    
    @Test
    public void testAgePropertyChange() {
        // 修改 age 属性
        userBean.setAge(25);
        
        // 验证监听器被触发
        assertTrue(listener.isPropertyChanged());
        assertEquals("age", listener.getChangedProperty());
        assertEquals(0, listener.getOldValue());
        assertEquals(25, listener.getNewValue());
    }
    
    @Test
    public void testRemoveListener() {
        // 移除监听器
        userBean.removePropertyChangeListener(listener);
        
        // 修改属性
        userBean.setName("王五");
        
        // 验证监听器未被触发
        assertFalse(listener.isPropertyChanged());
    }
}