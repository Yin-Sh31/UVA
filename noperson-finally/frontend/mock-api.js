// 模拟API服务器 - 使用ES模块
import http from 'http';
import { fileURLToPath } from 'url';
import { dirname } from 'path';

// 模拟数据
const mockData = {
  '/api/owner/device/stat': {
    code: 200,
    data: {
      ownerId: 10,
      ownerName: '测试机主',
      total: 5,
      available: 3,
      working: 1,
      maintaining: 1,
      typeDistribution: {
        '巡检无人机': 3,
        '喷洒无人机': 2
      }
    },
    msg: 'success'
  },
  '/api/owner/detail': {
    code: 200,
    data: {
      auditStatus: 1
    },
    msg: 'success'
  },
  '/api/user/info': {
    code: 200,
    data: {
      userId: 10,
      username: 'testadmin',
      roleType: 3,
      roleName: '管理员'
    },
    msg: 'success',
    PUT: (data) => {
      console.log('更新用户信息:', data);
      return {
        code: 200,
        data: {
          ...data,
          updatedAt: new Date().toISOString()
        },
        msg: '个人资料更新成功'
      };
    }
  },
  '/api/owner/profile': {
    PUT: (data) => {
      console.log('更新机主信息:', data);
      return {
        code: 200,
        data: {
          ...data,
          updatedAt: new Date().toISOString()
        },
        msg: '机主信息更新成功'
      };
    }
  },
  '/api/device/list': {
    code: 200,
    data: {
      total: 5,
      records: [
        {
          id: 1,
          deviceInfo: {
            deviceId: 'DR001',
            deviceName: '大疆Mavic 3',
            model: 'Mavic 3',
            serialNumber: 'SN2024001',
            deviceType: '巡检无人机',
            endurance: '45分钟'
          },
          rentalStatus: 0, // 可租借
          renterInfo: null,
          rentInfo: null
        },
        {
          id: 2,
          deviceInfo: {
            deviceId: 'DR002',
            deviceName: '大疆Phantom 4',
            model: 'Phantom 4',
            serialNumber: 'SN2024002',
            deviceType: '航拍无人机',
            endurance: '30分钟'
          },
          rentalStatus: 1, // 已租借
          renterInfo: {
            flyerId: 'FL001',
            flyerName: '张三'
          },
          rentInfo: {
            rentTime: '2024-01-20 09:30:00',
            expectedReturnTime: '2024-01-25 18:00:00'
          }
        },
        {
          id: 3,
          deviceInfo: {
            deviceId: 'DR003',
            deviceName: '极飞P40',
            model: 'P40',
            serialNumber: 'SN2024003',
            deviceType: '喷洒无人机',
            endurance: '35分钟'
          },
          rentalStatus: 2, // 审核中
          renterInfo: {
            flyerId: 'FL002',
            flyerName: '李四'
          },
          rentInfo: {
            rentTime: null,
            expectedReturnTime: null
          }
        },
        {          id: 4,
          deviceInfo: {
            deviceId: 'DR004',
            deviceName: '大疆Air 2S',
            model: 'Air 2S',
            serialNumber: 'SN2024004',
            deviceType: '航拍无人机',
            endurance: '31分钟',
            flyerId: 8 // 添加flyerId表示已租借
          },
          rentalStatus: 1, // 已租借
          renterInfo: {
            flyerId: 8,
            flyerName: '飞手8'
          },
          rentInfo: {
            rentTime: '2024-01-21 10:00:00',
            expectedReturnTime: '2024-01-26 18:00:00'
          }
        },
        {
          id: 5,
          deviceInfo: {
            deviceId: 'DR005',
            deviceName: '大疆T40',
            model: 'T40',
            serialNumber: 'SN2024005',
            deviceType: '喷洒无人机',
            endurance: '28分钟'
          },
          rentalStatus: 1, // 已租借
          renterInfo: {
            flyerId: 'FL003',
            flyerName: '王五'
          },
          rentInfo: {
            rentTime: '2024-01-18 14:20:00',
            expectedReturnTime: '2024-01-22 12:00:00'
          }
        }
      ]
    },
    msg: 'success'
  },
  '/api/device/rent': {
    POST: (deviceId) => {
      return {
        code: 200,
        message: '租借成功'
      };
    }
  },
  '/api/device/return': {
    POST: (deviceId) => {
      return {
        code: 200,
        message: '归还成功'
      };
    }
  },
  '/api/device/rent/approve': {
    POST: (deviceId) => {
      return {
        code: 200,
        message: '审核通过'
      };
    }
  }
};

