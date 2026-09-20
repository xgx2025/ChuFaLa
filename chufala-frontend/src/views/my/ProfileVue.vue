<template>
  <div class="profile-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>个人中心</h1>
      <p>管理您的账户信息和旅行偏好</p>
    </div>

    <!-- 个人信息概览卡片 -->
    <el-card class="profile-card" shadow="hover">
      <div class="profile-header">
        <div class="avatar-section">
          <el-avatar :size="100" class="user-avatar">
            <img :src="userInfo.avatar" alt="用户头像" />
          </el-avatar>
          <div class="user-status">
            <span class="online-dot"></span>
            <span class="status-text">在线</span>
          </div>
        </div>

        <div class="user-info">
          <div class="user-name-section">
            <h2>{{ userInfo.username }}</h2>
            <el-badge :value="userInfo.vip" type="primary" class="member-badge">
              {{ getMemberLevelText(userInfo.vip) }}
            </el-badge>
          </div>
          
          <div class="user-stats">
            <div class="stat-item">
              <span class="stat-value">{{ userInfo.points ? userInfo.points : '0' }}</span>
              <span class="stat-label">积分</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ userInfo.tripsCount ? userInfo.tripsCount : '0' }}</span>
              <span class="stat-label">旅程</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ userInfo.favoritesCount ? userInfo.favoritesCount : '0' }}</span>
              <span class="stat-label">收藏</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ userInfo.friendsCount ? userInfo.friendsCount : '0' }}</span>
              <span class="stat-label">旅伴</span>
            </div>
          </div>
        </div>

        <div class="action-buttons">
          <el-button type="primary" :icon="Edit" @click="handleEditProfile">
            编辑资料
          </el-button>
          <el-button type="warning" @click="router.push('/my/subscription')">
            <el-icon><Trophy /></el-icon> &nbsp{{ userInfo.vip === 1 ? '会员中心' : '升级会员' }}
          </el-button>
          <el-button type="success" @click="handleClaimRewards">
            <el-icon><Gift /></el-icon> &nbsp领取权益
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 个人详细信息卡片 -->
    <div class="info-cards-container">
      <el-card class="detail-info-card" shadow="hover">
        <div class="card-header">
          <h2>基本信息</h2>
        </div>
        
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">昵称</span>
            <span class="info-value">{{ userInfo.username }}</span>
          </div>
          
          <div class="info-item">
            <span class="info-label">性别</span>
            <span class="info-value">{{ userInfo.gender}}</span>
          </div>
          
          <div class="info-item">
            <span class="info-label">生日</span>
            <span class="info-value">{{ formatDate(userInfo.birthday) }}</span>
          </div>
          
          <div class="info-item">
            <span class="info-label">联系电话</span>
            <span class="info-value">{{ formatPhone(userInfo.phone) }}</span>
          </div>
          
          <div class="info-item">
            <span class="info-label">电子邮箱</span>
            <span class="info-value">{{ userInfo.email }}</span>
          </div>
          
          <div class="info-item">
            <span class="info-label">注册时间</span>
            <span class="info-value">{{ formatDate(userInfo.registerDate, 'YYYY-MM-DD HH:mm:ss') }}</span>
          </div>

          <div class="info-item">
            <span class="info-label">个人简介</span>
            <span class="info-value">{{ userInfo.bio }}</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 编辑资料弹窗 -->
    <el-dialog
      title="编辑个人资料"
      v-model="isEditDialogOpen"
      width="600px"
      :close-on-click-modal="false"
      :before-close="handleDialogClose"
    >
      <el-form
        :model="editForm"
        :rules="editFormRules"
        ref="editFormRef"
        label-width="100px"
        class="edit-profile-form"
      >
        <!-- 头像上传 -->
        <el-form-item label="用户头像" class="avatar-upload-item">
          <div class="avatar-upload-container">
            <el-avatar :size="100" class="preview-avatar">
              <img :src="editForm.avatar" alt="预览头像" />
            </el-avatar>
            <el-upload
              class="avatar-uploader"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleFileChange"
              :before-upload="beforeAvatarUpload"
              accept="image/jpeg,image/png,image/jpg"
            >
              <!-- 原为 type="text" icon="el-icon-plus"：
                   Element Plus 里 text 类型改用 text 属性，且 icon 接受的是组件而不是类名字符串 -->
              <el-button text :icon="Plus">更换头像</el-button>
            </el-upload>
          </div>
        </el-form-item>

        <!-- 昵称 -->
        <el-form-item label="昵称" prop="username">
          <el-input
            v-model="editForm.username"
            placeholder="请输入您的昵称"
            maxlength="16"
            show-word-limit
          />
        </el-form-item>

        <!-- 性别 -->
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="editForm.gender">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
            <el-radio label="保密">保密</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 生日 -->
        <el-form-item label="生日" prop="birthday">
          <el-date-picker
            v-model="editForm.birthday"
            type="date"
            placeholder="请选择生日"
            value-format="YYYY-MM-DD"
            :disabled-date="disabledFutureDate"
          />
        </el-form-item>

        <!-- 联系电话 -->
        <el-form-item label="联系电话" prop="phone">
          <el-input
            v-model="editForm.phone"
            placeholder="请输入11位手机号码"
            maxlength="11"
            type="tel"
            disabled=true
          />
        </el-form-item>

        <!-- 电子邮箱 -->
        <el-form-item label="电子邮箱" prop="email">
          <el-input
            v-model="editForm.email"
            placeholder="请输入您的电子邮箱"
            type="email"
            maxlength="50"
            disabled=true
            show-word-limit
          />
        </el-form-item>

        <!-- 个人简介 -->
        <el-form-item label="个人简介" prop="bio">
          <el-input
            v-model="editForm.bio"
            placeholder="请简要介绍自己"
            type="textarea"
            :rows="3"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="handleDialogClose">取消</el-button>
        <el-button type="primary" @click="handleFormSubmit">保存修改</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref,reactive } from 'vue';
