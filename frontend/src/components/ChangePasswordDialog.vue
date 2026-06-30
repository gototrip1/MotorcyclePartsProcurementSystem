<template>
  <el-dialog
    v-model="visible"
    title="修改密码"
    width="420px"
    :close-on-click-modal="false"
    @closed="onClosed"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="90px"
      @submit.prevent
    >
      <el-form-item label="原密码" prop="oldPassword">
        <el-input
          v-model="form.oldPassword"
          type="password"
          show-password
          placeholder="请输入原密码"
          autocomplete="current-password"
        />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input
          v-model="form.newPassword"
          type="password"
          show-password
          placeholder="请输入新密码（至少 6 位）"
          autocomplete="new-password"
        />
      </el-form-item>
      <el-form-item label="确认新密码" prop="confirmPassword">
        <el-input
          v-model="form.confirmPassword"
          type="password"
          show-password
          placeholder="请再次输入新密码"
          autocomplete="new-password"
          @keyup.enter="onSubmit"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="onSubmit">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi } from '@/api/user'
import { useUserStore } from '@/stores/user'

const visible = ref(false)
const loading = ref(false)
const formRef = ref()
const userStore = useUserStore()

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== form.newPassword) {
    callback(new Error('两次输入的新密码不一致'))
  } else {
    callback()
  }
}

const validateNewDiff = (rule, value, callback) => {
  if (value && value === form.oldPassword) {
    callback(new Error('新密码不能与原密码相同'))
  } else {
    callback()
  }
}

const rules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 32, message: '新密码长度为 6~32 位', trigger: 'blur' },
    { validator: validateNewDiff, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

// 暴露给父组件打开弹窗
function open() {
  visible.value = true
}

function onClosed() {
  formRef.value?.resetFields()
  form.oldPassword = ''
  form.newPassword = ''
  form.confirmPassword = ''
}

async function onSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    // 调用后端改密接口，由后端校验原密码（原密码错误时拦截器会弹出提示）
    await userApi.changePassword({
      id: userStore.userInfo.id,
      oldPassword: form.oldPassword,
      newPassword: form.newPassword
    })
    ElMessage.success('密码修改成功')
    visible.value = false
  } finally {
    loading.value = false
  }
}

defineExpose({ open })
</script>
