// 测试request.js的默认导出
const request = require('./request').request;

console.log('request:', request);
console.log('typeof request:', typeof request);

// 如果request是函数，尝试一个简单的调用
if (typeof request === 'function') {
  console.log('request is a function, testing call...');
  try {
    // 不实际发送请求，只是测试函数调用
    const result = request('/test', 'GET');
    console.log('request call result:', result);
  } catch (error) {
    console.error('Error calling request:', error);
  }
}