// 创建服务器
const server = http.createServer((req, res) => {
  console.log(`收到请求: ${req.method} ${req.url}`);
  
  // 设置CORS头
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');
  
  // 处理预检请求
  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }
  
  // 处理请求
  if (req.method === 'GET') {
    // 检查是否有模拟数据
    if (mockData[req.url]) {
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify(mockData[req.url]));
    } else {
      // 检查是否是设备租借相关的动态URL
      if (req.url.startsWith('/api/device/')) {
        // 模拟设备详情接口
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({
          code: 200,
          data: {
            deviceId: req.url.split('/').pop(),
            deviceName: '测试设备',
            status: 1
          },
          msg: 'success'
        }));
      } else {
        res.writeHead(404, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ code: 404, msg: 'Not Found' }));
      }
    }
  } else if (req.method === 'PUT') {
    // 处理PUT请求
    let body = '';
    req.on('data', chunk => {
      body += chunk.toString();
    });
    req.on('end', () => {
      try {
        const requestData = body ? JSON.parse(body) : {};
        console.log(`PUT请求数据: ${req.url}`, requestData);
        
        // 检查是否有对应的PUT处理函数
        if (mockData[req.url] && typeof mockData[req.url][req.method] === 'function') {
          const responseData = mockData[req.url][req.method](requestData);
          res.writeHead(200, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify(responseData));
        } else {
          // 默认成功响应
          res.writeHead(200, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({
            code: 200,
            data: requestData,
            msg: '操作成功'
          }));
        }
      } catch (error) {
        console.error('处理PUT请求时出错:', error);
        res.writeHead(500, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ code: 500, msg: '内部服务器错误' }));
      }
    });
  } else if (req.method === 'POST') {
    // 处理POST请求
    // 图片上传接口
    if (req.url === '/api/upload/image') {
      console.log('处理图片上传请求');
      // 模拟返回上传成功的图片URL
      const mockImageUrl = '/avatar/mock-device-image.jpg';
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ code: 200, data: { url: mockImageUrl } }));
      return;
    }
    
    let body = '';
    req.on('data', chunk => {
      body += chunk.toString();
    });
    req.on('end', () => {
      try {
        // 解析URL路径，获取设备ID
        const pathParts = req.url.split('/');
        const deviceId = pathParts.pop();
        
        // 检查是否是设备编辑相关的API（无/api前缀）
        if (req.url.startsWith('/device/') && req.url.includes('/edit/')) {
          res.writeHead(200, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({
            code: 200,
            data: {},
            message: '设备更新成功'
          }));
        } 
        // 检查是否是设备租借相关的API
        else if (req.url.startsWith('/api/device/rent/')) {
          res.writeHead(200, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({
            code: 200,
            message: req.url.includes('approve') ? '审核通过' : '租借成功'
          }));
        } else if (req.url.startsWith('/api/device/return/')) {
          res.writeHead(200, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({
            code: 200,
            message: '归还成功'
          }));
        } else {
          res.writeHead(200, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({
            code: 200,
            message: '操作成功'
          }));
        }
      } catch (error) {
        console.error('处理POST请求时出错:', error);
        res.writeHead(500, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ code: 500, msg: '内部服务器错误' }));
      }
    });
  } else {
    res.writeHead(405, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ code: 405, msg: 'Method Not Allowed' }));
  }
});

// 启动服务器
const PORT = 3001;
server.listen(PORT, () => {
  console.log(`模拟API服务器启动在端口 ${PORT}`);
  console.log('可用接口:');
  Object.keys(mockData).forEach(route => {
    if (typeof mockData[route] === 'object' && !Array.isArray(mockData[route])) {
      if (mockData[route].code !== undefined) {
        console.log(`  GET http://localhost:${PORT}${route}`);
      }
      if (mockData[route].PUT) {
        console.log(`  PUT http://localhost:${PORT}${route}`);
      }
      if (mockData[route].POST) {
        console.log(`  POST http://localhost:${PORT}${route}`);
      }
    }
  });
});