package org.aopbuddy.plugin.service;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import org.aopbuddy.plugin.mapper.BaseMapper;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.InputStream;
import java.util.function.Function;

@Service(Service.Level.APP)  // 全局应用级服务（IDE启动时初始化，全局唯一）
public final class DatabaseService{
    private static final Logger LOGGER = Logger.getInstance(DatabaseService.class);
    private SqlSessionFactory sqlSessionFactory = null;
    private DataSource dataSource; // 新增：持有数据源引用，用于关闭连接池


    public DatabaseService() {
        // 在构造函数中启动线程初始化数据库
        new Thread(this::buildSqlSessionFactory).start();
    }

    private void buildSqlSessionFactory() {
        String basePath = System.getProperty("user.home");
        File dbDir = new File(basePath + "/.aopbuddy");
        File dbFile = new File(dbDir, "aopbuddy.mv.db");  // H2 文件标志
        // 数据源配置 (POOLED 类型)
        String url = "jdbc:h2:file:" + dbFile.getAbsolutePath().replace(".mv.db", "") + ";AUTO_SERVER=TRUE";  // 文件模式
        String driver = "org.h2.Driver";
        String username = "sa";
        String password = "";
        this.dataSource = new PooledDataSource(driver, url, username, password);
        LOGGER.info("H2 DB URL: " + url);
        // 事务管理器配置 (JDBC 类型)
        TransactionFactory transactionFactory = new JdbcTransactionFactory();

        // 可选：设置隔离级别
        // transactionFactory.getTransaction(dataSource).setIsolationLevel(TransactionIsolationLevel.READ_COMMITTED);

        Environment environment = new Environment(
                "development", transactionFactory, dataSource
        );

        // MyBatis 配置
        Configuration configuration = new Configuration(environment);
//         添加 Mapper XML 资源 (对应 <mapper resource="mapper/CallRecordMapper.xml"/>
//        configuration.addMapper(CallRecordMapper.class);
//        configuration.addMappers("org.aopbuddy.plugin.mapper");

        try {
            // 加载XML映射文件
            // 注意：这里使用的是您提到的 CallMapper.xml
            InputStream mapperInputStream = Resources.getResourceAsStream("mapper/CallRecordMapper.xml");
            if (mapperInputStream != null) {
                XMLMapperBuilder mapperBuilder = new XMLMapperBuilder(
                        mapperInputStream,
                        configuration,
                        "mapper/CallRecordMapper.xml",
                        configuration.getSqlFragments()
                );
                mapperBuilder.parse();
                LOGGER.info("成功加载XML映射文件: mapper/CallMapper.xml");
            } else {
                LOGGER.error("未找到映射文件: mapper/CallMapper.xml");
            }
        } catch (Exception e) {
            LOGGER.error("加载XML映射文件失败: mapper/CallMapper.xml", e);
        }


        // 构建 SqlSessionFactory
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
        this.sqlSessionFactory = sqlSessionFactory;
    }

    public <T extends BaseMapper, R> R execute(Class<T> mapperClass, Function<T, R> mapperFunction) {
        try {
            if (sqlSessionFactory == null) {
                throw new IllegalStateException("SqlSessionFactory is not initialized yet");
            }

            try (SqlSession session = sqlSessionFactory.openSession()) {
                T mapper = session.getMapper(mapperClass);
                R result = mapperFunction.apply(mapper);
                session.commit();
                return result;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute database operation: " + e.getMessage(), e);
        }
    }
}
