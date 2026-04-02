<template>
  <div class="banners-container">
    <el-card class="header-card">
      <h2>轮播图管理</h2>
      <p>管理飞手端和农户端的轮播图内容</p>
    </el-card>

    <el-card class="content-card">
      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button type="primary" @click="showAddDialog">
          <el-icon><Plus /></el-icon>
          添加轮播图
        </el-button>
      </div>

      <!-- 搜索和筛选 -->
      <div class="search-filter">
        <el-input
          v-model="searchQuery"
          placeholder="搜索标题"
          prefix-icon="Search"
          style="width: 250px; margin-right: 10px"
        />
        <el-select v-model="filterType" placeholder="选择类型" style="width: 150px; margin-right: 10px">
          <el-option label="全部" value="" />
          <el-option label="农户端" value="farmer" />
          <el-option label="飞手端" value="flyer" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="选择状态" style="width: 120px">
          <el-option label="全部" value="" />
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </div>

      <!-- 轮播图列表 -->
      <el-table :data="filteredBanners" style="margin-top: 20px" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" width="200" />
        <el-table-column label="图片" width="150">
          <template #default="scope">
            <el-image
              :src="scope.row.imageUrl"
              fit="cover"
              style="width: 100px; height: 60px"
              :preview-src-list="[scope.row.imageUrl]"
            />
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.type === 'farmer' ? 'primary' : 'success'">
              {{ scope.row.type === 'farmer' ? '农户端' : '飞手端' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="showEditDialog(scope.row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination" v-if="banners.length > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="banners.length"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑轮播图' : '添加轮播图'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="formData"
        label-width="80px"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="图片" prop="imageUrl">
          <div class="upload-container">
            <!-- 图片预览区域 -->
            <div v-if="formData.imageUrl" class="image-preview">
              <el-image
                :src="formData.imageUrl"
                fit="cover"
                style="width: 200px; height: 120px; margin-bottom: 10px"
              />
            </div>
            <el-upload
              class="avatar-uploader"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleFileChange"
            >
              <el-button type="primary">选择本地图片</el-button>
            </el-upload>
            <div class="form-tip">提示：图片将在点击确定按钮时上传</div>
          </div>
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="formData.type">
            <el-radio label="farmer">农户端</el-radio>
            <el-radio label="flyer">飞手端</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="formData.status"
            :active-value="1"
            :inactive-value="0"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import axios from '../utils/axios';
import { Plus } from '@element-plus/icons-vue';

export default {
  components: {
    Plus
  },
  data() {
        return {
          banners: [],
          searchQuery: '',
          filterType: '',
          filterStatus: '',
          currentPage: 1,
          pageSize: 10,
          dialogVisible: false,
          isEdit: false,
          tempFile: null, // 临时存储选择的文件
          formData: {
            id: null,
            title: '',
            imageUrl: '',
            type: 'farmer',
            sort: 0,
            status: 1
          }
        };
  },
  computed: {
    filteredBanners() {
      let result = [...this.banners];
      
      // 搜索筛选
      if (this.searchQuery) {
        result = result.filter(banner => 
          banner.title.toLowerCase().includes(this.searchQuery.toLowerCase())
        );
      }
      
      // 类型筛选
      if (this.filterType) {
        result = result.filter(banner => banner.type === this.filterType);
      }
      
      // 状态筛选
      if (this.filterStatus !== '') {
        result = result.filter(banner => banner.status === Number(this.filterStatus));
      }
      
      // 排序
      result.sort((a, b) => {
        if (a.sort !== b.sort) {
          return a.sort - b.sort;
        }
        return b.createTime.localeCompare(a.createTime);
      });
      
      // 分页
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return result.slice(start, end);
    }
  },
  mounted() {
    this.loadBanners();
  },
  methods: {
    // 处理文件选择，创建临时预览
    handleFileChange(file) {
      // 文件类型和大小校验
      const isJPG = file.raw.type === 'image/jpeg' || file.raw.type === 'image/jpg';
      const isPNG = file.raw.type === 'image/png';
      const isGIF = file.raw.type === 'image/gif';
      const isWEBP = file.raw.type === 'image/webp';
      const isLt2M = file.raw.size / 1024 / 1024 < 2;

      if (!isJPG && !isPNG && !isGIF && !isWEBP) {
        this.$message.error('上传图片只能是 JPG、PNG、GIF 或 WEBP 格式!');
        return;
      }
      if (!isLt2M) {
        this.$message.error('上传图片大小不能超过 2MB!');
        return;
      }

      // 保存临时文件
      this.tempFile = file.raw;
      
      // 创建本地预览URL
      const reader = new FileReader();
      reader.onload = (e) => {
        this.formData.imageUrl = e.target.result;
      };
      reader.readAsDataURL(file.raw);
      
      this.$message.success('图片已选择，将在点击确定时上传');
    },
    
    // 加载轮播图列表
    async loadBanners() {
      try {
        const response = await axios.get('/admin/banners');
        if (response.code === 200 || response.code === 0) {
          this.banners = response.data || [];
        } else {
          this.$message.error('获取轮播图列表失败');
        }
      } catch (error) {
        console.error('获取轮播图失败:', error);
        this.$message.error('获取轮播图列表失败');
      }
    },
    
    // 显示添加对话框
    showAddDialog() {
      this.isEdit = false;
      this.tempFile = null; // 重置临时文件
      this.formData = {
        id: null,
        title: '',
        imageUrl: '',
        type: 'farmer',
        sort: 0,
        status: 1
      };
      this.dialogVisible = true;
    },
    
    // 显示编辑对话框
    showEditDialog(row) {
      this.isEdit = true;
      this.tempFile = null; // 重置临时文件
      // 确保只传递需要的字段，不包含targetUrl
      this.formData = {
        id: row.id,
        title: row.title,
        imageUrl: row.imageUrl,
        type: row.type,
        sort: row.sort,
        status: row.status
      };
      this.dialogVisible = true;
    },
    
    // 提交表单
    async handleSubmit() {
      this.$refs.formRef.validate(async (valid) => {
        if (valid) {
          try {
            // 如果有临时文件，先上传图片
            if (this.tempFile) {
              // 创建FormData对象
              const formData = new FormData();
              formData.append('file', this.tempFile);

              // 上传文件到服务器
              const uploadResponse = await axios.post('/admin/banners/upload', formData, {
                headers: {
                  'Content-Type': 'multipart/form-data'
                }
              });

              // 检查响应状态，支持从message字段获取图片URL（如果data为空）
              if (uploadResponse && (uploadResponse.code === 200 || uploadResponse.code === 0)) {
                // 优先从data字段获取，如果data为空则从message字段获取
                this.formData.imageUrl = uploadResponse.data || uploadResponse.message;
                console.log('图片上传成功，URL:', this.formData.imageUrl);
              } else {
                console.error('图片上传响应异常:', uploadResponse);
                this.$message.error('图片上传失败: ' + (uploadResponse.message || uploadResponse.msg || '未知错误'));
                return;
              }
            }
            
            // 保存轮播图信息
            let response;
            if (this.isEdit) {
              response = await axios.put(`/admin/banners/${this.formData.id}`, this.formData);
            } else {
              response = await axios.post('/admin/banners', this.formData);
            }
            
            if (response.code === 200 || response.code === 0) {
              this.$message.success(this.isEdit ? '更新成功' : '添加成功');
              this.dialogVisible = false;
              this.loadBanners();
            } else {
              console.error('保存轮播图失败:', response);
              this.$message.error(this.isEdit ? '更新失败: ' + (response.msg || response.message || '') : '添加失败: ' + (response.msg || response.message || ''));
            }
          } catch (error) {
            console.error('保存轮播图异常:', error);
            this.$message.error(this.isEdit ? '更新失败' : '添加失败');
          }
        }
      });
    },
    
    // 删除轮播图
    handleDelete(id) {
      this.$confirm('确定要删除这个轮播图吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await axios.delete(`/admin/banners/${id}`);
          if (response.code === 200 || response.code === 0) {
            this.$message.success('删除成功');
            this.loadBanners();
          } else {
            this.$message.error('删除失败');
          }
        } catch (error) {
          console.error('删除轮播图失败:', error);
          this.$message.error('删除失败');
        }
      }).catch(() => {
        // 取消删除
      });
    },
    
    // 修改状态
    async handleStatusChange(row) {
      try {
        const response = await axios.put(`/admin/banners/${row.id}`, row);
        if (response.code !== 200 && response.code !== 0) {
          this.$message.error('更新状态失败');
          // 恢复原状态
          row.status = row.status === 1 ? 0 : 1;
        }
      } catch (error) {
        console.error('更新状态失败:', error);
        this.$message.error('更新状态失败');
        // 恢复原状态
        row.status = row.status === 1 ? 0 : 1;
      }
    },
    
    // 分页处理
    handleSizeChange(size) {
      this.pageSize = size;
      this.currentPage = 1;
    },
    
    handleCurrentChange(current) {
      this.currentPage = current;
    }
  }
};
</script>

<style scoped>
.banners-container {
  padding: 0 20px;
}

.header-card {
  margin-bottom: 20px;
}

.content-card {
  padding: 20px;
}

.action-buttons {
  margin-bottom: 20px;
}

.search-filter {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.dialog-footer {
  text-align: right;
}
  .form-tip {
    color: #909399;
    font-size: 12px;
    margin-top: 5px;
  }
  
  .upload-container {
    margin-bottom: 10px;
  }
  
  .image-preview {
    margin-bottom: 10px;
  }
</style>