import { useRouter } from 'vue-router';
import { getUserInfoService,updateUserInfoService,updateUserAvatarService } from '@/api/user';
import { ElMessage } from 'element-plus';
import {Edit, Trophy, Plus} from '@element-plus/icons-vue';
import { Gift } from '@/components/Icon.vue';
import dayjs from 'dayjs';

const router = useRouter();

/**
 * 2. 使用 dayjs 实现日期格式化函数
 * @param {string|Date} date - 需要格式化的日期
 * @param {string} format - dayjs 格式字符串，默认 'YYYY-MM-DD'
 * @returns {string} 格式化后的日期字符串
 */
const formatDate = (date, format = 'YYYY-MM-DD') => {
  if (!date) return '';
  // dayjs 默认能处理 ISO 格式字符串 (如 '2018-03-22T10:30:00') 和 'YYYY-MM-DD' 格式
  return dayjs(date).format(format);
};


// 用户数据
const userInfo = ref({});

// 获取会员等级文本
const getMemberLevelText = (level) => {
  // const levels = ['普通会员', '银卡会员', '金卡会员', '白金会员', '钻石会员'];
  const levels = ['普通会员', '高级会员']
  return levels[level];
};

// 格式化电话号码
const formatPhone = (phone) => {
  if (!phone) return '';
  return phone.replace(/(\d{3})(\d{4})(\d{4})/, '$1-$2-$3');
};

// 处理领取权益
const handleClaimRewards = () => {
  ElMessage.success('成功领取会员权益！');
};

const getUserInfo = async() => { 
  const result = await getUserInfoService();
  userInfo.value = result.data;
  console.log('hhhuserInfo', userInfo.value);
};
getUserInfo();


// 编辑资料弹窗相关
const isEditDialogOpen = ref(false);
const editFormRef = ref(null);

// 编辑表单数据（默认值与userInfo对齐）
const editForm = reactive({
  avatar: '',
  username: '',
  gender: '保密',
  birthday: '',
  phone: '',
  email: '',
  bio: ''
});

// 表单验证规则
const editFormRules = reactive({
  username: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 16, message: '昵称长度在 2-16 个字符之间', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  // phone: [
  //   { required: true, message: '请输入联系电话', trigger: 'blur' },
  //   { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的11位手机号码', trigger: 'blur' }
  // ],
  email: [
    { required: true, message: '请输入电子邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的电子邮箱格式', trigger: 'blur' }
  ],
  bio: [
    { max: 100, message: '个人简介不能超过100个字符', trigger: 'blur' }
  ]
});

