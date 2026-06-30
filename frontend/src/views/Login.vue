<template>
  <div class="login-page">
    <el-card class="login-card">
      <div class="login-header">
        <el-icon class="login-icon"><Tools /></el-icon>
        <h2>摩托车零部件采购管理系统</h2>
        <p>Motorcycle Parts Procurement System</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="onSubmit">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入账号" size="large" :prefix-icon="User" clearable />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            :prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" style="width: 100%" @click="onSubmit">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-tips">
        测试账号：admin / purchase01 / req01，默认密码见后端初始化数据
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function onSubmit() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await userStore.login(form)
      ElMessage.success('登录成功')
      router.push('/')
    } catch (e) {
      // 错误提示已由 axios 拦截器统一弹出
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1f2d3d 0%, #409eff 100%);
}
.login-card {
  width: 400px;
  padding: 12px 16px 4px;
}
.login-header {
  text-align: center;
  margin-bottom: 24px;
}
.login-icon {
  font-size: 40px;
  color: #409eff;
}
.login-header h2 {
  margin: 12px 0 4px;
  font-size: 20px;
}
.login-header p {
  margin: 0;
  color: #909399;
  font-size: 12px;
}
.login-tips {
  text-align: center;
  color: #c0c4cc;
  font-size: 12px;
  padding-bottom: 8px;
}
</style>
