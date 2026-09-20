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
  background-color: var(--c-line);
  color: var(--c-ink-4);
}
.day-item.is-sold-out {
  background-color: var(--c-danger-soft);
  color: var(--c-danger);
}
.day-number {
  font-weight: bold;
  font-size: var(--fs-body-lg);
}
.day-info {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  font-size: var(--fs-caption);
}
.price {
  color: var(--c-accent);
  font-weight: bold;
}
.stock {
  color: var(--c-success);
}
.no-data {
  flex-grow: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--c-ink-4);
  font-size: var(--fs-caption);
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
