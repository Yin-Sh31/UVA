// view-report.js
const { api } = require('../../utils/request');

Page({
  data: {
    reportId: '',
    report: null,
    loading: false,
    error: null,
    isCachedData: false,
    reportStatus: {
      0: '待处理',
      1: '处理中',
      2: '已完成',
      3: '已取消'
    },
    lastUpdatedTime: null
  },
  
  onLoad: function(options) {
    // 如果从URL参数中获取reportId
    if (options && options.reportId) {
      this.setData({
        reportId: options.reportId
      });
      // 先尝试加载缓存数据
      this.loadCachedData(options.reportId);
      // 然后加载最新数据
      this.loadReportDetail(options.reportId);
    }
  },
  
  // 加载缓存数据
  loadCachedData: function(reportId) {
    try {
      const cachedData = wx.getStorageSync(`report_${reportId}`);
      if (cachedData) {
        this.setData({
          report: cachedData.report,
          lastUpdatedTime: cachedData.timestamp,
          isCachedData: true
        });
      }
    } catch (error) {
      console.error('加载缓存数据失败', error);
    }
  },
  
  // 保存到缓存
  saveToCache: function(reportId, report) {
    try {
      wx.setStorageSync(`report_${reportId}`, {
        report: report,
        timestamp: new Date().getTime()
      });
    } catch (error) {
      console.error('保存缓存数据失败', error);
    }
  },
  
  // 输入报告ID
  onReportIdInput: function(e) {
    this.setData({
      reportId: e.detail.value
    });
  },
  
  // 加载报告详情
  loadReportDetail: async function(reportId) {
    if (!reportId) {
      wx.showToast({
        title: '请输入报告ID',
        icon: 'none'
      });
      return;
    }
    
    this.setData({
      loading: true,
      error: null
    });
    
    try {
      // 添加超时处理
      const timeoutPromise = new Promise((_, reject) => {
        setTimeout(() => reject(new Error('网络请求超时')), 10000);
      });
      
      const reportPromise = api.getInspectionReport(reportId);
      const report = await Promise.race([reportPromise, timeoutPromise]);
      
      this.setData({
        report: report,
        isCachedData: false,
        lastUpdatedTime: new Date().getTime()
      });
      
      // 保存到缓存
      this.saveToCache(reportId, report);
      
      // 如果是从缓存显示切换到实时数据，给用户提示
      if (this.data.isCachedData !== undefined && this.data.isCachedData) {
        wx.showToast({
          title: '已更新至最新数据',
          icon: 'none',
          duration: 1500
        });
      }
    } catch (error) {
      console.error('加载报告失败', error);
      this.setData({
        error: error.message || '加载失败，请稍后重试'
      });
      
      // 如果没有缓存数据且加载失败，显示错误信息
      if (!this.data.report) {
        wx.showToast({
          title: this.data.error,
          icon: 'none'
        });
      }
    } finally {
      this.setData({
        loading: false
      });
    }
  },
  
  // 查询报告
  queryReport: function() {
    // 清除之前的错误状态
    this.setData({
      error: null
    });
    this.loadReportDetail(this.data.reportId);
  },
  
  // 刷新页面
  refreshPage: function() {
    if (this.data.reportId) {
      this.loadReportDetail(this.data.reportId);
    }
  },
  
  // 查看大图
  viewImage: function(e) {
    const currentImage = e.currentTarget.dataset.src;
    const imageList = this.data.report.imageUrls || [];
    
    // 添加图片加载提示
    wx.showLoading({
      title: '加载中...',
      mask: true
    });
    
    // 预加载第一张图片
    wx.getImageInfo({
      src: currentImage,
      success: () => {
        wx.hideLoading();
        wx.previewImage({
          current: currentImage,
          urls: imageList,
          success: () => {
            // 预加载其他图片
            imageList.forEach((url, index) => {
              if (url !== currentImage) {
                wx.getImageInfo({ src: url });
              }
            });
          },
          fail: (err) => {
            wx.hideLoading();
            wx.showToast({
              title: '图片预览失败',
              icon: 'none'
            });
            console.error('图片预览失败', err);
          }
        });
      },
      fail: () => {
        wx.hideLoading();
        wx.showToast({
          title: '图片加载失败',
          icon: 'none'
        });
      }
    });
  },
  
  // 下载报告
  downloadReport: function() {
    if (!this.data.report || !this.data.report.reportUrl) {
      wx.showToast({
        title: '暂无报告下载链接',
        icon: 'none'
      });
      return;
    }
    
    wx.showLoading({
      title: '准备下载...',
      mask: true
    });
    
    // 检查网络状态
    wx.getNetworkType({
      success: (res) => {
        if (res.networkType === 'none') {
          wx.hideLoading();
          wx.showToast({
            title: '当前无网络连接',
            icon: 'none'
          });
          return;
        }
        
        // 非WiFi环境下提示
        if (res.networkType !== 'wifi') {
          wx.hideLoading();
          wx.showModal({
            title: '提示',
            content: '当前非WiFi环境，下载可能产生流量费用，是否继续？',
            success: (modalRes) => {
              if (modalRes.confirm) {
                this.performDownload();
              }
            }
          });
        } else {
          wx.hideLoading();
          this.performDownload();
        }
      }
    });
  },
  
  // 执行下载
  performDownload: function() {
    const reportName = this.data.report.reportName || `报告_${this.data.reportId}`;
    
    wx.showLoading({
      title: '下载中...',
      mask: true
    });
    
    // 添加下载进度监听
    const downloadTask = wx.downloadFile({
      url: this.data.report.reportUrl,
      success: (res) => {
        wx.hideLoading();
        if (res.statusCode === 200) {
          // 打开文件
          const filePath = res.tempFilePath;
          wx.openDocument({
            filePath: filePath,
            showMenu: true,
            success: () => {
              wx.showToast({
                title: '文件打开成功',
                icon: 'success'
              });
            },
            fail: (err) => {
              console.error('文件打开失败', err);
              wx.showModal({
                title: '提示',
                content: '文件打开失败，是否尝试重新下载？',
                success: (modalRes) => {
                  if (modalRes.confirm) {
                    this.performDownload();
                  }
                }
              });
            }
          });
        } else {
          wx.showToast({
            title: '下载失败，状态码：' + res.statusCode,
            icon: 'none'
          });
        }
      },
      fail: (err) => {
        wx.hideLoading();
        console.error('下载失败', err);
        wx.showModal({
          title: '下载失败',
          content: '网络异常，是否重试？',
          success: (modalRes) => {
            if (modalRes.confirm) {
              this.performDownload();
            }
          }
        });
      }
    });
    
    // 监听下载进度
    downloadTask.onProgressUpdate((res) => {
      if (res.progress < 100) {
        // 可以在这里更新进度条
      }
    });
  },
  
  // 查看地图
  viewMap: function() {
    if (!this.data.report || !this.data.report.landBoundary) {
      wx.showToast({
        title: '暂无地块边界信息',
        icon: 'none'
      });
      return;
    }
    
    try {
      // 解析地块边界数据
      const boundary = JSON.parse(this.data.report.landBoundary);
      
      // 检查微信版本是否支持地图选点
      const systemInfo = wx.getSystemInfoSync();
      if (systemInfo.platform === 'devtools') {
        wx.showModal({
          title: '提示',
          content: '开发者工具暂不支持地图组件，建议在真机上查看',
          showCancel: false
        });
      } else {
        // 在实际项目中，这里应该跳转到地图页面或调用地图组件
        wx.navigateTo({
          url: `/pages/map/view?boundary=${encodeURIComponent(this.data.report.landBoundary)}&title=${encodeURIComponent(this.data.report.landName || '地块地图')}`
        });
      }
    } catch (error) {
      console.error('地块边界数据解析失败', error);
      wx.showToast({
        title: '地块边界数据格式错误',
        icon: 'none'
      });
    }
  },
  
  // 评价飞手
  evaluateFlyer: function() {
    if (!this.data.report || !this.data.report.flyerId) {
      wx.showToast({
        title: '暂无飞手信息',
        icon: 'none'
      });
      return;
    }
    
    wx.navigateTo({
      url: `/pages/evaluation/create?flyerId=${this.data.report.flyerId}&orderId=${this.data.report.orderId}&flyerName=${encodeURIComponent(this.data.report.flyerName || '')}`
    });
  },
  
  // 页面卸载时清理
  onUnload: function() {
    // 清理可能的定时器等
  },
  
  // 下拉刷新
  onPullDownRefresh: function() {
    if (this.data.reportId) {
      this.loadReportDetail(this.data.reportId).then(() => {
        wx.stopPullDownRefresh();
      }).catch(() => {
        wx.stopPullDownRefresh();
      });
    } else {
      wx.stopPullDownRefresh();
    }
  }
})