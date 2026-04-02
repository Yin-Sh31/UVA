import axios from 'axios';

// 配置axios实例
const apiClient = axios.create({
  baseURL: 'http://localhost:3001/api',
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer test-token' // 添加认证token
  }
});

// 测试设备统计API
async function testDeviceStat() {
  console.log('开始测试设备统计API...');
  try {
    const response = await apiClient.get('/owner/device/stat');
    console.log('✅ API调用成功!');
    console.log('响应状态码:', response.status);
    console.log('响应数据:', response.data);
    
    // 检查响应数据结构
    if (response.data && response.data.data) {
      console.log('\n📊 设备统计数据:');
      console.log('- ownerId:', response.data.data.ownerId);
      console.log('- total:', response.data.data.total);
      console.log('- available:', response.data.data.available);
      console.log('- working:', response.data.data.working);
      console.log('- maintaining:', response.data.data.maintaining);
      console.log('- typeDistribution:', response.data.data.typeDistribution);
    }
    
    return response.data;
  } catch (error) {
    console.log('❌ API调用失败:', error.message);
    if (error.response) {
      console.log('响应状态码:', error.response.status);
      console.log('响应数据:', error.response.data);
    }
    return null;
  }
}

// 测试机主详情API
async function testOwnerDetail() {
  console.log('\n开始测试机主详情API...');
  try {
    const response = await apiClient.get('/owner/detail');
    console.log('✅ API调用成功!');
    console.log('响应状态码:', response.status);
    console.log('响应数据:', response.data);
    
    return response.data;
  } catch (error) {
    console.log('❌ API调用失败:', error.message);
    if (error.response) {
      console.log('响应状态码:', error.response.status);
      console.log('响应数据:', error.response.data);
    }
    return null;
  }
}

// 运行测试
async function runTests() {
  console.log('===== API连接测试 =====\n');
  
  const statResult = await testDeviceStat();
  const detailResult = await testOwnerDetail();
  
  console.log('\n===== 测试完成 =====');
}

runTests();