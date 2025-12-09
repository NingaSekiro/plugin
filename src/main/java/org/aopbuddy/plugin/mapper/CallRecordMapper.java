package org.aopbuddy.plugin.mapper;


import com.aopbuddy.record.CallRecordDo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CallRecordMapper extends BaseMapper {

  // 批量插入调用记录
  int insertBatchCallRecords(@Param("list") List<CallRecordDo> callRecords,
      @Param("tableName") String tableName);

  // 查出方法调用链头
  List<CallRecordDo> selectMaxIdMethodsPerChain(@Param("tableName") String tableName);


  // 查出调用链中某目标方法列表
  List<CallRecordDo> selectMethodsByChainId(@Param("chainId") int chainId,
      @Param("tableName") String tableName);

  List<CallRecordDo> selectSonThreadLocalIds(@Param("chainId") int chainId,
      @Param("tableName") String tableName,
      @Param("threadLocalMethodIdList") List<Integer> threadLocalMethodIdList);

  // 查询具体方法详情
  CallRecordDo selectById(@Param("id") Long id, @Param("tableName") String tableName);

  void createTableWithName(@Param("tableName") String tableName);

  void dropTableWithName(@Param("tableName") String tableName);

  //    查出所有表名
  List<String> selectAllTableNames();

}

