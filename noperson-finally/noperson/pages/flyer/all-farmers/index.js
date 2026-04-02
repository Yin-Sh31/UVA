// flyer/all-farmers/index.js

const { request } = require('../../../utils/request');

Page({
  data: {
    farmers: [],
    currentPage: 1,
    pageSize: 10,
    total: 0,
    loading: false,
    noData: false,
    refreshing: false
  },

  onLoad() {
    // 默认加载农户列表
    this.loadFarmersList();
  },

  // 加载农户列表
  loadFarmersList() {
    const { currentPage, pageSize } = this.data;
    
    this.setData({ loading: true });
    
    const apiUrl = '/farmer/flyer/getFlyerList';
    
    // 直接使用request函数调用API
    request(apiUrl, 'GET', {
      pageNum: currentPage,
      pageSize: pageSize
    })
      .then(res => {
        let records = [];
        let total = 0;
        
        // 优先检查标准API响应格式：{code, message, data:{records:[]}}
        if (res && res.data && res.data.records && Array.isArray(res.data.records)) {
          records = res.data.records;
          total = res.data.total || 0;
        }
        // 检查API响应格式：{code, message, data:[]}
        else if (res && res.data && Array.isArray(res.data)) {
          records = res.data;
          total = records.length;
        }
        // 检查标准分页结构（如果直接返回data部分）
        else if (res && res.records && Array.isArray(res.records)) {
          records = res.records;
          total = res.total || 0;
        } 
        // 检查是否直接返回数组
        else if (Array.isArray(res)) {
          records = res;
          total = records.length;
        }
        // 降级处理：如果res本身就是对象数组元素
        else if (res && typeof res === 'object' && !Array.isArray(res)) {
          // 检查是否是单个对象，或者对象本身包含农户信息
          const hasFarmerInfo = res.username || res.phone || res.createTime || res.lastLoginTime;
          if (hasFarmerInfo) {
            records = [res]; // 包装成数组
            total = 1;
          }
        }
        
        console.log('原始数据:', records);
        
        // 处理数据，确保每个记录都包含所需字段
        const processedFarmers = records.map((record, index) => {
          console.log('单个农户数据:', {
            userId: record.userId,
            realName: record.realName,
            username: record.username,
            phone: record.phone
          });
          
          return {
            userId: record.userId || `farmer_${(currentPage - 1) * pageSize + index + 1}`,
            username: record.username || `农户${(currentPage - 1) * pageSize + index + 1}`,
            phone: record.phone || '未设置手机号',
            avatar: record.avatar || '/assets/images/user-avatar.png',
            createTime: record.createTime || '',
            lastLoginTime: record.lastLoginTime || ''
          };
        });
        
        console.log('处理后的数据:', processedFarmers);
        
        // 更新数据
        this.setData({
          farmers: processedFarmers,
          total: total,
          noData: processedFarmers.length === 0,
          loading: false,
          refreshing: false
        });
      })
  },

  // 查看农户详情
  viewFarmerDetail(e) {
    const farmerId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/flyer/all-farmers/farmer-detail?id=${farmerId}`
    });
  },

  // 下拉刷新
  onPullDownRefresh() {
    this.setData({ currentPage: 1, refreshing: true });
    this.loadFarmersList();
  },

  // 上拉加载更多
  onReachBottom() {
    try {
      const { currentPage, total, pageSize, loading } = this.data;
      const totalPages = Math.ceil(total / pageSize);
      
      if (!loading && currentPage < totalPages) {
        const nextPage = currentPage + 1;
        this.setData({ 
          currentPage: nextPage,
          loading: true
        });
        
        // 使用与loadFarmersList相同的接口
        const apiUrl = '/farmer/flyer/getFlyerList';
        
        request(apiUrl, 'GET', {
          pageNum: nextPage,
          pageSize: pageSize
        })
          .then(res => {
            let records = [];
            
            // 优先检查标准API响应格式：{code, message, data:{records:[]}}
            if (res && res.data && res.data.records && Array.isArray(res.data.records)) {
              records = res.data.records;
            }
            // 检查标准分页结构（如果直接返回data部分）
            else if (res && res.records && Array.isArray(res.records)) {
              records = res.records;
            }
            // 检查是否直接返回数组
            else if (Array.isArray(res)) {
              records = res;
            }
            // 降级处理：如果res本身就是对象数组元素
            else if (res && typeof res === 'object' && !Array.isArray(res)) {
              // 检查是否是单个对象，或者对象本身包含农户信息
              const hasFarmerInfo = res.username || res.phone || res.createTime || res.lastLoginTime;
              if (hasFarmerInfo) {
                records = [res]; // 包装成数组
              }
            }
            
            console.log('加载更多原始数据:', records);
            
            // 处理数据，确保每个记录都包含所需字段
            const newFarmers = records.map((record, index) => {
              console.log('加载更多单个农户数据:', {
                userId: record.userId,
                realName: record.realName,
                username: record.username,
                phone: record.phone
              });
              
              return {
                userId: record.userId || `farmer_${(nextPage - 1) * pageSize + index + 1}`,
                username: record.username || `农户${(nextPage - 1) * pageSize + index + 1}`,
                phone: record.phone || '未设置手机号',
                avatar: record.avatar || '/assets/images/user-avatar.png',
                createTime: record.createTime || '',
                lastLoginTime: record.lastLoginTime || ''
              };
            });
            
            console.log('加载更多处理后的数据:', newFarmers);
            
            // 合并并更新数据
            const mergedFarmers = [...this.data.farmers, ...newFarmers];
            this.setData({
              farmers: mergedFarmers,
              loading: false
            });
          })
          .catch(err => {
            console.error('加载更多请求失败');
            this.setData({ loading: false });
          });
      }
    } catch (error) {
      console.error('加载更多函数异常');
      this.setData({ loading: false });
    }
  }
});