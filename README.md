## 本地java进程连接：

1. 选择“1.未连接JVM"
2. 选择目标JVM进程，连接

![image-20260125170720844](https://s2.loli.net/2026/01/25/ZTbNi6GcQHAWgRL.png)



## 附着方式一：运行时动态附着

优点：不用重启

缺点：重启后需要重新附着

适合于临时测试

步骤：

1. 上传agent包
2. 运行命令

```sh
java -jar agent-jar-with-dependencies.jar
```

3. 选择对应JVM进程
4. 插件连接进程所在Ip:12345（可能需要端口转发）

![image-20260126205003674](https://s2.loli.net/2026/01/26/wQOmBsbHgVfKTx1.png)

![image-20260126205141935](https://s2.loli.net/2026/01/26/fL4yGxlIkCNuZDO.png)

## 附着方式二：启动时附着

优点：重启后自动附着

缺点：

1. 第一次使用需要重启

步骤：

1. 上传agent包到目标Java进程所依赖的lib包下
2. 重启目标Java进程
3. 插件连接进程所在Ip:12345

## 附着方式三：启动时附着

优点：重启后自动附着

缺点：第一次使用需要重启，需要修改启动命令

步骤：

1. 上传agent包
2. 修改目标Java进程启动命令，加上：-javaagent:D:\Tmp\agent-jar-with-dependencies.jar
3. 插件连接进程所在Ip:12345
## 1.Run the Method：

#### 运行指定方法

1. 在要运行的方法处右键选择“Run The Method”
2. 在下方的Groovy Console处，选择类加载器，修改方法的入参，点击运行按钮
3. 查看运行方法结果

![image-20260125170839704](https://s2.loli.net/2026/01/25/BfC7n91xdziQskO.png)

![image-20260125184928338](https://s2.loli.net/2026/01/25/pDbTnt3Q6rjvlim.png)

#### 运行本地groovy文件

前提条件：

1. 附着目标java进程
2. groovy文件处右键

![image-20260125230100285](https://s2.loli.net/2026/01/25/FbR574mnc1Vfzxr.png)

## 2. 预置DSL：

### 2.1 获取指定类型的对象实例数组

```java
public Object[] get(Class<?> cla)
```

**功能**: 获取指定类型的对象实例数组，默认限制为10个实例。

**参数**:

- `cla`: 要获取实例的类类型

**返回值**:

- `Object[]`: 包含指定类型对象实例的数组

**使用示例**:

```java
// 获取所有String类型的对象实例（最多10个）
Object[] strings = tool.get(String.class);
for (Object obj : strings) {
    System.out.println((String) obj);
}
```

### 2.2 获取指定类型的第一个对象实例

```java
public Object getObject(Class<?> cla)
```

**功能**: 获取指定类型的第一个对象实例。

**参数**:

- `cla`: 要获取实例的类类型

**返回值**:

- `Object`: 指定类型的第一个对象实例

**使用示例**:

```java
// 获取第一个User类型的对象实例
Object userObj = tool.getObject(User.class);
if (userObj != null) {
    User user = (User) userObj;
    System.out.println("First user: " + user.getName());
}
```

### 2.3 将对象转换为JSON字符串

```java
public String toJson(Object value)
```

**功能**: 将任意对象转换为JSON格式的字符串。

**参数**:

- `value`: 要转换的对象

**返回值**:

- `String`: JSON格式的字符串表示

### 2.4 将JSON字符串转换为对象

```java
public <T> T jsonToObj(String text, Class<T> clazz)
```

**功能**: 将JSON字符串转换为指定类型的对象。

**参数**:

- `text`: JSON格式的字符串
- `clazz`: 目标对象的类类型

**返回值**:

- `T`: 转换后的对象实例

### 2.5 读取文件内容

```java
public String readFile(String filePath)
```

**功能**: 以UTF-8编码读取指定文件的内容。

**参数**:

- `filePath`: 文件路径

**返回值**:

- `String`: 文件内容的字符串表示

### 2.6 反编译CLass

```java
public String jadClass(String className)
```

**功能**: 反编译Class

**参数**:

- `className`: 对应类的全类名

**返回值**:

- `String`: 对应类的java代码

## 热更新方法体：

#### 应用限制：

1. 只限于更改方法体和已有field（不能更改方法入参，返参）
2. 采用本地 Idea的project jdk进行编译



#### 使用方法：

在commit窗口处选择要更改的文件，再点击下方的狗头图标，进行hotswap，右下方处提示热部署即为成功。

![image-20260125185835732](https://s2.loli.net/2026/01/25/SO5NDmoHLxMAvwT.png)
### watch说明：

记录方法的入参和返参（json结构），不适合watch无法json序列化的字段

再次点击狗头图标后会取消watch

![image-20260125190406218](https://s2.loli.net/2026/01/25/XVeA7fwpO9zl1P3.png)





### watch结果：

watch方法结果储存在本地的H2数据库中

用户名：sa

密码：123456

```
jdbc:h2:file:C:\Users\你的用户名\.aopbuddy\aopbuddy;AUTO_SERVER=TRUE
```

![image-20260125195732972](https://s2.loli.net/2026/01/25/IEO1suwJWKaxbQB.png)


录制默认设置：

1. 只有附着程序后才能正常录制
2. 同种链路只会保存8个
3. 录制的内存数据默认保存10min
4. 录制设置：取自project里的目录
5. 自定义package：用户自填（不要填过大范围）
6. 为减少录制数据体积，不支持jdk方法录制，不支持getter，setter方法录制。

![image-20260125230510323](https://s2.loli.net/2026/01/25/Of4BjMSgNcvemWD.png)

![image-20260125230616552](https://s2.loli.net/2026/01/25/3QlTDWv9GfqJK21.png)

![image-20260125231012829](https://s2.loli.net/2026/01/25/31BQJ5b4qnszICa.png)

![image-20260127213157959](https://s2.loli.net/2026/01/27/UMW83RZ6smIplvB.png)

![image-20260127212216426](https://s2.loli.net/2026/01/27/LB5njzb4kT9Dgw3.png)

#### 插件数据储存位置：

插件的录制数据和watch数据默认存储在本地的H2数据库中。

用户名：sa

密码：123456

```
jdbc:h2:file:C:\Users\你的用户名\.aopbuddy\aopbuddy;AUTO_SERVER=TRUE
```

#### 联系：

进群或微信联系qinc135792

![image-20260127220529648](https://s2.loli.net/2026/01/27/aec3QRm5hWNVXFJ.png)


