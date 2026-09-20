<template>
  <el-dialog v-model="dialogVisible" title="快速编辑" width="400px">
    <el-form :model="form" label-width="80px">
      <el-form-item label="日期">
        <el-input :value="day" disabled />
      </el-form-item>
      <el-form-item label="价格">
        <el-input-number v-model="form.price" :min="0" :precision="2" />
      </el-form-item>
      <el-form-item label="库存">
        <el-input-number v-model="form.stock" :min="0" />
      </el-form-item>
      <el-form-item label="状态">
        <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="可售" inactive-text="停售" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleConfirm">确认</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue';

const props = defineProps(['visible', 'day', 'initialData']);
const emit = defineEmits(['update:visible', 'confirm']);

const dialogVisible = ref(false);
const form = ref({ price: 0, stock: 0, status: 1 });

watch(() => props.visible, (newVal) => {
  dialogVisible.value = newVal;
  if (newVal && props.initialData) {
    form.value = { ...props.initialData };
  }
});

watch(dialogVisible, (newVal) => {
  emit('update:visible', newVal);
});

const handleConfirm = () => {
  emit('confirm', form.value);
};
</script>
