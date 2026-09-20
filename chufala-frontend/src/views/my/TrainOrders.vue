<template>
    <!-- 筛选栏 -->
    <el-card shadow="hover" class="filter-card">
      <el-select  placeholder="订单类型" style="width: 180px; margin-right: 20px;">
        <el-option label="全部订单" value="all"></el-option>
        <el-option label="已取消" value="已取消"></el-option>
        <el-option label="待支付" value="待支付"></el-option>
        <el-option label="已支付" value="已支付"></el-option>
      </el-select>
      <el-button type="primary"  size="small" >查询</el-button>
    </el-card>
</template>