// 打开编辑弹窗（回显数据）
const handleEditProfile = () => {
  // 深拷贝用户信息到编辑表单
  editForm.avatar = userInfo.value.avatar;
  editForm.username = userInfo.value.username || '';
  editForm.gender = userInfo.value.gender || '保密';
  editForm.birthday = userInfo.value.birthday ? formatDate(userInfo.value.birthday) : '';
  editForm.phone = userInfo.value.phone || '';
  editForm.email = userInfo.value.email || '';
  editForm.bio = userInfo.value.bio || '';

  // 打开弹窗
  isEditDialogOpen.value = true;
};

// 关闭弹窗
const handleDialogClose = () => {
  // 重置表单
  editFormRef.value?.resetFields();
  isEditDialogOpen.value = false;
};

// 头像上传前校验
const beforeAvatarUpload = (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/jpg';
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isImage) {
    ElMessage.error('请上传 JPG/PNG 格式的图片');
    return false;
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB');
    return false;
  }
  return true;
};

// 头像上传
const handleFileChange = async(uploadFile) => {
  if(uploadFile.status !== "ready") return;
  const formData = new FormData();
  formData.append("file",uploadFile.raw) 
  try{
    const result = await updateUserAvatarService(formData)
    editForm.avatar = result.data;
    ElMessage.success('头像上传成功');
  }catch(error){
    ElMessage.error("头像上传失败");
  }
 

};

// 禁止选择未来日期（生日）
const disabledFutureDate = (time) => {
  return time.getTime() > Date.now();
};

// 提交表单
const handleFormSubmit = async () => {
  try {
    // 表单校验
    await editFormRef.value.validate();
    
    // 提交修改请求
    const result = await updateUserInfoService(editForm);
      ElMessage.success('资料修改成功');
      
      // 更新用户信息（同步到页面）
      userInfo.value = { ...userInfo.value, ...editForm };
      
      // 关闭弹窗
      handleDialogClose();
  } catch (error) {
    // 表单校验失败不做处理（Element Plus已提示）
    console.error('表单校验失败：', error);
  }
};

</script>

<style scoped>
/* 主容器样式 */
.profile-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

/* 页面标题 */
.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: var(--fs-h2);
  font-weight: 600;
  margin-bottom: 8px;
}

.page-header p {
  font-size: var(--fs-body);
  color: var(--c-ink-3);
}

/* 个人信息卡片 */
.profile-card {
  margin-bottom: 24px;
}

.profile-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
}

.avatar-section {
  position: relative;
}

.user-avatar {
  border: 4px solid var(--c-line);
  transition: border-color 0.3s ease, box-shadow 0.3s ease, transform 0.3s ease;
}

.user-avatar:hover {
  transform: scale(1.05);
  border-color: var(--c-primary-600);
}

.user-status {
  position: absolute;
  bottom: 0;
  right: 0;
  display: flex;
  align-items: center;
  background: white;
  border-radius: 12px;
  padding: 2px 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.online-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: var(--c-success);
  margin-right: 4px;
}

.status-text {
  font-size: var(--fs-caption);
  color: var(--c-ink);
}

.user-info {
  flex: 1;
  margin: 0 40px;
}

.user-name-section {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.user-name-section h2 {
  font-size: var(--fs-h3);
  font-weight: 600;
  margin-right: 12px;
}

.member-badge {
  height: 24px;
  line-height: 24px;
  padding: 0 8px;
  border-radius: 12px;
}

.user-stats {
  display: flex;
  gap: 30px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: var(--fs-body-lg);
  font-weight: 600;
  color: var(--c-ink);
}

.stat-label {
  font-size: var(--fs-caption);
  color: var(--c-ink-3);
}

.action-buttons {
  display: flex;
  gap: 12px;
}

/* 安全状态卡片 */
.security-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header h2 {
  font-size: var(--fs-body-lg);
  font-weight: 600;
}

.security-progress {
  width: 200px;
}

.security-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.security-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid var(--c-line);
}

.security-item:last-child {
  border-bottom: none;
}

