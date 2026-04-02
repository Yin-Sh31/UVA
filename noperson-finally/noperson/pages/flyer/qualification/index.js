// flyer/qualification/index.js
const { api } = require('../../../utils/request');

Page({
  data: {
    qualificationInfo: null,
    uploadStatus: {
      license: false
    },
    licenseFilePath: '',
    licenseFileName: '',
    qualificationStatus: 0, // 0: 未提交, 1: 审核中, 2: 已通过, 3: 已拒绝
    statusText: '',
    auditRemark: '',
    licenseType: '',
    licenseNo: ''
  },
  
  onLoad: function() {
    this.loadQualificationInfo();
  },
  
  onShow: function() {
    this.loadQualificationInfo();
  },
  
  // 加载资质信息
  loadQualificationInfo: async function() {
    wx.showLoading({
      title: '加载中...',
    });
    
    try {
      // 调用API获取飞手信息，包含资质信息
      const result = await api.getFlyerInfo();
      console.log('获取飞手信息成功:', result);
      
      const flyerInfo = result.data || {};
      
      // 设置资质状态文本
      let statusText = '';
      const status = flyerInfo.auditStatus || 0;
      switch(status) {
        case 0:
          statusText = '待审核';
          break;
        case 1:
          statusText = '已通过';
          break;
        case 2:
          statusText = '已拒绝';
          break;
        default:
          statusText = '未知';
      }
      
      this.setData({
        qualificationInfo: flyerInfo,
        qualificationStatus: status,
        statusText: statusText,
        auditRemark: flyerInfo.auditRemark || '',
        licenseType: flyerInfo.licenseType || '',
        licenseNo: flyerInfo.licenseNo || '',
        licenseFileUrl: flyerInfo.licenseUrl || ''
      });
    } catch (error) {
      console.error('加载资质信息失败', error);
      wx.hideLoading();
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      });
    } finally {
      wx.hideLoading();
    }
  },
  
  // 选择执照文件
  chooseLicenseImage: function() {
    this.chooseFile('license');
  },
  
  // 选择文件通用方法（支持图片和PDF）
  chooseFile: function(type) {
    wx.chooseMessageFile({
      count: 1,
      type: 'file',
      extension: ['jpg', 'jpeg', 'png', 'pdf'],
      success: (res) => {
        const tempFilePath = res.tempFiles[0].path;
        const fileName = res.tempFiles[0].name;
        const fileSize = res.tempFiles[0].size;
        
        // 检查文件大小（5MB限制）
        if (fileSize > 5 * 1024 * 1024) {
          wx.showToast({
            title: '文件大小不能超过5MB',
            icon: 'none'
          });
          return;
        }
        
        // 检查文件类型
        const fileExt = fileName.split('.').pop().toLowerCase();
        if (!['jpg', 'jpeg', 'png', 'pdf'].includes(fileExt)) {
          wx.showToast({
            title: '仅支持JPG、PNG、PDF格式',
            icon: 'none'
          });
          return;
        }
        
        // 只处理执照文件
        this.setData({
          licenseFilePath: tempFilePath,
          licenseFileName: fileName,
          uploadStatus: {
            license: true
          }
        });
      },
      fail: (err) => {
        console.error('选择文件失败', err);
        wx.showToast({
          title: '选择文件失败',
          icon: 'none'
        });
      }
    });
  },
  
  // 执照类型输入处理
  onLicenseTypeInput: function(e) {
    this.setData({
      licenseType: e.detail.value
    });
  },
    
    // 执照编号输入处理
  onLicenseNoInput: function(e) {
    this.setData({
      licenseNo: e.detail.value
    });
  },
  
  // 保险相关功能已移除
  
  // 上传资质
  uploadQualification: async function() {
    // 表单验证 - 只验证执照相关信息
    if (!this.data.licenseType.trim()) {
      wx.showToast({
        title: '请选择执照类型',
        icon: 'none'
      });
      return;
    }
    
    if (!this.data.licenseNo.trim()) {
      wx.showToast({
        title: '请填写执照编号',
        icon: 'none'
      });
      return;
    }
    
    if (!this.data.uploadStatus.license) {
      wx.showToast({
        title: '请上传执照文件',
        icon: 'none'
      });
      return;
    }
    
    // 验证文件格式（虽然在选择时已经验证，但再次确认）
    const validateFileFormat = (filename) => {
      const validFormats = ['jpg', 'jpeg', 'png', 'pdf'];
      const extension = filename.split('.').pop().toLowerCase();
      return validFormats.includes(extension);
    };
    
    if (!validateFileFormat(this.data.licenseFileName)) {
      wx.showToast({
        title: '执照文件格式不支持，请使用JPG、PNG或PDF',
        icon: 'none'
      });
      return;
    }
    
    // 保险文件已移除，不需要验证
    
    // 提示用户当前上传方式的限制
    wx.showLoading({
      title: '正在上传资质文件...',
    });
    
    // 记录日志，便于调试
    console.log('开始上传资质，表单数据完整，文件已选择');
    
    try {
        // 上传飞手执照文件作为licenseFile参数，确保正确存储到数据库中的licenseFile字段
        
        console.log('开始上传资质文件，使用执照文件作为主上传文件');
        
        // 获取token
        const token = wx.getStorageSync('token');
        const cleanToken = token ? token.trim() : '';
        
        // 根据需求，只需要上传飞手执照文件
        // 1. 上传licenseFile（必需）作为MultipartFile
        // 2. 数据参数以JSON格式提交
        
        // 构建JSON数据参数（只包含执照相关信息）
        const jsonData = {
          licenseType: this.data.licenseType,
          licenseNo: this.data.licenseNo
        };
        
        console.log('上传资质信息:', jsonData);
        
        // 准备formData，只包含JSON数据
        const formData = {
          jsonData: JSON.stringify(jsonData)
        };
        
        // 微信小程序的wx.uploadFile上传执照文件
        const uploadResult = await new Promise((resolve, reject) => {
          wx.uploadFile({
            url: 'http://localhost:8082/flyer/upload-qualification',
            filePath: this.data.licenseFilePath,  // 上传执照文件作为licenseFile参数
            name: 'licenseFile',                  // 字段名必须为licenseFile，确保执照文件正确存储到数据库中
            formData: formData,
            header: {
              'Authorization': `Bearer ${cleanToken}`
            },
            success: (res) => {
              console.log('上传响应:', res);
              console.log('响应状态码:', res.statusCode);
              console.log('原始响应数据:', res.data);
              
              try {
                let data;
                try {
                  data = JSON.parse(res.data);
                  console.log('解析后的数据:', data);
                } catch (parseError) {
                  console.error('JSON解析失败:', parseError);
                  reject(new Error(`服务器返回格式异常: ${res.data.substring(0, 100)}...`));
                  return;
                }
                
                resolve({ statusCode: res.statusCode, ...data });
              } catch (error) {
                console.error('处理响应时发生错误:', error);
                reject(error);
              }
            },
            fail: (err) => {
              console.error('上传请求失败:', err);
              reject(new Error(`网络请求失败: ${err.errMsg || '未知错误'}`));
            }
          });
        });
        
        // 检查上传结果
        if (uploadResult.statusCode !== 200 || uploadResult.code !== 200) {
          throw new Error(uploadResult.msg || uploadResult.message || '上传失败');
        }
        
        // 上传成功
        wx.hideLoading();
        wx.showToast({
          title: '资质上传成功，等待平台审核',
          icon: 'success'
        });
        
        // 更新状态为审核中
        this.setData({
          qualificationStatus: 1,
          statusText: '审核中'
        });
        
        // 重置表单 - 只重置执照相关字段
        this.setData({
          licenseType: '',
          licenseNo: '',
          licenseFilePath: '',
          licenseFileName: '',
          uploadStatus: {
            license: false
          }
        });
    } catch (error) {
        console.error('上传资质失败', error);
        console.error('错误详情:', error.stack);
        wx.hideLoading();
        
        // 根据错误类型提供更具体的错误提示
        let errorMessage = '上传失败，请重试';
        
        if (error.message) {
          if (error.message.includes('解析失败')) {
            errorMessage = '服务器返回数据格式错误，请稍后重试';
          } else if (error.message.includes('网络请求失败')) {
            errorMessage = '网络连接失败，请检查网络设置';
          } else if (error.message.includes('文件')) {
            errorMessage = '文件处理失败，请检查文件是否完好';
          } else {
            // 限制错误信息长度，避免显示过长内容
            errorMessage = error.message.length > 20 ? error.message.substring(0, 20) + '...' : error.message;
          }
        }
        
        wx.showToast({
          title: errorMessage,
          icon: 'none',
          duration: 3000
        });
      }
  },
  
  // 预览文件
  previewImage: function(e) {
    // 获取文件路径，优先使用已上传的文件URL
    let filePath = this.data.licenseFilePath;
    
    // 如果没有本地文件路径，使用服务器返回的文件URL
    if (!filePath && this.data.licenseFileUrl) {
      filePath = this.data.licenseFileUrl;
    }
    
    if (filePath) {
      // 判断文件类型
      const fileExt = filePath.split('.').pop().toLowerCase();
      const imageExts = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'];
      
      if (imageExts.includes(fileExt)) {
        // 图片文件，直接预览
        wx.previewImage({
          urls: [filePath],
          current: filePath,
          fail: (err) => {
            console.error('预览图片失败', err);
            wx.showToast({
              title: '无法预览图片',
              icon: 'none'
            });
          }
        });
      } else {
        // 非图片文件，下载后预览
        wx.showLoading({
          title: '加载文件中...',
        });
        
        wx.downloadFile({
          url: filePath,
          success: (res) => {
            wx.hideLoading();
            if (res.statusCode === 200) {
              wx.openDocument({
                filePath: res.tempFilePath,
                showMenu: true,
                fail: (err) => {
                  console.error('打开文件失败', err);
                  wx.showToast({
                    title: '无法预览该文件',
                    icon: 'none'
                  });
                }
              });
            } else {
              wx.showToast({
                title: '文件下载失败',
                icon: 'none'
              });
            }
          },
          fail: (err) => {
            wx.hideLoading();
            console.error('下载文件失败', err);
            wx.showToast({
              title: '文件下载失败',
              icon: 'none'
            });
          }
        });
      }
    }
  },
  
  // 重新上传资质
  reuploadQualification: function() {
    // 重置表单状态
    this.setData({
      licenseType: '',
      licenseNo: '',
      licenseFilePath: '',
      licenseFileName: '',
      uploadStatus: {
        license: false
      },
      qualificationStatus: 0, // 重置为待审核状态
      statusText: '待审核'
    });
  }
})