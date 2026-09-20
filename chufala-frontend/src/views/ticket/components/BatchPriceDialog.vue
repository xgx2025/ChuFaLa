<template>
  <el-dialog v-model="dialogVisible" title="批量设置价格" width="500px">
    <el-form :model="form" label-width="100px">
      <el-form-item label="应用范围">
        <el-radio-group v-model="form.scope">
          <el-radio label="range">指定日期范围</el-radio>
          <el-radio label="weekday">指定星期</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="form.scope === 'range'" label="日期范围">
        <el-date-picker v-model="form.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" />
      </el-form-item>
      <el-form-item v-if="form.scope === 'weekday'" label="选择星期">
        <el-checkbox-group v-model="form.weekdays">
          <el-checkbox :label="1">周一</el-checkbox>
          <el-checkbox :label="2">周二</el-checkbox>
          <el-checkbox :label="3">周三</el-checkbox>
          <el-checkbox :label="4">周四</el-checkbox>
          <el-checkbox :label="5">周五</el-checkbox>
          <el-checkbox :label="6">周六</el-checkbox>
          <el-checkbox :label="0">周日</el-checkbox>
        </el-checkbox-group>
      </el-form-item>
      <el-form-item label="统一价格">
        <el-input-number v-model="form.price" :min="0" :precision="2" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleConfirm">确认设置</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue';

const props = defineProps(['visible']);
const emit = defineEmits(['update:visible', 'confirm']);

const dialogVisible = ref(false);
const form = ref({
  scope: 'range',
  dateRange: [],
  weekdays: [],
  price: 0,
});

watch(() => props.visible, (newVal) => {
  dialogVisible.value = newVal;
});

watch(dialogVisible, (newVal) => {
  emit('update:visible', newVal);
});

const handleConfirm = () => {
  // 这里可以做一些校验
  if (form.value.scope === 'range' && !form.value.dateRange) {
    // 提示选择日期
    return;
  }
  if (form.value.scope === 'weekday' && form.value.weekdays.length === 0) {
    // 提示选择星期
    return;
  }
  emit('confirm', form.value);
};
</script>
