<template>
  <div class="forbidden">
    <el-result icon="error" title="403" sub-title="抱歉，您没有权限访问该页面">
      <template #extra>
        <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
// 无权限提示页
</script>

<style scoped>
.forbidden {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