.security-icon {
  width: 40px;
  height: 40px;
  background-color: var(--c-bg-sub);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.security-icon i {
  font-size: var(--fs-body-lg);
  color: var(--c-primary-600);
}

.security-info {
  flex: 1;
}

.security-title {
  font-size: var(--fs-body);
  color: var(--c-ink);
  margin-right: 8px;
}

.security-status {
  font-size: var(--fs-caption);
}

.status-verified {
  color: var(--c-success);
}

.status-unverified {
  color: var(--c-danger);
}

/* 信息卡片容器 */
.info-cards-container {
  display: flex;
  gap: 24px;
  margin-bottom: 24px;
}

.detail-info-card, .preferences-card {
  flex: 1;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
}

.info-label {
  font-size: var(--fs-caption);
  color: var(--c-ink-4);
  margin-bottom: 4px;
}

.info-value {
  font-size: var(--fs-body);
  color: var(--c-ink);
}

.preferences-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.preference-item {
  display: flex;
  flex-direction: column;
}

.preference-label {
  font-size: var(--fs-caption);
  color: var(--c-ink-4);
  margin-bottom: 8px;
}

.preference-value {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.travel-type-tag {
  background-color: var(--c-primary-50);
  color: var(--c-primary-600);
}

.transport-tag {
  background-color: var(--c-success-soft);
  color: var(--c-success);
}

.hotel-tag {
  background-color: var(--c-accent-soft);
  color: var(--c-accent);
}

.food-tag {
  background-color: var(--c-danger-soft);
  color: var(--c-danger);
}

/* 地址卡片 */
.addresses-card {
  margin-bottom: 24px;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.address-item {
  padding: 16px;
  border: 1px solid var(--c-line);
  border-radius: 4px;
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
}

.address-item:hover {
  border-color: var(--c-primary-600);
  background-color: var(--c-primary-50);
}

.address-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.address-type {
  font-size: var(--fs-body);
  font-weight: 600;
  color: var(--c-ink);
}

.address-actions {
  display: flex;
  gap: 8px;
}

.address-detail {
  margin-bottom: 12px;
}

.address-name {
  font-size: var(--fs-body);
  color: var(--c-ink);
  margin-bottom: 4px;
}

.address-full {
  font-size: var(--fs-caption);
  color: var(--c-ink-3);
  line-height: 1.5;
}

.address-footer {
  display: flex;
  justify-content: flex-end;
}

/* 统计卡片容器 */
.stats-container {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
}

.stat-card {
  flex: 1;
  min-width: 250px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.stat-card-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.stat-icon {
  font-size: var(--fs-h2);
  color: var(--c-primary-600);
  margin-right: 12px;
}

.stat-card-header h3 {
  font-size: var(--fs-body-lg);
  font-weight: 600;
}

.stat-card-body {
  text-align: center;
  margin-bottom: 16px;
}

.stat-number {
  font-size: var(--fs-h1);
  font-weight: 700;
  color: var(--c-ink);
  line-height: 1;
}

.stat-label {
  font-size: var(--fs-body);
  color: var(--c-ink-3);
  margin-top: 8px;
}

.stat-card-footer {
  text-align: center;
  font-size: var(--fs-caption);
  color: var(--c-ink-4);
}

/* 响应式样式 */
@media (max-width: 768px) {
  .profile-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .user-info {
    margin: 16px 0;
    width: 100%;
  }
  
  .action-buttons {
    width: 100%;
    justify-content: space-between;
  }
  
  .info-cards-container {
    flex-direction: column;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .stats-container {
    flex-direction: column;
  }
  
  .stat-card {
    width: 100%;
  }
}

/* 编辑资料弹窗样式 */
.edit-profile-form {
  padding: 10px 0;
}

.avatar-upload-container {
  display: flex;
  align-items: center;
  gap: 20px;
}

.preview-avatar {
  border: 2px solid var(--c-line);
  /* 父级 .avatar-upload-container 是 flex 容器，避免头像被压缩变形 */
  flex-shrink: 0;
}

.avatar-uploader {
  margin-top: 10px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .el-dialog {
    width: 90% !important;
  }
  
  .avatar-upload-container {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .avatar-tip {
    margin-left: 0;
  }
}
</style>