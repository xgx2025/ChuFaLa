<template>
  <div class="day-item" :class="{ 'is-past': isPast, 'is-sold-out': !isAvailable }">
    <div class="day-number">{{ dayNumber }}</div>
    <div v-if="stockData" class="day-info">
      <div class="price">¥{{ stockData.price }}</div>
      <div class="stock">库存: {{ stockData.stock }}</div>
    </div>
    <div v-else class="no-data">未设置</div>
    <el-icon v-if="!isPast" class="edit-icon" @click.stop="handleEdit"><Edit /></el-icon>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { Edit } from '@element-plus/icons-vue';

const props = defineProps({
  day: { type: String, required: true },
  stockData: { type: Object, default: null },
  isPast: { type: Boolean, default: false },
});

const emit = defineEmits(['edit']);

const dayNumber = computed(() => props.day.split('-').pop());
const isAvailable = computed(() => props.stockData && props.stockData.status === 1 && props.stockData.stock > 0);

const handleEdit = () => {
  emit('edit', props.day, props.stockData);
};
</script>

<style scoped>
.day-item {
  position: relative;
  height: 100%;
  width: 100%;
  padding: 5px;
  box-sizing: border-box;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
}
.day-item.is-past {
  background-color: #f0f0f0;
  color: #bbb;
}
.day-item.is-sold-out {
  background-color: #fef0f0;
  color: #f56c6c;
}
.day-number {
  font-weight: bold;
  font-size: 16px;
}
.day-info {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  font-size: 12px;
}
.price {
  color: #e6a23c;
  font-weight: bold;
}
.stock {
  color: #67c23a;
}
.no-data {
  flex-grow: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 12px;
}
.edit-icon {
  position: absolute;
  top: 5px;
  right: 5px;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s;
}
.day-item:hover .edit-icon {
  opacity: 1;
}
</style>
