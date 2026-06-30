<template>
  <el-tag v-if="info" :type="info.tag" disable-transitions>{{ info.text }}</el-tag>
  <span v-else>{{ value ?? '-' }}</span>
</template>

<script setup>
import { computed } from 'vue'

// 通用状态标签：传入枚举映射表 map 与状态值 value，自动渲染文本与颜色
const props = defineProps({
  map: { type: Object, required: true },
  value: { type: [Number, String], default: null }
})

const info = computed(() => props.map[props.value] || null)
</script>
