<template>
  <el-container class="layout">
    <!-- 侧边栏 -->
    <el-aside :width="collapse ? '64px' : '210px'" class="aside">
      <div class="logo">
        <el-icon><Tools /></el-icon>
        <span v-show="!collapse" class="logo-text">摩配采购系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="collapse"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item v-for="m in visibleMenus" :key="m.path" :index="'/' + m.path">
          <el-icon><component :is="m.meta.icon" /></el-icon>
          <template #title>{{ m.meta.title }}</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶栏 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="collapse = !collapse">
            <Fold v-if="!collapse" />
            <Expand v-else />
          </el-icon>
          <span class="page-title">{{ currentTitle }}</span>
        </div>
        <div class="header-right">
          <el-dropdown @command="onCommand">
            <span class="user-info">
              <el-icon><UserFilled /></el-icon>
              {{ userStore.userInfo.realName || userStore.userInfo.username }}
              <el-tag size="small" type="info" effect="plain" style="margin-left: 6px">
                {{ userStore.roleText }}
              </el-tag>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>
                  部门：{{ userStore.userInfo.department || '-' }}
                </el-dropdown-item>
                <el-dropdown-item divided command="changePassword">修改密码</el-dropdown-item>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 修改密码弹窗 -->
      <ChangePasswordDialog ref="pwdDialogRef" />

      <!-- 内容区 -->
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <keep-alive :max="10">
            <component :is="Component" />
          </keep-alive>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { businessRoutes } from '@/router'
import ChangePasswordDialog from '@/components/ChangePasswordDialog.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const collapse = ref(false)
const pwdDialogRef = ref()

// 仅渲染：非隐藏 且 当前角色有权访问 的菜单
const visibleMenus = computed(() =>
  businessRoutes.filter(
    (m) => !m.meta.hidden && userStore.hasRole(m.meta.roles)
  )
)

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta?.title || '摩托车零部件采购管理系统')

function onCommand(cmd) {
  if (cmd === 'changePassword') {
    pwdDialogRef.value?.open()
    return
  }
  if (cmd === 'logout') {
    ElMessageBox.confirm('确认退出登录？', '提示', { type: 'warning' })
      .then(async () => {
        await userStore.logout(true)
        router.push('/login')
      })
      .catch(() => {})
  }
}
</script>

<style scoped>
.layout {
  height: 100%;
}
.aside {
  background-color: #304156;
  transition: width 0.2s;
  overflow-x: hidden;
}
.logo {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background-color: #2b3a4d;
}
.logo-text {
  white-space: nowrap;
}
.aside .el-menu {
  border-right: none;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.collapse-btn {
  font-size: 20px;
  cursor: pointer;
}
.page-title {
  font-size: 16px;
  font-weight: 600;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  outline: none;
}
.main {
  background-color: #f0f2f5;
  padding: 0;
}
</style>
