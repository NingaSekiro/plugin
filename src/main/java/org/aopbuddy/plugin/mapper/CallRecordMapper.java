package org.aopbuddy.plugin.mapper;


import com.aopbuddy.record.CallRecordDo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CallRecordMapper extends BaseMapper {
    void insertBatchCallRecords(@Param("list") List<CallRecordDo> callRecords, @Param("tableName") String tableName);

    List<CallRecordDo> selectByIdGreaterThan(@Param("id") Long id, @Param("tableName") String tableName);

    int selectMaxChainId(@Param("tableName") String tableName);

    List<CallRecordDo> selectByMethods(@Param("list") List<String> methods, @Param("tableName") String tableName);

    List<CallRecordDo> selectMaxIdMethodsPerChain(@Param("tableName") String tableName);

    List<CallRecordDo> selectMethodsByChainId(@Param("chainId") int chainId, @Param("tableName") String tableName);

    CallRecordDo selectById(@Param("id") Long id, @Param("tableName") String tableName);

    void createTableWithName(@Param("tableName") String tableName);

    void dropTableWithName(@Param("tableName") String tableName);

    //    查出所有表名
    List<String> selectAllTableNames();

}

